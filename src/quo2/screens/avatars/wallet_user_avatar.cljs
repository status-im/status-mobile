(ns quo2.screens.avatars.wallet-user-avatar
  (:require [quo.react-native :as rn]
            [quo.previews.preview :as preview]
            [reagent.core :as reagent]
            [quo2.components.avatars.wallet-user-avatar :as quo2]
            [quo2.foundations.colors :as colors]))

(def descriptor [{:label   "First name"
                  :key     :f-name
                  :type    :text}
                 {:label   "Last name"
                  :key     :l-name
                  :type    :text}
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
                  :options (map
                            (fn [c]
                              {:key   c
                               :value c})
                            (keys colors/customization))}])

(defn cool-preview []
  (let [state     (reagent/atom {:first-name "empty"
                                 :last-name "name"
                                 :size :x-large
                                 :color :indigo})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [preview/customizer state descriptor]
        [rn/view {:padding-vertical 60}
         [quo2/wallet-user-avatar @state]]]])))

(defn preview-wallet-user-avatar []
  [rn/view {:background-color (colors/theme-colors colors/white colors/neutral-90)
            :flex             1}
   [rn/flat-list {:flex                      1
                  :keyboardShouldPersistTaps :always
                  :header                    [cool-preview]
                  :key-fn                    str}]])
