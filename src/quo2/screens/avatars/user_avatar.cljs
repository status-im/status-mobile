(ns quo2.screens.avatars.user-avatar
  (:require [quo.react-native :as rn]
            [quo2.foundations.colors :as colors]
            [quo.previews.preview :as preview]
            [quo2.components.avatars.user-avatar :as quo2]
            [reagent.core :as reagent]
            [status-im.react-native.resources :as resources]))

(def descriptor [{:label   "Size:"
                  :key     :size
                  :type    :select
                  :options [{:key   :big
                             :value "Big"}
                            {:key   :medium
                             :value "Medium"}
                            {:key   :small
                             :value "Small"}
                            {:key   :xs
                             :value "x Small"}
                            {:key   :xxs
                             :value "xx Small"}
                            {:key   :xxxs
                             :value "xxx Small"}]}
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
                 {:label   "Profile Picture"
                  :key     :profile-picture
                  :type    :select
                  :options [{:value "None"
                             :key   nil}
                            {:value "Alicia Keys"
                             :key   (resources/get-mock-image :user-picture-female2)}
                            {:value "pedro.eth"
                             :key   (resources/get-mock-image :user-picture-male4)}]}])

(defn cool-preview []
  (let [state  (reagent/atom {:full-name "A Y"
                              :status-indicator? true
                              :online? true
                              :size :medium
                              :ring? true})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [rn/view {:flex 1}
         [preview/customizer state descriptor]]
        [rn/view {:padding-vertical 60
                  :flex-direction   :row
                  :justify-content  :center}
         [quo2/user-avatar @state]]]])))

(defn preview-user-avatar []
  [rn/view {:background-color (colors/theme-colors colors/white colors/neutral-90)
            :flex             1}
   [rn/flat-list {:flex                      1
                  :keyboardShouldPersistTaps :always
                  :header                    [cool-preview]
                  :key-fn                    str}]])
