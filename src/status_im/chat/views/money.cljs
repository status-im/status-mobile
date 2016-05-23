(ns status-im.chat.views.money
  (:require
    [status-im.chat.views.command :refer [simple-command-input-view]]))

(defn money-input-view [command input]
  [simple-command-input-view command input
   {:keyboardType :numeric}])
