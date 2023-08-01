(ns status-im2.contexts.quo-preview.loaders.community-card-skeleton
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]))

(defn cool-preview
  []
  [rn/view {:padding-bottom 150 :margin 20}
   [quo/community-card-skeleton]])

(defn preview-skeleton
  []
  [rn/view
   {:background-color (colors/theme-colors colors/white colors/neutral-95)
    :flex             1}
   [cool-preview]])
