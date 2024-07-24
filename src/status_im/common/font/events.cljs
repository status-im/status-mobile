(ns status-im.common.font.events
  (:require
    [clojure.string :as string]
    status-im.common.font.effects
    [utils.re-frame :as rf]))

(rf/defn init-abs-root-path
  {:events [:font/init-font-file-for-initials-avatar]}
  [{:keys [db]} initials-avatar-font-file]
  (when-not (string/blank? initials-avatar-font-file)
    {:db (assoc db :initials-avatar-font-file initials-avatar-font-file)}))
