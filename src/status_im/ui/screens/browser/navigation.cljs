(ns status-im.ui.screens.browser.navigation
  (:require [status-im.ui.screens.navigation :as navigation]))

(defmethod navigation/preload-data! :browser
  [db [_ _ options]]
  (assoc db :browser/options options))
