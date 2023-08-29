(ns status-im2.contexts.quo-preview.avatars.group-avatar
  (:require [quo2.core :as quo2]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.common.resources :as resources]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label   "Size"
    :key     :size
    :type    :select
    :options [{:key   :x-small
               :value "x-small"}
              {:key   :small
               :value "Small"}
              {:key   :medium
               :value "Medium"}
              {:key   :large
               :value "Large"}
              {:key   :x-large
               :value "x-Large"}]}
   {:label "Avatar"
    :key   :picture?
    :type  :boolean}
   (preview/customization-color-option)])

(def avatar (resources/get-mock-image :user-picture-male4))

(defn cool-preview
  []
  (let [state (reagent/atom {:theme               :light
                             :customization-color :blue
                             :size                :small
                             :picture?            false})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:style {:padding-bottom 150}}
        [rn/view {:style {:flex 1}}
         [preview/customizer state descriptor]]
        [rn/view
         {:style {:padding-vertical 60
                  :flex-direction   :row
                  :justify-content  :center}}
         (let [{:keys [picture?]} @state
               params             (if picture? (assoc @state :picture avatar) @state)]
           [quo2/group-avatar params])]]])))

(defn preview-group-avatar
  []
  [rn/view
   {:style {:background-color (colors/theme-colors colors/white
                                                   colors/neutral-90)
            :flex             1}}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
