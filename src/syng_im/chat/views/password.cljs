(ns syng-im.chat.views.password
  (:require
   [syng-im.chat.views.command
    :refer [simple-command-input-view]]))

(defn password-input-view [command]
  [simple-command-input-view command {:secureTextEntry true}])
