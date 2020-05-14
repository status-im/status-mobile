(ns status-im.hardwallet.simulated-keycard
  (:require [re-frame.core :as re-frame]
            [re-frame.db :as re-frame.db]
            [status-im.constants :as constants]
            [status-im.ethereum.core :as ethereum]
            [status-im.hardwallet.keycard :as keycard]
            [status-im.native-module.core :as status]
            [status-im.utils.types :as types]
            [status-im.utils.utils :as utils]
            [status-im.i18n :as i18n]
            [clojure.string :as string]
            [taoensso.timbre :as log]))

(def initial-state
  {:card-connected?  false
   :application-info {:initialized? false}})

(defonce state (atom initial-state))

(defn connect-card []
  (swap! state assoc :card-connected? true)
  (doseq [callback (vals (get @state :on-card-connected))]
    (callback)))

(defn connect-selected-card []
  (swap! state assoc :application-info
         {:free-pairing-slots     5
          :app-version            "2.2"
          :secure-channel-pub-key "04c3071768912a515c00aeab7ceb8a5bfda91d036f4a4e60b7944cee3ca7fb67b6d118e8df1e2480b87fd636c6615253245bbbc93a6a407f155f2c58f76c96ef0e",
          :instance-uid           "1b360b10a9a68b7d494e8f059059f118"
          :paired?                true
          :has-master-key?        true
          :initialized?           true
          :pin-retry-counter      3
          :puk-retry-counter      5
          :key-uid                (get-in @re-frame.db/app-db [:multiaccounts/login :key-uid])})
  (connect-card))

(defn disconnect-card []
  (swap! state assoc :card-connected? false)
  (doseq [callback (vals (get @state :on-card-disconnected))]
    (callback)))

(defn reset-state []
  (reset! state initial-state))

(defn- later [f]
  (utils/set-timeout f 500))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn check-nfc-support [{:keys [on-success]}]
  (later #(on-success true)))

(defn check-nfc-enabled [{:keys [on-success]}]
  (later #(on-success true)))

(defn open-nfc-settings [])

(defn on-card-connected [callback]
  (let [id (random-uuid)]
    (swap! state update :on-card-connected assoc id callback)
    id))

(defn on-card-disconnected [callback]
  (let [id (random-uuid)]
    (swap! state update :on-card-disconnected assoc id callback)
    id))

(defn register-card-events [args]
  (on-card-connected (:on-card-connected args))
  (on-card-disconnected (:on-card-disconnected args)))

(defn remove-event-listener [id]
  (swap! state update :on-card-connected dissoc id)
  (swap! state update :on-card-disconnected dissoc id))

(defn remove-event-listeners []
  (swap! state dissoc :on-card-connected)
  (swap! state dissoc :on-card-disconnected))

(defn get-application-info [{:keys [on-success]}]
  (later #(on-success (get @state :application-info))))

(defn install-applet [_])
(defn install-cash-applet [_])

(def kk1-password "000000")
(def default-puk "000000000000")
(defn init-card [{:keys [pin on-success]}]
  (swap! state assoc :application-info
         {:free-pairing-slots     5
          :app-version            "2.2"
          :secure-channel-pub-key "04c3071768912a515c00aeab7ceb8a5bfda91d036f4a4e60b7944cee3ca7fb67b6d118e8df1e2480b87fd636c6615253245bbbc93a6a407f155f2c58f76c96ef0e",
          :key-uid                "",
          :instance-uid           "1b360b10a9a68b7d494e8f059059f118"
          :paired?                false
          :has-master-key?        false
          :initialized?           true})
  (swap! state assoc :pin pin)
  (later
   #(on-success {:password kk1-password
                 :puk      default-puk
                 :pin      pin})))

(defn install-applet-and-init-card [_])

(def kk1-pair "ADEol+GCD67EO7zU6ko0DNK7XrNs9w2+h9GxcibNY4yf")

(defn pair [{:keys [password on-success]}]
  (when (= password kk1-password)
    (later #(on-success kk1-pair))))

(defn generate-and-load-key
  [{:keys [pin pairing on-success multiaccount]}]
  (when (and (= pin (get @state :pin))
             (= pairing kk1-pair))
    (let [{:keys [id address public-key derived key-uid]}
          multiaccount
          whisper     (get derived constants/path-whisper-keyword)
          wallet      (get derived constants/path-default-wallet-keyword)
          wallet-root (get derived constants/path-wallet-root-keyword)
          password    (ethereum/sha3 pin)
          response    {:key-uid                key-uid
                       :encryption-public-key  (ethereum/sha3 pin)
                       :address                address
                       :whisper-public-key     (:public-key whisper)
                       :instance-uid           "1b360b10a9a68b7d494e8f059059f118"
                       :wallet-root-public-key (:public-key wallet-root)
                       :wallet-root-address    (:address wallet-root)
                       :whisper-address        (:address whisper)
                       :public-key             public-key
                       :whisper-private-key    "34bc7d0c258c4f2ac1dac4fd6c55c9478bac1f4a9d8b9f1152c8551ab7187b43"
                       :wallet-address         (:address wallet)
                       :wallet-public-key      (:public-key wallet)}]
      (log/debug "[simulated kk] generate-and-load-key response" response)
      (status/multiaccount-store-derived
       id
       [constants/path-wallet-root
        constants/path-eip1581
        constants/path-whisper
        constants/path-default-wallet]
       password
       #(on-success response)))))

(defn unblock-pin
  [{:keys [puk on-success on-failure]}]
  (if (= puk default-puk)
    (do
      (swap! state update :application-info assoc
             :pin-retry-counter 3
             :puk-retry-counter 5)
      (later #(on-success true)))
    (do
      (swap! state update-in
             [:application-info :puk-retry-counter]
             (fnil dec 5))
      (later
       #(on-failure
         #js {:code    "EUNSPECIFIED"
              :message "Unexpected error SW, 0x63C2"})))))

(defn verify-pin [{:keys [pin pairing on-success on-failure]}]
  (if (and (= pairing kk1-pair)
           (= pin (get @state :pin)))
    (later #(on-success 3))
    (do
      (swap! state update-in
             [:application-info :pin-retry-counter]
             (fnil dec 3))
      (later #(on-failure
               #js {:code    "EUNSPECIFIED"
                    :message "Unexpected error SW, 0x63C2"})))))

(defn change-pin [args]
  (log/warn "change-pin not implemented" args))
(defn unpair [args]
  (log/warn "unpair not implemented" args))
(defn delete [args]
  (log/warn "delete not implemented" args))
(defn remove-key [args]
  (log/warn "remove-key not implemented" args))
(defn remove-key-with-unpair [args]
  (log/warn "remove-key-with-unpair not implemented" args))

(defn normalize-path [path]
  (if (string/starts-with? path "m/")
    (str constants/path-wallet-root
         "/" (last (string/split path "/")))
    path))

(defn export-key [{:keys [pin on-success on-failure]}]
  (let [wallet-root-address (get-in
                             @re-frame.db/app-db
                             [:multiaccount :wallet-root-address])
        accounts            (get @re-frame.db/app-db :multiaccount/accounts)
        hashed-password     (ethereum/sha3 pin)
        path-num            (inc (get-in @re-frame.db/app-db [:multiaccount :latest-derived-path]))
        path                (str "m/" path-num)]
    (status/multiaccount-load-account
     wallet-root-address
     hashed-password
     (fn [value]
       (let [{:keys [id error]} (types/json->clj value)]
         (if error
           (re-frame/dispatch [::new-account-error :password-error error])
           (status/multiaccount-derive-addresses
            id
            [path]
            (fn [derived]
              (let [derived-address (get-in (types/json->clj derived) [(keyword path) :address])]
                (if (some #(= derived-address (get % :address)) accounts)
                  (re-frame/dispatch [::new-account-error :account-error (i18n/label :t/account-exists-title)])
                  (status/multiaccount-store-derived
                   id
                   [path]
                   hashed-password
                   (fn [result]
                     (let [{:keys [error] :as result}  (types/json->clj result)
                           {:keys [publicKey]} (get result (keyword path))]
                       (if error
                         (on-failure error)
                         (on-success publicKey)))))))))))))))

(defn unpair-and-delete [_])

;; It is a bit complicated to verify password before we have multiaccs main
;; wallet address, so we just define a set of "allowed" pins
(def allowed-pins
  #{"121212" "111111" "222222" "123123"})

(defn get-keys [{:keys [on-success on-failure pin]}]
  (if (contains? allowed-pins pin)
    (do
      (swap! state assoc :pin pin)
      (later
       #(on-success
         {:key-uid               (get-in @state [:application-info :key-uid])
          :encryption-public-key (ethereum/sha3 pin)})))
    (do
      (log/debug "Incorrect PIN" pin)
      (swap! state update-in
             [:application-info :pin-retry-counter]
             (fnil dec 3))
      (later
       #(on-failure
         #js {:code    "EUNSPECIFIED"
              :message "Unexpected error SW, 0x63C2"})))))

(defn sign [{:keys [pin on-success on-failure]}]
  (if (= pin (get @state :pin))
    (later
     #(on-success "123"))
    (do
      (swap! state update-in
             [:application-info :pin-retry-counter]
             (fnil dec 3))
      (later
       #(on-failure
         #js {:code    "EUNSPECIFIED"
              :message "Unexpected error SW, 0x63C2"})))))

(defn sign-typed-data [args]
  (log/warn "sign-typed-data not implemented" args))

(defn save-multiaccount-and-login
  [{:keys [multiaccount-data password settings node-config accounts-data]}]
  (status/save-account-and-login
   (types/clj->json multiaccount-data)
   password
   (types/clj->json settings)
   node-config
   (types/clj->json accounts-data)))

(defn login [{:keys [multiaccount-data password]}]
  (status/login multiaccount-data password))

(defn send-transaction-with-signature
  [{:keys [transaction on-completed]}]
  (status/send-transaction transaction (ethereum/sha3 (:pin @state)) on-completed))

(defrecord SimulatedKeycard []
  keycard/Keycard
  (keycard/check-nfc-support [this args]
    (check-nfc-support args))
  (keycard/check-nfc-enabled [this args]
    (check-nfc-enabled args))
  (keycard/open-nfc-settings [this]
    (open-nfc-settings))
  (keycard/register-card-events [this args]
    (register-card-events args))
  (keycard/on-card-connected [this callback]
    (on-card-connected callback))
  (keycard/on-card-disconnected [this callback]
    (on-card-disconnected callback))
  (keycard/remove-event-listener [this event]
    (remove-event-listener event))
  (keycard/remove-event-listeners [this]
    (remove-event-listeners))
  (keycard/get-application-info [this args]
    (get-application-info args))
  (keycard/install-applet [this args]
    (install-applet args))
  (keycard/init-card [this args]
    (init-card args))
  (keycard/install-applet-and-init-card [this args]
    (install-applet-and-init-card args))
  (keycard/pair [this args]
    (pair args))
  (keycard/generate-and-load-key [this args]
    (generate-and-load-key args))
  (keycard/unblock-pin [this args]
    (unblock-pin args))
  (keycard/verify-pin [this args]
    (verify-pin args))
  (keycard/change-pin [this args]
    (change-pin args))
  (keycard/unpair [this args]
    (unpair args))
  (keycard/delete [this args]
    (delete args))
  (keycard/remove-key [this args]
    (remove-key args))
  (keycard/remove-key-with-unpair [this args]
    (remove-key-with-unpair args))
  (keycard/export-key [this args]
    (export-key args))
  (keycard/unpair-and-delete [this args]
    (unpair-and-delete args))
  (keycard/get-keys [this args]
    (get-keys args))
  (keycard/sign [this args]
    (sign args))
  (keycard/save-multiaccount-and-login [this args]
    (save-multiaccount-and-login args))
  (keycard/login [this args]
    (login args))
  (keycard/send-transaction-with-signature [this args]
    (send-transaction-with-signature args)))
