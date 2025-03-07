(ns status-im.contexts.keycard.sign.events
  (:require [utils.address]
            [utils.re-frame :as rf]))

(defn- append-account-path
  [db payload]
  (let [accounts (get-in db [:wallet :accounts])]
    (->> (:address payload)
         (get accounts)
         :path
         (assoc payload :path))))

(defn- add-paths-to-payloads
  [db payloads]
  (reduce (fn [acc message-data]
            (conj acc (append-account-path db message-data)))
          []
          payloads))

(rf/reg-event-fx :keycard/sign-payloads
 (fn [_ [data]]
   {:effects.keycard/sign-payloads data}))

(rf/reg-event-fx
 :keycard/connect-and-sign-payloads
 (fn [{:keys [db]} [{:keys [keycard-pin payloads on-success on-failure]}]]
   (let [key-uid (get-in db [:profile/profile :key-uid])]
     {:fx [[:dispatch
            [:keycard/connect
             {:key-uid key-uid
              :on-success
              (fn []
                (rf/dispatch
                 [:keycard/sign-payloads
                  {:pin        keycard-pin
                   :payloads   (add-paths-to-payloads db payloads)
                   :on-success (fn [signatures]
                                 (rf/dispatch [:keycard/disconnect])
                                 (when on-success (on-success signatures)))
                   :on-failure on-failure}]))}]]]})))
