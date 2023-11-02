(ns status-im2.contexts.quo-preview.dropdowns.dropdown-input
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im2.common.resources :as resources]
    [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [])

(defn view
  []
  (let [state (reagent/atom {})]
    [:f>
     (fn []
       [preview/preview-container
        {:state      state
         :descriptor descriptor}
        [quo/dropdown
         {:on-press #(js/console.log "sooqa")
          :icon?    true}
         [quo/text
          "Dropdown"]]])]))
