(ns status-im2.contexts.onboarding.syncing.results.view
  (:require [quo2.core :as quo]
            [utils.i18n :as i18n]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [utils.re-frame :as rf]
            [react-native.safe-area :as safe-area]
            [status-im2.contexts.onboarding.syncing.results.style :as style]
            [status-im2.contexts.onboarding.common.syncing.render-device :as device]
            [status-im2.contexts.onboarding.common.background.view :as background]))

(defn page-title
  []
  [rn/view {:style {:padding-horizontal 20}}
   [quo/text
    {:accessibility-label :notifications-screen-title
     :weight              :semi-bold
     :size                :heading-1
     :style               {:color colors/white}}
    (i18n/label :t/sync-devices-complete-title)]
   [quo/text
    {:accessibility-label :notifications-screen-sub-title
     :weight              :regular
     :size                :paragraph-1
     :style               {:color colors/white}}
    (i18n/label :t/sync-devices-complete-sub-title)]])

(defn navigation-bar
  []
  [rn/view {:style style/navigation-bar}
   [quo/page-nav]])

(defn current-device
  [installations]
  [rn/view {:style style/current-device}
   [device/render-device
    (merge (first installations)
           {:this-device? true})]
   [quo/text
    {:accessibility-label :notifications-screen-sub-title
     :weight              :regular
     :size                :paragraph-1
     :style               {:color colors/white}}
    (i18n/label :t/sync-with)]])

(defn render-device-list
  [installations]
  [rn/view {:style style/device-list}
   (when (seq installations)
     [rn/flat-list
      {:data                            (rest installations)
       :default-separator?              false
       :shows-vertical-scroll-indicator false
       :key-fn                          :installation-id
       :header                          [current-device installations]
       :render-fn                       device/render-device}])])

(defn view
  []
  (let [profile-color (:color (rf/sub [:onboarding-2/profile]))
        installations (rf/sub [:pairing/installations])]
    [safe-area/consumer
     (fn [insets]
       [rn/view {:style (style/page-container (:top insets))}
        [background/view true]
        [navigation-bar]
        [page-title]
        [render-device-list installations]
        [quo/button
         {:on-press                  #(rf/dispatch [:init-root :enable-notifications])
          :accessibility-label       :enable-notifications-later-button
          :override-background-color (colors/custom-color profile-color 60)
          :style                     {:margin-top         20
                                      :padding-horizontal 20}}
         (i18n/label :t/continue)]])]))
