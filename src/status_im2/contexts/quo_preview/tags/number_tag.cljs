(ns status-im2.contexts.quo-preview.tags.number-tag
  (:require
    [quo.components.tags.number-tag.view :as number-tag]
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im2.contexts.quo-preview.preview :as preview]))

(defn view
  []
  (let [state (reagent/atom {:type   :squared
                             :number "148"
                             :size   :size-32
                             :blur?  false})]
    (fn []
      [preview/preview-container
       {:state                 state
        :descriptor            (preview/generate-descriptor number-tag/?schema)
        :blur?                 (:blur? @state)
        :show-blur-background? true}
       [quo/number-tag @state]])))
