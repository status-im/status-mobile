(ns status-im.accounts.logout.core
  (:require [re-frame.core :as re-frame]
            [status-im.chaos-mode.core :as chaos-mode]
            [status-im.i18n :as i18n]
            [status-im.init.core :as init]
            [status-im.node.core :as node]
            [status-im.transport.core :as transport]
            [status-im.utils.fx :as fx]))

(fx/defn logout
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:keychain/clear-user-password (get-in db [:account/account :address])
             :dispatch [:accounts.logout/filters-removed]
             :dev-server/stop              nil}
            (transport/stop-whisper)
            (chaos-mode/stop-checking)))

(fx/defn leave-account
  [cofx]
  (fx/merge
   cofx
   (init/initialize-app-db)
   (init/load-accounts-and-initialize-views)
   (node/stop)))

(fx/defn show-logout-confirmation [_]
  {:ui/show-confirmation
   {:title               (i18n/label :t/logout-title)
    :content             (i18n/label :t/logout-are-you-sure)
    :confirm-button-text (i18n/label :t/logout)
    :on-accept           #(re-frame/dispatch [:accounts.logout.ui/logout-confirmed])
    :on-cancel           nil}})
