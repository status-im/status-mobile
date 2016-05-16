(ns syng-im.chat.views.phone
  (:require
   [syng-im.chat.views.command
    :refer [simple-command-input-view]]
   [syng-im.utils.phone-number :refer [valid-mobile-number?]]))

(defn phone-input-view [command]
  [simple-command-input-view command {:keyboardType :phone-pad}
   :validator valid-mobile-number?])
