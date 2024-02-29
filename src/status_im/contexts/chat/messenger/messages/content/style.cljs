(ns status-im.contexts.chat.messenger.messages.content.style
  (:require
    [quo.foundations.colors :as colors]))

(def ^:private message-padding-scaling-ratio 4.5)

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
  [{:keys [outgoing outgoing-status six-reactions? window-scale small-screen?]}]
  {:border-radius      16
   :padding-horizontal 8
   :padding-top        4
   :padding-bottom     (if (or small-screen?
                               (and
                                (> 3 window-scale)
                                six-reactions?))
                         (* message-padding-scaling-ratio window-scale)
                         4)
   :opacity            (if (and outgoing (= outgoing-status :sending))
                         0.5
                         1)})

(def drawer-message-container
  {:padding-horizontal 4
   :padding-vertical   8})
