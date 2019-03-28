(ns status-im.stickers.core
  (:require [status-im.utils.fx :as fx]
            [cljs.reader :as edn]
            [status-im.accounts.core :as accounts]
            [status-im.ui.screens.navigation :as navigation]
            [re-frame.core :as re-frame]
            [status-im.utils.multihash :as multihash]
            [status-im.constants :as constants]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.ethereum.stickers :as ethereum.stickers]
            [status-im.models.wallet :as models.wallet]
            [status-im.utils.money :as money]
            [status-im.utils.ethereum.abi-spec :as abi-spec]
            [status-im.utils.ethereum.erc20 :as erc20]))

(fx/defn init-stickers-packs [{:keys [db]}]
  (let [sticker-packs (into {} (map #(let [pack (edn/read-string %)]
                                       (vector (:id pack) pack))
                                    (get-in db [:account/account :stickers])))]
    {:db (assoc db :stickers/packs-installed sticker-packs :stickers/packs sticker-packs)}))

(fx/defn install-stickers-pack [{{:account/keys [account] :as db} :db :as cofx} id]
  (let [pack (get-in db [:stickers/packs id])]
    (fx/merge
     cofx
     {:db (-> db
              (assoc-in [:stickers/packs-installed id] pack)
              (assoc :stickers/selected-pack id))}
     (accounts/update-stickers (conj (:stickers account) (pr-str pack))))))

(fx/defn load-sticker-pack-success [{:keys [db] :as cofx} edn-string id price open?]
  (let [pack (assoc (get (edn/read-string edn-string) 'meta)
                    :id id :price price)]
    (fx/merge cofx
              {:db (-> db (assoc-in [:stickers/packs id] pack))}
              #(when open? (navigation/navigate-to-cofx % :stickers-pack-modal pack)))))

(defn pack-data-callback [id open?]
  (fn [[category owner mintable timestamp price contenthash]]
    (let [proto-code (subs contenthash 2 4)
          hash       (when contenthash (multihash/base58 (multihash/create :sha2-256 (subs contenthash 12))))]
      (when (and (#{constants/swarm-proto-code constants/ipfs-proto-code} proto-code) hash)
        (re-frame/dispatch [:stickers/load-pack proto-code hash id price open?])))))

(fx/defn open-sticker-pack
  [{{:keys [network] :stickers/keys [packs packs-installed] :as db} :db :as cofx} id]
  (when id
    (let [pack    (or (get packs-installed id) (get packs id))
          network (get-in db [:account/account :networks network])]
      (if pack
        (navigation/navigate-to-cofx cofx :stickers-pack-modal pack)
        {:stickers/pack-data-fx [network id true]}))))

(fx/defn load-pack [cofx proto-code hash id price open?]
  {:http-get {:url                   (str (if (= constants/swarm-proto-code proto-code)
                                            "https://swarm-gateways.net/bzz:/"
                                            "https://ipfs.infura.io/ipfs/")
                                          hash)
              :success-event-creator (fn [o]
                                       [:stickers/load-sticker-pack-success o id price open?])
              :failure-event-creator (constantly nil)}})

(fx/defn load-packs [{{:keys [network] :as db} :db}]
  (let [network (get-in db [:account/account :networks network])
        address (ethereum/normalized-address (get-in db [:account/account :address]))]
    {:stickers/owned-packs-fx [network address]
     :stickers/load-packs-fx [network]}))

(defn prepare-transaction [id tx on-result]
  (merge {:id     id
          :symbol :ETH
          :method constants/web3-send-transaction
          :amount (money/bignumber 0)}
         (when on-result {:on-result on-result})
         tx))

(fx/defn approve-pack [{db :db} pack-id price]
  (let [network           (get-in db [:account/account :networks (:network db)])
        address           (ethereum/normalized-address (get-in db [:account/account :address]))
        chain             (ethereum/network->chain-keyword network)
        stickers-contract (get ethereum.stickers/contracts chain)
        data              (abi-spec/encode "buyToken(uint256,address)" [pack-id address])
        tx-object         {:to   (get erc20/snt-contracts chain)
                           :data (abi-spec/encode "approveAndCall(address,uint256,bytes)" [stickers-contract price data])}]
    (models.wallet/open-modal-wallet-for-transaction
     db
     (prepare-transaction "approve" tx-object [:stickers/pending-pack pack-id])
     tx-object)))

(fx/defn pending-pack
  [{{:keys [network] :as db} :db :as cofx} id]
  (let [network (get-in db [:account/account :networks network])
        address (ethereum/normalized-address (get-in db [:account/account :address]))]
    (fx/merge cofx
              {:db (update db :stickers/packs-pendning conj id)
               :stickers/owned-packs-fx [network address]}
              (navigation/navigate-to-clean :wallet-transaction-sent-modal {})
              #(when (zero? (count (:stickers/packs-pendning db)))
                 {:stickers/set-pending-timout-fx nil}))))

(fx/defn pending-timeout
  [{{:keys [network] :stickers/keys [packs-pendning packs-owned] :as db} :db}]
  (let [packs-diff (clojure.set/difference packs-pendning packs-owned)
        network (get-in db [:account/account :networks network])
        address (ethereum/normalized-address (get-in db [:account/account :address]))]
    (merge {:db (assoc db :stickers/packs-pendning packs-diff)}
           (when-not (zero? (count packs-diff))
             {:stickers/owned-packs-fx [network address]
              :stickers/set-pending-timout-fx nil}))))

(fx/defn pack-owned [{db :db} id]
  {:db (update db :stickers/packs-owned conj id)})

(fx/defn get-owned-pack
  [{{:keys [network] :as db} :db}]
  (let [address (ethereum/normalized-address (get-in db [:account/account :address]))]
    {:stickers/owned-packs-fx [network address]}))

(re-frame/reg-fx
 :stickers/pack-data-fx
 (fn [[network id open?]]
   (when-let [contract (get ethereum.stickers/contracts (ethereum/network->chain-keyword network))]
     (ethereum.stickers/pack-data contract id (pack-data-callback id open?)))))

(re-frame/reg-fx
 :stickers/set-pending-timout-fx
 (fn []
   (js/setTimeout #(re-frame/dispatch [:stickers/pending-timout]) 10000)))

(re-frame/reg-fx
 :stickers/load-packs-fx
 (fn [[network]]
   (when-let [contract (get ethereum.stickers/contracts (ethereum/network->chain-keyword network))]
     (ethereum.stickers/pack-count contract
                                   (fn [count]
                                     (dotimes [n count]
                                       (ethereum.stickers/pack-data contract n (pack-data-callback n false))))))))

(re-frame/reg-fx
 :stickers/owned-packs-fx
 (fn [[network address]]
   (when-let [contract (get ethereum.stickers/contracts (ethereum/network->chain-keyword network))]
     (ethereum.stickers/owned-tokens contract address
                                     (fn [tokens]
                                       (doseq [n tokens]
                                         (ethereum.stickers/token-pack-id contract n
                                                                          #(re-frame/dispatch [:stickers/pack-owned %]))))))))
