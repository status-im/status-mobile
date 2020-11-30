(ns status-im.ui.screens.profile.models
  (:require [clojure.string :as clojure.string]
            [re-frame.core :as re-frame]
            [status-im.ui.components.react :as react]
            [status-im.multiaccounts.update.core :as multiaccounts.update]
            [taoensso.timbre :as log]
            [status-im.utils.fx :as fx]))

(defn enter-two-random-words [{:keys [db]}]
  (let [{:keys [mnemonic]} (:multiaccount db)
        shuffled-mnemonic (shuffle (map-indexed vector (clojure.string/split mnemonic #" ")))]
    {:db (assoc db :my-profile/seed {:step        :first-word
                                     :first-word  (first shuffled-mnemonic)
                                     :second-word (second shuffled-mnemonic)})}))

(defn set-step [step {:keys [db]}]
  {:db (update db :my-profile/seed assoc :step step :error nil :word nil)})

(fx/defn finish [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db (update db :my-profile/seed assoc :step :finish :error nil :word nil)}
            (multiaccounts.update/clean-seed-phrase)))

(defn copy-to-clipboard! [value]
  (react/copy-to-clipboard value))

(def show-tooltip!
  (let [tooltips (atom {})]
    (fn [tooltip-id]
      (when-let [{:keys [interval-id]} (@tooltips tooltip-id)]
        (js/clearInterval interval-id))
      (let [interval-id (js/setInterval
                         #(let [{:keys [opacity interval-id cnt]} (@tooltips tooltip-id)]
                            (when opacity
                              (swap! tooltips assoc-in [tooltip-id :cnt] (inc cnt))
                              (if (and opacity (>= 0.0 opacity))
                                (do
                                  (log/debug "remove interval:" interval-id)
                                  (js/clearInterval interval-id)
                                  (re-frame/dispatch [:set-in [:tooltips tooltip-id] nil])
                                  (swap! tooltips dissoc interval-id))
                                (do (re-frame/dispatch [:set-in [:tooltips tooltip-id] opacity])
                                    (when (< 10 cnt)
                                      (swap! tooltips assoc-in [tooltip-id :opacity] (- opacity 0.05)))))))
                         100)]
        (swap! tooltips assoc tooltip-id {:opacity 1.0 :interval-id interval-id :cnt 0})))))
