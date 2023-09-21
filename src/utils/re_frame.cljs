(ns utils.re-frame
  (:require-macros utils.re-frame)
  (:require [re-frame.core :as re-frame]
            [re-frame.interceptor :as interceptor]
            [taoensso.timbre :as log]
            [utils.datetime :as datetime])
  (:refer-clojure :exclude [merge reduce]))

(def handler-nesting-level (atom 0))

(re-frame/reg-cofx :now (fn [coeffects _] (assoc coeffects :now (datetime/timestamp))))

(def debug-handlers-names
  "Interceptor which logs debug information to js/console for each event."
  (interceptor/->interceptor
   :id     :debug-handlers-names
   :before (fn debug-handlers-names-before
             [context]
             (when js/goog.DEBUG
               (reset! handler-nesting-level 0))
             (log/debug "Handling re-frame event: " (first (interceptor/get-coeffect context :event)))
             context)))

(defn register-handler-fx
  ([name handler]
   (register-handler-fx name nil handler))
  ([name interceptors handler]
   (re-frame/reg-event-fx
    name
    [debug-handlers-names (re-frame/inject-cofx :now) interceptors]
    handler)))

(defn- update-db
  [cofx fx]
  (if-let [db (:db fx)]
    (assoc cofx :db db)
    cofx))

(def ^:private mergeable-keys (atom nil))

(defn set-mergeable-keys
  [v]
  (reset! mergeable-keys v))

(defn- safe-merge
  [fx new-fx]
  (if (:merging-fx-with-common-keys fx)
    fx
    (clojure.core/reduce (fn [merged-fx [k v]]
                           (if (= :db k)
                             (assoc merged-fx :db v)
                             (if (get merged-fx k)
                               (if (get @mergeable-keys k)
                                 (update merged-fx k into v)
                                 (do (log/error "Merging fx with common-key: " k v (get merged-fx k))
                                     (reduced {:merging-fx-with-common-keys k})))
                               (assoc merged-fx k v))))
                         fx
                         new-fx)))

(defn merge
  "Takes a map of co-effects and forms as argument.
  The first optional form can be map of effects
  The next forms are functions applying effects and returning a map of effects.
  The fn ensures that updates to db are passed from function to function within the cofx :db key and
  that only a :merging-fx-with-common-keys effect is returned if some functions are trying
  to produce the same effects (excepted :db, :data-source/tx effects).
  :data-source/tx and effects are handled specially and their results
  (list of transactions) are compacted to one transactions list (for each effect). "
  [{:keys [db] :as cofx} & args]
  (when js/goog.DEBUG
    (swap! handler-nesting-level inc))
  (let [[first-arg & rest-args] args
        initial-fxs? (map? first-arg)
        fx-fns (if initial-fxs? rest-args args)
        res
        (clojure.core/reduce (fn [fxs fx-fn]
                               (let [updated-cofx (update-db cofx fxs)]
                                 (if fx-fn
                                   (safe-merge fxs (fx-fn updated-cofx))
                                   fxs)))
                             (if initial-fxs? first-arg {:db db})
                             fx-fns)]
    (swap! handler-nesting-level dec)
    res))

(def sub (comp deref re-frame/subscribe))

(def dispatch re-frame/dispatch)
