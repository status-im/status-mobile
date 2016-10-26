(ns status-im.utils.scheduler
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [<! timeout]]))

(defn s->ms [s] (* 1000 s))

(defn execute-later
  [function timeout-ms]
  (go (<! (timeout timeout-ms))
      (function)))
