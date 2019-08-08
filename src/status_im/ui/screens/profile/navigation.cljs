(ns status-im.ui.screens.profile.navigation
  (:require [status-im.ui.screens.navigation :as navigation]))

(defmethod navigation/unload-data! :my-profile
  [db]
  (dissoc db :my-profile/editing?))
