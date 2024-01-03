(ns status-im.contexts.preview.quo.dividers.divider-line
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.contexts.preview.quo.preview :as preview]))

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
