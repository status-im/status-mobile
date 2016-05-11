(ns syng-im.components.chat.input.phone
  (:require
   [syng-im.components.chat.input.simple-command
    :refer [simple-command-input-view]]
   [syng-im.utils.phone-number :refer [valid-mobile-number?]]))

(defn phone-input-view [command]
  [simple-command-input-view command {:keyboardType :phone-pad}
   :validator valid-mobile-number?])
