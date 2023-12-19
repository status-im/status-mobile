(ns legacy.status-im.ui.screens.privacy-and-security-settings.events
  (:require
    [clojure.string :as string]
    [legacy.status-im.utils.deprecated-types :as types]
    [native-module.core :as native-module]
    [re-frame.core :as re-frame]
    [taoensso.timbre :as log]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
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
             native-module/sha3)]
     (native-module/verify
      address
      hashed-password
      (fn [result]
        (let [{:keys [error]} (types/json->clj result)]
          (log/info "[delete-profile] verify-password" result error)
          (if-not (safe-blank? error)
            (callback :wrong-password nil)
            (native-module/delete-multiaccount
             key-uid
             (fn [result]
               (let [{:keys [error]} (types/json->clj result)]
                 (callback error nil)))))))))))

(rf/defn delete-profile
  {:events [::delete-profile]}
  [{:keys [db] :as cofx} masked-password]
  (log/info "[delete-profile] delete")
  (let [{:keys [key-uid wallet-root-address]} (:profile/profile db)]
    {:db (dissoc db :delete-profile/error)
     ::delete-profile
     {:masked-password masked-password
      :key-uid key-uid
      :address wallet-root-address
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
  {:effects.utils/show-popup
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
