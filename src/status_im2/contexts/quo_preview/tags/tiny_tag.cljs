(ns status-im2.contexts.quo-preview.tags.tiny-tag
  (:require [quo2.core :as quo]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label "Blur?"
    :key   :blur?
    :type  :boolean}
   {:label "Label"
    :key   :label
    :type  :text}])

(defn preview-tiny-tag
  []
  (let [state (reagent/atom {:blur? false
                             :label "1,000 SNT"})]
    (fn []
      [preview/preview-container
       {:state                 state
        :descriptor            descriptor
        :blur?                 (:blur? @state)
        :show-blur-background? true}
       [rn/view {:style {:align-items :center}}
        [quo/tiny-tag @state]]])))
