(ns status-im2.contexts.quo-preview.selectors.reactions
  (:require [quo2.core :as quo]
            [react-native.core :as rn]
            [status-im2.contexts.quo-preview.preview :as preview]
            [status-im2.constants :as constants]))

(defn view
  []
  [preview/preview-container
   {:component-container-style {:flex            1
                                :padding-top     20
                                :flex-direction  :row
                                :justify-content :center
                                :align-items     :center}}
   [rn/view {:flex-direction :row}
    (for [emoji (vals constants/reactions)]
      ^{:key emoji}
      [quo/reactions emoji {:container-style {:margin-right 5}}])]])
