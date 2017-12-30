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
             (when @pre-event-callback
               (@pre-event-callback (get-coeffect context :event)))
             (log/debug "Handling re-frame event: " (first (get-coeffect context :event)))
             context)))

(def check-spec
  "throw an exception if db doesn't match the spec"
  (->interceptor
   :id check-spec
   :after
   (fn check-handler
     [context]
     (let [new-db (get-effect context :db)
           v (get-coeffect context :event)]
       (when (and new-db (not (spec/valid? :status-im.ui.screens.db/db new-db)))
         (throw (ex-info (str "spec check failed on: " (first v) "\n " (spec/explain-str :status-im.ui.screens.db/db new-db)) {})))
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
    (let [hashtags (map #(string/lower-case (subs % 1))
                        (re-seq #"#[^ !?,;:.]+" status))]
      (set (or hashtags [])))
    #{}))

(defn identities [contacts]
  (->> (map second contacts)
       (remove (fn [{:keys [dapp? pending?]}]
                 (or pending? dapp?)))
       (map :whisper-identity)))
