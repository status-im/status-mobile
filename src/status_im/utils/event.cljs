(ns status-im.utils.event
  (:require [cljs.core.async :refer [<!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn handle-channel-events [chan handler]
  (go (loop [[message args] (<! chan)]
        (when message
          (handler message args)
          (recur (<! chan))))))