(ns status-im.ui.screens.network-settings.navigation
  (:require [status-im.ui.screens.navigation :as navigation]))

(defmethod navigation/preload-data! :network-details
  [db [_ _ network]]
  (assoc db :networks/selected-network network))