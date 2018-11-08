(ns status-im.ui.screens.pairing.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.utils.config :as config]
            [status-im.ui.screens.main-tabs.styles :as main-tabs.styles]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.screens.home.styles :as home.styles]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.ui.components.button.view :as buttons]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.toolbar.actions :as toolbar.actions]
            [status-im.ui.screens.profile.components.views :as profile.components]
            [status-im.ui.screens.pairing.styles :as styles]))

(defn synchronize-installations! []
  (re-frame/dispatch [:pairing.ui/synchronize-installation-pressed]))

(defn pair! []
  (re-frame/dispatch [:pairing.ui/pair-devices-pressed]))

(defn enable-installation! [installation-id _]
  (re-frame/dispatch [:pairing.ui/enable-installation-pressed installation-id]))

(defn disable-installation! [installation-id _]
  (re-frame/dispatch [:pairing.ui/disable-installation-pressed installation-id]))

(defn footer []
  [react/touchable-highlight {:style main-tabs.styles/tabs-container}
   [react/view
    {:style styles/footer-content
     :on-press synchronize-installations!}
    [react/text
     {:style styles/footer-text}
     (i18n/label :t/sync-all-devices)]]])

(defn pair-this-device []
  [react/touchable-highlight {:on-press pair!
                              :style styles/pair-this-device}
   [react/view {:style styles/pair-this-device-actions}
    [react/view
     [react/view (styles/pairing-button true)
      [icons/icon :icons/add (styles/pairing-button-icon true)]]]
    [react/view {:style styles/pairing-actions-text}
     [react/view
      [react/text {:style styles/pair-this-device-title} (i18n/label :t/pair-this-device)]]
     [react/view
      [react/text (i18n/label :t/pair-this-device-description)]]]]])

(defn render-row [{:keys [device-type enabled? installation-id]}]
  [react/touchable-highlight
   {:on-press (if enabled?
                (partial disable-installation! installation-id)
                (partial enable-installation! installation-id))
    :accessibility-label :installation-item}
   [react/view styles/installation-item
    [react/view (styles/pairing-button enabled?)
     [icons/icon (if (= "desktop"
                        device-type)
                   :icons/desktop
                   :icons/mobile)
      (styles/pairing-button-icon enabled?)]]
    [react/view {:style styles/pairing-actions-text}
     [react/view
      [react/text {:style styles/installation-item-name-text}
       (gfycat/generate-gfy installation-id)]]
     [react/view
      [react/text
       (if enabled?
         (i18n/label :t/syncing-enabled)
         (i18n/label :t/syncing-disabled))]]]]])

(defn render-rows [installations]
  [react/view styles/wrapper
   [list/flat-list {:data               installations
                    :default-separator? false
                    :key-fn             :installation-id
                    :render-fn          render-row}]])

(views/defview installations []
  (views/letsubs [installations [:pairing/installations]]
    [react/view {:flex 1}
     [status-bar/status-bar]
     [toolbar/toolbar {}
      toolbar/default-nav-back
      [toolbar/content-title (i18n/label :t/devices)]]
     [react/scroll-view {:style {:background-color :white}}
      [pair-this-device]
      (when (seq installations)
        [react/view {:style styles/installation-list}
         [react/view styles/paired-devices-title
          [react/text (i18n/label :t/paired-devices)]]
         (render-rows installations)])]
     (when (seq installations) [footer])]))
