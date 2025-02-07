(ns status-im.contexts.keycard.sign.events
  (:require [utils.address]
            [utils.re-frame :as rf]))

(defn- append-account-path
  [db message-data]
  (let [accounts (get-in db [:wallet :accounts])]
    (->> (:address message-data)
         (get accounts)
         :path
         (assoc message-data :path))))

(defn- add-paths-to-signing-data
  [db signing-data]
  (reduce (fn [acc message-data]
            (conj acc (append-account-path db message-data)))
          []
          signing-data))

(rf/reg-event-fx :keycard/sign-messages
 (fn [_ [data]]
   {:effects.keycard/sign-messages data}))

(rf/reg-event-fx
 :keycard/connect-and-sign-messages
 (fn [{:keys [db]} [{:keys [keycard-pin messages on-success on-failure]}]]
   (let [key-uid (get-in db [:profile/profile :key-uid])]
     {:fx [[:dispatch
            [:keycard/connect
             {:key-uid key-uid
              :on-success
              (fn []
                (rf/dispatch
                 [:keycard/sign-messages
                  {:pin           keycard-pin
                   :messages-data (add-paths-to-signing-data db messages)
                   :on-success    (fn [signatures]
                                    (rf/dispatch [:keycard/disconnect])
                                    (when on-success (on-success signatures)))
                   :on-failure    on-failure}]))}]]]})))
