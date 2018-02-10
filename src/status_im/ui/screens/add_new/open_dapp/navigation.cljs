(ns status-im.ui.screens.add-new.open-dapp.navigation
  (:require [status-im.ui.screens.navigation :as navigation]))

(defmethod navigation/preload-data! :dapp-description
  [db [_ _ {:keys [dapp]}]]
  (assoc db :new/open-dapp dapp))
