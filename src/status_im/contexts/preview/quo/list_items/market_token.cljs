(ns status-im.contexts.preview.quo.list-items.market-token
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.contexts.preview.quo.preview :as preview]))

(def descriptor
  [{:key     :token
    :type    :select
    :options [{:key :eth}
              {:key :snt}
              {:key :btc}]}
   {:key  :token-rank
    :type :number}
   {:key  :percentage-change
    :type :number}
   {:key  :token-name
    :type :text}
   {:key  :market-cap
    :type :text}
   {:key  :price
    :type :text}
   (preview/customization-color-option)])

(defn view
  []
  (let [state (reagent/atom {:token               :snt
                             :token-rank          10
                             :token-name          "Status"
                             :market-cap          "$332.1B"
                             :price               "$0.8"
                             :percentage-change   5.2
                             :customization-color :blue})]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:align-items :center
                                    :margin-top  50}}
       [quo/market-token @state]])))

