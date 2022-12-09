(ns quo2.screens.avatars.icon-avatar
  (:require [reagent.core :as reagent]
            [quo.react-native :as rn]
            [quo.previews.preview :as preview]
            [quo.design-system.colors :as colors]
            [quo2.components.avatars.icon-avatar :as quo2]))

(def descriptor [{:label   "Size"
                  :key     :size
                  :type    :select
                  :options [{:key   :small
                             :value "Small"}
                            {:key   :medium
                             :value "Medium"}
                            {:key   :big
                             :value "Big"}]}
                 {:label   "Icon"
                  :key     :icon
                  :type    :select
                  :options [{:key   :main-icons/placeholder20
                             :value "Placeholder"}
                            {:key   :main-icons/walelt
                             :value "Wallet"}
                            {:key   :main-icons/play
                             :value "Play"}]}
                 {:label   "Color"
                  :key     :color
                  :type    :select
                  :options [{:key   :primary
                             :value "Primary"}
                            {:key   :purple
                             :value "Purple"}
                            {:key   :indigo
                             :value "Indigo"}
                            {:key   :turquoise
                             :value "Turquoise"}
                            {:key   :blue
                             :value "Blue"}
                            {:key   :green
                             :value "Green"}
                            {:key   :yellow
                             :value "yellow"}
                            {:key   :orange
                             :value "Orange"}
                            {:key   :red
                             :value "Red"}
                            {:key   :pink
                             :value "Pink"}
                            {:key   :brown
                             :value "Brown"}
                            {:key   :beige
                             :value "Beige"}]}])

(defn cool-preview []
  (let [state (reagent/atom {:size :big
                             :icon :main-icons/placeholder20
                             :color :primary})]
    (fn []
      [rn/view {:margin-bottom 50
                :padding       16}
       [preview/customizer state descriptor]
       [rn/view {:padding-vertical 60
                 :align-items      :center}
        [quo2/icon-avatar @state]]])))

(defn preview-icon-avatar []
  [rn/view {:background-color (:ui-background @colors/theme)
            :flex             1}
   [rn/flat-list {:flex                      1
                  :keyboardShouldPersistTaps :always
                  :header                    [cool-preview]
                  :key-fn                    str}]])