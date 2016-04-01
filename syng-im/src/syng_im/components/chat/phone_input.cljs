(ns syng-im.components.chat.phone-input
  (:require
   [re-frame.core :refer [subscribe dispatch dispatch-sync]]
   [syng-im.components.chat.simple-command-input :refer [simple-command-input-view]]
   [syng-im.utils.utils :refer [log toast http-post]]
   [syng-im.utils.logging :as log]))

(defn phone-input-view [command]
  [simple-command-input-view command "phone-pad"])
