(ns status-im.contexts.preview.quo.dividers.new-messages
  (:require
    [quo.core :as quo]
    [utils.reagent :as reagent]
    [status-im.contexts.preview.quo.preview :as preview]))

(def descriptor
  [{:key  :label
    :type :text}
   (preview/customization-color-option)])

(defn view
  []
  (let [state (reagent/atom {:label "New messages"
                             :color :primary})]
    (fn []
      [preview/preview-container {:state state :descriptor descriptor}
       [quo/new-messages @state]])))
