(ns status-im.contexts.preview.quo.tags.tiny-tag
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [utils.reagent :as reagent]
    [status-im.contexts.preview.quo.preview :as preview]))

(def descriptor
  [{:key :blur? :type :boolean}
   {:key :label :type :text}])

(defn view
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
