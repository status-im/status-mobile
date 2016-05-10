(ns syng-im.components.chat.input.money
  (:require
    [syng-im.components.chat.input.simple-command
     :refer [simple-command-input-view]]
    [syng-im.components.chat.input.input-styles :as st]))

(defn money-input-view [command]
  [simple-command-input-view command
   {:keyboardType :numeric}])
