(ns status-im.multiaccounts.biometric.core
  (:require [quo.design-system.colors :as colors]
            [re-frame.core :as re-frame]
            [utils.i18n :as i18n]
            [native-module.core :as native-module]
            [status-im.popover.core :as popover]
            [utils.re-frame :as rf]
            [status-im.utils.keychain.core :as keychain]
            [status-im.utils.platform :as platform]
            [taoensso.timbre :as log]
            [react-native.touch-id :as touch-id]))

(defn get-label
  [supported-biometric-auth]
  (case supported-biometric-auth
    :fingerprint (i18n/label :t/biometric-fingerprint)
    :FaceID      (i18n/label :t/biometric-faceid)
    (i18n/label :t/biometric-touchid)))






(rf/defn update-biometric
  [{db :db :as cofx} biometric-auth?]
  (let [key-uid (or (get-in db [:profile/profile :key-uid])
                    (get-in db [:profile/login :key-uid]))]
    (rf/merge cofx
              #_(keychain/save-auth-method
                 key-uid
                 (if biometric-auth?
                   keychain/auth-method-biometric
                   keychain/auth-method-none))
              #(when-not biometric-auth?
                 {:keychain/clear-user-password key-uid}))))

(rf/defn biometric-auth-switched
  {:events [:multiaccounts.ui/biometric-auth-switched]}
  [cofx biometric-auth?]
  (if biometric-auth?
    (authenticate
     cofx
     #(re-frame/dispatch [:biometric-init-done %])
     {})
    (update-biometric cofx false)))



(rf/defn biometric-init-done
  {:events [:biometric-init-done]}
  [cofx {:keys [bioauth-success bioauth-message bioauth-code]}]
  (if bioauth-success
    (popover/show-popover cofx {:view :enable-biometric})
    (show-message cofx bioauth-message bioauth-code)))

(rf/defn enable
  {:events [:biometric/enable]}
  [cofx]
  (rf/merge
   cofx
   (popover/hide-popover)
   (authenticate #(re-frame/dispatch [:biometric/setup-done %]) {})))

(rf/defn disable
  {:events [:biometric/disable]}
  [{:keys [db] :as cofx}]
  (rf/merge
   cofx
   {:db (-> db
            (assoc :auth-method keychain/auth-method-none)
            (assoc-in [:profile/login :save-password?] false))}
   (popover/hide-popover)))

(rf/defn setup-done
  {:events [:biometric/setup-done]}
  [{:keys [db] :as cofx} {:keys [bioauth-success bioauth-message bioauth-code]}]
  (log/debug "[biometric] setup-done"
             "bioauth-success" bioauth-success
             "bioauth-message" bioauth-message
             "bioauth-code"    bioauth-code)
  (if bioauth-success
    {:db (assoc db :auth-method keychain/auth-method-biometric-prepare)}
    (show-message cofx bioauth-message bioauth-code)))
