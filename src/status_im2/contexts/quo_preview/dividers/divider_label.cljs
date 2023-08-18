(ns status-im2.contexts.quo-preview.dividers.divider-label
  (:require [quo2.core :as quo]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:type :text :key :label}
   {:type :text :key :counter-value}
   {:type :boolean :key :increase-padding-top?}
   {:type :boolean :key :blur?}
   {:key     :chevron-position
    :type    :select
    :options [{:key :left}
              {:key :right}]}])

(defn view
  []
  (let [state (reagent/atom {:blur?                 false
                             :chevron-position      :left
                             :counter-value         "0"
                             :increase-padding-top? true
                             :label                 "Welcome"})]
    (fn []
      [preview/preview-container
       {:state                 state
        :descriptor            descriptor
        :blur?                 (:blur? @state)
        :show-blur-background? true}
       [quo/divider-label @state]])))
