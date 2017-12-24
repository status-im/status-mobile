(ns status-im.ui.screens.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :refer [dispatch]]
            [status-im.ui.components.react :refer [view modal]]
            [status-im.ui.components.styles :as common-styles]
            [status-im.ui.screens.accounts.login.views :refer [login]]
            [status-im.ui.screens.profile.views :refer [profile]]))

(defn validate-current-view
  [current-view signed-up?]
  (if (or (contains? #{:login} current-view)
          signed-up?)
    current-view
    current-view))

(defview main []
   (letsubs [signed-up? [:signed-up?]
             view-id [:get :view-id]
             modal-view [:get :modal]]
    (when view-id
      (let [
            current-view (validate-current-view view-id signed-up?)]
        (let [component (case current-view
                          :login login
                          :profile profile
                          (throw (str "Unknown view: " current-view))
                          )]
          [view common-styles/flex
           [component]]
          )
        )))

    )

