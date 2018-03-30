(ns status-im.utils.mixpanel
  (:require-macros [status-im.utils.slurp :as slurp])
  (:require [cljs.core.async :as async]
            [cljs.reader :as reader]
            [goog.crypt.base64 :as b64]
            [status-im.utils.build :as build]
            [status-im.utils.config :as config]
            [status-im.utils.datetime :as datetime]
            [status-im.utils.http :as http]
            [status-im.utils.platform :as platform]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]))

(def base-url "http://api.mixpanel.com/")
(def base-track-url (str base-url "track/"))

(defn encode [m]
  (b64/encodeString (types/clj->json m)))

(defn- make-event [id label props]
  {:event      label
   :properties (merge
                {:token       config/mixpanel-token
                 :distinct_id id
                 :os          platform/os
                 :os-version  platform/version
                 :app-version build/version
                 :time        (datetime/timestamp)}
                props)})

;; holds events that are accumulated while offline. will start throwing away old
;; events if we accumulate more than 2000
(def ^:private pending-events-queue (async/chan (async/sliding-buffer 2000)))

(defn- submit-batch
  "Submit a batch of events to mixpanel via POST"
  [events]
  (let [done-chan (async/chan)]
    (log/debug "submitting" (count events) "events")
    (http/post base-track-url
               (str "data=" (encode (into [] events)))
               (fn [_]
                 (log/debug "successfully submitted events")
                 (async/go (async/close! done-chan)))
               (fn [error]
                 (log/error "error while submitting events" error)
                 (async/go (async/close! done-chan))))
    done-chan))

;; maximum number of events that should be submitted to mixpanel's batch
;; endpoint at once (see https://mixpanel.com/help/reference/http)
(def max-batch-size 50)

(defn drain-events-queue!
  "Drains accumulated events and submits them in batches of <max-batch-size>"
  ([]
   (drain-events-queue! pending-events-queue submit-batch))
  ([queue callback]
   (let [events (loop [accumulator []]
                  (if-let [event (async/poll! queue)]
                    (recur (conj accumulator event))
                    accumulator))]
     (async/go
       (doseq [batch (partition-all max-batch-size events)]
         (async/<! (callback batch)))))))

(defn track
  "Track or accumulate an event"
  [id label props offline?]
  (log/debug "tracking" id label props offline?)
  (let [event (make-event id label props)]
    ;; enqueue event
    (async/go (async/>! pending-events-queue event))
    ;; drain queue if we are online
    (when-not offline?
      (async/go (async/<! (drain-events-queue!))))))

;; Mixpanel events definition
(def events (reader/read-string (slurp/slurp "./src/status_im/utils/mixpanel_events.edn")))
(def event-by-trigger (reduce-kv #(assoc %1 (:trigger %3) %3) {} events))

(defn matches? [event trigger]
  (cond (= 1 (count trigger))
        (= (first event) (first trigger))
        (= 2 (count trigger))
        (and
          (= (first event) (first trigger))
          (= (second event) (second trigger)))
        :else
        (= event trigger)))

(defn matching-events [event definitions]
  (reduce-kv #(if (matches? event %2) (conj %1 %3) %1) [] definitions))

