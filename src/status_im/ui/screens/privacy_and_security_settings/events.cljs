(ns status-im.ui.screens.privacy-and-security-settings.events
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.ethereum.core :as ethereum]
            [i18n.i18n :as i18n]
            [status-im.native-module.core :as status]
            [utils.re-frame :as rf]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]
            [utils.security.core :as security]))

(defn safe-blank?
  [s]
  (or (not s)
      (string/blank? s)))

(re-frame/reg-fx
 ::delete-profile
 (fn [{:keys [address key-uid callback masked-password]}]
   (let [hashed-password
         (-> masked-password
             security/safe-unmask-data
             ethereum/sha3)]
     (status/verify
      address
      hashed-password
      (fn [result]
        (let [{:keys [error]} (types/json->clj result)]
          (log/info "[delete-profile] verify-password" result error)
          (if-not (safe-blank? error)
            (callback :wrong-password nil)
            (status/delete-multiaccount
             key-uid
             (fn [result]
               (let [{:keys [error]} (types/json->clj result)]
                 (callback error nil)))
             (fn [error-message]
               (log/debug "error while status/delete-multiaccount" error-message))
             ))))
      (fn [error-message]
        (log/debug "error while status/verify" error-message))))))

(rf/defn delete-profile
  {:events [::delete-profile]}
  [{:keys [db] :as cofx} masked-password]
  (log/info "[delete-profile] delete")
  (let [{:keys [key-uid wallet-root-address]} (:multiaccount db)]
    {:db              (dissoc db :delete-profile/error)
     ::delete-profile
     {:masked-password masked-password
      :key-uid         key-uid
      :address         wallet-root-address
      :callback
      (fn [error result]
        (log/info "[delete-profile] callback" error)
        (if (safe-blank? error)
          (re-frame/dispatch [::on-delete-profile-success result])
          (re-frame/dispatch [::on-delete-profile-failure error])))}}))

(rf/defn on-delete-profile-success
  {:events [::on-delete-profile-success]}
  [cofx]
  (log/info "[delete-profile] on-success")
  {:utils/show-popup
   {:title      (i18n/label :t/profile-deleted-title)
    :content    (i18n/label :t/profile-deleted-content)
    :on-dismiss #(re-frame/dispatch [:logout])}})

(rf/defn on-delete-profile-failure
  {:events [::on-delete-profile-failure]}
  [{:keys [db]} error]
  (log/info "[delete-profile] on-failure" error)
  {:db (assoc db :delete-profile/error error)})

(rf/defn keep-keys-on-keycard
  {:events [::keep-keys-on-keycard]}
  [{:keys [db] :as cofx} checked?]
  {:db (assoc-in db [:delete-profile/keep-keys-on-keycard?] checked?)})
