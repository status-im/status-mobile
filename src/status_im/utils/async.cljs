(ns status-im.utils.async
  "Utility namespace containing `core.async` helper constructs"
  (:require [cljs.core.async :as async]
            [status-im.utils.utils :as utils]
            [taoensso.timbre :as log]))

(defn timeout
  [ms]
  (let [c (async/chan)]
    (utils/set-timeout (fn [] (async/close! c)) ms)
    c))

;; This wrapping is required as core.async macro replaces tries and catch with
;; https://github.com/clojure/core.async/blob/18d2f903b169c681ed008dd9545dc33458604b89/src/main/clojure/cljs/core/async/impl/ioc_helpers.cljs#L74
;; and this does not seem to play nice with desktop, and the error is bubble up killing the go-loop
(defn run-task
  [f]
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
  (async/go-loop [acc    []
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
  "Creates `core.async` channel which will process 0 arg functions put there in serial fashion.
  Takes the same argument/s as `core.async/chan`, those arguments will be delegated to the
  channel constructor.
  Returns task-queue where tasks represented by 0 arg task functions can be put for processing."
  [& args]
  (let [queue (apply async/chan args)]
    (async/go-loop [task-fn (async/<! queue)]
      (run-task task-fn)
      (recur (async/<! queue)))
    queue))

;; ---------------------------------------------------------------------------
;; Periodic background job
;; ---------------------------------------------------------------------------

(defn async-periodic-run!
  ([async-periodic-chan]
   (async-periodic-run! async-periodic-chan true))
  ([async-periodic-chan worker-fn]
   (async/put! async-periodic-chan worker-fn)))

(defn async-periodic-stop!
  [async-periodic-chan]
  (async/close! async-periodic-chan))

(defn async-periodic-exec
  "Periodically execute an function.

  Takes a work-fn of one argument `finished-fn -> any` this function
  is passed a finished-fn that must be called to signal that the work
  being performed in the work-fn is finished.

  Returns a go channel that represents a way to control the looping process.

  Stop the polling loop with `async-periodic-stop!`

  The work-fn can be forced to run immediately with `async-periodic-run!`

  Or you can queue up another fn `finished-fn -> any` to execute on
  the queue with `async-periodic-run!`."
  [work-fn interval-ms timeout-ms]
  {:pre [(fn? work-fn) (integer? interval-ms) (integer? timeout-ms)]}
  (let [do-now-chan (async/chan (async/sliding-buffer 1))
        try-it      (fn [exec-fn catch-fn] (try (exec-fn) (catch :default e (catch-fn e))))]
    (async/go-loop []
      (let [timeout-chan  (timeout interval-ms)
            finished-chan (async/promise-chan)
            [v ch]        (async/alts! [do-now-chan timeout-chan])
            worker        (if (and (= ch do-now-chan) (fn? v))
                            v
                            work-fn)]
        (when-not (and (= ch do-now-chan) (nil? v))
          ;; don't let try catch be parsed by go-block
          (try-it #(worker (fn [] (async/put! finished-chan true)))
                  (fn [e]
                    (log/error "failed to run job" e)
                    ;; if an error occurs in work-fn log it and consider it done
                    (async/put! finished-chan true)))
          ;; sanity timeout for work-fn
          (async/alts! [finished-chan (timeout timeout-ms)])
          (recur))))
    do-now-chan))

(comment
  (def c (atom nil))

  (let [periodic-task-chan (async-periodic-exec #(prn :task) 5000 1000)]
    (reset! c periodic-task-chan))

  (async-periodic-stop! @c))
