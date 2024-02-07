(ns status-im.common.standard-authentication.events
  (:require
    [schema.core :as schema]
    [status-im.common.standard-authentication.enter-password.view :as enter-password]
    [taoensso.timbre :as log]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.security.core :as security]))

(defn authorize
  [{:keys [db]} [args]]
  (let [key-uid (get-in db [:profile/profile :key-uid])]
    {:fx [[:biometric/check-if-available
           {:key-uid    key-uid
            :on-success [:standard-auth/authorize-with-biometric args]
            :on-fail    [:standard-auth/authorize-with-password args]}]]}))

(rf/reg-event-fx :standard-auth/authorize authorize)

(def ?authorize-map
  [:map {:closed true}
   [:on-auth-success fn?]
   [:on-auth-fail {:optional true} [:maybe fn?]]
   [:on-close {:optional true} [:maybe fn?]]
   [:auth-button-label {:optional true} [:maybe string?]]
   [:auth-button-icon-left {:optional true} [:maybe keyword?]]
   [:blur? {:optional true} [:maybe boolean?]]
   [:theme {:optional true} [:maybe :schema.common/theme]]])

(schema/=> authorize
  [:=>
   [:catn
    [:cofx :schema.re-frame/cofx]
    [:args
     [:tuple ?authorize-map]]]
   :schema.re-frame/event-fx])

(defn authorize-with-biometric
  [_ [{:keys [on-auth-success on-auth-fail] :as args}]]
  (let [args-with-biometric-btn
        (assoc args
               :on-press-biometric
               #(rf/dispatch [:standard-auth/authorize-with-biometric args]))]
    {:fx [[:dispatch [:dismiss-keyboard]]
          [:dispatch
           [:biometric/authenticate
            {:prompt-message (i18n/label :t/biometric-auth-confirm-message)
             :on-cancel      [:standard-auth/authorize-with-password args-with-biometric-btn]
             :on-success     [:standard-auth/on-biometric-success on-auth-success]
             :on-fail        [:standard-auth/on-biometric-fail on-auth-fail]}]]]}))

(schema/=> authorize-with-biometric
  [:=>
   [:catn
    [:cofx :schema.re-frame/cofx]
    [:args
     [:tuple ?authorize-map]]]
   :schema.re-frame/event-fx])

(rf/reg-event-fx :standard-auth/authorize-with-biometric authorize-with-biometric)

(defn on-biometric-success
  [{:keys [db]} [on-auth-success]]
  (let [key-uid (get-in db [:profile/profile :key-uid])]
    {:fx [[:keychain/get-user-password [key-uid on-auth-success]]]}))

(schema/=> on-biometric-success
  [:=>
   [:catn
    [:cofx :schema.re-frame/cofx]
    [:args
     [:tuple fn?]]]
   :schema.re-frame/event-fx])

(rf/reg-event-fx :standard-auth/on-biometric-success on-biometric-success)

(defn on-biometric-fail
  [_ [on-auth-fail error]]
  (when on-auth-fail
    (on-auth-fail error))
  (log/error (ex-message error)
             (-> error
                 ex-data
                 (assoc :code  (ex-cause error)
                        :event :standard-auth/on-biometric-fail)))
  {:fx [[:dispatch [:biometric/show-message (ex-cause error)]]]})

(schema/=> on-biometric-fail
  [:=>
   [:catn
    [:cofx :schema.re-frame/cofx]
    [:args
     [:tuple
      [:maybe fn?]
      [:maybe :schema.common/error]]]]
   :schema.re-frame/event-fx])

(rf/reg-event-fx :standard-auth/on-biometric-fail on-biometric-fail)

(defn- bottom-sheet-password-view
  [{:keys [on-press-biometric on-auth-success auth-button-icon-left auth-button-label]}]
  (fn []
    (let [handle-password-success (fn [password]
                                    (-> password security/hash-masked-password on-auth-success))]
      [enter-password/view
       {:on-enter-password   handle-password-success
        :on-press-biometrics on-press-biometric
        :button-icon-left    auth-button-icon-left
        :button-label        auth-button-label}])))

(defn authorize-with-password
  [_ [{:keys [on-close theme blur?] :as args}]]
  {:fx [[:dispatch [:standard-auth/reset-login-password]]
        [:dispatch
         [:show-bottom-sheet
          {:on-close on-close
           :theme    theme
           :shell?   blur?
           :content  #(bottom-sheet-password-view args)}]]]})

(rf/reg-event-fx :standard-auth/authorize-with-password authorize-with-password)

(schema/=> authorize-with-password
  [:=>
   [:catn
    [:cofx :schema.re-frame/cofx]
    [:args
     [:tuple ?authorize-map]]]
   :schema.re-frame/event-fx])

(rf/reg-event-fx
 :standard-auth/reset-login-password
 (fn [{:keys [db]}]
   {:db (update db :profile/login dissoc :password :error)}))
