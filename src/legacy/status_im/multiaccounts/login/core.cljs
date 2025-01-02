(ns legacy.status-im.multiaccounts.login.core
  (:require
    [legacy.status-im.ui.components.react :as react]
    [legacy.status-im.utils.deprecated-types :as types]
    [native-module.core :as native-module]
    [re-frame.core :as re-frame]
    [react-native.platform :as platform]
    [utils.re-frame :as rf]
    [utils.security.core :as security]))

(re-frame/reg-fx
 ::export-db
 (fn [[key-uid account-data hashed-password callback]]
   (native-module/export-db key-uid account-data hashed-password callback)))

(re-frame/reg-fx
 ::import-db
 (fn [[key-uid account-data hashed-password]]
   (native-module/import-db key-uid account-data hashed-password)))

(rf/defn export-db-submitted
  {:events [:multiaccounts.login.ui/export-db-submitted]}
  [{:keys [db]}]
  (let [{:keys [key-uid password name]} (:profile/login db)]
    {::export-db [key-uid
                  (types/clj->json {:name    name
                                    :key-uid key-uid})
                  (native-module/sha3 (security/safe-unmask-data password))
                  (fn [path]
                    (when platform/ios?
                      (let [uri (str "file://" path)]
                        (.share ^js react/sharing
                                (clj->js {:title "Unencrypted database"
                                          :url   uri})))))]}))

(rf/defn import-db-submitted
  {:events [:multiaccounts.login.ui/import-db-submitted]}
  [{:keys [db]}]
  (let [{:keys [key-uid password name]} (:profile/login db)]
    {::import-db [key-uid
                  (types/clj->json {:name    name
                                    :key-uid key-uid})
                  (native-module/sha3 (security/safe-unmask-data password))]}))
