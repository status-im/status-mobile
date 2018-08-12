(ns status-im.ui.screens.accounts.recover.navigation
  (:require [status-im.ui.screens.navigation :as nav]))

(defmethod nav/preload-data! :recover
  [db]
  (update db :accounts/recover dissoc :password :passphrase :processing?))