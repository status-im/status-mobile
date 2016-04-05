(ns syng-im.handlers.suggestions
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.db :as db]
            [syng-im.models.chat :refer [current-chat-id]]
            [syng-im.models.commands :refer [commands suggestions]]
            [syng-im.utils.utils :refer [log on-error http-post]]
            [syng-im.utils.logging :as log]))

(defn get-suggestions [text]
  (if (= (get text 0) "!")
    ;; TODO change 'commands' to 'suggestions'
    (filterv #(.startsWith (:text %) text) commands)
    []))
