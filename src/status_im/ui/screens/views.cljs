(ns status-im.ui.screens.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :refer [dispatch]]
            [status-im.ui.components.react :refer [view modal]]
            [status-im.ui.components.styles :as common-styles]

            [status-im.chat.screen :refer [chat]]
            [status-im.chat.new-chat.view :refer [new-chat]]
            [status-im.chat.new-public-chat.view :refer [new-public-chat]]
            [status-im.ui.screens.network-settings.views :refer [network-settings]]
            [status-im.ui.screens.accounts.login.views :refer [login]]
            [status-im.ui.screens.profile.qr-code.views :refer [qr-code-view]]
            [status-im.ui.screens.profile.views :refer [profile]]))

(defn validate-current-view
  [current-view signed-up?]
  (if (or (contains? #{:login :chat} current-view)
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
                          :new-chat new-chat
                          :new-public-chat new-public-chat
                          :chat chat
                          :network-settings network-settings
                          :qr-code-view qr-code-view
                          (throw (str "Unknown view: " current-view))
                          )]
          [view common-styles/flex
           [component]]
          )
        )))

    )

