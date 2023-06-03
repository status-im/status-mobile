(ns status-im2.contexts.onboarding.syncing.results.view
  (:require [quo2.core :as quo]
            [utils.i18n :as i18n]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [utils.re-frame :as rf]
            [status-im2.contexts.onboarding.syncing.results.style :as style]
            [status-im2.common.syncing.view :as device]
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
           {:this-device? true})]
   [quo/text
    {:accessibility-label :sync-with-sub-title
     :weight              :regular
     :size                :paragraph-1
     :style               {:color colors/white}}
    (i18n/label :t/sync-with)]])

(defn devices-list
  []
  (let [installations (rf/sub [:pairing/enabled-installations])]
    [rn/view {:style style/device-list}
     [rn/flat-list
      {:data                            (rest installations)
       :shows-vertical-scroll-indicator false
       :key-fn                          :installation-id
       :header                          [current-device (first installations)]
       :render-fn                       device/view}]]))

(defn continue-button
  []
  (let [profile-color (:color (rf/sub [:onboarding-2/profile]))]
    [quo/button
     {:on-press                  #(rf/dispatch [:init-root :enable-notifications])
      :accessibility-label       :continue-button
      :override-background-color (colors/custom-color profile-color 60)
      :style                     style/continue-button}
     (i18n/label :t/continue)]))

(defn view
  []
  [rn/view {:style style/page-container}
   [background/view true]
   [quo/page-nav]
   [page-title]
   [devices-list]
   [continue-button]])
