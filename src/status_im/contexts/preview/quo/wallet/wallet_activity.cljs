(ns status-im.contexts.preview.quo.wallet.wallet-activity
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.contexts.preview.quo.preview :as preview]))

(def context-tags (vals preview/named-context-tags))

(def prefixes
  [{:key :t/to}
   {:key :t/in}
   {:key :t/via}
   {:key :t/from}
   {:key :t/on}
   {:key :t/at}])

(def descriptor
  [{:key     :blur?
    :type    :select
    :options [{:key true}
              {:key false}]}
   {:key     :transaction
    :type    :select
    :options [{:key :receive}
              {:key :send}
              {:key :swap}
              {:key :bridge}
              {:key :buy}
              {:key :destroy}
              {:key :mint}]}
   {:key     :status
    :type    :select
    :options [{:key :pending}
              {:key :confirmed}
              {:key :finalised}
              {:key :failed}]}
   {:key  :timestamp
    :type :text}
   {:key     :counter
    :type    :select
    :options [{:key   1
               :value 1}
              {:key   2
               :value 2}
              {:key   3
               :value 3}]}
   {:label   "Slot 1"
    :key     :first-tag
    :type    :select
    :options context-tags}
   {:label   "Slot 2 prefix"
    :key     :second-tag-prefix
    :type    :select
    :options prefixes}
   {:label   "Slot 2"
    :key     :second-tag
    :type    :select
    :options context-tags}
   {:label   "Slot 3 prefix"
    :key     :third-tag-prefix
    :type    :select
    :options prefixes}
   {:label   "Slot 3"
    :key     :third-tag
    :type    :select
    :options context-tags}
   {:label   "Slot 4 prefix"
    :key     :fourth-tag-prefix
    :type    :select
    :options prefixes}
   {:label   "Slot 4"
    :key     :fourth-tag
    :type    :select
    :options context-tags}])

(defn view
  []
  (let [state (reagent/atom
               {:transaction       :send
                :timestamp         "Today 22:20"
                :status            :pending
                :counter           1
                :first-tag         (get-in preview/named-context-tags [:asset.snt :key])
                :second-tag-prefix :t/from
                :second-tag        (get-in preview/named-context-tags [:account.piggy :key])
                :third-tag-prefix  :t/to
                :third-tag         (get-in preview/named-context-tags [:person.aretha :key])
                :fourth-tag-prefix :t/via
                :fourth-tag        (get-in preview/named-context-tags [:network.mainnet :key])
                :blur?             false})]
    (fn []
      [preview/preview-container
       {:state                 state
        :descriptor            descriptor
        :blur?                 (:blur? @state)
        :show-blur-background? true}
       [rn/view {:style {:align-self :center}}
        [quo/wallet-activity
         (assoc @state
                :on-press
                #(js/alert "Pressed"))]]])))
