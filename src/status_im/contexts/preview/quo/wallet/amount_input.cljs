(ns status-im.contexts.preview.quo.wallet.amount-input
  (:require
    [quo.components.wallet.amount-input.schema :refer [?schema]]
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.contexts.preview.quo.preview :as preview]
    [status-im.contexts.preview.quo.preview-generator :as preview-gen]))

(def descriptor (preview-gen/schema->descriptor ?schema))

(defn view
  []
  (let [state        (reagent/atom {:max-value 10000
                                    :min-value 0
                                    :value     1
                                    :status    :default})
        on-inc-press (fn [] (swap! state update :value inc))
        on-dec-press (fn [] (swap! state update :value dec))]
    (fn []
      [preview/preview-container {:state state :descriptor descriptor}
       [quo/amount-input
        (assoc @state
               :on-dec-press on-dec-press
               :on-inc-press on-inc-press)]])))
