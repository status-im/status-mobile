(ns status-im.ui.screens.pairing.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.utils.config :as config]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.ui.components.button.view :as buttons]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.toolbar.actions :as toolbar.actions]
            [status-im.ui.screens.profile.components.views :as profile.components]
            [status-im.ui.screens.pairing.styles :as styles]))

(defn synchronize-installation! [id]
  #_(re-frame/dispatch [:pairing.ui/synchronize-installation-pressed id]))

(defn pair! []
  (re-frame/dispatch [:pairing.ui/pair-devices-pressed]))

(defn enable-installation! [installation-id _]
  (re-frame/dispatch [:pairing.ui/enable-installation-pressed installation-id]))

(defn disable-installation! [installation-id _]
  (re-frame/dispatch [:pairing.ui/disable-installation-pressed installation-id]))

(defn render-row [{:keys [device-type enabled? installation-id]}]
  [react/touchable-highlight
   {:on-press            #(synchronize-installation! installation-id)
    :accessibility-label :installation-item}
   [react/view styles/installation-item
    [react/view styles/installation-item-inner
     [react/view
      [react/text {:style styles/installation-item-name-text}
       (str (gfycat/generate-gfy installation-id) " - " (or device-type
                                                            "unknown"))]]
     [react/view
      (if enabled?
        [buttons/primary-button
         {:on-press (partial disable-installation! installation-id)}
         (i18n/label :t/enabled)]
        [buttons/secondary-button
         {:on-press (partial enable-installation! installation-id)}
         (i18n/label :t/disabled)])]]]])

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
      [toolbar/content-title (i18n/label :t/devices)]
      [toolbar/actions
       [(toolbar.actions/add false pair!)]]]
     (render-rows installations)]))
