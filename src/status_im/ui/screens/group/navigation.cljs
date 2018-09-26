(ns status-im.ui.screens.group.navigation
  (:require [status-im.ui.screens.navigation :as nav]))

(defmethod nav/preload-data! :add-participants-toggle-list
  [db _]
  (assoc db :selected-participants #{}))
