(ns status-im.contexts.preview.quo.wallet.confirmation-progress
  (:require
    [quo.components.wallet.confirmation-progress.schema :refer [?schema]]
    [quo.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.contexts.preview.quo.preview :as preview]
    [status-im.contexts.preview.quo.preview-generator :as preview-gen]))

(def descriptor (preview-gen/schema->descriptor ?schema {:exclude-keys #{:counter :total-box}}))

(def total-box 85)
(def counter (reagent/atom 0))
(def interval-id (reagent/atom nil))
(def interval-ms 50)

(defn- stop-interval
  []
  (when @interval-id
    (js/clearInterval @interval-id)
    (reset! interval-id nil)))

(defn- clear-counter
  []
  (reset! counter 0))

(defn- update-counter
  [state]
  (let [new-counter-value (inc @counter)]
    (if (or (and (= state :pending) (> new-counter-value 0))
            (and (= state :sending) (> new-counter-value 2))
            (and (= state :confirmed) (> new-counter-value 4))
            (and (= state :finalising) (> new-counter-value 18))
            (and (= state :finalized) (> new-counter-value total-box))
            (and (= state :error) (> new-counter-value 2)))
      (stop-interval)
      (reset! counter new-counter-value))))

(defn- start-interval
  [state]
  (reset! interval-id
    (js/setInterval
     (fn []
       (update-counter state))
     interval-ms)))

(defn- f-view
  [state]
  (fn []
    (rn/use-effect
     (fn []
       (start-interval (:state @state))
       (clear-counter)
       (fn []
         (stop-interval)))
     [(:state @state)])
    [preview/preview-container {:state state :descriptor descriptor}
     [quo/confirmation-progress
      (assoc @state
             :counter
             @counter)]]))

(defn view
  []
  (let [state (reagent/atom
               {:total-box           total-box
                :progress-value      10
                :network             :mainnet
                :state               :pending
                :customization-color :blue})]
    [:f> f-view state]))
