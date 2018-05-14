(ns status-im.ui.screens.browser.navigation
  (:require [status-im.ui.screens.navigation :as navigation]))

(defmethod navigation/preload-data! :browser
  [db [_ _ {:keys [browser/browser-id browser/new?]}]]
  (assoc db :browser/options {:browser-id browser-id
                              :new?       new?}))
