(ns status-im2.contexts.quo-preview.selectors.disclaimer
  (:require [quo2.core :as quo]
            [quo2.theme :as theme]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label "Checked:"
    :key   :checked?
    :type  :boolean}
   {:label "Blur (only for dark theme):"
    :key   :blur?
    :type  :boolean}
   {:label "Text"
    :key   :text
    :type  :text}])

(defn blur-background
  [blur?]
  (when (and blur? (theme/dark?))
    [rn/view
     {:style {:position :absolute
              :top      0
              :bottom   0
              :left     0
              :right    0}}
     [preview/blur-view
      {:style                 {:flex 1}
       :show-blur-background? true}]]))

(defn preview-disclaimer
  []
  (let [state (reagent/atom {:checked? false
                             :blur?    true
                             :text     "I agree with the community rules"})]
    (fn []
      (let [{:keys [blur? checked? text]} @state]
        [preview/preview-container
         {:state      state
          :descriptor descriptor}
         [rn/view {:style {:padding-horizontal 15}}
          [blur-background blur?]
          [rn/view {:style {:margin-vertical 50}}
           [quo/disclaimer
            {:blur?     blur?
             :checked?  checked?
             :on-change #(swap! state update :checked? not)}
            text]]
          [quo/button {:disabled? (not checked?)}
           "submit"]]]))))
