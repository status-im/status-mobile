(ns status-im.protocol.web3.filtering
  (:require [status-im.protocol.web3.utils :as u]
            [cljs.spec.alpha :as s]
            [taoensso.timbre :as log]))

(def status-topic "0xaabb11ee")
(defonce filters (atom {}))

(s/def ::options (s/keys :opt-un [:message/to :message/topics]))

(defn remove-filter! [web3 options]
  (when-let [filter (get-in @filters [web3 options])]
    (.stopWatching filter
                   (fn [error _]
                     (when error
                       (log/warn :remove-filter-error options error))))
    (log/debug :stop-watching options)
    (swap! filters update web3 dissoc options)))

(defn add-shh-filter!
  [web3 {:keys [key type] :as options} callback]
  (let [type     (or type :asym)
        options' (cond-> (dissoc options :key)
                         (= type :asym) (assoc :privateKeyID key)
                         (= type :sym) (assoc :symKeyID key))
        filter   (.newMessageFilter (u/shh web3) (clj->js options')
                                    callback
                                    #(log/warn :add-filter-error (.stringify js/JSON (clj->js options')) %))]
    (swap! filters assoc-in [web3 options] filter)))

(defn add-filter!
  [web3 {:keys [topics to] :as options} callback]
  (remove-filter! web3 options)
  (log/debug :add-filter options)
  (add-shh-filter! web3 options callback))

(defn remove-all-filters! []
  (doseq [[web3 filters] @filters]
    (doseq [options (keys filters)]
      (remove-filter! web3 options))))
