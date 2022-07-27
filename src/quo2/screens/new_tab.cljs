(ns quo2.screens.new-tab
  (:require [quo.react-native :as rn]
            [quo.previews.preview :as preview]
            [reagent.core :as reagent]
            [quo2.components.new-tab :as quo2]
            [quo.design-system.colors :as colors]))

(def descriptor [{:label   "Size"
                  :key     :size
                  :type    :select
                  :options [{:key   :large
                             :value "Large"}
                            {:key   :medium
                             :value "Medium"}]}
                 {:label "Text"
                  :key   :text
                  :type  :text}
                 {:label "Icon (Type none for none!)"
                  :key   :icon
                  :type  :text}
                 {:label "Opaque"
                  :key   :opaque?
                  :type  :boolean}
                 {:label "Theme"
                  :key   :theme
                  :type  :select
                  :options [{:key :light
                             :value "Light"}
                            {:key :dark
                             :value "Dark"}]}])

(defn cool-preview []
  (let [state (reagent/atom {})]
    (fn []
      [rn/view {:margin-bottom 50
                :padding       16}
       [preview/customizer state descriptor]
       [rn/view {:padding-vertical 60
                 :align-items      :center}
        [quo2/new-tab @state ]]])))

(defn preview-new-tabs []
  [rn/view {:background-color (:ui-background @colors/theme)
            :flex             1}
   [rn/flat-list {:flex                      1
                  :keyboardShouldPersistTaps :always
                  :header                    [cool-preview]
                  :key-fn                    str}]])
