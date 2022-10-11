(ns quo2.screens.buttons.dynamic-button
  (:require [quo.react-native :as rn]
            [reagent.core :as reagent]
            [quo.previews.preview :as preview]
            [quo2.foundations.colors :as colors]
            [quo2.components.buttons.dynamic-button :as quo2]))

(def descriptor [{:label   "Type:"
                  :key     :type
                  :type    :select
                  :options [{:key   :jump-to
                             :value "Jump To"}
                            {:key   :mention
                             :value "Mention"}
                            {:key   :notification-down
                             :value "Notification Down"}
                            {:key   :notification-up
                             :value "Notification Up"}
                            {:key   :search
                             :value "Search"}
                            {:key   :search-with-label
                             :value "Search With Label"}
                            {:key   :bottom
                             :value "Bottom"}]}
                 {:label "Count"
                  :key   :count
                  :type  :text}])

(defn cool-preview []
  (let [state (reagent/atom {:count 5 :type :jump-to})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [preview/customizer state descriptor]
        [rn/view {:padding-vertical 60
                  :align-items      :center}
         [quo2/dynamic-button @state]]]])))

(defn preview-dynamic-button []
  [rn/view {:background-color (colors/theme-colors colors/white colors/neutral-90)
            :flex             1}
   [rn/flat-list {:flex                      1
                  :keyboardShouldPersistTaps :always
                  :header                    [cool-preview]
                  :key-fn                    str}]])
