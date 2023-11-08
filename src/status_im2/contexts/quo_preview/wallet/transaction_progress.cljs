(ns status-im2.contexts.quo-preview.wallet.transaction-progress
  (:require [quo.components.wallet.transaction-progress.view :as quo]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.common.resources :as resources] 
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label "Title:"
    :key   :title
    :type  :text}
   {:label "Tag name:"
    :key   :tag-name
    :type  :text}
   {:label   "Network Type:"
    :key     :network-type
    :type    :select
    :options [{:key   :mainnet
               :value "mainnet"}
              {:key   :optimism-arbitrum
               :value "optimism/arbitrum"}]}
   {:label   "Network State:"
    :key     :network-state
    :type    :select
    :options [{:key   :pending
               :value "pending"}
              {:key   :sending
               :value "sending"}
              {:key   :confirmed
               :value "confirmed"}
              {:key   :finalising
               :value "finalising"}
              {:key   :finalized
               :value "finalized"}
              {:key   :error
               :value "error"}]}])

(defn get-props
  [data]
  (when (:toggle-props data) (js/console.warn data))
  (merge
   data
   {:toggle-props (when (:toggle-props data)
                    {:checked?  true
                     :on-change (fn [new-value] (js/alert new-value))})
    :button-props (when (:button-props data)
                    {:title "Button" :on-press (fn [] (js/alert "Button pressed"))})
    :communities-props
    (when (:communities-props data)
      {:data
       [{:source (resources/mock-images :rarible)}
        {:source (resources/mock-images :decentraland)}
        {:source (resources/mock-images :coinbase)}]})
    :status-tag-props (when (:status-tag-props data)
                        {:size           :small
                         :status         {:type :positive}
                         :no-icon?       true
                         :label          "example"
                         :override-theme :dark})}))

(defn cool-preview
  []
  (let [state (reagent/atom {:title               :Title
                             :accessibility-label :transaction-progress-item
                             :network-state       :pending
                             :network-type        :mainnet
                             :start-interval-now  true
                             :btn-title           "Retry"
                             :tag-name            "Doodle #120"
                             :tag-photo           (resources/get-mock-image :collectible)
                             :on-press            (fn []
                                                    (js/alert "Transaction progress item pressed"))})]
    (fn []
      [rn/view
       {:margin-bottom 50}
       [preview/customizer state descriptor]
       [rn/view
        {:padding-vertical   100
         :padding-horizontal 20
         :align-items        :center}
        [:f> quo/view (get-props @state)]]])))

(defn view
  []
  [rn/view {:style {:flex 1}}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])