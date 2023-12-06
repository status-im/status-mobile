(ns status-im2.common.standard-authentication.password-input.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [status-im2.common.keychain.events :as keychain]
    [status-im2.common.standard-authentication.forgot-password-doc.view :as forgot-password-doc]
    [status-im2.common.standard-authentication.password-input.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.security.core :as security]))

(defn get-error-message
  [error]
  (if (and (some? error)
           (or (= error "file is not a database")
               (string/starts-with? error "failed to set ")
               (string/starts-with? error "Failed")))
    (i18n/label :t/oops-wrong-password)
    error))

(defn- on-change-password
  [entered-password]
  (rf/dispatch [:set-in [:profile/login :password]
                (security/mask-data entered-password)])
  (rf/dispatch [:set-in [:profile/login :error] ""]))

(defn- view-internal
  [{:keys [default-password theme shell?]}]
  (let [{:keys [error processing]} (rf/sub [:profile/login])
        auth-method                (rf/sub [:auth-method])
        error-message              (get-error-message error)
        error?                     (boolean (seq error-message))]
    [:<>
     [rn/view {:style style/input-container}
      [quo/input
       {:type            :password
        :blur?           true
        :container-style style/input
        :disabled?       processing
        :placeholder     (i18n/label :t/type-your-password)
        :auto-focus      true
        :error?          error?
        :label           (i18n/label :t/profile-password)
        :on-change-text  on-change-password
        :default-value   (security/safe-unmask-data default-password)}]
      (when (= auth-method keychain/auth-method-biometric)
        [quo/button
         {:type       :outline
          :icon-only? true
          :on-press   #(rf/dispatch [:profile.login/biometric-auth])
          :background :blur
          :size       40}
         :i/face-id])]
     (when error?
       [rn/view {:style style/error-message}
        [quo/info-message
         {:type :error
          :size :default
          :icon :i/info}
         error-message]
        [rn/pressable
         {:hit-slop {:top 6 :bottom 20 :left 0 :right 0}
          :disabled processing
          :on-press (fn []
                      (rn/dismiss-keyboard!)
                      (rf/dispatch [:show-bottom-sheet
                                    {:content #(forgot-password-doc/view {:shell? shell?})
                                     :theme   theme
                                     :shell?  shell?}]))}
         [rn/text
          {:style                 {:text-decoration-line :underline
                                   :color                (colors/theme-colors
                                                          (colors/custom-color :danger 50)
                                                          (colors/custom-color :danger 60)
                                                          theme)}
           :size                  :paragraph-2
           :suppress-highlighting true}
          (i18n/label :t/forgot-password)]]])]))

(def view (quo.theme/with-theme view-internal))
