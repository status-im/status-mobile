(ns syng-im.components.chat.input.phone
  (:require
   [syng-im.components.chat.input.simple-command
    :refer [simple-command-input-view]]))

(defn phone-input-view [command]
  [simple-command-input-view command {:keyboardType :phone-pad}])
