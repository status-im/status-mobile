(ns status-im.contexts.preview.quo.text-combinations.channel-name
  (:require [quo.core :as quo]
            [utils.reagent :as reagent]
            [status-im.contexts.preview.quo.preview :as preview]))


(def descriptor
  [{:key  :channel-name
    :type :text}
   {:key  :unlocked?
    :type :boolean}
   {:key  :muted?
    :type :boolean}
   {:key  :blur?
    :type :boolean}])

(defn view
  []
  (let [state (reagent/atom {:channel-name "random"
                             :unlocked?    true
                             :muted?       true
                             :blur?        false})]
    (fn []
      [preview/preview-container
       {:state                 state
        :descriptor            descriptor
        :show-blur-background? true
        :blur?                 (:blur? @state)}
       [quo/channel-name @state]])))
