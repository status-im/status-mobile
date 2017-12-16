(ns status-im.ui.screens.profile.navigation
  (:require [status-im.ui.screens.navigation :as navigation]))

(defmethod navigation/preload-data! :qr-code-view
  [{:accounts/keys [current-account-id] :as db} [_ _ {:keys [contact qr-source qr-value]}]]
  (update db :qr-modal #(merge % {:contact   (or contact
                                                 (get-in db [:accounts/accounts current-account-id]))
                                  :qr-source qr-source
                                  :qr-value  qr-value})))
