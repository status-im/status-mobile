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
            [status-im2.contexts.profile.events :as profile.events]
            status-im2.common.theme.events
            [status-im2.navigation.events :as navigation]
            [native-module.core :as native-module]))

(re-frame/reg-cofx :now (fn [coeffects _] (assoc coeffects :now (datetime/timestamp))))

(rf/defn initialize-app-db
  "Initialize db to initial state"
  [{{:keys         [keycard supported-biometric-auth goto-key-storage?]
     :network/keys [type]
     :keycard/keys [banner-hidden]}
    :db}]
  {:db (assoc db/app-db
              :network/type             type
              :keycard/banner-hidden    banner-hidden
              :keycard                  (dissoc keycard :secrets :pin :application-info)
              :supported-biometric-auth supported-biometric-auth
              :goto-key-storage?        goto-key-storage?)})

(re-frame/reg-fx
 :profile/get-profiles-overview
 (fn [callback]
   (native-module/open-accounts callback)))

(rf/defn get-profiles-overview-success
  {:events [:profile/get-profiles-overview-success]}
  [cofx profiles-overview]
  (if (seq profiles-overview)
    (profile.events/init-profiles-overview cofx profiles-overview)
    (navigation/init-root cofx :intro)))

(rf/defn start-app
  {:events [:app-started]}
  [cofx]
  (rf/merge cofx
            {:theme/init-theme                       nil
             :biometric/get-supported-biometric-auth nil
             :network/listen-to-network-info         nil
             :keycard/register-card-events           nil
             :keycard/check-nfc-support              nil
             :keycard/check-nfc-enabled              nil
             :keycard/retrieve-pairings              nil
             :profile/get-profiles-overview          #(re-frame/dispatch
                                                       [:profile/get-profiles-overview-success %])}
            (initialize-app-db)))
