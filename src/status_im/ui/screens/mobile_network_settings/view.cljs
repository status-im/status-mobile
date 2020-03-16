(ns status-im.ui.screens.mobile-network-settings.view
  (:require-macros [status-im.utils.views :as views])
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.screens.mobile-network-settings.style :as styles]
            [status-im.i18n :as i18n]
            [re-frame.core :as re-frame]
            status-im.ui.screens.mobile-network-settings.events
            [status-im.ui.screens.profile.components.views :as profile.components]
            [status-im.utils.platform :as platform]
            [status-im.ui.screens.mobile-network-settings.sheets :as sheets]
            [status-im.ui.components.topbar :as topbar]))

(defn settings-separator []
  [react/view
   {:style (styles/settings-separator)}])

(views/defview mobile-network-settings []
  (views/letsubs
    [{:keys [syncing-on-mobile-network?
             remember-syncing-choice?]}
     [:multiaccount]]
    [react/view {:style styles/container}
     [topbar/topbar {:title :t/mobile-network-settings}]
     [react/view {:style styles/switch-container}
      [profile.components/settings-switch-item
       {:label-kw  :t/mobile-network-use-mobile
        :value     syncing-on-mobile-network?
        :action-fn #(re-frame/dispatch [:mobile-network/set-syncing %])}]]
     [react/view {:style styles/details}
      [react/text {:style styles/use-mobile-data-text}
       (i18n/label :t/mobile-network-use-mobile-data)]]
     [react/view {:style styles/switch-container}
      [profile.components/settings-switch-item
       {:label-kw  :t/mobile-network-ask-me
        :value     (not remember-syncing-choice?)
        :action-fn #(re-frame/dispatch [:mobile-network/ask-on-mobile-network? %])}]]
     [settings-separator]
     [react/view
      {:style styles/defaults-container}
      [react/text
       {:style    styles/defaults
        :on-press #(re-frame/dispatch [:mobile-network/restore-defaults])}
       "Restore Defaults"]]]))

(def settings-sheet
  {:content-height 340
   :content        sheets/settings-sheet})

(def offline-sheet
  {:content        sheets/offline-sheet
   :content-height 180})
