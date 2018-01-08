(ns status-im.ui.screens.dev.events
  (:require [status-im.utils.handlers :refer [register-handler-db register-handler-fx]]))

(register-handler-db
  :toggle-dev-setting
  (fn [db [_ id]]
    (update-in db [:dev/settings id] not)))

(register-handler-db
  :set-dev-setting
  (fn [db [_ id value]]
    (assoc-in db [:dev/settings id] value)))
