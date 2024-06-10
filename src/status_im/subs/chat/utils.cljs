(ns status-im.subs.chat.utils
  (:require [status-im.constants :as constants]))

(defn online?
  [visibility-status-type]
  (or (= visibility-status-type constants/visibility-status-automatic)
      (= visibility-status-type constants/visibility-status-always-online)))
