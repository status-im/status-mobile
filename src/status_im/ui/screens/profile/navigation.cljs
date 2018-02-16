(ns status-im.ui.screens.profile.navigation
  (:require [status-im.ui.screens.navigation :as navigation]))

;;TODO(goranjovic) - replace this with an atomic navigation event that calls functions, not dispatch
;; possibly use the generic event, see https://github.com/status-im/status-react/issues/2987
(defmethod navigation/preload-data! :profile-qr-viewer
  [{:accounts/keys [account] :as db} [_ _ {:keys [contact source value]}]]
  (update db :qr-modal #(merge % {:contact (or contact
                                               (:address account))
                                  :source  source
                                  :value   value})))
