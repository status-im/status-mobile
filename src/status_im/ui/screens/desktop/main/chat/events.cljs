(ns status-im.ui.screens.desktop.main.chat.events
  (:require [re-frame.core :as re-frame]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.fx :as fx]))

(defn show-profile-desktop [identity {:keys [db] :as cofx}]
  (fx/merge cofx
            {:db (assoc db :contacts/identity identity)}
            (navigation/navigate-to-cofx :chat-profile nil)))

(handlers/register-handler-fx
 :show-profile-desktop
 (fn [cofx [_ identity]]
   (show-profile-desktop identity cofx)))
