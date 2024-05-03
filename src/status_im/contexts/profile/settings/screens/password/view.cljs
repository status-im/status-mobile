(ns status-im.contexts.profile.settings.screens.password.view
  (:require [quo.core :as quo]
            [quo.theme :as quo.theme]
            [status-im.common.biometric.utils :as biometric]
            [status-im.common.not-implemented :as not-implemented]
            [status-im.config :as config]
            [status-im.constants :as constants]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- on-press-biometric-enable
  [button-label theme]
  (fn []
    (rf/dispatch
     [:standard-auth/authorize-with-password
      {:blur?             true
       :theme             theme
       :auth-button-label (i18n/label :t/biometric-enable-button {:bio-type-label button-label})
       :on-auth-success   (fn [password]
                            (rf/dispatch [:hide-bottom-sheet])
                            (rf/dispatch
                             [:biometric/authenticate
                              {:on-success #(rf/dispatch [:biometric/enable password])
                               :on-fail    #(rf/dispatch [:biometric/show-message (ex-cause %)])}]))}])))

(defn- get-biometric-item
  [theme]
  (let [auth-method    (rf/sub [:auth-method])
        biometric-type (rf/sub [:biometrics/supported-type])
        label          (biometric/get-label-by-type biometric-type)
        icon           (biometric/get-icon-by-type biometric-type)
        supported?     (boolean biometric-type)
        enabled?       (= auth-method constants/auth-method-biometric)
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
                    :checked?  biometric-on?}
     :on-press     (when supported? press-handler)}))

(defn- get-change-password-item
  []
  (when config/show-not-implemented-features?
    {:title       (i18n/label :t/change-password)
     :on-press    not-implemented/alert
     :blur?       true
     :image       :icon
     :image-props :i/password
     :action      :arrow}))

(defn- navigate-back
  []
  (rf/dispatch [:navigate-back]))

(defn view
  []
  (let [theme (quo.theme/use-theme)]
    [quo/overlay {:type :shell :top-inset? true}
     [quo/page-nav
      {:background :blur
       :icon-name  :i/arrow-left
       :on-press   navigate-back}]
     [quo/page-top {:title (i18n/label :t/password)}]
     [quo/category
      {:key       :category
       :data      [(get-biometric-item theme)
                   (get-change-password-item)]
       :blur?     true
       :list-type :settings}]]))
