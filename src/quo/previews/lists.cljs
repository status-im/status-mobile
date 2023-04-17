(ns quo.previews.lists
  (:require [quo.core :as quo]
            [quo.design-system.colors :as colors]
            [quo.previews.preview :as preview]
            [quo.react-native :as rn]
            [reagent.core :as reagent]))

(def all-props (preview/list-comp [] {}))

(defn avatar
  []
  [rn/view
   {:border-radius    20
    :width            40
    :height           40
    :justify-content  :center
    :align-items      :center
    :background-color :red}
   [quo/text
    {:weight :bold
     :size   :large}
    "T"]])

(defn icon-element
  [type]
  (case type
    :icon      :main-icons/add-contact
    :component [avatar]
    nil))

(def descriptor
  [{:label   "Accessory:"
    :key     :accessory
    :type    :select
    :options [{:key   :radio
               :value "Radio"}
              {:key   :checkbox
               :value "Checkbox"}
              {:key   :switch
               :value "Switch"}
              {:key   :text
               :value "Text"}
              {:key   :default
               :value "Default"}]}
   {:label   "Size:"
    :key     :size
    :type    :select
    :options [{:key   :small
               :value "Small"}
              {:key   :default
               :value "Default"}]}
   {:label   "Icon:"
    :key     :icon
    :type    :select
    :options [{:key   :icon
               :value "Icon"}
              {:key   :component
               :value "Component"}]}
   {:label   "Theme:"
    :key     :theme
    :type    :select
    :options [{:key   :main
               :value "Main"}
              {:key   :accent
               :value "Accent"}
              {:key   :negative
               :value "Negative"}
              {:key   :positive
               :value "Positive"}]}
   {:label "Selectable"
    :key   :selectable
    :type  :boolean}
   {:label "Chevron"
    :key   :chevron
    :type  :boolean}
   {:label "Disabled:"
    :key   :disabled
    :type  :boolean}
   {:label "Title"
    :key   :title
    :type  :text}
   {:label "Subtitle"
    :key   :subtitle
    :type  :text}])

(defn render-item
  [_]
  [rn/view {:style {:padding-vertical 24}}])

(defn cool-preview
  []
  (let [state      (reagent/atom {:title  "Title"
                                  :active false})
        icon       (reagent/cursor state [:icon])
        active     (reagent/cursor state [:active])
        selectable (reagent/cursor state [:selectable])]
    (fn []
      [rn/view {:margin-bottom 50}
       [rn/view {:padding-horizontal 16}
        [preview/customizer state descriptor]]
       [rn/view {:padding-vertical 16}
        [quo/list-item
         (merge (dissoc @state :active :selectable)
                (when @selectable
                  {:active   @active
                   :on-press #(swap! active not)})
                {:accessory-text "Accessory"
                 :icon           (icon-element @icon)})]]])))

(defn preview
  []
  [rn/view
   {:background-color (:ui-background @colors/theme)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :data                         all-props
     :render-fn                    render-item
     :key-fn                       str}]])
