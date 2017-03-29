(ns status-im.protocol.web3.filtering
  (:require [status-im.protocol.web3.utils :as u]
            [cljs.spec :as s]
            [taoensso.timbre :as log]))

(def status-topic "status-dapp-topic")
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
  [web3 options callback]
  (fn do-add-filter-fn
    ([] (do-add-filter-fn nil))
    ([keyname]
     (let [options' (if keyname
                      (assoc options :keyname keyname)
                      options)
           filter   (.filter (u/shh web3) (clj->js options')
                             callback
                             #(log/warn :add-filter-error options %))]
       (swap! filters assoc-in [web3 options] filter)))))

(defn add-filter!
  [web3 {:keys [topics to] :as options} callback]
  (remove-filter! web3 options)
  (log/debug :add-filter options)
  (let [shh           (u/shh web3)
        encrypted?    (boolean to)
        do-add-filter (add-shh-filter! web3 options callback)]
    (if encrypted?
      (do-add-filter)
      (let [topic (first topics)]
        (.hasSymKey
          shh topic
          (fn [error res]
            (if-not res
              (.addSymKey
                shh topic u/status-key-data
                (fn [error res]
                  (when-not error (do-add-filter topic))))
              (do-add-filter topic))))))))

(defn remove-all-filters! []
  (doseq [[web3 filters] @filters]
    (doseq [options (keys filters)]
      (remove-filter! web3 options))))
