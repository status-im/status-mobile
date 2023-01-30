(ns status-im2.contexts.syncing.view
  (:require [utils.i18n :as i18n]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [status-im2.contexts.syncing.sheets.sync-device-notice.view :as sync-device-notice]
            [status-im2.contexts.syncing.styles :as styles]
            [utils.re-frame :as rf]))

(defn render-device
  [device-name device-status]
  [:<>
   [rn/view {:style styles/device-row}
    [quo/icon-avatar
     {:size  :medium
      :icon  :i/placeholder
      :color :primary
      :style {:margin-vertical 10}}]
    [rn/view {:style styles/device-column}
     [quo/text
      {:accessibility-label :device-name
       :weight              :medium
       :size                :paragraph-1
       :style               {:color colors/neutral-100}} device-name]

     [quo/text
      {:accessibility-label :device-status
       :weight              :regular
       :size                :paragraph-2
       :style               {:color colors/neutral-50}} device-status]]]])

(defn views
  []
  [rn/view {:style styles/container-main}
   [quo/text
    {:accessibility-label :synced-devices-title
     :weight              :medium
     :size                :paragraph-2
     :style               {:color colors/neutral-50}} (i18n/label :t/synced-devices)]
   [rn/view {:style styles/devices-container}
    [render-device "iPhone 11" (i18n/label :t/this-device)] ;; note : the device name is hardcoded for
                                                            ;; now
    [rn/view {:style styles/sync-device-container}
     [quo/button
      {:label    :primary
       :size     40
       :before   :i/placeholder
       :on-press #(rf/dispatch [:bottom-sheet/show-sheet
                                {:show-handle? false
                                 :content      (fn []
                                                 [sync-device-notice/sheet])}])}
      (i18n/label :t/sync-new-device)]]]])
