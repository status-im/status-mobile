(ns syng-im.components.chat.input.confirmation-code
  (:require
   [re-frame.core :refer [subscribe dispatch dispatch-sync]]
   [syng-im.components.chat.input.simple-command :refer [simple-command-input-view]]
   [syng-im.utils.utils :refer [log toast http-post]]
   [syng-im.utils.logging :as log]))

(defn confirmation-code-input-view [command]
  [simple-command-input-view command {:keyboardType "numeric"}])
