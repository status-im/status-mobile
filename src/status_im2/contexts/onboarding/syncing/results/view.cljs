(ns status-im2.contexts.onboarding.syncing.results.view
  (:require [quo2.core :as quo]
            [utils.i18n :as i18n]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [utils.re-frame :as rf]
            [status-im2.contexts.onboarding.syncing.results.style :as style]
            [status-im2.contexts.syncing.device.view :as device]
            [status-im2.contexts.onboarding.common.background.view :as background]))

(defn page-title
  []
  [quo/title
   {:title                        (i18n/label :t/sync-devices-complete-title)
    :title-accessibility-label    :sync-devices-title
    :subtitle                     (i18n/label :t/sync-devices-complete-sub-title)
    :subtitle-accessibility-label :sync-devices-complete-sub-title}])

(defn current-device
  [installation]
  [rn/view {:style style/current-device}
   [device/view
    (merge installation
           {:this-device? true})]])

(defn devices-list
  []
  (let [installations (rf/sub [:pairing/enabled-installations])
        this-device   (first installations)
        other-devices (rest installations)]
    [rn/view {:style style/device-list}
     [current-device this-device]
     [quo/text
      {:accessibility-label :synced-with-sub-title
       :weight              :regular
       :size                :paragraph-2
       :style               {:color colors/white-opa-40}}
      (i18n/label :t/synced-with)]
     [rn/flat-list
      {:data                            other-devices
       :shows-vertical-scroll-indicator false
       :key-fn                          :installation-id
       :render-fn                       device/view}]]))

(defn continue-button
  []
  (let [profile-color (:color (rf/sub [:onboarding-2/profile]))]
    [quo/button
     {:on-press            #(rf/dispatch [:init-root :enable-notifications])
      :accessibility-label :continue-button
      :customization0color profile-color
      :style               style/continue-button}
     (i18n/label :t/continue)]))

(defn view
  []
  (let [top (safe-area/get-top)]
    [rn/view {:style (style/page-container top)}
     [background/view true]
     [rn/view
      {:style {:margin-top    56
               :margin-bottom 26
               :flex          1}}
      [page-title]
      [devices-list]
      [continue-button]]]))
