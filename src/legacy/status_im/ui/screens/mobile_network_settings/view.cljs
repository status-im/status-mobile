(ns legacy.status-im.ui.screens.mobile-network-settings.view
  (:require-macros [legacy.status-im.utils.views :as views])
  (:require
    legacy.status-im.mobile-sync-settings.core
    [legacy.status-im.ui.components.react :as react]
    [legacy.status-im.ui.screens.mobile-network-settings.sheets :as sheets]
    [legacy.status-im.ui.screens.mobile-network-settings.style :as styles]
    [legacy.status-im.ui.screens.profile.components.views :as profile.components]
    [re-frame.core :as re-frame]
    [utils.i18n :as i18n]))

(defn hide-sheet-and-dispatch
  [event]
  (re-frame/dispatch [:bottom-sheet/hide-old])
  (re-frame/dispatch event))

(defn settings-separator
  []
  [react/view
   {:style (styles/settings-separator)}])

;; TODO(Ferossgp): To refactor, uses outdated components
(views/defview mobile-network-settings
  []
  (views/letsubs
    [{:keys [syncing-on-mobile-network?
             remember-syncing-choice?]}
     [:profile/profile]]
    [:<>
     [react/view
      {:style               styles/switch-container
       :accessibility-label "mobile-network-use-mobile"}
      [profile.components/settings-switch-item
       {:label-kw  :t/mobile-network-use-mobile
        :value     syncing-on-mobile-network?
        :action-fn #(hide-sheet-and-dispatch [:mobile-network/set-syncing %])}]]
     [react/view {:style styles/details}
      [react/text {:style styles/use-mobile-data-text}
       (i18n/label :t/mobile-network-use-mobile-data)]]
     [react/view
      {:style               styles/switch-container
       :accessibility-label "mobile-network-ask-me"}
      [profile.components/settings-switch-item
       {:label-kw  :t/mobile-network-ask-me
        :value     (not remember-syncing-choice?)
        :action-fn #(hide-sheet-and-dispatch [:mobile-network/ask-on-mobile-network? %])}]]
     [settings-separator]
     [react/view
      {:style styles/defaults-container}
      [react/text
       {:style               styles/defaults
        :accessibility-label "restore-defaults"
        :on-press            #(hide-sheet-and-dispatch [:mobile-network/restore-defaults])}
       (i18n/label :t/restore-defaults)]]]))

(def offline-sheet
  {:content sheets/offline-sheet})
