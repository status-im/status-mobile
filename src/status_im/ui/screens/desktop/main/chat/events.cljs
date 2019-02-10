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

(handlers/register-handler-fx
 :desktop/insert-emoji
 (fn [{{:keys [desktop current-chat-id chats] :as db} :db} [_ emoji]]
   (let [inp-txt (get-in chats [current-chat-id :input-text])
         input (:input-ref desktop)
         sel (:input-selection desktop)
         new-text (if sel (str (subs inp-txt 0 sel) emoji (subs inp-txt sel)) (str inp-txt emoji))]
     (when input (.setNativeProps input (clj->js {:text new-text})))
     {:db (assoc-in db [:chats current-chat-id :input-text] new-text)})))