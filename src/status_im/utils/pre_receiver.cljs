(ns status-im.utils.pre-receiver
  (:require [cljs.core.async :as async]
            [taoensso.timbre :as log]
            [status-im.utils.async :as async-utils]))

;; See status-im.test.utils.pre-receiver for justification.

(defn- add-message-mock [{:keys [id clock-value] :as msg}]
  (log/debug "add-message-mock:" id clock-value))

(defn start!
  "Starts a pre-receiver that returns channel to put messages on. Once
  'delay-ms' (default 50ms) time has passed, calls add-fn on message."
  [& [{:keys [delay-ms reorder? add-fn]
       :or {delay-ms 50 reorder? true add-fn add-message-mock}}]]
  (let [in-ch     (async/chan)
        mature-ch (async/chan)
        msg-queue (atom #{})]
    (async/go-loop []
      (let [msg (async/<! in-ch)]
        (if reorder?
          (swap! msg-queue conj msg)
          (async/put! mature-ch msg)))
      (recur))
    (when reorder?
      (async/go-loop []
        (async/<! (async-utils/timeout delay-ms))
        (doseq [msg (->> @msg-queue
                         (sort #(< (:clock-value %1)
                                   (:clock-value %2))))]
          (async/put! mature-ch msg)
          (swap! msg-queue disj msg))
        (recur)))
    (async/go-loop []
      (add-fn (async/<! mature-ch))
      (recur))
    in-ch))
