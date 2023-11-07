(ns status-im2.common.log
  (:require
    [clojure.pprint :as p]
    [clojure.string :as string]
    [native-module.core :as native-module]
    [re-frame.core :as re-frame]
    [status-im2.config :as config]
    [taoensso.timbre :as log]
    [utils.re-frame :as rf]
    [utils.transforms :as transforms]))

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
  (let [handle-error   (fn [res]
                         (let [{:keys [error]} (transforms/json->clj res)]
                           (when-not (string/blank? error)
                             (log/error "init statusgo logging failed" error))))
        logging-params {:enable?        true
                        :mobile-system? false
                        :log-level      level
                        :callback       handle-error}]
    (log/merge-config! {:ns-filter {:allow #{"*"} :deny #{"taoensso.sente"}}})
    (if (string/blank? level)
      (native-module/init-status-go-logging (merge logging-params {:log-level "WARN"}))
      (do
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
                                 (partial mapv #(if (string? %) % (with-out-str (p/pprint %))))))]})
        (native-module/init-status-go-logging logging-params)))))

(re-frame/reg-fx
 :logs/set-level
 (fn [level]
   (setup level)))

(rf/defn set-log-level
  [{:keys [db]} log-level]
  (let [log-level (or log-level config/log-level)]
    {:db             (assoc-in db [:profile/profile :log-level] log-level)
     :logs/set-level log-level}))
