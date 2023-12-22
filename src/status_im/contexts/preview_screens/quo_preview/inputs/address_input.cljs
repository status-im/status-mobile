(ns status-im.contexts.preview-screens.quo-preview.inputs.address-input
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.constants :as constants]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(def descriptor
  [{:label "Scanned value:"
    :key   :scanned-value
    :type  :text}
   {:key  :blur?
    :type :boolean}])

(defn view
  []
  (let [state (reagent/atom {:scanned-value         ""
                             :blur?                 false
                             :valid-ens-or-address? false})
        timer (atom nil)]
    (fn []
      [preview/preview-container
       {:state                 state
        :descriptor            descriptor
        :blur?                 (:blur? @state)
        :show-blur-background? true}
       [quo/address-input
        (merge @state
               {:on-scan       #(js/alert "Not implemented yet")
                :ens-regex     constants/regx-ens
                :on-detect-ens (fn [_]
                                 (swap! state assoc :valid-ens-or-address? false)
                                 (when @timer
                                   (js/clearTimeout @timer))
                                 (reset! timer (js/setTimeout
                                                #(swap! state assoc :valid-ens-or-address? true)
                                                2000)))})]])))
