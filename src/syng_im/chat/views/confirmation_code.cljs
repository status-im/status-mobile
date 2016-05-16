(ns syng-im.chat.views.confirmation-code
  (:require
    [syng-im.chat.views.command :refer [simple-command-input-view]]))

(defn confirmation-code-input-view [command]
  [simple-command-input-view command {:keyboardType :numeric}])
