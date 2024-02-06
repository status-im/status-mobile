(ns status-im.common.standard-authentication.standard-auth.authorize
  (:require
    [react-native.biometrics :as biometrics]
    [schema.core :as schema]
    [status-im.common.standard-authentication.enter-password.view :as enter-password]
    [taoensso.timbre :as log]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.security.core :as security]))

(defn- reset-password
  []
  (rf/dispatch [:set-in [:profile/login :password] nil])
  (rf/dispatch [:set-in [:profile/login :error] ""]))

(defn authorize
  [{:keys [biometric-auth? on-auth-success on-auth-fail on-close
           auth-button-label theme blur? auth-button-icon-left]}]
  (let [handle-auth-success (fn [biometric?]
                              (fn [entered-password]
                                (let [sha3-masked-password (if biometric?
                                                             entered-password
                                                             (security/hash-masked-password
                                                              entered-password))]
                                  (on-auth-success sha3-masked-password))))
        password-login      (fn [{:keys [on-press-biometrics]}]
                              (rf/dispatch [:show-bottom-sheet
                                            {:on-close on-close
                                             :theme    theme
                                             :shell?   blur?
                                             :content  (fn []
                                                         [enter-password/view
                                                          {:on-enter-password   (handle-auth-success
                                                                                 false)
                                                           :on-press-biometrics on-press-biometrics
                                                           :button-icon-left    auth-button-icon-left
                                                           :button-label        auth-button-label}])}]))
        ; biometrics-login recursively passes itself as a parameter because if the user
        ; fails biometric auth they will be shown the password bottom sheet with an option
        ; to retrigger biometric auth, so they can endlessly repeat this cycle.
        biometrics-login    (fn [on-press-biometrics]
                              (rf/dispatch [:dismiss-keyboard])
                              (rf/dispatch
                               [:biometric/authenticate
                                {:prompt-message (i18n/label :t/biometric-auth-confirm-message)
                                 :on-success     (fn []
                                                   (on-close)
                                                   (rf/dispatch [:standard-auth/on-biometric-success
                                                                 (handle-auth-success true)]))
                                 :on-fail        (fn [error]
                                                   (on-close)
                                                   (log/error
                                                    (ex-message error)
                                                    (-> error ex-data (assoc :code (ex-cause error))))
                                                   (when on-auth-fail (on-auth-fail error))
                                                   (password-login {:on-press-biometrics
                                                                    #(on-press-biometrics
                                                                      on-press-biometrics)}))}]))]
    (if biometric-auth?
      (-> (biometrics/get-supported-type)
          (.then (fn [biometric-type]
                   (if biometric-type
                     (biometrics-login biometrics-login)
                     (do
                       (reset-password)
                       (password-login {})))))
          (.catch #(password-login {})))
      (password-login {}))))

(schema/=> authorize
  [:=>
   [:cat
    [:map {:closed true}
     [:biometric-auth? {:optional true} [:maybe boolean?]]
     [:on-auth-success fn?]
     [:on-auth-fail {:optional true} [:maybe fn?]]
     [:on-close {:optional true} [:maybe fn?]]
     [:auth-button-label {:optional true} [:maybe string?]]
     [:theme {:optional true} [:maybe :schema.common/theme]]
     [:blur? {:optional true} [:maybe boolean?]]
     [:auth-button-icon-left {:optional true} [:maybe keyword?]]]]
   :any])
