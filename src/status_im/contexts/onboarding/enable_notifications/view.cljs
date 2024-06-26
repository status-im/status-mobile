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

(defn enable-notification-buttons
  [{:keys [insets]}]
  (let [profile-color (rf/sub [:onboarding/customization-color])]
    [rn/view {:style (style/buttons insets)}
     [quo/button
      {:on-press            (fn []
                              (shell.utils/change-selected-stack-id
                               shell.constants/default-selected-stack
                               true
                               nil)
                              (rf/dispatch
                               [:request-permissions
                                {:permissions [:post-notifications]
                                 :on-allowed  #(log/info "push notification permissions were allowed")
                                 :on-denied   #(log/error "user denied push notification permissions")}])
                              (rf/dispatch [:push-notifications/switch true])
                              (rf/dispatch [:navigate-to-within-stack
                                            [:screen/onboarding.welcome
                                             :screen/onboarding.enable-notifications]]))
       :type                :primary
       :icon-left           :i/notifications
       :accessibility-label :enable-notifications-button
       :customization-color profile-color}
      (i18n/label :t/intro-wizard-title6)]
     [quo/button
      {:on-press            (fn []
                              (shell.utils/change-selected-stack-id
                               shell.constants/default-selected-stack
                               true
                               nil)
                              (rf/dispatch [:navigate-to-within-stack
                                            [:screen/onboarding.welcome
                                             :screen/onboarding.enable-notifications]]))
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
      [quo/page-nav
       {:background :blur
        :icon-name  :i/arrow-left
        :on-press   #(rf/dispatch [:navigate-back])}]
      [page-title]]
     [enable-notifications-simple]
     [enable-notification-buttons {:insets insets}]]))
