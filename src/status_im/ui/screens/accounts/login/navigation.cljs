(ns status-im.ui.screens.accounts.login.navigation
  (:require [status-im.ui.screens.navigation :as nav]))

(defmethod nav/preload-data! :login
  [db]
  (update db :accounts/login dissoc :error))
