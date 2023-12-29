(ns status-im.contexts.chat.messages.content.style
  (:require
    [quo.foundations.colors :as colors]))

(defn message-container
  ([]
   (message-container false nil nil false))
  ([in-pinned-view? pinned-by mentioned last-in-group?]
   (cond-> {:border-radius     16
            :margin-horizontal 8}

     (and (not in-pinned-view?) (or mentioned pinned-by))
     (assoc :background-color colors/primary-50-opa-5 :margin-bottom 4)

     (and (not in-pinned-view?) (or mentioned pinned-by last-in-group?))
     (assoc :margin-top 4))))

(defn user-message-content
  [{:keys [outgoing outgoing-status]}]
  {:border-radius      16
   :padding-horizontal 8
   :padding-vertical   4
   :opacity            (if (and outgoing (= outgoing-status :sending))
                         0.5
                         1)})
