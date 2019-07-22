(ns status-im.stickers.core
  (:require [cljs.reader :as edn]
            [re-frame.core :as re-frame]
            [status-im.accounts.core :as accounts]
            [status-im.constants :as constants]
            [status-im.ethereum.abi-spec :as abi-spec]
            [status-im.ethereum.contracts :as contracts]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.fx :as fx]
            [status-im.utils.multihash :as multihash]
            [status-im.utils.utils :as utils]
            [status-im.signing.core :as signing]))

(defn pack-data-callback
  [id open?]
  (fn [[category owner mintable timestamp price contenthash]]
    (let [proto-code (subs contenthash 2 4)
          hash       (when contenthash
                       (multihash/base58 (multihash/create :sha2-256 (subs contenthash 12))))]
      (when (and (#{constants/swarm-proto-code constants/ipfs-proto-code}
                  proto-code) hash)
        (re-frame/dispatch [:stickers/load-pack proto-code hash id price open?])))))

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
  (let [sticker-packs (into {} (map #(let [pack (edn/read-string %)]
                                       (vector (:id pack) pack))
                                    (get-in db [:account/account :stickers])))]
    {:db (assoc db
                :stickers/packs-installed sticker-packs
                :stickers/packs sticker-packs)}))

(fx/defn install-stickers-pack
  [{{:account/keys [account] :as db} :db :as cofx} id]
  (let [pack (get-in db [:stickers/packs id])]
    (fx/merge
     cofx
     {:db (-> db
              (assoc-in [:stickers/packs-installed id] pack))}
              ;;(assoc :stickers/selected-pack id))}
     (accounts/update-stickers (conj (:stickers account) (pr-str pack))))))

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
  [{{:stickers/keys [packs packs-installed] :as db} :db :as cofx} id]
  (when id
    (let [pack    (or (get packs-installed id)
                      (get packs id))
          contract-address (contracts/get-address db :status/stickers)]
      (if pack
        (navigation/navigate-to-cofx cofx :stickers-pack-modal pack)
        (when contract-address
          {:stickers/pack-data-fx [contract-address id]})))))

(fx/defn load-pack
  [cofx proto-code hash id price open?]
  {:http-get {:url (str (if (= constants/swarm-proto-code proto-code)
                          "https://swarm-gateways.net/bzz:/"
                          "https://ipfs.infura.io/ipfs/")
                        hash)
              :success-event-creator
              (fn [o]
                [:stickers/load-sticker-pack-success o id price open?])}})

(fx/defn load-packs
  [{:keys [db]}]
  (let [contract      (contracts/get-address db :status/stickers)
        pack-contract (contracts/get-address db :status/sticker-pack)
        address       (ethereum/current-address db)]
    (when contract
      {:stickers/owned-packs-fx [pack-contract address]
       :stickers/load-packs-fx [contract]})))

(fx/defn approve-pack
  [{db :db :as cofx} pack-id price]
  (let [address                 (ethereum/current-address db)
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
        address  (ethereum/current-address db)]
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
        address    (ethereum/current-address db)]
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
        address  (ethereum/current-address db)]
    (when contract
      {:stickers/owned-packs-fx [contract address]})))
