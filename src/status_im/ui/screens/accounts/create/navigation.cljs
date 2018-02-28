(ns status-im.ui.screens.accounts.create.navigation
  (:require [status-im.ui.screens.navigation :as nav]))

(defmethod nav/preload-data! :create-account
  [db]
  (update db :accounts/create
          #(-> %
               (assoc :step :enter-password)
               (dissoc :password :password-confirm :name :error))))