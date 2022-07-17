(ns quo2.screens.wallet-user-avatar
  (:require [quo.react-native :as rn]
            [quo.previews.preview :as preview]
            [reagent.core :as reagent]
            [quo2.components.wallet-user-avatar :as quo2]
            [quo.design-system.colors :as colors]))

(def descriptor [{:label   "First name"
                  :key     :f-name
                  :type    :text}
                 {:label   "Last name"
                  :key     :l-name
                  :type    :text}
                 {:label   "Dark"
                  :key     :dark?
                  :type    :boolean}
                 {:label   "Size"
                  :key     :size
                  :type    :select
                  :options [{:key   :small
                             :value "Small"}
                            {:key   :medium
                             :value "Medium"}
                            {:key   :large
                             :value "Large"}
                            {:key   :x-large
                             :value "X Large"}]}
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
  (let [state     (reagent/atom {})]
    (fn []
      [rn/view {:margin-bottom 50
                :padding       16}
       [preview/customizer state descriptor]
       [rn/view {:padding-vertical 60}
        [quo2/wallet-user-avatar @state]]])))

(defn preview-wallet-user-avatar []
  [rn/view {:background-color (:ui-background @colors/theme)
            :flex             1}
   [rn/flat-list {:flex                      1
                  :keyboardShouldPersistTaps :always
                  :header                    [cool-preview]
                  :key-fn                    str}]])