(ns syng-im.components.chat.input.password
  (:require
   [syng-im.components.chat.input.simple-command
    :refer [simple-command-input-view]]))

(defn password-input-view [command]
  [simple-command-input-view command {:secureTextEntry true}])
