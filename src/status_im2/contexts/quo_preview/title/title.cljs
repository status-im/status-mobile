(ns status-im2.contexts.quo-preview.title.title
  (:require [quo2.components.text-combinations.title.view :as quo2]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label "Title"
    :key   :title
    :type  :text}
   {:label "Subtitle"
    :key   :subtitle
    :type  :text}])

(defn cool-preview
  []
  (let [state (reagent/atom {:title                        "Title"
                             :title-accessibility-label    :title
                             :subtitle                     ""
                             :subtitle-accessibility-label :subtitle})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [preview/customizer state descriptor]
        [rn/view
         {:padding-vertical 60
          :align-items      :center}
         [quo2/title @state]]]])))

(defn preview-title
  []
  [rn/view
   {:background-color (colors/theme-colors colors/white colors/neutral-90)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :flex-grow                    1
     :nested-scroll-enabled        true
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
