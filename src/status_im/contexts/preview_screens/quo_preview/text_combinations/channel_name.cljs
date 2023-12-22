(ns status-im.contexts.preview-screens.quo-preview.text-combinations.channel-name
  (:require [quo.core :as quo]
            [reagent.core :as reagent]
            [status-im.contexts.preview-screens.quo-preview.preview :as preview]))


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
