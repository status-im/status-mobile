(ns status-im.stickers.core
  (:require [cljs.reader :as edn]
            [re-frame.core :as re-frame]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ethereum.abi-spec :as abi-spec]
            [status-im.ethereum.contracts :as contracts]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.fx :as fx]
            [status-im.utils.utils :as utils]
            [status-im.signing.core :as signing]
            [status-im.utils.contenthash :as contenthash]
            [status-im.multiaccounts.update.core :as multiaccounts.update]
            [clojure.string :as string]))

(defn pack-data-callback
  [id open?]
  (fn [[category owner mintable timestamp price contenthash]]
    (when-let [url (contenthash/url contenthash)]
      (re-frame/dispatch [:stickers/load-pack url id price open?]))))

(re-frame/reg-fx
 :stickers/set-pending-timout-fx
 (fn []
   (utils/set-timeout #(re-frame/dispatch [:stickers/pending-timout])
                      10000)))

(defn eth-call-pack-data
  [contract id open?]
  (json-rpc/eth-call
   {:contract contract
    ;; Returns vector of pack data parameters by pack id:
    ;; [category owner mintable timestamp price contenthash]
    :method "getPackData(uint256)"
    :params [id]
    :outputs ["bytes4[]" "address" "bool" "uint256" "uint256" "bytes"]
    :on-success (pack-data-callback id open?)}))

(re-frame/reg-fx
 :stickers/pack-data-fx
 (fn [[contract id]]
   (eth-call-pack-data contract id true)))

(re-frame/reg-fx
 :stickers/load-packs-fx
 (fn [[contract]]
   (json-rpc/eth-call
    {:contract contract
     ;; Returns number of packs registered in the contract
     :method "packCount()"
     :outputs ["uint256"]
     :on-success
     (fn [[count]]
       (dotimes [id count]
         (eth-call-pack-data contract id false)))})))

(re-frame/reg-fx
 :stickers/owned-packs-fx
 (fn [[contract address]]
   (json-rpc/eth-call
    {:contract contract
     ;; Returns vector of owned tokens ids in the contract by address
     :method "balanceOf(address)"
     :params [address]
     :outputs ["uint256"]
     :on-success
     (fn [[count]]
       (dotimes [id count]
         (json-rpc/eth-call
          {:contract contract
           ;; Returns pack id in the contract by token id
           :method "tokenOfOwnerByIndex(address,uint256)"
           :params [address id]
           :outputs ["uint256"]
           :on-success
           (fn [[token-id]]
             (json-rpc/eth-call
              {:contract contract
               ;; Returns pack id in the contract by token id
               :method "tokenPackId(uint256)"
               :params [token-id]
               :outputs ["uint256"]
               :on-success
               (fn [[pack-id]]
                 (re-frame/dispatch [:stickers/pack-owned pack-id]))}))})))})))

(fx/defn init-stickers-packs
  [{:keys [db]}]
  (let [sticker-packs (get-in db [:multiaccount :stickers/packs-installed])]
    {:db (assoc db
                :stickers/packs-installed sticker-packs
                :stickers/packs sticker-packs)}))

(fx/defn install-stickers-pack
  [{{:keys [multiaccount] :as db} :db :as cofx} id]
  (let [pack (get-in db [:stickers/packs id])]
    (fx/merge
     cofx
     {:db (-> db
              (assoc-in [:stickers/packs-installed id] pack))}
     ;;(assoc :stickers/selected-pack id))} TODO it doesn't scroll to selected pack on Android
     (multiaccounts.update/multiaccount-update
      {:stickers/packs-installed (assoc (:stickers/packs-installed multiaccount) id pack)}
      {}))))

(defn valid-sticker? [sticker]
  (contains? sticker :hash))

(fx/defn load-sticker-pack-success
  [{:keys [db] :as cofx} edn-string id price open?]
  (let [{:keys [stickers] :as pack} (assoc (get (edn/read-string edn-string) 'meta)
                                           :id id :price price)]
    (fx/merge cofx
              {:db (cond-> db
                     (and (seq stickers)
                          (every? valid-sticker? stickers))
                     (assoc-in [:stickers/packs id] pack))}
              #(when open?
                 (navigation/navigate-to-cofx % :stickers-pack-modal pack)))))

(fx/defn open-sticker-pack
  [{{:stickers/keys [packs packs-installed] :networks/keys [current-network] :as db} :db :as cofx} id]
  (when (and id (string/starts-with? current-network "mainnet"))
    (let [pack    (or (get packs-installed id)
                      (get packs id))
          contract-address (contracts/get-address db :status/stickers)]
      (if pack
        (navigation/navigate-to-cofx cofx :stickers-pack-modal pack)
        (when contract-address
          {:stickers/pack-data-fx [contract-address id]})))))

(fx/defn load-pack
  [cofx url id price open?]
  {:http-get {:url url
              :success-event-creator
              (fn [o]
                [:stickers/load-sticker-pack-success o id price open?])}})

(fx/defn load-packs
  [{:keys [db]}]
  (let [contract      (contracts/get-address db :status/stickers)
        pack-contract (contracts/get-address db :status/sticker-pack)
        address       (ethereum/default-address db)]
    (when contract
      {:stickers/owned-packs-fx [pack-contract address]
       :stickers/load-packs-fx [contract]})))

(fx/defn approve-pack
  [{db :db :as cofx} pack-id price]
  (let [address                 (ethereum/default-address db)
        sticker-market-contract (contracts/get-address db :status/sticker-market)
        snt-contract            (contracts/get-address db :status/snt)]
    (signing/eth-transaction-call
     cofx
     {:contract snt-contract
      :method "approveAndCall(address,uint256,bytes)"
      :params [sticker-market-contract
               price
               (abi-spec/encode "buyToken(uint256,address,uint256)" [pack-id address price])]
      :on-result [:stickers/pending-pack pack-id]})))

(fx/defn pending-pack
  [{:keys [db] :as cofx} id]
  (let [contract (contracts/get-address db :status/sticker-pack)
        address  (ethereum/default-address db)]
    (when contract
      (fx/merge cofx
                {:db (update db :stickers/packs-pending conj id)
                 :stickers/owned-packs-fx [contract address]}
                #(when (zero? (count (:stickers/packs-pending db)))
                   {:stickers/set-pending-timout-fx nil})))))

(fx/defn pending-timeout
  [{{:stickers/keys [packs-pending packs-owned] :as db} :db}]
  (let [packs-diff (clojure.set/difference packs-pending packs-owned)
        contract   (contracts/get-address db :status/sticker-pack)
        address    (ethereum/default-address db)]
    (when contract
      (merge {:db (assoc db :stickers/packs-pending packs-diff)}
             (when-not (zero? (count packs-diff))
               {:stickers/owned-packs-fx [contract address]
                :stickers/set-pending-timout-fx nil})))))

(fx/defn pack-owned [{db :db} id]
  {:db (update db :stickers/packs-owned conj id)})

(fx/defn get-owned-pack
  [{:keys [db]}]
  (let [contract (contracts/get-address db :status/sticker-pack)
        address  (ethereum/default-address db)]
    (when contract
      {:stickers/owned-packs-fx [contract address]})))
