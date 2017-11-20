(ns status-im.ui.screens.discover.navigation
  (:require [status-im.ui.screens.navigation :as navigation]
            [status-im.data-store.discover :as discoveries]))

(defmethod navigation/preload-data! :discover
  [db _]
  (assoc-in db [:toolbar-search :show] nil))
