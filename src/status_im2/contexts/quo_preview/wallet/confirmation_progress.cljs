(ns status-im2.contexts.quo-preview.wallet.confirmation-progress
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im2.contexts.quo-preview.preview :as preview]))
  
  (def descriptor
    [{:type :text
      :key  :optimism-progress-percentage}
     {:type :text
      :key  :arbitrum-progress-percentage}
     {:type    :select
      :key     :network
      :options [{:key :mainnet}
                {:key :optimism}
                {:key :arbitrum}]}
     {:type    :select
      :key     :state-mainnet
      :options [{:key :pending}
                {:key :sending}
                {:key :confirmed}
                {:key :finalising}
                {:key :finalized}
                {:key :error}]}
     {:type    :select
      :key     :state-optimism
      :options [{:key :pending}
                {:key :sending}
                {:key :confirmed}
                {:key :finalising}
                {:key :finalized}
                {:key :error}]}
     {:type    :select
      :key     :state-arbitrum
      :options [{:key :pending}
                {:key :sending}
                {:key :confirmed}
                {:key :finalising}
                {:key :finalized}
                {:key :error}]}
     (preview/customization-color-option)])
  
  (def total-box 85)
  (def counter (reagent/atom 0))
  (def interval-id (reagent/atom nil))
  (def interval-ms 50)
  
  (defn stop-interval
    []
    (when @interval-id
      (js/clearInterval @interval-id)
      (reset! interval-id nil)))
  
  (defn clear-counter
    []
    (reset! counter 0))
  
  (defn update-counter
    [state]
    (let [new-counter-value (-> @counter inc)]
      (if (or (and (= state :pending) (> new-counter-value 0))
              (and (= state :sending) (> new-counter-value 2))
              (and (= state :confirmed) (> new-counter-value 4))
              (and (= state :finalising) (> new-counter-value 18))
              (and (= state :finalized) (> new-counter-value total-box))
              (and (= state :error) (> new-counter-value 2)))
        (stop-interval)
        (reset! counter new-counter-value))))
  
  (defn start-interval
    [state]
    (reset! interval-id
            (js/setInterval
             (fn []
               (update-counter state))
             interval-ms)))
  
  (defn view
    []
    (let [state (reagent/atom
                 {:counter                      counter
                  :total-box                    total-box
                  :optimism-progress-percentage "10"
                  :arbitrum-progress-percentage "10"
                  :network                      :mainnet
                  :state-mainnet                :pending
                  :state-arbitrum               :pending
                  :state-optimism               :pending
                  :customization-color          :blue})]
      [:f>
       (fn []
         (rn/use-effect
          (fn []
            (start-interval (:state-mainnet @state))
            (clear-counter)
            (fn []
              (stop-interval)))
          [(:state-mainnet @state)])
         [preview/preview-container {:state state :descriptor descriptor}
          [quo/confirmation-propgress @state]])]))
