(ns syng-im.components.chat.input.confirmation-code
  (:require
    [syng-im.components.chat.input.simple-command
     :refer [simple-command-input-view]]))

(defn confirmation-code-input-view [command]
  [simple-command-input-view command {:keyboardType :numeric}])
