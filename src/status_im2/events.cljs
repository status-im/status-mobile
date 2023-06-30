(ns status-im2.events
  (:require [re-frame.core :as re-frame]
            [status-im2.common.json-rpc.events]
            [status-im2.common.toasts.events]
            [status-im2.contexts.add-new-contact.events]
            status-im2.contexts.onboarding.events
            [status-im.bottom-sheet.events]
            [status-im2.db :as db]
            [utils.re-frame :as rf]
            [utils.datetime :as datetime]
            status-im2.contexts.shell.share.events
            status-im2.contexts.syncing.events
            status-im2.contexts.chat.events
            status-im2.common.password-authentication.events
            status-im2.contexts.communities.overview.events
            status-im2.contexts.chat.photo-selector.events
            status-im2.common.theme.events))

(re-frame/reg-cofx :now (fn [coeffects _] (assoc coeffects :now (datetime/timestamp))))

(rf/defn initialize-app-db
  [{{:keys         [keycard biometric/supported-type goto-key-storage?]
     :network/keys [type]
     :keycard/keys [banner-hidden]}
    :db}]
  {:db (assoc db/app-db
         :network/type type
         :keycard/banner-hidden banner-hidden
         :keycard (dissoc keycard :secrets :pin :application-info)
         :biometric/supported-type supported-type
         :goto-key-storage? goto-key-storage?)})

(rf/defn start-app
  {:events [:app-started]}
  [cofx]
  (rf/merge cofx
            {:theme/init-theme                       nil
             :network/listen-to-network-info         nil
             :keycard/register-card-events           nil
             :keycard/check-nfc-support              nil
             :keycard/check-nfc-enabled              nil
             :keycard/retrieve-pairings              nil
             :biometric/get-supported-biometric-type nil
             :profile/get-profiles-overview          #(rf/dispatch
                                                       [:profile/get-profiles-overview-success %])}
            (initialize-app-db)))
