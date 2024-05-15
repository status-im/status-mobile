(ns status-im.contexts.preview.quo.wallet.transaction-summary
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.contexts.preview.quo.preview :as preview]))

(def context-tags (vals preview/context-tag-options))

(def prefixes
  [{:key :t/to}
   {:key :t/in}
   {:key :t/via}
   {:key :t/from}
   {:key :t/on}
   {:key :t/at}])

(def descriptor
  [{:key     :transaction
    :type    :select
    :options [{:key :send}
              {:key :swap}
              {:key :bridge}]}
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
    :options context-tags}
   {:label   "Slot 5"
    :key     :fifth-tag
    :type    :select
    :options context-tags}
   {:key  :max-fees
    :type :text}
   {:key  :nonce
    :type :number}
   {:key  :input-data
    :type :text}])

(defn view
  []
  (let [state (reagent/atom
               {:transaction       :send
                :first-tag         (get-in preview/context-tag-options [:asset.snt :key])
                :second-tag-prefix :t/from
                :second-tag        (get-in preview/context-tag-options [:account.piggy :key])
                :third-tag-prefix  nil
                :third-tag         (get-in preview/context-tag-options [:person.aretha :key])
                :fourth-tag-prefix :t/via
                :fourth-tag        (get-in preview/context-tag-options [:network.mainnet :key])
                :fifth-tag         (get-in preview/context-tag-options [:network.optimism :key])
                :max-fees          "€55.57"
                :nonce             26
                :input-data        "Hello from Porto"})]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :show-blur-background?     true
        :component-container-style {:padding-bottom 50}}
       [quo/transaction-summary
        (assoc @state
               :on-press
               #(js/alert "Pressed"))]])))
