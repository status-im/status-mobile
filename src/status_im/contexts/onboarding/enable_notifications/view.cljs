(ns status-im.contexts.onboarding.enable-notifications.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [status-im.common.resources :as resources]
    [status-im.contexts.onboarding.enable-notifications.style :as style]
    [status-im.contexts.shell.jump-to.constants :as shell.constants]
    [status-im.contexts.shell.jump-to.utils :as shell.utils]
    [taoensso.timbre :as log]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn page-title
  []
  [quo/text-combinations
   {:container-style                 style/page-title
    :title                           (i18n/label :t/intro-wizard-title6)
    :title-accessibility-label       :notifications-title
    :description                     (i18n/label :t/enable-notifications-sub-title)
    :description-accessibility-label :notifications-sub-title}])

(defn- finish-onboarding
  []
  (shell.utils/change-selected-stack-id shell.constants/default-selected-stack true nil)
  (rf/dispatch [:init-root :shell-stack])
  (rf/dispatch [:profile/show-testnet-mode-banner-if-enabled])
  (rf/dispatch [:universal-links/process-stored-event]))

(defn- enable-notifications-and-finish-onboarding
  []
  (rf/dispatch [:request-permissions
                {:permissions [:post-notifications]
                 :on-allowed  #(log/info "push notification permissions were allowed")
                 :on-denied   #(log/error "user denied push notification permissions")}])
  (rf/dispatch [:push-notifications/switch true])
  (finish-onboarding))

(defn enable-notification-buttons
  [{:keys [insets]}]
  (let [profile-color (rf/sub [:onboarding/customization-color])]
    [rn/view {:style (style/buttons insets)}
     [quo/button
      {:on-press            enable-notifications-and-finish-onboarding
       :type                :primary
       :icon-left           :i/notifications
       :accessibility-label :enable-notifications-button
       :customization-color profile-color}
      (i18n/label :t/intro-wizard-title6)]
     [quo/button
      {:on-press            finish-onboarding
       :accessibility-label :enable-notifications-later-button
       :type                :grey
       :background          :blur
       :container-style     {:margin-top 12}}
      (i18n/label :t/maybe-later)]]))

(defn enable-notifications-simple
  []
  (let [width (:width (rn/get-window))]
    [rn/image
     {:resize-mode :contain
      :style       (style/page-illustration width)
      :source      (resources/get-image :notifications)}]))

(defn view
  []
  (let [insets (safe-area/get-insets)]
    [rn/view {:style (style/page-container insets)}
     [rn/view {:style style/page-heading}
      [quo/page-nav {:type :no-title :background :blur}]
      [page-title]]
     [enable-notifications-simple]
     [enable-notification-buttons {:insets insets}]]))
