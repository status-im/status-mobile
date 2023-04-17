(ns status-im2.contexts.quo-preview.banners.banner
  (:require [quo2.core :as quo2]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label "message"
    :key   :latest-pin-text
    :type  :text}
   {:label "number of messages"
    :key   :pins-count
    :type  :text}
   {:label "hide pin icon?"
    :key   :hide-pin?
    :type  :boolean}
  ])

(defn cool-preview
  []
  (let [state (reagent/atom
               {:hide-pin?       false
                :pins-count      2
                :latest-pin-text "Be respectful of fellow community members, even if they"})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [rn/view {:flex 1}
         [preview/customizer state descriptor]]
        [rn/view
         {:padding-vertical 60
          :flex-direction   :row
          :justify-content  :center}
         [quo2/banner @state]]]])))

(defn preview-banner
  []
  [rn/view
   {:background-color (colors/theme-colors colors/white colors/neutral-90)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
