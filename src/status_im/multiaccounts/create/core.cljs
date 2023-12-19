(ns status-im.multiaccounts.create.core
  (:require
    [native-module.core :as native-module]
    [re-frame.core :as re-frame]
    [status-im.data-store.settings :as data-store.settings]
    [status-im.node.core :as node]
    [status-im.ui.components.colors :as colors]
    [status-im.utils.deprecated-types :as types]
    [status-im.utils.signing-phrase.core :as signing-phrase]
    [status-im2.config :as config]
    [status-im2.constants :as constants]
    [utils.ethereum.eip.eip55 :as eip55]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.security.core :as security]))

(defn normalize-derived-data-keys
  [derived-data]
  (->> derived-data
       (map (fn [[path {:keys [publicKey compressedKey] :as data}]]
              [path
               (cond-> (-> data
                           (dissoc :publicKey :compressedKey)
                           (assoc
                            :public-key     publicKey
                            :compressed-key compressedKey)))]))
       (into {})))

(defn normalize-multiaccount-data-keys
  [{:keys [publicKey keyUid derived compressedKey] :as data}]
  (cond-> (-> data
              (dissoc :keyUid :publicKey)
              (assoc :key-uid        keyUid
                     :compressed-key compressedKey
                     :public-key     publicKey))
    derived
    (update :derived normalize-derived-data-keys)))

(re-frame/reg-cofx
 ::get-signing-phrase
 (fn [cofx _]
   (assoc cofx :signing-phrase (signing-phrase/generate))))

(re-frame/reg-fx
 :multiaccount-generate-and-derive-addresses
 (fn []
   (native-module/multiaccount-generate-and-derive-addresses
    5
    12
    [constants/path-whisper
     constants/path-wallet-root
     constants/path-default-wallet]
    #(re-frame/dispatch [:multiaccount-generate-and-derive-addresses-success
                         (mapv normalize-multiaccount-data-keys
                               (types/json->clj %))]))))

(rf/defn multiaccount-generate-and-derive-addresses-success
  {:events [:multiaccount-generate-and-derive-addresses-success]}
  [{:keys [db]} result]
  {:db          (update db
                        :intro-wizard
                        (fn [data]
                          (-> data
                              (dissoc :processing?)
                              (assoc :multiaccounts         result
                                     :selected-storage-type :default
                                     :selected-id           (-> result first :id)
                                     :step                  :choose-key))))
   :navigate-to :choose-name})

(rf/defn generate-and-derive-addresses
  {:events [:generate-and-derive-addresses]}
  [{:keys [db]}]
  {:db                                         (-> db
                                                   (update :intro-wizard
                                                           #(-> %
                                                                (assoc :processing? true)
                                                                (dissoc :recovering?)))
                                                   (dissoc :recovered-account?))
   :multiaccount-generate-and-derive-addresses nil})

(rf/defn save-multiaccount-and-login-with-keycard
  [_ args]
  {:keycard/save-multiaccount-and-login args})

(re-frame/reg-fx
 ::save-account-and-login
 (fn [[key-uid multiaccount-data hashed-password settings config accounts-data]]
   (native-module/save-account-and-login
    key-uid
    multiaccount-data
    hashed-password
    settings
    config
    accounts-data)))

(rf/defn save-account-and-login
  [{:keys [db]} key-uid multiaccount-data password settings node-config accounts-data]
  {:db                      (assoc-in db [:syncing :login-sha3-password] password)
   ::save-account-and-login [key-uid
                             (types/clj->json multiaccount-data)
                             password
                             (types/clj->json settings)
                             node-config
                             (types/clj->json accounts-data)]})

(defn prepare-accounts-data
  [multiaccount]
  [(let [{:keys [public-key address]}
         (get-in multiaccount [:derived constants/path-default-wallet-keyword])]
     {:public-key public-key
      :address    (eip55/address->checksum address)
      :color      colors/blue-persist
      :wallet     true
      :path       constants/path-default-wallet
      :name       (i18n/label :t/main-account)})
   (let [{:keys [compressed-key public-key address name]}
         (get-in multiaccount [:derived constants/path-whisper-keyword])]
     {:public-key     public-key
      :compressed-key compressed-key
      :address        (eip55/address->checksum address)
      :name           name
      :path           constants/path-whisper
      :chat           true})])

(rf/defn on-multiaccount-created
  [{:keys [signing-phrase random-guid-generator db] :as cofx}
   {:keys [address chat-key keycard-instance-uid key-uid
           keycard-pairing keycard-paired-on mnemonic recovered]
    :as   multiaccount}
   password
   {:keys [save-mnemonic? login?] :or {login? true save-mnemonic? false}}]
  (let [[wallet-account
         {:keys [public-key
                 compressed-key
                 name]} :as accounts-data]
        (prepare-accounts-data
         multiaccount)
        multiaccount-data {:name            name
                           :address         address
                           :key-uid         key-uid
                           :keycard-pairing keycard-pairing}
        keycard-multiaccount? (boolean keycard-pairing)
        eip1581-address (get-in multiaccount
                                [:derived
                                 constants/path-eip1581-keyword
                                 :address])
        new-multiaccount
        (cond->
          (merge
           {;; address of the master key
            :address address
            ;; sha256 of master public key
            :key-uid key-uid
            ;; The address from which we derive any wallet
            :wallet-root-address
            (get-in multiaccount
                    [:derived
                     constants/path-wallet-root-keyword
                     :address])
            :name name
            ;; public key of the chat account
            :public-key public-key
            ;; compressed key of the chat account
            :compressed-key compressed-key
            ;; default address for Dapps
            :dapps-address (:address wallet-account)
            :latest-derived-path 0
            :signing-phrase signing-phrase
            :backup-enabled? true
            :installation-id (random-guid-generator)
            ;; default mailserver (history node) setting
            :use-mailservers? true
            :recovered recovered}
           config/default-multiaccount)
          ;; The address from which we derive any chat account/encryption keys
          eip1581-address
          (assoc :eip1581-address eip1581-address)
          save-mnemonic?
          (assoc :mnemonic mnemonic)
          keycard-multiaccount?
          (assoc :keycard-instance-uid keycard-instance-uid
                 :keycard-pairing      keycard-pairing
                 :keycard-paired-on    keycard-paired-on))
        db (assoc db
                  :profile/login            {:key-uid    key-uid
                                             :name       name
                                             :password   password
                                             :creating?  true
                                             :processing true}
                  :profile/profile          new-multiaccount
                  :profile/wallet-accounts  [wallet-account]
                  :networks/current-network config/default-network
                  :networks/networks        (data-store.settings/rpc->networks config/default-networks))
        settings (assoc new-multiaccount
                        :networks/current-network config/default-network
                        :networks/networks        config/default-networks)]
    (rf/merge cofx
              {:db db}
              (if keycard-multiaccount?
                (save-multiaccount-and-login-with-keycard
                 {:key-uid           key-uid
                  :multiaccount-data multiaccount-data
                  :password          password
                  :settings          settings
                  :node-config       (node/get-new-config db)
                  :accounts-data     accounts-data
                  :chat-key          chat-key})
                (save-account-and-login
                 key-uid
                 multiaccount-data
                 (native-module/sha3 (security/safe-unmask-data password))
                 settings
                 (node/get-new-config db)
                 accounts-data)))))
