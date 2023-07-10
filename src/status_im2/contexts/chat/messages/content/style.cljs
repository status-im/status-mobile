(ns status-im2.contexts.chat.messages.content.style
  (:require [quo2.foundations.colors :as colors]))

(defn message-container
  ([]
   (message-container false nil nil false))
  ([in-pinned-view? pinned-by mentioned last-in-group?]
   (cond-> {:border-radius     16
            :margin-horizontal 8}

     (and (not in-pinned-view?) (or mentioned pinned-by))
     (assoc :background-color colors/primary-50-opa-5 :margin-bottom 4)

     (and (not in-pinned-view?) (or mentioned pinned-by last-in-group?))
     (assoc :margin-top 8))))
