(ns status-im.utils.handlers
  (:require [cljs.spec.alpha :as spec]
            [clojure.string :as string]
            [re-frame.core :refer [reg-event-db reg-event-fx] :as re-frame]
            [re-frame.interceptor :refer [->interceptor get-coeffect get-effect]]
            [taoensso.timbre :as log])
  (:require-macros status-im.utils.handlers))

(def pre-event-callback (atom nil))

(defn add-pre-event-callback [callback]
  (reset! pre-event-callback callback))

(def debug-handlers-names
  "Interceptor which logs debug information to js/console for each event."
  (->interceptor
   :id     :debug-handlers-names
   :before (fn debug-handlers-names-before
             [context]
             (when @pre-event-callback
               (@pre-event-callback (get-coeffect context :event)))
             (log/debug "Handling re-frame event: " (first (get-coeffect context :event)))
             context)))

(defn- check-spec-msg-path-problem [problem]
  (let [pred (:pred problem)]
    (str "Spec: " (-> problem :via last) "\n"
         "Predicate: " (subs (str (:pred problem)) 0 50))))

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


(defn register-handler
  ([name handler] (register-handler name nil handler))
  ([name middleware handler]
   (reg-event-db name [debug-handlers-names (when js/goog.DEBUG check-spec) middleware] handler)))

(def default-interceptors
  [debug-handlers-names (when js/goog.DEBUG check-spec) (re-frame/inject-cofx :now)])

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

(defn update-db [cofx fx]
  (if-let [db (:db fx)]
    (assoc cofx :db db)
    cofx))

(defn safe-merge [fx new-fx]
  (if (:merging-fx-with-common-keys fx)
    fx
    (let [common-keys (clojure.set/intersection (into #{} (keys fx))
                                                (into #{} (keys new-fx)))]
      (if (empty? (disj common-keys :db))
        (merge fx new-fx)
        {:merging-fx-with-common-keys common-keys}))))
