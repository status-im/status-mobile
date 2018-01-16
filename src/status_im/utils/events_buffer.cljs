(ns status-im.utils.events-buffer
  (:require [cljs.core.async :as async]
            [re-frame.core :as re-frame])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

;; NOTE:(dmitryn) Ideally we should not exceed current buffer size.
;; Buffer length is an experimental number, consider to change it.
(defonce ^:private buffer (async/chan 10000))

;; NOTE:(dmitryn) Reference to re-frame event loop mechanism
;; https://github.com/Day8/re-frame/blob/master/src/re_frame/router.cljc#L8
;; Might need future improvements.
;; "Fast" events could be processed in batches to speed up things,
;; so multiple buffers/channels could be introduced.
(defn- start-loop! [c t]
  "Dispatches events to re-frame processing queue,
   but in a way that doesn't block events processing."
  (go-loop [e (async/<! c)]
    (re-frame/dispatch e)
    (async/<! (async/timeout t))
    (recur (async/<! c))))

(defonce ^:private dispatch-loop (start-loop! buffer 0))

;; Accepts re-frame event vector [:event-id args]
;; NOTE(dmitryn) Puts all events into a single buffer (naive approach).
(defn dispatch [event]
  (async/put! buffer event))
