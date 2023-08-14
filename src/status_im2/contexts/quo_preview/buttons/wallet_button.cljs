(ns status-im2.contexts.quo-preview.buttons.wallet-button
  (:require
    [quo2.core :as quo]
    [quo2.foundations.colors :as colors]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label "Disabled?:"
    :key   :disabled?
    :type  :boolean}])

(defn cool-preview
  []
  (let [state (reagent/atom {:icon          :i/placeholder
                             :on-press      #(js/alert "pressed")
                             :on-long-press #(js/alert "long pressed")})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:flex 1 :padding-bottom 20}
        [rn/view {:height 200}
         [preview/customizer state descriptor]]
        [rn/view
         {:flex            1
          :align-items     :center
          :justify-content :center}
         [quo/wallet-button @state]]]])))

(defn preview
  []
  [rn/view
   {:background-color (colors/theme-colors colors/white colors/neutral-95)
    :flex             1}
   [cool-preview]])
