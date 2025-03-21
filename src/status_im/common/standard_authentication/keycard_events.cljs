(ns status-im.common.standard-authentication.keycard-events
  (:require [status-im.contexts.keycard.pin.view :as keycard.pin]
            [utils.re-frame :as rf]
            [utils.security.core :as security]))

(defn authorize-with-keycard
  [_ [{:keys [on-complete]}]]
  {:fx [[:dispatch
         [:show-bottom-sheet
          {:hide-on-background-press? false
           :on-close                  #(rf/dispatch [:standard-auth/reset-login-password])
           :content                   (fn []
                                        [keycard.pin/auth-sheet {:on-complete on-complete}])}]]]})
(rf/reg-event-fx :standard-auth/authorize-with-keycard authorize-with-keycard)

(defn- authorize-with-keycard-key
  [{:keys [db]} [{:keys [on-auth-success]}]]
  (let [key-uid (get-in db [:profile/profile :key-uid])]
    {:fx [[:dispatch
           [:standard-auth/authorize-with-keycard
            {:on-complete
             (fn [pin]
               (rf/dispatch
                [:keycard/connect
                 {:key-uid key-uid
                  :on-success
                  (fn []
                    (rf/dispatch
                     [:keycard/get-keys
                      {:pin        pin
                       :on-success #(rf/dispatch [:standard-auth/on-keycard-key-success %
                                                  on-auth-success])
                       :on-failure #(rf/dispatch [:standard-auth/on-keycard-key-fail])}]))}]))}]]]}))
(rf/reg-event-fx :standard-auth/authorize-with-keycard-key authorize-with-keycard-key)

(defn on-keycard-key-success
  [_ [key-data on-auth-success]]
  {:fx [[:dispatch [:keycard/disconnect]]
        [:dispatch
         [:standard-auth/finish-auth
          {:on-auth-success on-auth-success
           :masked-password (-> key-data
                                :encryption-public-key
                                security/mask-data)}]]]})
(rf/reg-event-fx :standard-auth/on-keycard-key-success on-keycard-key-success)

(defn on-keycard-key-fail
  [_ [error on-auth-fail]]
  {:fx [[:dispatch [:keycard/on-action-with-pin-error error]]
        [:effects.standard-auth/on-keycard-key-fail error on-auth-fail]]})
(rf/reg-event-fx :standard-auth/on-keycard-key-fail on-keycard-key-fail)

(rf/reg-fx :effects.standard-auth/on-keycard-key-fail
 (fn [error on-auth-fail]
   (when on-auth-fail
     (on-auth-fail error))))
