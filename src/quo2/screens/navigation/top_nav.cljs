(ns quo2.screens.navigation.top-nav
  (:require [quo.react-native :as rn]
            [quo.previews.preview :as preview]
            [reagent.core :as reagent]
            [quo2.components.navigation.top-nav :as quo2]
            [quo2.foundations.colors :as colors]))

(def descriptor [{:label   "Type"
                  :key     :type
                  :type    :select
                  :options [{:key   :default
                             :value "Default"}
                            {:key   :blur-bg
                             :value "Blurred BG"}
                            {:key   :shell
                             :value "Shell"}]}
                 {:label "New Notifications?"
                  :key   :new-notifications?
                  :type  :boolean}
                 {:label   "Notification Indicator"
                  :key     :notification-indicator
                  :type    :select
                  :options [{:key   :unread-dot
                             :value :unread-dot}
                            {:key   :counter
                             :value :counter}]}
                 {:label "Counter Label"
                  :key   :counter-label
                  :type  :text}])

(defn cool-preview []
  (let [state (reagent/atom {:type                   :default
                             :new-notifications?     true
                             :notification-indicator :unread-dot
                             :counter-label          5})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [preview/customizer state descriptor]
        [rn/view {:padding-vertical 60
                  :flex-direction   :row
                  :align-items      :center}
         [quo2/top-nav @state (:value @state)]]]])))

(defn preview-top-nav []
  [rn/view {:background-color (colors/theme-colors colors/white colors/neutral-95)
            :flex             1}
   [rn/flat-list {:flex                      1
                  :keyboardShouldPersistTaps :always
                  :header                    [cool-preview]
                  :key-fn                    str}]])
