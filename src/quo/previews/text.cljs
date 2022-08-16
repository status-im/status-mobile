(ns quo.previews.text
  (:require [reagent.core :as reagent]
            [quo.core :as quo]
            [quo.animated :as animated]
            [quo.react-native :as rn]
            [quo.design-system.colors :as colors]
            [quo.previews.preview :as preview]))

(def all-props (preview/list-comp [size   [:tiny :small :base :large :x-large :xx-large]
                                   weight [:regular :medium :semi-bold :bold :monospace]]
                                  {:weight weight
                                   :size   size}))

(def descriptor [{:label   "Size:"
                  :key     :size
                  :type    :select
                  :options [{:key   :tiny
                             :value "Tiny"}
                            {:key   :small
                             :value "Small"}
                            {:key   :base
                             :value "Base"}
                            {:key   :large
                             :value "Large"}
                            {:key   :x-large
                             :value "X-Large"}
                            {:key   :xx-large
                             :value "XX-Large"}]}
                 {:label   "Weight:"
                  :key     :weight
                  :type    :select
                  :options [{:key   :regular
                             :value "Regular"}
                            {:key   :medium
                             :value "Medium"}
                            {:key   :semi-bold
                             :value "Semi-bold"}
                            {:key   :bold
                             :value "Bold"}
                            {:key   :monospace
                             :value "Monospace"}]}
                 {:label   "Color:"
                  :key     :color
                  :type    :select
                  :options [{:key   :main
                             :value "main"}
                            {:key   :secondary
                             :value "secondary"}
                            {:key   :secondary-inverse
                             :value "secondary-inverse"}
                            {:key   :link
                             :value "link"}
                            {:key   :negative
                             :value "negative"}
                            {:key   :positive
                             :value "positive"}]}
                 {:label "Animated:"
                  :key   :animated?
                  :type  :boolean}])

(defn render-item [props]
  [rn/view {:style {:padding-vertical   24
                    :padding-horizontal 16}}
   [quo/text props
    (str "Text size " props " number 0 1x2")]])

(defn cool-preview []
  (let [state     (reagent/atom {})
        animation    (animated/value 0)]
    (fn []
      [rn/view {:margin-bottom 50
                :padding       16}
       [animated/code {:exec (animated/set animation (animated/loop* {:duration 1000}))}]
       [preview/customizer state descriptor]
       [rn/view {:padding-vertical 16}
        [quo/text (merge @state
                         (when (:animated? @state)
                           {:opacity animation}))
         "This is a demo text 1 2 0 2x2 0x0"]]])))

(defn preview-text []
  [rn/view {:background-color (:ui-background @colors/theme)
            :flex             1}
   [rn/flat-list {:flex                      1
                  :keyboardShouldPersistTaps :always
                  :header                    [cool-preview]
                  :data                      all-props
                  :render-fn                 render-item
                  :key-fn                    str}]])
