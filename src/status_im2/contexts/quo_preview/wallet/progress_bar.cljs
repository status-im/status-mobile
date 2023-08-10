(ns status-im2.contexts.quo-preview.wallet.progress-bar
  (:require [clojure.string :as string]
            [quo2.components.wallet.progress-bar.view :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def ^:private descriptors
  [{:label "Width"
    :key   :width
    :type  :text}
   {:label "Height"
    :key   :height
    :type  :text}
   {:label   "Network State"
    :key     :network-state
    :type    :select
    :options [{:key   "pending"
               :value "Pending"}
              {:key   "confirmed"
               :value "Confirmed"}
              {:key   "finalised"
               :value "Finalised"}
              {:key   "error"
               :value "Error"}]}])

(defn cool-preview
  []
  (let [state (reagent/atom {:width         "80"
                             :height        "120"
                             :network-state "pending"})]
    (fn []
      [rn/view
       {:margin-bottom 50
        :padding       16}
       [preview/customizer state descriptors]
       [rn/view
        {:padding-vertical 60
         :align-items      :center}
        [quo/progress-bar @state]]])))

(defn preview
  []
  [rn/view
   {:background-color (colors/theme-colors colors/white colors/neutral-90)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
