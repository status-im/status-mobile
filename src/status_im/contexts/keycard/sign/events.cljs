(ns status-im.contexts.keycard.sign.events
  (:require [utils.address]
            [utils.re-frame :as rf]))

(defn get-signature-map
  [tx-hash signature]
  {tx-hash {:r (subs signature 0 64)
            :s (subs signature 64 128)
            :v (str (js/parseInt (subs signature 128 130) 16))}})

(rf/reg-event-fx :keycard/sign-hash
 (fn [{:keys [db]} [pin-text tx-hash]]
   (let [current-address (get-in db [:wallet :current-viewing-account-address])
         path            (get-in db [:wallet :accounts current-address :path])
         key-uid         (get-in db [:profile/profile :key-uid])]
     {:fx [[:dispatch
            [:keycard/read-card
             {:key-uid    key-uid
              :on-read-fx [[:effects.keycard/sign
                            {:pin pin-text
                             :path path
                             :hash (utils.address/naked-address tx-hash)
                             :on-success
                             #(do
                                (rf/dispatch [:keycard/hide-connection-sheet])
                                (rf/dispatch
                                 [:wallet/proceed-with-transactions-signatures
                                  (get-signature-map tx-hash %)]))
                             :on-failure #(rf/dispatch [:keycard/on-action-with-pin-error %])}]]}]]]})))
