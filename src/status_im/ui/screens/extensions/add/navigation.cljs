(ns status-im.ui.screens.extensions.add.navigation
  (:require [status-im.ui.screens.navigation :as navigation]))

(defmethod navigation/preload-data! :add-extension
  [db]
  (dissoc db :extension-url))
