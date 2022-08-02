(ns quo2.screens.user-avatar
  (:require [quo.react-native :as rn]
            [quo.design-system.colors :as colors]
            [quo.previews.preview :as preview]
            [quo2.components.user-avatar :as quo2]
            [reagent.core :as reagent]))

(def descriptor [{:label   "Size:"
                  :key     :size
                  :type    :select
                  :options [{:key   :big
                             :value "Big"}
                            {:key   :medium
                             :value "Medium"}
                            {:key   :small
                             :value "Small"}]}
                 {:label "Online status"
                  :key   :online?
                  :type  :boolean}
                 {:label "Status Indicator"
                  :key   :status-indicator?
                  :type  :boolean}
                 {:label "Identicon Ring"
                  :key   :ring?
                  :type  :boolean}
                 {:label "Full name separated by space"
                  :key   :full-name
                  :type  :text}
                 {:label "Profile Picture URL"
                  :key   :profile-picture
                  :type  :text}])

(defn cool-preview []
  (let [state  (reagent/atom {:full-name "john doe"
                              :status-indicator? true
                              :online? true
                              :size :big
                              :ring? true})]
    (fn []
      [rn/view {:margin-bottom 50
                :padding       16}
       [rn/view {:flex 1}
        [preview/customizer state descriptor]]
       [rn/view {:padding-vertical 60
                 :flex-direction   :row
                 :justify-content  :center}
        [quo2/user-avatar @state]]])))

(defn preview-user-avatar []
  [rn/view {:background-color (:ui-background @colors/theme)
            :flex             1}
   [rn/flat-list {:flex                      1
                  :keyboardShouldPersistTaps :always
                  :header                    [cool-preview]
                  :key-fn                    str}]])