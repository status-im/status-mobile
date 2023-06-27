(ns status-im2.contexts.quo-preview.markdown.list
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]
            [status-im2.common.resources :as resources]))

(def descriptor
  [{:label "Title:"
    :key   :title
    :type  :text}
   {:label   "Description:"
    :key     :description
    :type    :select
    :options [{:key   :normal
               :value :normal}
              {:key   :avatar
               :value :avatar}
              {:key   :none
               :value :none}]}
   {:label "Index:"
    :key   :index
    :type  :text}
   {:label   "Customization Color:"
    :key     :customization-color
    :type    :select
    :options [{:key   :blue
               :value :blue}
              {:key   :army
               :value :army}
              {:key   :none
               :value :none}]}])

(defn cool-preview
  []
  (let [state (reagent/atom {:title       "Be respectful"
                             :description :normal})]
    (fn []
      (let [title       (:title @state)
            index       (:index @state)
            step-props  (case (:customization-color @state)
                          :blue {:type                :complete
                                 :customization-color :blue}
                          :army {:type                :complete
                                 :customization-color :army}
                          nil)
            description (case (:description @state)
                          :normal "Lorem ipsum dolor sit amet."
                          :avatar [rn/view {:style {:flex-direction :row :align-items :center}}
                                   [quo/text {} "Lorem ipsum "]
                                   [quo/context-tag {:size :small} (resources/get-mock-image :monkey)
                                    "dolor"]
                                   [quo/text {} " sit amet."]]
                          nil)]
        [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
         [rn/view {:padding-bottom 150}
          [preview/customizer state descriptor]
          [rn/view {:padding-vertical 60}
           [quo/markdown-list
            {:title       (when (pos? (count title)) title)
             :index       index
             :description description
             :step-props  step-props}]]]]))))

(defn preview-markdown-list
  []
  [rn/view
   {:background-color (colors/theme-colors colors/white colors/neutral-90)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
