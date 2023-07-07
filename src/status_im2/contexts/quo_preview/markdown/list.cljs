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
   {:label "Description:"
    :key   :description
    :type  :text}
   {:label "Tag name:"
    :key   :tag-name
    :type  :text}
   {:label "Description After Tag (Set tag name):"
    :key   :description-after-tag
    :type  :text}
   {:label   "Type: (step uses index)"
    :key     :type
    :type    :select
    :options [{:key   :bullet
               :value :bullet}
              {:key   :step
               :value :step}]}
   {:label "Step Number:"
    :key   :step-number
    :type  :text}
   {:label   "Customization Color:"
    :key     :customization-color
    :type    :select
    :options [{:key   :blue
               :value :blue}
              {:key   :army
               :value :army}
              {:key   :none
               :value :none}]}
   {:label "Blur? (Dark only):"
    :key   :blur?
    :type  :boolean}])

(defn cool-preview
  []
  (let [state (reagent/atom {:title       "Be respectful"
                             :description "Lorem ipsum dolor sit amet."})]
    (fn []
      (let [{:keys [title step-number customization-color description tag-name description-after-tag type
                    blur?]} @state
            tag-picture     (when tag-name (resources/get-mock-image :monkey))]
        [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
         [rn/view {:padding-bottom 150}
          [preview/customizer state descriptor]
          [rn/view
           {:padding-vertical 60
            :background-color (when blur? "#484F5E")}
           [quo/markdown-list
            {:type                  type
             :blur?                 blur?
             :title                 (when (pos? (count title)) title)
             :step-number           step-number
             :description           description
             :tag-name              tag-name
             :tag-picture           tag-picture
             :description-after-tag description-after-tag
             :customization-color   customization-color}]]]]))))

(defn preview-markdown-list
  []
  [rn/view
   {:background-color
    (colors/theme-colors colors/white colors/neutral-95)
    :flex 1}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
