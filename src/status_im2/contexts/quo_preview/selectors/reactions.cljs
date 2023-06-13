(ns status-im2.contexts.quo-preview.selectors.reactions
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [status-im2.constants :as constants]))

(defn cool-preview
  []
  [rn/view
   [rn/view {:style {:margin-vertical 24}}
    (into [rn/view
           {:style {:flex            1
                    :margin-top      200
                    :flex-direction  :row
                    :justify-content :center
                    :align-items     :center}}]
          (for [emoji (vals constants/reactions)]
            ^{:key emoji}
            [quo/reactions emoji {:container-style {:margin-right 5}}]))]])

(defn preview
  []
  [rn/view
   {:style {:background-color (colors/theme-colors colors/white colors/neutral-95)
            :flex             1}}
   [rn/flat-list
    {:style                        {:flex 1}
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
