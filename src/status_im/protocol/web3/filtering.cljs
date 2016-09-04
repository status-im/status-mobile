(ns status-im.protocol.web3.filtering
  (:require [status-im.protocol.web3.utils :as u]
            [cljs.spec :as s]
            [taoensso.timbre :refer-macros [debug]]))

(def status-topic "status-dapp-topic")
(defonce filters (atom {}))

(s/def ::options (s/keys :opt-un [:message/to :message/topics]))

(defn remove-filter! [web3 options]
  (when-let [filter (get-in @filters [web3 options])]
    (.stopWatching filter)
    (debug :stop-watching options)
    (swap! filters update web3 dissoc options)))

(defn add-filter!
  [web3 options callback]
  (remove-filter! web3 options)
  (debug :add-filter options)
  (let [filter (.filter (u/shh web3)
                        (clj->js options)
                        callback)]
    (swap! filters assoc-in [web3 options] filter)))

(defn remove-all-filters! []
  (doseq [[web3 filters] @filters]
    (doseq [options (keys filters)]
      (remove-filter! web3 options))))
