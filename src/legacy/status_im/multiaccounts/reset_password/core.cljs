(ns legacy.status-im.multiaccounts.reset-password.core
  (:require
    [clojure.string :as string]
    [legacy.status-im.popover.core :as popover]
    [legacy.status-im.utils.deprecated-types :as types]
    [native-module.core :as native-module]
    [re-frame.core :as re-frame]
    [utils.re-frame :as rf]
    [utils.security.core :as security]))

(rf/defn on-input-change
  {:events [::handle-input-change]}
  [{:keys [db]} input-id value]
  (let [new-password (get-in db [:multiaccount/reset-password-form-vals :new-password])
        error        (when (and (= input-id :confirm-new-password)
                                (pos? (count new-password))
                                (pos? (count value))
                                (not= value new-password))
                       :t/password-mismatch)]
    {:db (-> db
             (assoc-in [:multiaccount/reset-password-form-vals input-id] value)
             (assoc-in [:multiaccount/reset-password-errors input-id] error))}))

(rf/defn clear-form-vals
  {:events [::clear-form-vals]}
  [{:keys [db]}]
  {:db (dissoc db :multiaccount/reset-password-form-vals :multiaccount/reset-password-errors)})

(rf/defn set-current-password-error
  {:events [::handle-verification-error ::password-reset-error]}
  [{:keys [db]} error]
  {:db (assoc-in db [:multiaccount/reset-password-errors :current-password] error)})

(rf/defn password-reset-success
  {:events [::password-reset-success]}
  [{:keys [db] :as cofx}]
  (rf/merge cofx
            {:db (dissoc
                  db
                  :multiaccount/reset-password-form-vals
                  :multiaccount/reset-password-errors
                  :multiaccount/reset-password-next-enabled?
                  :multiaccount/resetting-password?)}))

(defn change-db-password-cb
  [res]
  (let [{:keys [error]} (types/json->clj res)]
    (if (not (string/blank? error))
      (re-frame/dispatch [::password-reset-error error])
      (re-frame/dispatch [::password-reset-success]))))

(re-frame/reg-fx
 ::change-db-password
 (fn [[key-uid {:keys [current-password new-password]}]]
   (native-module/reset-password
    key-uid
    (native-module/sha3 (security/safe-unmask-data current-password))
    (native-module/sha3 (security/safe-unmask-data new-password))
    change-db-password-cb)))

(rf/defn handle-verification-success
  {:events [::handle-verification-success]}
  [{:keys [db] :as cofx} form-vals]
  (let [{:keys [key-uid name]} (:profile/profile db)]
    (rf/merge cofx
              {::change-db-password [key-uid form-vals]
               :db                  (assoc db
                                           :multiaccount/resetting-password?
                                           true)}
              (popover/show-popover {:view             :password-reset-popover
                                     :prevent-closing? true}))))

(defn handle-verification
  [form-vals result]
  (let [{:keys [error]} (types/json->clj result)]
    (if (not (string/blank? error))
      (re-frame/dispatch [::handle-verification-error :t/wrong-password])
      (re-frame/dispatch [::handle-verification-success form-vals]))))

(re-frame/reg-fx
 ::validate-current-password-and-reset
 (fn [{:keys [address current-password] :as form-vals}]
   (let [hashed-pass (native-module/sha3 (security/safe-unmask-data current-password))]
     (native-module/verify address
                           hashed-pass
                           (partial handle-verification form-vals)))))

(rf/defn reset
  {:events [::reset]}
  [{:keys [db]} form-vals]
  {::validate-current-password-and-reset
   (assoc form-vals
          :address
          (get-in db [:profile/profile :wallet-root-address]))})
