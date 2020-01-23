(ns status-im.utils.handlers
  (:require [cljs.spec.alpha :as spec]
            [re-frame.core :as re-frame]
            [clojure.set :as set]
            [re-frame.interceptor :refer [->interceptor get-coeffect get-effect]]
            [status-im.multiaccounts.model :as multiaccounts.model]
            [status-im.utils.config :as config]
            [taoensso.timbre :as log]))

;; Taken from re-frisk https://github.com/flexsurfer/re-frisk/blob/c2783436712bdb682770d5cf741d9f2db041d8de/src/re_frisk/diff.cljs#L1
;; clojure.data/diff is hard to work with:
;; (diff {:a [0 1 2]} {:a [0 1]}) => ({:a [nil nil 2]} nil {:a [0 1]})
;; (data/diff {:a [2]} {:a [1]}) => ({:a [2]} {:a [1]} nil)
;; ... so roll our own

(declare diff)

(defn- mv-keys [coll]
  (if (map? coll)
    (keys coll)
    (keep-indexed #(when-not (nil? %2) %1) coll)))

(defn- diff-coll [a b]
  (into {}
        (for [key (set/union (set (mv-keys a)) (set (mv-keys b)))]
          (let [val-a (get a key)
                val-b (get b key)]
            (cond
              (= val-a val-b) nil
              (and val-a val-b) [key (diff val-a val-b)]
              val-a [key {:deleted val-a}]
              val-b [key val-b])))))

(defn- diff-set [a b]
  {:deleted (set/difference a b)
   :added (set/difference b a)})

(defn- diff-rest [a b]
  {:before a :after b})

(defn diff [a b]
  (cond
    (= a b) nil
    (and (map? a) (map? b)) (diff-coll a b)
    (and (vector? a) (vector? b)) (diff-coll a b)
    (and (set? a) (set? b)) (diff-set a b)
    :else (diff-rest a b)))

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
   :id     :handlers-names
   :before (fn handlers-names-before
             [context]
             (do
               (when @pre-event-callback
                 (@pre-event-callback (get-coeffect context :event)))
               (log/info "Handling re-frame event: " (pretty-print-event context))
               context))
   :after (fn handlers-names-after
            [context]
            (do
              (log/info "Handled re-frame event: " (pretty-print-event context))
              (log/info "Diff: " (diff (get-in context [:coeffects :db])
                                       (get-in context [:effects :db])))
              context))))

(def logged-in
  "Interceptor which stops the event chain if the user is logged out"
  (->interceptor
   :id     :logged-in
   :before (fn logged-in-before
             [context]
             (when (multiaccounts.model/logged-in? (:coeffects context))
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
