(ns status-im.protocol.web3.filtering
  (:require [status-im.protocol.web3.utils :as u]
            [status-im.utils.config :as config]
            [cljs.spec.alpha :as s]
            [taoensso.timbre :as log]
            [goog.string :as gstring]
            [goog.string.format]))

;; XXX(oskarth): Perf issue to have one topic
;; See https://github.com/status-im/ideas/issues/55#issuecomment-355511183
(def status-topic "0xaabb1122")

(defonce filters (atom {}))

;; NOTE(oskarth): This has concerns for upgradability and chatting cross
;; versions. How can we do this breaking change gradually?

;; NOTE(oskarth): Due to perf we don't want a single topic for all messages,
;; instead we want many. We need a way for user A and B to agree on which topics
;; to use. By using first 10 characters of the pub-key, we construct a topic
;; that requires no coordination for 1-1 chats.
(defn identity->topic [identity]
  (apply str (take 10 identity)))


(defn get-topics [& [identity]]
  (if config/many-whisper-topics-enabled?
    (do (log/info "FLAG: many-whisper-topics-enabled ON")
        [(identity->topic identity)])
    (do (log/info "FLAG: many-whisper-topics-enabled OFF")
        [status-topic])))

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
