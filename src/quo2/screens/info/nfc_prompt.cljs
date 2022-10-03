(ns quo2.screens.info.nfc-prompt
  (:require [quo.react-native :as rn]
            [reagent.core :as reagent]
            [quo.previews.preview :as preview]
            [quo2.foundations.colors :as colors]
            [quo2.components.info.nfc-prompt :as quo2]))

(def descriptor [{:label   "Prompt status"
                  :key     :prompt-status
                  :type    :select
                  :options [{:key   :ready
                             :value "Ready"}
                            {:key   :connected
                             :value "Connected"}
                            {:key   :success
                             :value "Success"}]}])

(defn cool-preview []
  (let [state (reagent/atom {:on-press identity
                             :prompt-status :ready})]
    (fn []
      [rn/view {:margin-bottom 50
                :padding-vertical 16}
       [preview/customizer state descriptor]
       [quo2/nfc-prompt @state]])))

(defn preview-nfc-prompt []
  [rn/view {:background-color (colors/theme-colors colors/white colors/neutral-90)
            :flex             1}
   [rn/flat-list {:flex                      1
                  :keyboardShouldPersistTaps :always
                  :header                    [cool-preview]
                  :key-fn                    str}]])