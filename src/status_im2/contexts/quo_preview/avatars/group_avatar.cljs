(ns status-im2.contexts.quo-preview.avatars.group-avatar
  (:require [quo2.core :as quo2]
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
              {:key   :large
               :value "Large"}]}
   {:label "Color"
    :key :color
    :type :select
    :options
    (map
     (fn [c]
       {:key   c
        :value c})
     ["#ff0000" "#0000ff"])}]) ; TODO: this is temporary only. Issue: https://github.com/status-im/status-mobile/issues/14566

(defn cool-preview
  []
  (let [state (reagent/atom {:theme :light
                             :color :purple
                             :size  :small})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [rn/view {:flex 1}
         [preview/customizer state descriptor]]
        [rn/view
         {:padding-vertical 60
          :flex-direction   :row
          :justify-content  :center}
         [quo2/group-avatar @state]]]])))

(defn preview-group-avatar
  []
  [rn/view
   {:background-color (colors/theme-colors colors/white
                                           colors/neutral-90)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
