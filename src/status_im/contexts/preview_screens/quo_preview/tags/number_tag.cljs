(ns status-im.contexts.preview-screens.quo-preview.tags.number-tag
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(def descriptor
  [{:key     :type
    :type    :select
    :options [{:key :rounded}
              {:key :squared}]}
   {:key  :number
    :type :text}
   {:key     :size
    :type    :select
    :options [{:key   :size-32
               :value "32"}
              {:key   :size-24
               :value "24"}
              {:key   :size-20
               :value "20"}
              {:key   :size-16
               :value "16"}
              {:key   :size-14
               :value "14"}]}
   {:key  :blur?
    :type :boolean}])

(defn view
  []
  (let [state (reagent/atom {:type   :squared
                             :number "148"
                             :size   :size-32
                             :blur?  false})]
    (fn []
      [preview/preview-container
       {:state                 state
        :descriptor            descriptor
        :blur?                 (:blur? @state)
        :show-blur-background? true}
       [quo/number-tag @state]])))
