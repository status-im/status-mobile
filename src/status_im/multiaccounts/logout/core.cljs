(ns status-im.multiaccounts.logout.core
  (:require [re-frame.core :as re-frame]
            [status-im.chaos-mode.core :as chaos-mode]
            [status-im.i18n :as i18n]
            [status-im.init.core :as init]
            [status-im.native-module.core :as status]
            [status-im.transport.core :as transport]
            [status-im.utils.fx :as fx]
            [clojure.string :as string]))

(fx/defn logout
  {:events [:logout]}
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {::logout nil
             ;;TODO sort out this mess with lower case addresses
             :keychain/clear-user-password (string/lower-case (get-in db [:multiaccount :address]))
             ::init/open-multiaccounts #(re-frame/dispatch [::init/initialize-multiaccounts %])}
            (transport/stop-whisper)
            (chaos-mode/stop-checking)
            (init/initialize-app-db)))

(fx/defn show-logout-confirmation [_]
  {:ui/show-confirmation
   {:title               (i18n/label :t/logout-title)
    :content             (i18n/label :t/logout-are-you-sure)
    :confirm-button-text (i18n/label :t/logout)
    :on-accept           #(re-frame/dispatch [:multiaccounts.logout.ui/logout-confirmed])
    :on-cancel           nil}})

(re-frame/reg-fx
 ::logout
 (fn []
   (status/logout)))
