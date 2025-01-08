(ns status-im.common.log
  (:require
    [clojure.pprint :as pprint]
    [clojure.string :as string]
    [re-frame.core :as re-frame]
    [taoensso.timbre :as log]))

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
  (log/merge-config! {:ns-filter {:allow #{"*"} :deny #{"taoensso.sente"}}})
  (when-not (string/blank? level)
    (log/set-min-level! (-> level
                            string/lower-case
                            keyword))
    (log/merge-config!
     {:output-fn  (fn [& data]
                    (let [res (apply log/default-output-fn data)]
                      (add-log-entry res)
                      res))
      :middleware [(fn [data]
                     (update data
                             :vargs
                             (partial mapv
                                      #(if (string? %) % (with-out-str (pprint/pprint %))))))]})))

(re-frame/reg-fx
 :logs/set-level
 (fn [level]
   (setup level)))
