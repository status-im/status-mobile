(ns quo2.screens.navigation.bottom-nav-tab
  (:require [quo.react-native :as rn]
            [quo.previews.preview :as preview]
            [reagent.core :as reagent]
            [quo2.components.navigation.bottom-nav-tab :as quo2]
            [quo2.foundations.colors :as colors]))

(def descriptor [{:label   "Type:"
                  :key     :icon
                  :type    :select
                  :options [{:key   :main-icons2/communities
                             :value "Communities"}
                            {:key   :main-icons2/messages
                             :value "Messages"}
                            {:key   :main-icons2/wallet
                             :value "Wallet"}
                            {:key   :main-icons2/browser
                             :value "Browser"}]}
                 {:label "Selected?:"
                  :key   :selected?
                  :type  :boolean}
                 {:label "Pass through?:"
                  :key   :pass-through?
                  :type  :boolean}
                 {:label "Notification?:"
                  :key   :notification?
                  :type  :boolean}
                 {:label   "Notification Type"
                  :key     :notification-type
                  :type    :select
                  :options [{:key   :counter
                             :value :counter}
                            {:key   :unread
                             :value :unread}]}
                 {:label "Counter Label"
                  :key   :counter-label
                  :type  :text}])

(defn cool-preview []
  (let [state (reagent/atom {:icon                :main-icons2/communities
                             :selected?           true
                             :pass-through?       true
                             :notification?       true
                             :notification-type   :counter
                             :counter-label       8
                             :preview-label-color colors/white})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [preview/customizer state descriptor]
        [rn/view {:padding-vertical 60
                  :align-items      :center}
         [quo2/bottom-nav-tab @state (:value @state)]]]])))

(defn preview-bottom-nav-tab []
  [rn/view {:background-color colors/neutral-100
            :flex             1}
   [rn/flat-list {:flex                      1
                  :keyboardShouldPersistTaps :always
                  :header                    [cool-preview]
                  :key-fn                    str}]])
