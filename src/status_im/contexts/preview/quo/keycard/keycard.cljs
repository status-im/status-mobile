(ns status-im.contexts.preview.quo.keycard.keycard
  (:require
    [quo.core :as quo]
    [utils.reagent :as reagent]
    [status-im.contexts.preview.quo.preview :as preview]))

(def descriptor
  [{:key :holder-name :type :text}
   {:key :locked? :type :boolean}])

(defn view
  []
  (let [state (reagent/atom {:holder-name ""
                             :locked?     true})]
    (fn
      []
      [preview/preview-container {:state state :descriptor descriptor}
       [quo/keycard @state]])))
