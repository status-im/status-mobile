(ns status-im.accounts.logout.core
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.init.core :as init]
            [status-im.transport.core :as transport]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.handlers-macro :as handlers-macro]))

(defn logout
  [{:keys [db] :as cofx}]
  (let [{:transport/keys [chats]} db]
    (handlers-macro/merge-fx
     cofx
     {:keychain/clear-user-password (get-in db [:account/account :address])
      :dev-server/stop              nil}
     (navigation/navigate-to-clean :login)
     (transport/stop-whisper)
     (init/initialize-keychain))))

(defn show-logout-confirmation []
  {:ui/show-confirmation
   {:title               (i18n/label :t/logout-title)
    :content             (i18n/label :t/logout-are-you-sure)
    :confirm-button-text (i18n/label :t/logout)
    :on-accept           #(re-frame/dispatch [:accounts.logout.ui/logout-confirmed])
    :on-cancel           nil}})
