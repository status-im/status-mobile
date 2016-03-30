(ns syng-im.handlers.commands
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.db :as db]
            [syng-im.utils.utils :refer [log on-error http-post]]
            [syng-im.utils.logging :as log]))

(defn set-input-command [db command]
  (assoc-in db db/input-command-path command))
