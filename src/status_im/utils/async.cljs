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
