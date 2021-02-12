(ns status-im.multiaccounts.create.core
  (:require [clojure.set :refer [map-invert]]
            [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.data-store.settings :as data-store.settings]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.eip55 :as eip55]
            [status-im.keycard.nfc :as nfc]
            [status-im.i18n.i18n :as i18n]
            [status-im.native-module.core :as status]
            [status-im.node.core :as node]
            [status-im.bottom-sheet.core :as bottom-sheet]
            [status-im.ui.components.colors :as colors]
            [status-im.navigation :as navigation]
            [status-im.utils.config :as config]
            [status-im.utils.fx :as fx]
            [status-im.utils.platform :as platform]
            [status-im.utils.security :as security]
            [status-im.utils.signing-phrase.core :as signing-phrase]
            [status-im.utils.types :as types]
            [status-im.utils.utils :as utils]
            [taoensso.timbre :as log]))

(def step-kw-to-num
  {:generate-key         1
   :choose-key           2
   :select-key-storage   3
   :create-code          4})

(defn decrement-step [step]
  (let [inverted  (map-invert step-kw-to-num)]
    (if (and (= step :create-code)
             (or (not platform/android?)
                 (not (nfc/nfc-supported?))))
      :choose-key
      (inverted (dec (step-kw-to-num step))))))

(defn inc-step [step]
  (let [inverted (map-invert step-kw-to-num)]
    (if (and (= step :choose-key)
             (or (not (or platform/android?
                          config/keycard-test-menu-enabled?))
                 (not (nfc/nfc-supported?))))
      :create-code
      (inverted (inc (step-kw-to-num step))))))

;; multiaccounts create module
(defn get-selected-multiaccount [{:keys [db]}]
  (let [{:keys [selected-id multiaccounts]} (:intro-wizard db)]
    (some #(when (= selected-id (:id %)) %) multiaccounts)))

(defn normalize-derived-data-keys [derived-data]
  (->> derived-data
       (map (fn [[path {:keys [publicKey] :as data}]]
              [path (cond-> (-> data
                                (dissoc :publicKey)
                                (assoc :public-key publicKey)))]))
       (into {})))

(defn normalize-multiaccount-data-keys
  [{:keys [publicKey keyUid derived] :as data}]
  (cond-> (-> data
              (dissoc :keyUid :publicKey)
              (assoc :key-uid keyUid
                     :public-key publicKey))
    derived
    (update :derived normalize-derived-data-keys)))

(fx/defn create-multiaccount
  [{:keys [db]} key-code]
  (let [{:keys [selected-id]} (:intro-wizard db)
        key-uid (some
                 (fn [{:keys [id key-uid]}]
                   (when (= id selected-id)
                     key-uid))
                 (get-in db [:intro-wizard :multiaccounts]))
        hashed-password (ethereum/sha3 (security/safe-unmask-data key-code))
        callback (fn [result]
                   (let [derived-data (normalize-derived-data-keys (types/json->clj result))
                         public-key (get-in derived-data [constants/path-whisper-keyword :public-key])]
                     (status/gfycat-identicon-async
                      public-key
                      (fn [name identicon]
                        (let [derived-whisper (derived-data constants/path-whisper-keyword)
                              derived-data-extended (assoc-in derived-data
                                                              [constants/path-whisper-keyword]
                                                              (merge derived-whisper {:name name :identicon identicon}))]
                          (re-frame/dispatch [::store-multiaccount-success
                                              key-code derived-data-extended]))))))]
    {::store-multiaccount [selected-id key-uid hashed-password callback]}))

(fx/defn prepare-intro-wizard
  [{:keys [db] :as cofx}]
  {:db (assoc db :intro-wizard {:step :generate-key
                                :back-action :intro-wizard/navigate-back
                                :forward-action :intro-wizard/step-forward-pressed})})

(fx/defn intro-wizard
  {:events [:multiaccounts.create.ui/intro-wizard]}
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db (update db :keycard dissoc :flow)}
            (prepare-intro-wizard)
            (navigation/navigate-to-cofx :create-multiaccount-generate-key nil)))

(fx/defn get-new-key
  {:events [:multiaccounts.create.ui/get-new-key]}
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            (prepare-intro-wizard)
            (bottom-sheet/hide-bottom-sheet)
            (navigation/navigate-to-cofx :create-multiaccount-generate-key nil)))

(fx/defn dec-step
  {:events [:intro-wizard/dec-step]}
  [{:keys [db] :as cofx}]
  (let  [step (get-in db [:intro-wizard :step])]
    (fx/merge cofx
              (if (or (= step :enter-phrase) (= :generate-key step))
                #(prepare-intro-wizard %)
                {:db (assoc-in db [:intro-wizard :step] (decrement-step step))}))))

(fx/defn intro-step-back
  {:events [:intro-wizard/navigate-back]}
  [{:keys [db] :as cofx} skip-alert?]
  (let  [step (get-in db [:intro-wizard :step])]
    (if (and (= step :choose-key) (not skip-alert?))
      (utils/show-question
       (i18n/label :t/are-you-sure-to-cancel)
       (i18n/label :t/you-will-start-from-scratch)
       #(re-frame/dispatch [:intro-wizard/navigate-back true]))
      (fx/merge cofx
                dec-step
                navigation/navigate-back))))

(fx/defn exit-wizard
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db (dissoc db :intro-wizard)}
            (navigation/navigate-to-cofx :notifications-onboarding nil)))

(fx/defn init-key-generation
  [{:keys [db] :as cofx}]
  {:db (assoc-in db [:intro-wizard :processing?] true)
   :intro-wizard/start-onboarding nil})

(fx/defn store-key-code [{:keys [db] :as cofx}]
  (let [key-code  (get-in db [:intro-wizard :key-code])]
    {:db (update db :intro-wizard
                 assoc :stored-key-code key-code
                 :key-code nil)}))

(fx/defn intro-step-forward
  {:events [:intro-wizard/step-forward-pressed]}
  [{:keys [db] :as cofx} {:keys [skip? key-code] :as opts}]
  (let [{:keys [step selected-storage-type processing?]} (:intro-wizard db)]
    (log/debug "[multiaccount.create] intro-step-forward"
               "step" step)
    (cond (= step :generate-key)
          (init-key-generation cofx)

          (and (= step :create-code)
               (not processing?))
          (fx/merge cofx
                    {:db (assoc-in db [:intro-wizard :processing?] true)}
                    (create-multiaccount key-code))

          (and  (= step :create-code)
                (:multiaccounts/login db))
          (exit-wizard cofx)

          (and (= step :select-key-storage)
               (= :advanced selected-storage-type))
          {:dispatch [:keycard/start-onboarding-flow]}

          :else (let [next-step (inc-step step)]
                  (fx/merge cofx
                            {:db (update db :intro-wizard
                                         assoc :processing? false
                                         :step next-step)}
                            (when (= step :create-code)
                              store-key-code)
                            (navigation/navigate-to-cofx (->> next-step name (str "create-multiaccount-") keyword) nil))))))

(defn prepare-accounts-data
  [multiaccount]
  [(let [{:keys [public-key address]}
         (get-in multiaccount [:derived constants/path-default-wallet-keyword])]
     {:public-key public-key
      :address    (eip55/address->checksum address)
      :color      colors/blue-persist
      :wallet     true
      :path       constants/path-default-wallet
      :name       (i18n/label :t/ethereum-account)})
   (let [{:keys [public-key address name identicon]}
         (get-in multiaccount [:derived constants/path-whisper-keyword])]
     {:public-key public-key
      :address    (eip55/address->checksum address)
      :name       name
      :identicon identicon
      :path       constants/path-whisper
      :chat       true})])

(fx/defn save-multiaccount-and-login-with-keycard
  [_ args]
  {:keycard/save-multiaccount-and-login args})

(fx/defn save-account-and-login
  [_ key-uid multiaccount-data password settings node-config accounts-data]
  {::save-account-and-login [key-uid
                             (types/clj->json multiaccount-data)
                             password
                             (types/clj->json settings)
                             node-config
                             (types/clj->json accounts-data)]})

(fx/defn on-multiaccount-created
  [{:keys [signing-phrase random-guid-generator db] :as cofx}
   {:keys [address chat-key keycard-instance-uid key-uid
           keycard-pairing keycard-paired-on mnemonic recovered]
    :as multiaccount}
   password
   {:keys [save-mnemonic? login?] :or {login? true save-mnemonic? false}}]
  (let [[wallet-account {:keys [public-key identicon name]} :as accounts-data] (prepare-accounts-data multiaccount)
        multiaccount-data {:name       name
                           :address    address
                           :identicon identicon
                           :key-uid    key-uid
                           :keycard-pairing keycard-pairing}
        keycard-multiaccount? (boolean keycard-pairing)
        eip1581-address (get-in multiaccount [:derived
                                              constants/path-eip1581-keyword
                                              :address])
        new-multiaccount
        (cond-> (merge
                 {;; address of the master key
                  :address               address
                  ;; sha256 of master public key
                  :key-uid               key-uid
                  ;; The address from which we derive any wallet
                  :wallet-root-address
                  (get-in multiaccount [:derived
                                        constants/path-wallet-root-keyword
                                        :address])
                  :name                  name
                  :identicon            identicon
                  ;; public key of the chat account
                  :public-key            public-key
                  ;; default address for Dapps
                  :dapps-address         (:address wallet-account)
                  :latest-derived-path   0
                  :signing-phrase        signing-phrase
                  :send-push-notifications? true
                  :installation-id       (random-guid-generator)
                  ;; default mailserver (history node) setting
                  :use-mailservers?      true
                  :recovered             (or recovered (get-in db [:intro-wizard :recovering?]))}
                 config/default-multiaccount)
          ;; The address from which we derive any chat
          ;; account/encryption keys
          eip1581-address
          (assoc :eip1581-address eip1581-address)
          save-mnemonic?
          (assoc :mnemonic mnemonic)
          keycard-multiaccount?
          (assoc :keycard-instance-uid keycard-instance-uid
                 :keycard-pairing keycard-pairing
                 :keycard-paired-on keycard-paired-on))
        db (assoc db
                  :multiaccounts/login {:key-uid    key-uid
                                        :name       name
                                        :identicon identicon
                                        :password   password
                                        :creating?  true
                                        :processing true}
                  :multiaccount new-multiaccount
                  :multiaccount/accounts [wallet-account]
                  :networks/current-network config/default-network
                  :networks/networks (data-store.settings/rpc->networks config/default-networks))
        settings (assoc new-multiaccount
                        :networks/current-network config/default-network
                        :networks/networks config/default-networks)]
    (fx/merge cofx
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
                 (ethereum/sha3 (security/safe-unmask-data password))
                 settings
                 (node/get-new-config db)
                 accounts-data))
              (when (:intro-wizard db)
                (intro-step-forward {})))))

(re-frame/reg-fx
 :intro-wizard/start-onboarding
 (fn []
   (status/multiaccount-generate-and-derive-addresses
    5
    12
    [constants/path-whisper
     constants/path-wallet-root
     constants/path-default-wallet]
    #(re-frame/dispatch [:intro-wizard/on-keys-generated
                         (mapv normalize-multiaccount-data-keys
                               (types/json->clj %))]))))

(fx/defn on-keys-generated
  {:events [:intro-wizard/on-keys-generated]}
  [{:keys [db] :as cofx} result]
  (fx/merge
   {:db (update db :intro-wizard
                (fn [data]
                  (-> data
                      (dissoc :processing?)
                      (assoc :multiaccounts result
                             :selected-storage-type :default
                             :selected-id (-> result first :id)
                             :step :choose-key))))}
   (navigation/navigate-to-cofx :create-multiaccount-choose-key nil)))

(fx/defn on-key-selected
  {:events [:intro-wizard/on-key-selected]}
  [{:keys [db] :as cofx} id]
  {:db (assoc-in db [:intro-wizard :selected-id] id)})

(fx/defn on-key-storage-selected
  {:events [:intro-wizard/on-key-storage-selected]}
  [{:keys [db] :as cofx} storage-type]
  {:db (assoc-in db [:intro-wizard :selected-storage-type] storage-type)})

(fx/defn on-learn-more-pressed
  {:events [:intro-wizard/on-learn-more-pressed]}
  [{:keys [db] :as cofx}]
  {:db (assoc-in db [:intro-wizard :show-learn-more?] true)})

(re-frame/reg-cofx
 ::get-signing-phrase
 (fn [cofx _]
   (assoc cofx :signing-phrase (signing-phrase/generate))))

(fx/defn create-multiaccount-success
  {:events [::store-multiaccount-success]
   :interceptors [(re-frame/inject-cofx :random-guid-generator)
                  (re-frame/inject-cofx ::get-signing-phrase)]}
  [cofx password derived]
  (on-multiaccount-created cofx
                           (assoc
                            (get-selected-multiaccount cofx)
                            :derived
                            derived)
                           password
                           {:save-mnemonic? true}))

(re-frame/reg-fx
 ::store-multiaccount
 (fn [[id key-uid hashed-password callback]]
   (status/multiaccount-store-derived
    id
    key-uid
    [constants/path-wallet-root
     constants/path-eip1581
     constants/path-whisper
     constants/path-default-wallet]
    hashed-password
    callback)))

(re-frame/reg-fx
 ::save-account-and-login
 (fn [[key-uid multiaccount-data hashed-password settings config accounts-data]]
   (status/save-account-and-login
    key-uid
    multiaccount-data
    hashed-password
    settings
    config
    accounts-data)))
