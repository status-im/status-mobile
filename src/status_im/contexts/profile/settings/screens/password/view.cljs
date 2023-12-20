(ns status-im.contexts.profile.settings.screens.password.view
  (:require [quo.core :as quo]
            [quo.theme :as quo.theme]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [status-im.common.biometric.events :as biometric]
            [status-im.common.not-implemented :as not-implemented]
            [status-im.common.standard-authentication.standard-auth.authorize :as authorize]
            [status-im.constants :as constants]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]
            [utils.security.core :as security]))

(defn- on-press-biometric-enable
  [button-label theme]
  (fn []
    (authorize/authorize
     {:biometric-auth?   false
      :blur?             true
      :theme             theme
      :auth-button-label (i18n/label :t/biometric-enable-button {:bio-type-label button-label})
      :on-close          (fn [] (rf/dispatch [:standard-auth/reset-login-password]))
      :on-auth-success   (fn [password]
                           (rf/dispatch [:hide-bottom-sheet])
                           (rf/dispatch [:standard-auth/reset-login-password])
                           (rf/dispatch [:biometric/enable
                                         (security/mask-data password)]))})))

(defn- get-biometric-item
  [theme]
  (let [auth-method    (rf/sub [:auth-method])
        biometric-type (rf/sub [:biometric/supported-type])
        supported?     (boolean biometric-type)
        enabled?       (= auth-method constants/auth-method-biometric)
        label          (biometric/get-label-by-type biometric-type)
        icon           (biometric/get-icon-by-type biometric-type)
        biometric-on?  (and supported? enabled?)
        press-handler  (if biometric-on?
                         (fn [] (rf/dispatch [:biometric/disable]))
                         (on-press-biometric-enable label theme))]
    {:title        label
     :image-props  icon
     :image        :icon
     :blur?        true
     :action       :selector
     :action-props {:disabled? (not supported?)
                    :on-change press-handler
                    :checked?  (and supported? enabled?)}
     :on-press     press-handler}))

(defn- get-change-password-item
  []
  {:title       (i18n/label :t/change-password)
   :on-press    not-implemented/alert
   :blur?       true
   :image       :icon
   :image-props :i/password
   :action      :arrow})

(defn- view-internal
  [{:keys [theme]}]
  (let [insets (safe-area/get-insets)]
    [quo/overlay {:type :shell}
     [rn/view
      {:style {:padding-top (:top insets)}}
      [quo/page-nav
       {:background :blur
        :icon-name  :i/arrow-left
        :on-press   #(rf/dispatch [:navigate-back])}]]
     [rn/view
      {:style {:padding-horizontal 20
               :padding-bottom     8
               :padding-top        12}}
      [quo/text
       {:accessibility-label :password-settings-label
        :weight              :semi-bold
        :number-of-lines     1
        :size                :heading-1} (i18n/label :t/password)]]
     [quo/category
      {:data      [(get-biometric-item theme)
                   (get-change-password-item)]
       :blur?     true
       :list-type :settings}]]))

(def view (quo.theme/with-theme view-internal))
