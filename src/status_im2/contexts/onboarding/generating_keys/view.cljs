(ns status-im2.contexts.onboarding.generating-keys.view
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [status-im2.contexts.onboarding.generating-keys.style :as style]
            [status-im2.contexts.onboarding.common.background.view :as background]
            [utils.i18n :as i18n]))

(defn page
  [{:keys [navigation-bar-top]}]
  [rn/view {:style style/page-container}
   [rn/view
    {:style {:height     56
             :margin-top navigation-bar-top}}]
   [rn/view {:style {:padding-horizontal 20}}
    [quo/text
     {:size   :heading-1
      :weight :semi-bold
      :style  {:color colors/white}} (i18n/label :t/generating-keys)]]])

(defn generating-keys
  []
  (let [{:keys [top]} (safe-area/get-insets)]
    [rn/view {:style {:flex 1}}
     [page {:navigation-bar-top top}]]))
