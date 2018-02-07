(ns status-im.transport.filters
  (:require [status-im.transport.utils :as utils]
            [status-im.utils.config :as config]
            [re-frame.core :as re-frame]
            [taoensso.timbre :as log]))

(defonce filters (atom {}))

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
  (let [filter   (.newMessageFilter (utils/shh web3) (clj->js options)
                                    callback
                                    #(log/warn :add-filter-error (.stringify js/JSON (clj->js options)) %))]
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

(re-frame/reg-fx
  :shh/add-filter
  (fn [{:keys [web3 sym-key-id topic chat-id]}]
    (add-filter! web3
                 {:symKeyID sym-key-id
                  :topics [topic]}
                 (fn [js-error js-message]
                   (re-frame/dispatch [:protocol/receive-whisper-message js-error js-message chat-id])))))
