(ns status-im2.contexts.quo-preview.tags.number-tag
  (:require [quo2.core :as quo]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:key     :type
    :type    :select
    :options [{:key :rounded}
              {:key :squared}]}
   {:key  :number
    :type :text}
   {:key     :size
    :type    :select
    :options [{:key   :size/s-32
               :value "32"}
              {:key   :size/s-24
               :value "24"}
              {:key   :size/s-20
               :value "20"}
              {:key   :size/s-16
               :value "16"}
              {:key   :size/s-14
               :value "14"}]}
   {:key  :blur?
    :type :boolean}])

(defn preview
  []
  (let [state (reagent/atom {:type   :squared
                             :number "148"
                             :size   :size/s-32
                             :blur?  false})]
    (fn []
      [preview/preview-container {:state state :descriptor descriptor}
       [rn/view
        {:padding-vertical 60
         :flex-direction   :row
         :justify-content  :center}
        [quo/number-tag @state]]])))
