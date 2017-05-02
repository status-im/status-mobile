(ns status-im.models.commands
  (:require [status-im.db :as db]
            [tailrecursion.priority-map :refer [priority-map-by]]
            [status-im.bots.constants :as bots-constants]))

(defn parse-command-message-content
  [commands global-commands content]
  (if (map? content)
    (let [{:keys [command bot]} content]
      (if (and bot (not (bots-constants/mailman-bot? bot)))
        (update content :command #((keyword bot) global-commands))
        (update content :command #((keyword command) commands))))
    content))

(defn parse-command-request [commands content]
  (update content :command #((keyword %) commands)))
