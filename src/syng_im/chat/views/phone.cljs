(ns syng-im.chat.views.phone
  (:require
   [syng-im.chat.views.command
    :refer [simple-command-input-view]]))

(defn phone-input-view [command]
  [simple-command-input-view command {:keyboardType :phone-pad}])
