(ns status-im2.contexts.quo-preview.switcher.base-card
  (:require [quo.react-native :as rn]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]
            [status-im2.common.resources :as resources]))

(def descriptor
  [{:label "Banner?"
    :key   :banner?
    :type  :boolean}
   {:label "Customization"
    :key :customization-color
    :type :select
    :options
    (map
     (fn [c]
       {:key   c
        :value c})
     (keys colors/customization))}])

;; Mock data
(def banner {:source (resources/get-mock-image :community-banner)})

(defn cool-preview
  []
  (let [state (reagent/atom {:customization-color :sky
                             :banner?             false})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [preview/customizer state descriptor]
        [rn/view
         {:padding-vertical 60
          :align-items      :center}
         [quo/switcher-base-card
          (merge @state
                 {:title    "jonathan.eth"
                  :subtitle "Message"
                  :banner   (when (:banner? @state) banner)})]]]])))

(defn preview-base-card
  []
  [rn/view
   {:background-color (colors/theme-colors colors/white colors/neutral-90)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
