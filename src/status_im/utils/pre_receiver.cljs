(ns status-im.utils.pre-receiver
  (:require-macros [cljs.core.async.macros :as async])
  (:require [cljs.core.async :as async]
            [taoensso.timbre :as log]))

;; See status-im.test.utils.pre-receiver for justification.

(defn- add-message-mock [{:keys [id clock-value] :as msg}]
  (log/debug "add-message-mock:" id clock-value))

(defn- earliest-clock-value-seen? [seen id clock-value]
  (->> seen
       (filter (fn [[_ x]] (= x id)))
       sort
       ffirst
       (= clock-value)))

(defn start!
  "Starts a pre-receiver that returns channel to put messages on. Once
  'delay-ms' (default 50ms) time has passed, calls add-fn on message."
  [& [{:keys [delay-ms reorder? add-fn]
       :or {delay-ms 50 reorder? true add-fn add-message-mock}}]]
  (let [in-ch     (async/chan)
        mature-ch (async/chan)
        seen      (atom #{})]
    (async/go-loop []
      (let [{:keys [message-id clock-value] :as msg} (async/<! in-ch)]
        (swap! seen conj [clock-value message-id])
        (async/<! (async/timeout delay-ms))
        (async/put! mature-ch msg))
      (recur))
    (async/go-loop []
      (let [{:keys [message-id clock-value] :as msg} (async/<! mature-ch)]
        (if reorder?
          (if (earliest-clock-value-seen? @seen message-id clock-value)
            (do (swap! seen disj [clock-value message-id])
                (add-fn msg))
            (async/put! mature-ch msg))
          (add-fn msg)))
      (recur))
    in-ch))
