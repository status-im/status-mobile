(ns status-im2.contexts.quo-preview.inputs.private-key-input
  (:require [quo2.core :as quo]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:key  :input-placeholder
    :type :text}
   {:key  :title-text
    :type :text}])

(defn view
  []
  (let [state (reagent/atom {:color             nil
                             :input-placeholder "cs2:4FH..."
                             :disabled?         false
                             :title-text        "Type or paste sync code"})]
    (fn []
      [preview/preview-container
       {:state      state
        :descriptor descriptor}
       [quo/private-key-input @state]])))
