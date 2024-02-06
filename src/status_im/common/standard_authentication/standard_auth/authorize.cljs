(ns status-im.common.standard-authentication.standard-auth.authorize
  (:require
    [react-native.biometrics :as biometrics]
    [schema.core :as schema]
    [status-im.common.standard-authentication.enter-password.view :as enter-password]
    [utils.re-frame :as rf]
    [utils.security.core :as security]))

(defn- enter-password-view
  [{:keys [on-press-biometrics on-auth-success auth-button-label auth-button-icon-left]}]
  (let [handle-password-success (fn [password]
                                  (-> password security/hash-masked-password on-auth-success))]
    [enter-password/view
     {:on-enter-password   handle-password-success
      :on-press-biometrics on-press-biometrics
      :button-icon-left    auth-button-icon-left
      :button-label        auth-button-label}]))

(defn- show-password-sheet
  "Shows password input in a bottom sheet.

  If `on-press-biometrics` is passed, biometrics can be triggered from the the bottom sheet."
  [{:keys [on-close theme blur?] :as args}]
  (rf/dispatch [:standard-auth/reset-login-password])
  (rf/dispatch [:show-bottom-sheet
                {:on-close on-close
                 :theme    theme
                 :shell?   blur?
                 :content  #(enter-password-view args)}]))

(defn- show-biometric
  "Shows biometrics prompt.

  If failed or canceled, falling back to password sheet. Passing itself to allow the user to
  trigger the biometric check again from the input."
  [{:keys [on-close on-auth-success on-auth-fail] :as args}]
  (let [show-password-sheet-with-biometric (fn []
                                             (show-password-sheet
                                              (assoc args
                                                     :on-press-biometrics
                                                     #(show-biometric args))))]
    (rf/dispatch [:standard-auth/biometric-auth
                  {:on-password-retrieved (fn [password]
                                            (on-close)
                                            (on-auth-success password))
                   :on-fail               (fn [error]
                                            (when on-auth-fail (on-auth-fail error))
                                            (show-password-sheet-with-biometric))
                   :on-cancel             show-password-sheet-with-biometric}])))

(defn authorize
  "Prompts either biometric (if specified and available) or password authentication.

  Biometrics can be triggered if available by passing `biometric-auth?`. If biometric
  authentication fails or is canceled, falling back to password authentication."
  [{:keys [biometric-auth?]
    :as   args}]
  (if biometric-auth?
    (-> (biometrics/get-supported-type)
        (.then (fn [biometric-type]
                 (if biometric-type
                   (show-biometric args)
                   (show-password-sheet args))))
        (.catch #(show-password-sheet args)))
    (show-password-sheet args)))

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
