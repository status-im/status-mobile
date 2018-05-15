(ns status-im.ui.screens.browser.navigation
  (:require [status-im.ui.screens.navigation :as navigation]))

(defmethod navigation/preload-data! :browser
  [db [_ _ {:browser/keys [browser-id new?]}]]
  (assoc db :browser/options {:browser-id browser-id
                              :new?       new?}))
