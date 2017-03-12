(ns status-im.models.commands
  (:require [status-im.db :as db]
            [tailrecursion.priority-map :refer [priority-map-by]]))

(defn parse-command-message-content [commands content]
  (if (map? content)
    (update content :command #((keyword %) commands))
    content))

(defn parse-command-request [commands content]
  (update content :command #((keyword %) commands)))
