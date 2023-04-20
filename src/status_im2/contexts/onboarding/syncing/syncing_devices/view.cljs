(ns status-im2.contexts.onboarding.syncing.syncing-devices.view
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [status-im2.contexts.onboarding.syncing.syncing-devices.style :as style]
            [status-im2.contexts.onboarding.common.background.view :as background]
            [status-im2.contexts.onboarding.common.navigation-bar.view :as navigation-bar]))

(defn page
  [{:keys [navigation-bar-top]}]
  [rn/view {:style style/page-container}
   [navigation-bar/navigation-bar
    {:top                   navigation-bar-top
     :right-section-buttons [{:type                :blur-bg
                              :icon                :i/info
                              :icon-override-theme :dark
                              :on-press            #(js/alert "Pending")}]}]
   [rn/view {:style {:padding-horizontal 20}}
    [quo/text
     {:size   :heading-1
      :weight :semi-bold
      :style  {:color colors/white}} "Syncing devices..."]
    [quo/text
     {:size   :heading-2
      :weight :semi-bold
      :style  {:color colors/white}} "will show sync complete if successful"]
    [quo/text
     {:size   :heading-2
      :weight :semi-bold
      :style  {:color colors/white}} "will show sync failed if unsuccessful"]]])

(defn syncing-devices
  []
  (fn []
    [safe-area/consumer
     (fn [{:keys [top]}]
       [rn/view {:style {:flex 1}}
        [background/view true]
        [page {:navigation-bar-top top}]])]))
