(ns status-im.ui.screens.profile.navigation
  (:require [status-im.ui.screens.navigation :as navigation]))

(defmethod navigation/preload-data! :qr-code-view
  [{:keys [current-account-id] :as db} [_ _ {:keys [contact qr-source amount?]}]]
  (assoc db :qr-modal {:contact   (or contact
                                      (get-in db [:accounts current-account-id]))
                       :qr-source qr-source
                       :amount?   amount?}))
