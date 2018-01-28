(ns status-im.utils.async
  "Utility namespace containing `core.async` helper constructs"
  (:require [cljs.core.async :as async]))

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
      (let [[v ch] (async/alts! [input-ch (async/timeout flush-time)])]
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
      (task-fn)
      (recur (async/<! task-queue)))
    task-queue))
