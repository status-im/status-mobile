(ns status-im.contexts.settings.wallet.events
  (:require
    [clojure.string :as string]
    [native-module.core :as native-module]
    [status-im.contexts.syncing.utils :as sync-utils]
    [taoensso.timbre :as log]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.security.core :as security]))

(rf/reg-event-fx
 :wallet/rename-keypair-success
 (fn [{:keys [db]} [key-uid name]]
   {:db (update-in db
                   [:wallet :keypairs]
                   (fn [keypairs]
                     (map (fn [keypair]
                            (if (= (keypair :key-uid) key-uid)
                              (assoc keypair :name name)
                              keypair))
                          keypairs)))
    :fx [[:dispatch [:navigate-back]]
         [:dispatch
          [:toasts/upsert
           {:type :positive
            :text (i18n/label :t/key-pair-name-updated)}]]]}))

(defn rename-keypair
  [_ [{:keys [key-uid keypair-name]}]]
  {:fx [[:json-rpc/call
         [{:method     "accounts_updateKeypairName"
           :params     [key-uid keypair-name]
           :on-success [:wallet/rename-keypair-success key-uid keypair-name]
           :on-error   #(log/info "failed to rename keypair " %)}]]]})

(rf/reg-event-fx :wallet/rename-keypair rename-keypair)

(defn key-pair-export-connection-string
  [{:keys [db]} [{:keys [sha3-pwd keypair-key-uid callback]}]]
  (let [error             (get-in db [:profile/login :error])
        handle-connection (fn [response]
                            (println response)
                            (when (sync-utils/valid-connection-string? response)
                              (callback response)
                              (rf/dispatch [:hide-bottom-sheet])))]
    (when-not (and error (string/blank? error))
      (let [key-uid    (get-in db [:profile/profile :key-uid])
            config-map (.stringify js/JSON
                                   (clj->js {:senderConfig {:loggedInKeyUid key-uid
                                                            :keystorePath (native-module/keystore-dir)
                                                            :keypairsToExport [keypair-key-uid]
                                                            :password (security/safe-unmask-data
                                                                       sha3-pwd)}
                                             :serverConfig {:timeout 0}}))]
        (native-module/get-connection-string-for-exporting-keypairs-keystores
         config-map
         handle-connection)))))

(rf/reg-event-fx :wallet/get-key-pair-export-connection key-pair-export-connection-string)
