(ns quo.components.wallet.confirmation-progress.view
  (:require [quo.components.wallet.confirmation-progress.style :as style]
            [quo.components.wallet.progress-bar.view :as progress-box]
            [quo.theme :as quo.theme]
            [react-native.core :as rn]))

(def ^:private max-progress 100)
(def ^:private min-progress 0)

(defn- calculate-box-state
  [state counter index]
  (cond
    (and (= state :sending) (>= counter index) (< index 3))                 :confirmed
    (and (= state :confirmed) (>= counter index) (< index 5))               :confirmed
    (and (= state :finalising) (>= counter index) (< index 5))              :confirmed
    (and (= state :finalising) (>= counter index) (> index 4) (< index 20)) :finalized
    (and (= state :finalized) (>= counter index) (< index 5))               :confirmed
    (and (= state :finalized) (>= counter index) (> index 4))               :finalized
    (and (= state :error) (>= counter index) (< index 2))                   :error
    :else                                                                   :pending))

(defn- calculate-box-state-sidenet
  [state]
  (case state
    :error                              :error
    (:confirmed :finalising :finalized) :finalized
    :pending))

(defn- calculate-progressed-value
  [state progress-value]
  (case state
    :finalising progress-value
    :finalized  max-progress
    min-progress))

(defn- progress-boxes-mainnet
  [{:keys [state counter total-box customization-color]}]
  [rn/view
   {:accessibility-label :mainnet-progress-box
    :style               (style/progress-box-container true)}
   (let [numbers (range 1 total-box)]
     (doall (for [n numbers]
              [progress-box/view
               {:state               (calculate-box-state state counter n)
                :customization-color customization-color
                :key                 n}])))])

(defn- progress-boxes-sidenet
  [{:keys [state bottom-large? customization-color progress-value]}]
  [rn/view
   {:accessibility-label :progress-box
    :style               (style/progress-box-container bottom-large?)}
   [progress-box/view
    {:state               (calculate-box-state-sidenet state)
     :customization-color customization-color}]
   [progress-box/view
    {:state               (calculate-box-state-sidenet state)
     :full-width?         true
     :progressed-value    (calculate-progressed-value state progress-value)
     :customization-color customization-color}]])

(defn- view-internal
  [{:keys [network] :as props}]
  (case network
    :mainnet              [progress-boxes-mainnet props]
    (:arbitrum :optimism) [progress-boxes-sidenet props]
    nil))

(def view (quo.theme/with-theme view-internal))
