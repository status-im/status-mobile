(ns status-im.utils.handlers
  (:require [cljs.spec.alpha :as spec]
            [clojure.string :as string]
            [re-frame.core :refer [reg-event-db reg-event-fx] :as re-frame]
            [re-frame.interceptor :refer [->interceptor get-coeffect get-effect]]
            [status-im.utils.instabug :as instabug]
            [status-im.utils.mixpanel :as mixpanel]
            [cljs.core.async :as async]
            [taoensso.timbre :as log]))

(def pre-event-callback (atom nil))

(defn add-pre-event-callback [callback]
  (reset! pre-event-callback callback))

(defn side-effect!
  "Middleware for handlers that will not affect db."
  [handler]
  (fn [db params]
    (handler db params)
    db))

(defn- pretty-print-event [ctx]
  (let [[first second] (get-coeffect ctx :event)]
    ;; TODO wrap passwords in a custom type so it won't be possible to print them occasionally
    (if (= first :wallet.send/set-password)
      (str first " " "******") ;; special case not to expose password to the logs
      (if (or (string? second) (keyword? second) (boolean? second))
        (str first " " second)
        first))))

(def debug-handlers-names
  "Interceptor which logs debug information to js/console for each event."
  (->interceptor
   :id     :debug-handlers-names
   :before (fn debug-handlers-names-before
             [context]
             (when @pre-event-callback
               (@pre-event-callback (get-coeffect context :event)))
             (log/debug "Handling re-frame event: " (pretty-print-event context))
             context)))

(defn- check-spec-msg-path-problem [problem]
  (str "Spec: " (-> problem :via last) "\n"
       "Predicate: " (subs (str (:pred problem)) 0 50)))

(defn- check-spec-msg-path-problems [path path-problems]
  (str "Key path: " path "\n"
       "Val: " (pr-str (-> path-problems first :val)) "\n\n"
       "Number of problems: " (count path-problems) "\n\n"
       (->> path-problems
            (map check-spec-msg-path-problem)
            (interpose "\n\n")
            (apply str))))

(defn- check-spec-msg [event-id db]
  (let [explain-data (spec/explain-data :status-im.ui.screens.db/db db)
        problems     (::spec/problems explain-data)
        db-root-keys (->> problems (map (comp first :in)) set)
        heading      #(str "\n\n------\n" % "\n------\n\n")
        msg          (str "re-frame db spec check failed."
                          (heading "SUMMARY")
                          "After event id: " event-id "\n"
                          "Number of problems: " (count problems) "\n\n"
                          "Failing root db keys:\n"
                          (->> db-root-keys (interpose "\n") (apply str))
                          (heading "PROBLEMS")
                          (->> problems
                               (group-by :in)
                               (map (fn [[path path-problems]]
                                      (check-spec-msg-path-problems path path-problems)))
                               (interpose "\n\n-----\n\n")
                               (apply str)))]
    msg))

(def check-spec
  "throw an exception if db doesn't match the spec"
  (->interceptor
   :id check-spec
   :after
   (fn check-handler
     [context]
     (let [new-db   (get-effect context :db)
           event-id (-> (get-coeffect context :event) first)]
       (when (and new-db (not (spec/valid? :status-im.ui.screens.db/db new-db)))
         (throw (ex-info (check-spec-msg event-id new-db) {})))
       context))))

(def track-mixpanel
  "send an event to mixpanel for tracking"
  (->interceptor
   :id track-mixpanel
   :after
   (fn track-handler
     [context]
     (let [new-db             (get-coeffect context :db)
           [event-name]       (get-coeffect context :event)]
       (when (or
              (mixpanel/force-tracking? event-name)
              (get-in new-db [:account/account :sharing-usage-data?]))
         (let [event    (get-coeffect context :event)
               offline? (or (= :offline (:network-status new-db))
                            (= :offline (:sync-state new-db)))
               anon-id  (:device-UUID new-db)]
           (doseq [{:keys [label properties]}
                   (mixpanel/matching-events new-db event mixpanel/event-by-trigger)]
             (mixpanel/track anon-id label properties offline?)
             (instabug/track label properties)))))
     context)))

(defn register-handler
  ([name handler] (register-handler name nil handler))
  ([name middleware handler]
   (reg-event-db name [debug-handlers-names (when js/goog.DEBUG check-spec) middleware] handler)))

(def default-interceptors
  [debug-handlers-names
   (when js/goog.DEBUG check-spec)
   (re-frame/inject-cofx :now)
   track-mixpanel])

(defn register-handler-db
  ([name handler] (register-handler-db name nil handler))
  ([name interceptors handler]
   (reg-event-db name [default-interceptors interceptors] handler)))

(defn register-handler-fx
  ([name handler] (register-handler-fx name nil handler))
  ([name interceptors handler]
   (reg-event-fx name [default-interceptors interceptors] handler)))

(defn get-hashtags [status]
  (if status
    (let [hashtags (map #(keyword (string/lower-case (subs % 1)))
                        (re-seq #"#[^ !?,;:.]+" status))]
      (set (or hashtags [])))
    #{}))

(defn identities [contacts]
  (->> (map second contacts)
       (remove (fn [{:keys [dapp? pending?]}]
                 (or pending? dapp?)))
       (map :whisper-identity)))

(re-frame.core/reg-fx
 :drain-mixpanel-events
 (fn []
   (async/go (async/<! (mixpanel/drain-events-queue!)))))
