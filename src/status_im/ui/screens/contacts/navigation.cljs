(ns status-im.ui.screens.contacts.navigation
  (:require [status-im.ui.screens.navigation :as nav]))

(defmethod nav/preload-data! :contact-list
  [db [_ _ click-handler]]
  (-> db
      (assoc-in [:contacts/list-ui-props :edit?] false)
      (assoc-in [:contacts/ui-props :edit?] false)
      (assoc :contacts/click-handler click-handler)))

(defmethod nav/preload-data! :contact-list-modal
  [db [_ _ {:keys [handler action params]}]]
  (assoc db :contacts/click-handler handler
            :contacts/click-action action
            :contacts/click-params params))