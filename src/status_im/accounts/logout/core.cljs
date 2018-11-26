(ns status-im.accounts.logout.core
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.transport.core :as transport]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.fx :as fx]
            [status-im.models.transactions :as transactions]))

(fx/defn logout
  [{:keys [db] :as cofx}]
  (let [{:transport/keys [chats]} db]
    (fx/merge cofx
              {:keychain/clear-user-password (get-in db [:account/account :address])
               :dev-server/stop              nil
               :keychain/get-encryption-key [:init.callback/get-encryption-key-success]}
              (transactions/stop-sync)
              (navigation/navigate-to-clean :login {})
              (transport/stop-whisper))))

(fx/defn show-logout-confirmation [_]
  {:ui/show-confirmation
   {:title               (i18n/label :t/logout-title)
    :content             (i18n/label :t/logout-are-you-sure)
    :confirm-button-text (i18n/label :t/logout)
    :on-accept           #(re-frame/dispatch [:accounts.logout.ui/logout-confirmed])
    :on-cancel           nil}})
