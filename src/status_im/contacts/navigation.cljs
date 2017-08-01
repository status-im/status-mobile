(ns status-im.contacts.navigation
  (:require [status-im.navigation.handlers :as nav]))

(defmethod nav/preload-data! :contact-list
  [db [_ _ click-handler]]
  (-> db
      (assoc-in [:toolbar-search :show] nil)
      (assoc-in [:contacts/list-ui-props :edit?] false)
      (assoc-in [:contacts/ui-props :edit?] false)
      (assoc :contacts/click-handler click-handler)))