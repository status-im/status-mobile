(ns syng-im.chat.views.money
  (:require
    [syng-im.chat.views.command :refer [simple-command-input-view]]))

(defn money-input-view [command]
  [simple-command-input-view command
   {:keyboardType :numeric}])
