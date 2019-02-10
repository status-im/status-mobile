(ns status-im.utils.handlers
  (:require [cljs.spec.alpha :as spec]
            [clojure.string :as string]
            [re-frame.core :as re-frame]
            [re-frame.interceptor :refer [->interceptor get-coeffect get-effect]]
            [status-im.accounts.db :as accounts.db]
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
    first))

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

(def logged-in
  "Interceptor which stops the event chain if the user is logged out"
  (->interceptor
   :id     :logged-in
   :before (fn logged-in-before
             [context]
             (when (accounts.db/logged-in? (:coeffects context))
               context))))

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

(def default-interceptors
  [debug-handlers-names
   (when js/goog.DEBUG
     check-spec)
   (re-frame/inject-cofx :now)])

(defn- parse-json
  ;; NOTE(dmitryn) Expects JSON response like:
  ;; {"error": "msg"} or {"result": true}
  [s]
  (try
    (let [res (-> s
                  js/JSON.parse
                  (js->clj :keywordize-keys true))]
      ;; NOTE(dmitryn): AddPeer() may return {"error": ""}
      ;; assuming empty error is a success response
      ;; by transforming {"error": ""} to {:result true}
      (if (and (:error res)
               (= (:error res) ""))
        {:result true}
        res))
    (catch :default e
      {:error (.-message e)})))

(defn response-handler [success-fn error-fn]
  (fn handle-response
    ([response]
     (let [{:keys [error result]} (parse-json response)]
       (handle-response error result)))
    ([error result]
     (if error
       (error-fn error)
       (success-fn result)))))

(defn register-handler-fx
  ([name handler]
   (register-handler-fx name nil handler))
  ([name interceptors handler]
   (re-frame/reg-event-fx name [default-interceptors interceptors] handler)))
