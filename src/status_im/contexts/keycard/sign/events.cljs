(ns status-im.contexts.keycard.sign.events
  (:require [utils.address]
            [utils.re-frame :as rf]))

(rf/reg-event-fx :keycard/sign-hashes
 (fn [_ [data]]
   {:effects.keycard/sign-hashes data}))

(rf/reg-event-fx :keycard/connect-and-sign-hashes
 (fn [{:keys [db]} [{:keys [keycard-pin address hashes on-success on-failure]}]]
   (let [{:keys [path key-uid]} (get-in db [:wallet :accounts address])]
     {:fx [[:dispatch
            [:keycard/connect
             {:key-uid key-uid
              :on-success
              (fn []
                (rf/dispatch
                 [:keycard/sign-hashes
                  {:pin        keycard-pin
                   :path       path
                   :hashes     hashes
                   :on-success (fn [signatures]
                                 (rf/dispatch [:keycard/disconnect])
                                 (when on-success (on-success signatures)))
                   :on-failure on-failure}]))}]]]})))
