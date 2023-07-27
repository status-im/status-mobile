(ns status-im2.contexts.quo-preview.avatars.account-avatar
  (:require [quo2.components.colors.color-picker.view :as color-picker]
            [quo2.components.avatars.account-avatar.view :as account-avatar]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label   "Type"
    :key     :type
    :type    :select
    :options [{:key   :default
               :value "default"}
              {:key   :watch-only
               :value "watch only"}]}
   {:label   "Size"
    :key     :size
    :type    :select
    :options [{:key   16
               :value "16"}
              {:key   20
               :value "20"}
              {:key   24
               :value "24"}
              {:key   28
               :value "28"}
              {:key   32
               :value "32"}
              {:key   48
               :value "48"}
              {:key   80
               :value "80"}]}
   {:label "Emoji"
    :key   :emoji
    :type  :text}
   {:label   "Customization color:"
    :key     :customization-color
    :type    :select
    :options (mapv (fn [color]
                     {:key color :value color})
                   color-picker/color-list)}])

(defn cool-preview
  []
  (let [state (reagent/atom {:customization-color :purple
                             :size                80
                             :emoji               "💰"
                             :type                :default})]
    (fn []
      [rn/view
       {:margin-bottom 50
        :padding       16}
       [preview/customizer state descriptor]
       [rn/view
        {:padding-vertical 60
         :align-items      :center}
        [account-avatar/view @state]]])))

(defn preview-account-avatar
  []
  [rn/view
   {:flex             1
    :background-color (colors/theme-colors colors/white colors/neutral-95)}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
