(ns status-im.ui.screens.desktop.main.chat.events
  (:require [re-frame.core :as re-frame]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.handlers-macro :as handlers-macro]))

(defn show-profile-desktop [identity {:keys [db] :as cofx}]
  (handlers-macro/merge-fx
   cofx
   {:db (assoc db :contacts/identity identity)}
   (navigation/navigate-to-cofx :chat-profile nil)))

(handlers/register-handler-fx
 :show-profile-desktop
 [re-frame/trim-v]
 (fn [cofx [identity]]
   (show-profile-desktop identity cofx)))