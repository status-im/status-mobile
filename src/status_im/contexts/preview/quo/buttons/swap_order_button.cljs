(ns status-im.contexts.preview.quo.buttons.swap-order-button
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.contexts.preview.quo.preview :as preview]))

(def descriptor
  [{:type :boolean
    :key  :disabled?}])

(defn view
  []
  (let [[state set-state] (rn/use-state {:disabled? false})]
    [preview/preview-container
     {:state      state
      :set-state  set-state
      :descriptor descriptor}
     [quo/swap-order-button
      (assoc state
             :on-press
             #(js/alert "Pressed"))]]))
