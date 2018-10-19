(ns status-im.utils.async
  "Utility namespace containing `core.async` helper constructs"
  (:require [cljs.core.async :as async]
            [taoensso.timbre :as log]
            [status-im.utils.utils :as utils]))

(defn timeout [ms]
  (let [c (async/chan)]
    (utils/set-timeout (fn [] (async/close! c)) ms)
    c))

;; This wrapping is required as core.async macro replaces tries and catch with
;; https://github.com/clojure/core.async/blob/18d2f903b169c681ed008dd9545dc33458604b89/src/main/clojure/cljs/core/async/impl/ioc_helpers.cljs#L74
;; and this does not seem to play nice with desktop, and the error is bubble up killing the go-loop
(defn run-task [f]
  (try
    (f)
    (catch :default e
      (log/error "failed to run task" e))))

(defn chunked-pipe!
  "Connects input channel to the output channel with time-based chunking.
  `flush-time` parameter decides for how long we are waiting to accumulate
  value from input channel in a vector before it's put on the output channel.
  When `flush-time` interval elapses and there are no values accumulated, nothing
  is put on the output channel till the next input arrives, which is then put on
  the output channel immediately (wrapped in a vector).
  When input channel is closed, output channel is closed as well and go-loop exits."
  [input-ch output-ch flush-time]
  (async/go-loop [acc []
                  flush? false]
    (if flush?
      (do (async/put! output-ch acc)
          (recur [] false))
      (let [[v ch] (async/alts! [input-ch (timeout flush-time)])]
        (if (= ch input-ch)
          (if v
            (recur (conj acc v) (and (seq acc) flush?))
            (async/close! output-ch))
          (recur acc (seq acc)))))))

(defn task-queue
  "Creates `core.async` channel which will process 0 arg functions put there in serial fashon.
  Takes the same argument/s as `core.async/chan`, those arguments will be delegated to the
  channel constructor.
  Returns task-queue where tasks represented by 0 arg task functions can be put for processing."
  [& args]
  (let [task-queue (apply async/chan args)]
    (async/go-loop [task-fn (async/<! task-queue)]
      (run-task task-fn)
      (recur (async/<! task-queue)))
    task-queue))
