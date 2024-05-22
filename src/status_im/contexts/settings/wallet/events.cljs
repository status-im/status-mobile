(ns status-im.contexts.settings.wallet.events
  (:require
    [native-module.core :as native-module]
    [status-im.contexts.syncing.events :as syncing-events]
    [status-im.contexts.syncing.utils :as sync-utils]
    [taoensso.timbre :as log]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.security.core :as security]
    [utils.transforms :as transforms]))

(defn- update-keypair
  [keypairs key-uid update-fn]
  (mapcat (fn [keypair]
            (if (= (keypair :key-uid) key-uid)
              (if-let [updated (update-fn keypair)]
                [updated]
                [])
              [keypair]))
   keypairs))

(rf/reg-event-fx
 :wallet/rename-keypair-success
 (fn [{:keys [db]} [key-uid name]]
   {:db (update-in db
                   [:wallet :keypairs]
                   #(update-keypair % key-uid (fn [keypair] (assoc keypair :name name))))
    :fx [[:dispatch [:navigate-back]]
         [:dispatch
          [:toasts/upsert
           {:type  :positive
            :theme :dark
            :text  (i18n/label :t/key-pair-name-updated)}]]]}))

(defn rename-keypair
  [_ [{:keys [key-uid keypair-name]}]]
  {:fx [[:json-rpc/call
         [{:method     "accounts_updateKeypairName"
           :params     [key-uid keypair-name]
           :on-success [:wallet/rename-keypair-success key-uid keypair-name]
           :on-error   #(log/info "failed to rename keypair " %)}]]]})

(rf/reg-event-fx :wallet/rename-keypair rename-keypair)

(defn get-key-pair-export-connection
  [{:keys [db]} [{:keys [sha3-pwd keypair-key-uid callback]}]]
  (let [key-uid           (get-in db [:profile/profile :key-uid])
        config-map        (transforms/clj->json {:senderConfig {:loggedInKeyUid key-uid
                                                                :keystorePath ""
                                                                :keypairsToExport [keypair-key-uid]
                                                                :password (security/safe-unmask-data
                                                                           sha3-pwd)}
                                                 :serverConfig {:timeout 0}})
        handle-connection (fn [response]
                            (when (sync-utils/valid-connection-string? response)
                              (callback response)
                              (rf/dispatch [:hide-bottom-sheet])))]
    (native-module/get-connection-string-for-exporting-keypairs-keystores
     config-map
     handle-connection)))

(rf/reg-event-fx :wallet/get-key-pair-export-connection get-key-pair-export-connection)

(rf/reg-event-fx
 :wallet/remove-keypair-success
 (fn [{:keys [db]} [key-uid]]
   {:db (update-in db
                   [:wallet :keypairs]
                   #(update-keypair % key-uid (fn [_] nil)))
    :fx [[:dispatch [:hide-bottom-sheet]]
         [:dispatch
          [:toasts/upsert
           {:type  :positive
            :theme :dark
            :text  (i18n/label :t/key-pair-removed)}]]]}))

(defn remove-keypair
  [_ [key-uid]]
  {:fx [[:json-rpc/call
         [{:method     "accounts_deleteKeypair"
           :params     [key-uid]
           :on-success [:wallet/remove-keypair-success key-uid]
           :on-error   #(log/info "failed to remove keypair " %)}]]]})

(rf/reg-event-fx :wallet/remove-keypair remove-keypair)

(defn extract-keypair-name
  [db key-uids-set]
  (when (= (count key-uids-set) 1)
    (let [key-uid  (first key-uids-set)
          keypairs (get-in db [:wallet :keypairs])]
      (->> (filter #(= (:key-uid %) key-uid) keypairs)
           first
           :name))))

(defn update-accounts
  [accounts key-uids-set]
  (into {}
        (map (fn [[k account]]
               (if (and (contains? key-uids-set (:key-uid account))
                        (= (:operable account) :no))
                 [k (assoc account :operable :fully)]
                 [k account]))
             accounts)))

(defn update-keypairs
  [keypairs key-uids-set]
  (map (fn [keypair]
         (if (contains? key-uids-set (:key-uid keypair))
           (update keypair
                   :accounts
                   (fn [accounts]
                     (map (fn [account]
                            (if (and (contains? key-uids-set (:key-uid account))
                                     (= (:operable account) ":no"))
                              (assoc account :operable ":fully")
                              account))
                          accounts)))
           keypair))
       keypairs))

(rf/reg-event-fx :wallet/make-key-pairs-fully-operable
 (fn [{:keys [db]} [key-uids-to-update]]
   (let [key-uids-set (set key-uids-to-update)
         keypair-name (extract-keypair-name db key-uids-set)]
     {:db (-> db
              (update-in [:wallet :accounts] update-accounts key-uids-set)
              (update-in [:wallet :keypairs] update-keypairs key-uids-set))
      :fx [[:dispatch
            [:toasts/upsert
             {:type  :positive
              :theme :dark
              :text  (if (= (count key-uids-to-update) 1)
                       (i18n/label :t/key-pair-imported-successfully {:name keypair-name})
                       (i18n/label :t/key-pairs-successfully-imported
                                   {:count (count key-uids-to-update)}))}]]]})))

(defn- input-connection-string-for-importing-keypairs-keystores-callback
  [res keypairs-key-uids]
  (log/info "[local-pairing] input-connection-string-for-importing-keypairs-keystores callback"
            {:response res
             :event    :settings/input-connection-string-for-importing-keypairs-keystores-callback})
  (let [error (when (syncing-events/extract-error res)
                (str "generic-error: " res))]
    (when-not (some? error)
      (rf/dispatch [:wallet/make-key-pairs-fully-operable keypairs-key-uids]))
    (when (some? error)
      (rf/dispatch [:toasts/upsert
                    {:type :negative
                     :text error}]))))

(defn connection-string-for-key-pair-import
  [{:keys [db]} [{:keys [sha3-pwd keypairs-key-uids connection-string]}]]
  (let [key-uid    (get-in db [:profile/profile :key-uid])
        config-map (.stringify js/JSON
                               (clj->js
                                {:receiverConfig
                                 {:loggedInKeyUid   key-uid
                                  :keystorePath     ""
                                  :password         (security/safe-unmask-data
                                                     sha3-pwd)
                                  :keypairsToImport keypairs-key-uids}}))]
    (native-module/input-connection-string-for-importing-keypairs-keystores
     connection-string
     config-map
     #(input-connection-string-for-importing-keypairs-keystores-callback % keypairs-key-uids))))

(rf/reg-event-fx :wallet/connection-string-for-key-pair-import connection-string-for-key-pair-import)
