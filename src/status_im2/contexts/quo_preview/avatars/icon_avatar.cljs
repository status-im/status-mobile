(ns status-im2.contexts.quo-preview.avatars.icon-avatar
  (:require [quo2.components.avatars.icon-avatar :as quo2]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label   "Size"
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
              {:key   :main-icons/wallet
               :value "Wallet"}
              {:key   :main-icons/play
               :value "Play"}]}
   {:label   "Color"
    :key     :color
    :type    :select
    :options (map
              (fn [c]
                {:key   c
                 :value c})
              (keys colors/customization))}])

(defn cool-preview
  []
  (let [state (reagent/atom {:size  :big
                             :icon  :main-icons/placeholder20
                             :color :primary})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [preview/customizer state descriptor]
        [rn/view
         {:padding-vertical 60
          :align-items      :center}
         [quo2/icon-avatar @state]]]])))

(defn preview-icon-avatar
  []
  [rn/view
   {:background-color (colors/theme-colors colors/white colors/neutral-90)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
