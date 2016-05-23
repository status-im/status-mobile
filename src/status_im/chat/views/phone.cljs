(ns status-im.chat.views.phone
  (:require
   [status-im.chat.views.command
    :refer [simple-command-input-view]]
   [status-im.utils.phone-number :refer [valid-mobile-number?]]))

(defn phone-input-view [command input]
  [simple-command-input-view command input {:keyboardType :phone-pad}
   :validator valid-mobile-number?])
