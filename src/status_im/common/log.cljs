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

(defn log-error
  ([error]
   (log/error error))
  ([message error]
   (log/error message error))
  ([message context error]
   (log/error message context error)))

(re-frame/reg-event-fx
 :log/error
 (fn [_ args]
   {:fx [[:effects.log/error args]]}))

(re-frame/reg-fx
 :effects.log/error
 (fn [args]
   (apply log-error args)))

(defn log-info
  ([info]
   (log/info info))
  ([message info]
   (log/info message info))
  ([message context info]
   (log/info message context info)))

(re-frame/reg-event-fx
 :log/info
 (fn [_ args]
   {:fx [[:effects.log/info args]]}))

(re-frame/reg-fx
 :effects.log/info
 (fn [args]
   (apply log-info args)))

(defn log-warn
  ([warning]
   (log/warn warning))
  ([message warning]
   (log/warn message warning))
  ([message context warning]
   (log/warn message context warning)))

(re-frame/reg-event-fx
 :log/warn
 (fn [_ args]
   {:fx [[:effects.log/warn args]]}))

(re-frame/reg-fx
 :effects.log/warn
 (fn [args]
   (apply log-warn args)))

(defn log-debug
  ([value]
   (log/debug value))
  ([message value]
   (log/debug message value))
  ([message context value]
   (log/debug message context value)))

(re-frame/reg-event-fx
 :log/debug
 (fn [_ args]
   {:fx [[:effects.log/debug args]]}))

(re-frame/reg-fx
 :effects.log/debug
 (fn [args]
   (apply log-debug args)))
