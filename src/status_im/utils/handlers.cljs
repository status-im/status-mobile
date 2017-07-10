(ns status-im.utils.handlers
  (:require [re-frame.core :refer [after dispatch debug] :as re-core]
            [clojure.string :as str]
            [taoensso.timbre :as log]
            [cljs.spec.alpha :as s]))

(defn side-effect!
  "Middleware for handlers that will not affect db."
  [handler]
  (fn [db params]
    (handler db params)
    db))

(defn debug-handlers-names
  "Middleware which logs debug information to js/console for each event.
  Includes a clojure.data/diff of the db, before vs after, showing the changes
  caused by the event."
  [handler]
  (fn debug-handler
    [db v]
    (log/debug "Handling re-frame event: " (first v))
    (let [new-db  (handler db v)]
      new-db)))

(defn check-spec
  "throw an exception if db doesn't match the spec"
  [handler]
  (fn check-handler
    [db v]
    (let [new-db  (handler db v)]
      (when-not (s/valid? :status-im.specs/db new-db)
        (throw (ex-info (str "spec check failed on: " (first v) "\n " (s/explain-str :status-im.specs/db new-db)) {})))
      new-db)))

(defn register-handler
  ([name handler] (register-handler name nil handler))
  ([name middleware handler]
   (re-core/register-handler name [debug-handlers-names (when js/goog.DEBUG check-spec) middleware] handler)))

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
