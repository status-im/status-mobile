(ns status-im2.contexts.quo-preview.wallet.transaction-progress
  (:require
   [quo.core :as quo]
   [reagent.core :as reagent]
   [status-im2.common.resources :as resources]
   [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:type    :text
    :key     :title}
   {:type    :text
    :key     :tag-name} 
   {:type    :text
    :key     :epoch-number} 
   {:type    :text
    :key     :tag-number}
   {:type    :select
    :key     :network
    :options [{:key :mainnet}
              {:key :optimism-arbitrum}]}
   {:type    :select
    :key     :state
    :options [{:key :pending}
              {:key :sending}
              {:key :confirmed}
              {:key :finalising}
              {:key :finalized}
              {:key :error}]}])

(defn view
  []
  (let [state (reagent/atom {:title "Title"
                             :tag-name "Doodle"
                             :tag-number "120"
                             :epoch-number "181,329"
                             :network :mainnet
                             :state :pending
                             :start-interval-now  true
                             :btn-title           "Retry"
                             :tag-photo           (resources/get-mock-image :collectible)
                             :on-press            (fn []
                                                    (js/alert "Transaction progress item pressed"))})]
    (fn []
      [preview/preview-container {:state state :descriptor descriptor}
       [quo/transaction-progress @state]])))
