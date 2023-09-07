(ns status-im2.contexts.quo-preview.dividers.divider-line
  (:require [quo2.core :as quo]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:key  :blur?
    :type :boolean}])

(defn view
  []
  (let [state (reagent/atom {:blur? false})]
    (fn []
      [preview/preview-container
       {:state                 state
        :descriptor            descriptor
        :blur?                 (:blur? @state)
        :show-blur-background? true}
       [quo/divider-line @state]])))
