(ns status-im.ui.screens.browser.navigation
  (:require [status-im.ui.screens.navigation :as navigation]
            [status-im.models.browser-history :as browser-history]))

(defmethod navigation/preload-data! :browser
  [db [_ _ {:keys [browser/browser-id]}]]
  (let [dont-store (browser-history/dont-store-history-on-nav-change-if-history-exists db browser-id)]
    (assoc db :browser/options (assoc dont-store :browser-id browser-id))))
