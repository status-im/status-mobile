(ns status-im.contexts.preview-screens.quo-preview.tags.token-tag
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(def descriptor
  [{:key     :size
    :type    :select
    :options [{:key   :size-24
               :value "Size 24"}
              {:key   :size-32
               :value "Size 32"}]}
   {:key     :token-value
    :type    :select
    :options [{:key   0
               :value "0"}
              {:key   10
               :value "10"}
              {:key   100
               :value "100"}
              {:key   1000
               :value "1000"}
              {:key   10000
               :value "10000"}]}
   {:key     :options
    :type    :select
    :options [{:key   false
               :value false}
              {:key   :add
               :value :add}
              {:key   :hold
               :value :hold}]}
   {:key  :blur?
    :type :boolean}
   {:key     :token-symbol
    :type    :select
    :options [{:key   "ETH"
               :value "ETH"}
              {:key   "SNT"
               :value "SNT"}]}])

(defn view
  []
  (let [state (reagent/atom {:size         :size-24
                             :token-value  10
                             :token-symbol "ETH"
                             :options      false
                             :blur?        false})]
    (fn []
      [preview/preview-container
       {:state                 state
        :blur?                 (:blur? @state)
        :show-blur-background? true
        :descriptor            descriptor}
       [rn/view {:style {:align-items :center}}
        [quo/token-tag @state]]])))
