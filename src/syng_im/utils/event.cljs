(ns syng-im.utils.event
  (:require [cljs.core.async :refer [<!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn handle-channel-events [chan handler]
  (go (loop [[msg args] (<! chan)]
        (when msg
          (handler msg args)
          (recur (<! chan))))))