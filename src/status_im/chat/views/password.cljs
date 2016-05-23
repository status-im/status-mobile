(ns status-im.chat.views.password
  (:require
   [status-im.chat.views.command
    :refer [simple-command-input-view]]))

(defn password-input-view [command input]
  [simple-command-input-view command input {:secureTextEntry true}])
