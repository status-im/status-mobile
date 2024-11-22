(ns status-im.contexts.keycard.sign.events
  (:require [utils.address]
            [utils.re-frame :as rf]))

(defn get-signature-map
  [tx-hash signature]
  {tx-hash {:r (subs signature 0 64)
            :s (subs signature 64 128)
            :v (subs signature 128 130)}})

(rf/reg-event-fx :keycard/sign
 (fn [_ [data]]
   {:effects.keycard/sign data}))

(rf/reg-event-fx :keycard/sign-hash
 (fn [{:keys [db]} [pin-text tx-hash]]
   (let [current-address (get-in db [:wallet :current-viewing-account-address])
         path            (get-in db [:wallet :accounts current-address :path])
         key-uid         (get-in db [:profile/profile :key-uid])]
     {:fx [[:dispatch
            [:keycard/connect
             {:key-uid key-uid
              :on-success
              (fn []
                (rf/dispatch
                 [:keycard/sign
                  {:pin        pin-text
                   :path       path
                   :hash       (utils.address/naked-address tx-hash)
                   :on-success (fn [signature]
                                 (rf/dispatch [:keycard/disconnect])
                                 (rf/dispatch [:wallet/proceed-with-transactions-signatures
                                               (get-signature-map tx-hash signature)]))
                   :on-failure #(rf/dispatch [:keycard/on-action-with-pin-error %])}]))}]]]})))
