(ns status-im2.common.log
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im2.config :as config]
            [taoensso.timbre :as log]
            [utils.re-frame :as rf]))

(def logs-queue (atom #queue []))
(def max-log-entries 1000)

(defn get-logs-queue [] @logs-queue)

(defn add-log-entry
  [entry]
  (swap! logs-queue conj entry)
  (when (>= (count @logs-queue) max-log-entries)
    (swap! logs-queue pop)))

(defn setup
  [level]
  (when-not (string/blank? level)
    (log/set-level! (-> level
                        string/lower-case
                        keyword))
    (log/merge-config!
     {:output-fn (fn [& data]
                   (let [res (apply log/default-output-fn data)]
                     (add-log-entry res)
                     res))})))

(re-frame/reg-fx
 :logs/set-level
 (fn [level]
   (setup level)))

(rf/defn set-log-level
  [{:keys [db]} log-level]
  (let [log-level (or log-level config/log-level)]
    {:db             (assoc-in db [:multiaccount :log-level] log-level)
     :logs/set-level log-level}))
