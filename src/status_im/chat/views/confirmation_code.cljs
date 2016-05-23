(ns status-im.chat.views.confirmation-code
  (:require
    [status-im.chat.views.command :refer [simple-command-input-view]]))

(defn confirmation-code-input-view [command input]
  [simple-command-input-view command input {:keyboardType :numeric}])
