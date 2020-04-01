(ns status-im.ui.starter-pack.events
  (:require [re-frame.core :as re-frame]
            [status-im.popover.core :as popover]
            [status-im.utils.fx :as fx]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.ethereum.contracts :as contracts]
            [status-im.ethereum.core :as ethereum]
            [status-im.stickers.core :as stickers]
            [status-im.utils.money :as money]
            [status-im.ethereum.transactions.core :as transaction]
            [status-im.js-dependencies :as dependencies]
            [status-im.utils.handlers :as handlers]))

(def tozemoon-id 0)

(re-frame/reg-sub
 ::starter-pack-state
 (fn [db]
   (get-in db [:iap/payment :starter-pack])))

(re-frame/reg-sub
 ::starter-pack-eligible
 (fn [db]
   (get-in db [:starter-pack :eligible])))

(re-frame/reg-sub
 ::starter-pack-amount
 (fn [db]
   (get-in db [:starter-pack :pack])))

(fx/defn close-starter-pack
  {:events [::close-starter-pack]}
  [{:keys [db]}]
  {:db (assoc-in db [:iap/payment :starter-pack] :hidden)})

(fx/defn success-buy
  {:events [::success-buy]}
  [{:keys [db] :as cofx} opts]
  (let [transaction (get opts "tx")]
    (prn transaction)
    (fx/merge cofx
              (transaction/watch-transaction transaction
                                             {:trigger-fn (constantly true)
                                              :on-trigger
                                              (fn []
                                                {:dispatch [::success-received]})})
              (close-starter-pack)
              (popover/show-popover {:view :starter-pack-success}))))

(fx/defn starter-pack-eligible
  {:events [::starter-pack-eligible]}
  [{:keys [db]} response]
  {:db (cond-> db
         (seq response)
         (assoc-in [:starter-pack :eligible] (first response)))})

(fx/defn starter-pack-amount
  {:events [::starter-pack-amount]}
  [{:keys [db]} [_ eth-amount tokens tokens-amount sticker-packs]]
  ;; TODO: Fetch all tokens names and symbols
  {:db (assoc-in db [:starter-pack :pack] {:eth-amount    (money/wei->ether eth-amount)
                                           :tokens        tokens
                                           :tokens-amount (mapv money/wei->ether tokens-amount)
                                           :sticker-packs sticker-packs})})

(fx/defn check-eligible
  {:events [::eligible]}
  [{:keys [db]}]
  (let [contract (contracts/get-address db :status/starter-pack)
        address  (ethereum/default-address db)]
    {::json-rpc/eth-call [{:contract   contract
                           :method     "eligible(address)"
                           :params     [address]
                           :outputs    ["bool"]
                           :on-success #(re-frame/dispatch [::starter-pack-eligible %])}]}))

(fx/defn success-received
  {:events [::success-received]}
  [{:keys [db]}]
  (let [contract        (contracts/get-address db :status/stickers)
        id              tozemoon-id
        on-success-load [:stickers/install-pack id]]
    ;; TODO: Notify user that tx was mined
    {:stickers/pack-data-fx [contract id on-success-load]}))

(fx/defn check-amount
  {:events [::check-amount]}
  [{:keys [db]}]
  (let [contract (contracts/get-address db :status/starter-pack)]
    {::json-rpc/eth-call [{:contract   contract
                           :method     "getPack()"
                           :outputs    ["address" "uint256" "address[]" "uint256[]" "uint256[]"]
                           :on-success #(re-frame/dispatch [::starter-pack-amount (vec %)])}]}))
