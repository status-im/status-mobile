(ns status-im.contexts.preview-screens.quo-preview.inputs.title-input
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(def descriptor
  [{:key  :disabled?
    :type :boolean}
   {:key  :placeholder
    :type :text}
   {:key  :max-length
    :type :number}
   {:key  :blur?
    :type :boolean}
   (preview/customization-color-option)])

(defn view
  []
  (let [state (reagent/atom {:color       nil
                             :placeholder "Type something here"
                             :max-length  24
                             :blur?       false
                             :disabled?   false})]
    (fn []
      [preview/preview-container
       {:state                 state
        :descriptor            descriptor
        :blur?                 (:blur? @state)
        :show-blur-background? true}
       [quo/title-input (assoc @state :default-value "")]])))
