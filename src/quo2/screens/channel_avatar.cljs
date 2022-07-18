(ns quo2.screens.channel-avatar
  (:require [reagent.core :as reagent]
            [quo.react-native :as rn]
            [quo.previews.preview :as preview]
            [quo2.foundations.colors :as colors]
            [quo2.components.channel-avatar :as quo2]))

(def descriptor [{:label "Size"
                  :key :big?
                  :type :boolean}
                 {:label "X"
                  :key :x
                  :type :text}
                 {:label "Y"
                  :key :y
                  :type :text}
                 {:label "Dark"
                  :key :dark?
                  :type :boolean}
                 {:label "Lock status"
                  :key :lock-status
                  :type :select
                  :options [{:key   :unlocked
                             :value "Unlocked"}
                            {:key   :locked
                             :value "Locked"}]}
                 {:label "Icon"
                  :key :icon
                  :type :text}])

(defn cool-preview []
  (let [state (reagent/atom {:x 20
                             :y 50
                             :big? true
                             :dark? true
                             :lock-status :locked
                             :icon "peach16"})]
    (fn []
      [rn/view {:margin-bottom 50
                :padding       16}
       [rn/view {:flex 1}
        [preview/customizer state descriptor]]
       [rn/view {:padding-vertical 60
                 :flex-direction   :row
                 :justify-content  :center}
        [quo2/channel-avatar @state]]])))

(defn preview-channel-avatar []
  [rn/view {:background-color (colors/theme-colors colors/white
                                                   colors/neutral-90)
            :flex             1}
   [rn/flat-list {:flex                      1
                  :keyboardShouldPersistTaps :always
                  :header                    [cool-preview]
                  :key-fn                    str}]])