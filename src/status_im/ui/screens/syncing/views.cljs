(ns status-im.ui.screens.syncing.views
  (:require [quo.react-native :as rn]
            [status-im.ui.screens.syncing.styles :as styles]
            [status-im.i18n.i18n :as i18n]
            [quo2.components.avatars.icon-avatar :as quo2]
            [quo.core :as quo]
            [re-frame.core :as re-frame]
            [status-im.ui.screens.syncing.sheets.sync-device-notice.views :as sync-device-notice]
            [status-im.utils.handlers :refer [>evt]]
            [quo2.components.buttons.button :as quo2-button]))

(defn render-device [device-name device-status]
  [:<>
   [rn/view {:style styles/device-row}
    [quo2/icon-avatar {:size  :medium
                       :icon  :main-icons/placeholder20
                       :color :primary
                       :style {:margin-vertical 10}}]
    [rn/view {:style styles/device-column}
     [rn/text {:style styles/device-name} device-name]
     [rn/text {:style styles/device-status} device-status]]]])

(defn views []
  [rn/view {:style styles/container-main}
   [rn/text {:style styles/synced-devices-text} (i18n/label :t/synced-devices)]
   [rn/view {:style styles/devices-container}
    [render-device "iPhone 11" "This device"]
    [rn/view {:style styles/sync-device-container}
     [quo2-button/button {:label :primary
                          :size 40
                          :before :main-icons2/placeholder
                          :on-press #(>evt [:bottom-sheet/show-sheet
                                            {:showHandle? false
                                             :content (fn []
                                                        [sync-device-notice/views])}])}
      "Sync New Device"]]]])
