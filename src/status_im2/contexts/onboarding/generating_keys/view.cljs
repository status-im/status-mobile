(ns status-im2.contexts.onboarding.generating-keys.view
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [status-im2.contexts.onboarding.generating-keys.style :as style]
            [status-im2.contexts.onboarding.common.background.view :as background]
            [utils.i18n :as i18n]))

(defn navigation-bar
  []
  [rn/view {:style style/navigation-bar}
   [quo/page-nav
    {:align-mid?  true
     :mid-section {:type :text-only :main-text ""}}]])

(defn page
  []
  [rn/view {:style style/page-container}
   [navigation-bar]
   [rn/view {:style {:padding-horizontal 20}}
    [quo/text
     {:size   :heading-1
      :weight :semi-bold
      :style  {:color colors/white}} (i18n/label :t/generating-keys)]]])

(defn generating-keys
  []
  [rn/view {:style {:flex 1}}
   [background/view true]
   [page]])
