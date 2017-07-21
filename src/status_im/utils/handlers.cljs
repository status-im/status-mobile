(ns status-im.utils.handlers
  (:require [re-frame.core :refer [reg-event-db reg-event-fx]]
            [re-frame.interceptor :refer [->interceptor get-coeffect]]
            [clojure.string :as str]
            [taoensso.timbre :as log]
            [cljs.spec.alpha :as s])
  (:require-macros status-im.utils.handlers))

(defn side-effect!
  "Middleware for handlers that will not affect db."
  [handler]
  (fn [db params]
    (handler db params)
    db))

(def debug-handlers-names
  "Interceptor which logs debug information to js/console for each event."
  (->interceptor
    :id     :debug-handlers-names
    :before (fn debug-handlers-names-before
              [context]
              (log/debug "Handling re-frame event: " (first (get-coeffect context :event)))
              context)))

(def check-spec
  "throw an exception if db doesn't match the spec"
  (->interceptor
    :id check-spec
    :after
    (fn check-handler
      [context]
      (let [new-db (get-coeffect context :db)
            v (get-coeffect context :event)]
        (when-not (s/valid? :status-im.specs/db new-db)
          (throw (ex-info (str "spec check failed on: " (first v) "\n " (s/explain-str :status-im.specs/db new-db)) {})))
        context))))

(defn register-handler
  ([name handler] (register-handler name nil handler))
  ([name middleware handler]
   (reg-event-db name [debug-handlers-names (when js/goog.DEBUG check-spec) middleware] handler)))

(defn register-handler-db
  ([name handler] (register-handler-db name nil handler))
  ([name interceptors handler]
   (reg-event-db name [debug-handlers-names (when js/goog.DEBUG check-spec) interceptors] handler)))

(defn register-handler-fx
  ([name handler] (register-handler-fx name nil handler))
  ([name interceptors handler]
   (reg-event-fx name [debug-handlers-names (when js/goog.DEBUG check-spec) interceptors] handler)))

(defn get-hashtags [status]
  (if status
    (let [hashtags (map #(str/lower-case (subs % 1))
                        (re-seq #"#[^ !?,;:.]+" status))]
      (set (or hashtags [])))
    #{}))

(defn identities [contacts]
  (->> (map second contacts)
       (remove (fn [{:keys [dapp? pending?]}]
                 (or pending? dapp?)))
       (map :whisper-identity)))
