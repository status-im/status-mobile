(ns status-im2.contexts.quo-preview.keycard.keycard
  (:require [quo2.core :as quo]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

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
