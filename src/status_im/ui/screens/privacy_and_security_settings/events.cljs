(ns status-im.ui.screens.privacy-and-security-settings.events
  (:require [status-im.utils.fx :as fx]
            [re-frame.core :as re-frame]
            [status-im.utils.security :as security]
            [status-im.native-module.core :as status]
            [status-im.ethereum.core :as ethereum]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]
            [clojure.string :as clojure.string]
            [status-im.i18n.i18n :as i18n]))

(defn safe-blank? [s]
  (or (not s)
      (clojure.string/blank? s)))

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
                 (callback error nil)))))))))))

(fx/defn delete-profile
  {:events [::delete-profile]}
  [{:keys [db] :as cofx} masked-password]
  (log/info "[delete-profile] delete")
  (let [{:keys [key-uid wallet-root-address]} (:multiaccount db)]
    {:db (dissoc db :delete-profile/error)
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

(fx/defn on-delete-profile-success
  {:events [::on-delete-profile-success]}
  [cofx]
  (log/info "[delete-profile] on-success")
  {:utils/show-popup
   {:title      (i18n/label :t/profile-deleted-title)
    :content    (i18n/label :t/profile-deleted-content)
    :on-dismiss #(re-frame/dispatch [:logout])}})

(fx/defn on-delete-profile-failure
  {:events [::on-delete-profile-failure]}
  [{:keys [db]} error]
  (log/info "[delete-profile] on-failure" error)
  {:db (assoc db :delete-profile/error error)})
