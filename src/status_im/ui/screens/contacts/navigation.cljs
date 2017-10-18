(ns status-im.ui.screens.contacts.navigation
  (:require [status-im.ui.screens.navigation :as nav]))

(defmethod nav/preload-data! :contact-list
  [db [_ _ click-handler]]
  (-> db
      (assoc-in [:toolbar-search :show] nil)
      (assoc-in [:contacts/list-ui-props :edit?] false)
      (assoc-in [:contacts/ui-props :edit?] false)
      (assoc :contacts/click-handler click-handler)))