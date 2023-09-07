(ns status-im2.contexts.quo-preview.password.tips
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label "Text"
    :key   :text
    :type  :text}
   {:label "Completed"
    :key   :completed?
    :type  :boolean}])

(defn preview-tips
  []
  (let [state      (reagent/atom {:text       "Lower case"
                                  :completed? false})
        text       (reagent/cursor state [:text])
        completed? (reagent/cursor state [:completed?])]
    (fn []
      [preview/preview-container
       {:state      state
        :descriptor descriptor}
       [rn/view {:padding-bottom 150}
        [rn/view {:padding 60 :background-color colors/neutral-95}
         [quo/tips {:completed? @completed?} @text]]]])))
