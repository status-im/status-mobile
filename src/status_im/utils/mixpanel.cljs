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
            [taoensso.timbre :as log]
            [status-im.utils.mixpanel-events :as mixpanel-events]
            [status-im.utils.config :as config]))

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

(def event-tag "events")

;; Mixpanel events definition
(defn event->triggers
  "Transform definitions vector into map which will be used for matching later

  [{:trigger [:en1]}
   {:trigger [:en1 :p1]}
   {:trigger [:en2 :p1]}
   {:trigger [:en3 :p1 :p2 :p3]}]

   will be transformed into

   {:en1 {events ({:trigger [:en1]})
          :p1      {events ({:trigger [:en1 :p1]})}}
    :en2 {:p1 {events ({:trigger [:en2 :p1]})}}
    :en3 {:p1 {:p2 {:p3 {events ({:trigger [:en3 :p1 :p2 :p3]})}}}}}"
  [events]
  (reduce (fn [m {:keys [trigger] :as event}]
            (update-in m (conj trigger event-tag) conj event))
          {}
          events))

(def event-by-trigger
  (event->triggers mixpanel-events/events))

(defn matching-events [db [event-name first-arg :as event] triggers]
  (let [cnt      (count event)
        triggers (cond->
                  ;; first we get all events which are triggered by event name
                  ;; (the case when :trigger contains only one element)
                  ;; {:trigger [:only-event-name-here]}
                  (get-in triggers [event-name event-tag])

                  ;; when event contains two or more elements we are trying
                  ;; to match by first two elements of :trigger
                  ;; {:trigger [:event-name :one-parameter]}
                   (>= cnt 2)
                   (concat (get-in triggers [event-name first-arg event-tag]))

                  ;; also if event contains more than one parameter (more than
                  ;; two elements) we are trying to match it with equal :trigger
                  ;; {:trigger [:e-name :p1 :p2 :p3]}
                  ;; will match only with [:e-name :p1 :p2 :p3] event
                   (> cnt 2)
                   (concat (get-in triggers (conj event event-tag))))]
    (->> triggers

         (filter (fn [{:keys [filter-fn]}]
                   (or (not filter-fn) (filter-fn db event))))

         (mapcat (fn [{:keys [data-fn] :as trigger}]
                   (if data-fn
                     (let [data (data-fn db event)]
                       (if (map? data)
                         [(update trigger :properties merge data)]
                         (map (partial update trigger :properties merge) data)))
                     [trigger]))))))

(defn force-tracking? [event-name]
  (and config/force-sr-ratio-tracking
       (contains? #{:signals/envelope-status :chat-received-message/add}
                  event-name)))
