(ns status-im.keycard.simulated-keycard
  (:require [re-frame.core :as re-frame]
            [re-frame.db :as re-frame.db]
            [status-im.constants :as constants]
            [status-im.ethereum.core :as ethereum]
            [status-im.keycard.keycard :as keycard]
            [status-im.native-module.core :as status]
            [status-im.utils.types :as types]
            [status-im.utils.utils :as utils]
            [status-im.i18n.i18n :as i18n]
            [clojure.string :as string]
            [taoensso.timbre :as log]
            [status-im.multiaccounts.create.core :as multiaccounts.create]))

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

(def initialization (atom false))

(defn connect-pairing-card []
  (reset! initialization false)
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
          :key-uid                "0x16839e8b1b8a395acb18100c7e2b161701c225adf31eefa02114099b4d81a30b"})
  (connect-card))

(defn disconnect-card []
  (swap! state assoc :card-connected? false)
  (doseq [callback (vals (get @state :on-card-disconnected))]
    (callback)))

(defn reset-state []
  (reset! state initial-state))

(defn- later [f]
  (when f
    (utils/set-timeout f 500)))

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
  (reset! initialization true)
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

(def derived-acc (atom nil))

(defn multiaccount->keys [pin data]
  (let [{:keys [selected-id multiaccounts root-key derived]}
        data

        {:keys [id address public-key derived key-uid]}
        (or (->> multiaccounts
                 (filter #(= (:id %) selected-id))
                 first)
            (assoc root-key :derived derived))

        whisper     (get derived constants/path-whisper-keyword)
        wallet      (get derived constants/path-default-wallet-keyword)
        wallet-root (get derived constants/path-wallet-root-keyword)
        password    (ethereum/sha3 pin)
        response    {:key-uid                key-uid
                     :encryption-public-key  password
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
    [id response]))

(defn pair [{:keys [password on-success]}]
  (when (and (not @initialization)
             (not @derived-acc))
    (status/multiaccount-import-mnemonic
     "night fortune spider version version armed amused winner matter tonight cave flag"
     nil
     (fn [result]
       (let [{:keys [id] :as root-data}
             (multiaccounts.create/normalize-multiaccount-data-keys
              (types/json->clj result))]
         (status-im.native-module.core/multiaccount-derive-addresses
          id
          [constants/path-wallet-root
           constants/path-eip1581
           constants/path-whisper
           constants/path-default-wallet]
          (fn [result]
            (let [derived-data (multiaccounts.create/normalize-derived-data-keys
                                (types/json->clj result))
                  public-key (get-in derived-data [constants/path-whisper-keyword :public-key])]
              (status/gfycat-identicon-async
               public-key
               (fn [name photo-path]
                 (let [derived-data-extended
                       (update derived-data
                               constants/path-whisper-keyword
                               merge {:name name :identicon photo-path})]
                   (reset! derived-acc
                           {:root-key root-data
                            :derived  derived-data-extended})))))))))))
  (when (= password kk1-password)
    (later #(on-success (str (rand-int 10000000))))))

(defn generate-and-load-key
  [{:keys [pin on-success]}]
  (when (= pin (get @state :pin))
    (let [[id response] (multiaccount->keys
                         pin
                         (:intro-wizard @re-frame.db/app-db))]
      (log/debug "[simulated kk] generate-and-load-key response" response)
      (status/multiaccount-store-derived
       id
       (:key-uid response)
       [constants/path-wallet-root
        constants/path-eip1581
        constants/path-whisper
        constants/path-default-wallet]
       (ethereum/sha3 pin)
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

(defn verify-pin [{:keys [pin on-success on-failure]}]
  (if (= pin (get @state :pin))
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
  (let [{:keys [key-uid wallet-root-address]}
        (get @re-frame.db/app-db :multiaccount)
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
                   key-uid
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
       (if @derived-acc
         (let [[id keys] (multiaccount->keys pin @derived-acc)]
           (status/multiaccount-store-derived
            id
            (:key-uid keys)
            [constants/path-wallet-root
             constants/path-eip1581
             constants/path-whisper
             constants/path-default-wallet]
            (ethereum/sha3 pin)
            #(on-success keys)))
         #(on-success
           {:key-uid               (get-in @state [:application-info :key-uid])
            :encryption-public-key (ethereum/sha3 pin)}))))
    (do
      (log/debug "Incorrect PIN" pin)
      (swap! state update-in
             [:application-info :pin-retry-counter]
             (fnil dec 3))
      (later
       #(on-failure
         #js {:code    "EUNSPECIFIED"
              :message "Unexpected error SW, 0x63C2"})))))

(defn sign [{:keys [pin hash data path typed? on-success on-failure]}]
  (if (= pin (get @state :pin))
    (later
     #(let [address
            (if path
              (reduce
               (fn [_ {:keys [address] :as acc}]
                 (when (= path (:path acc))
                   (reduced address)))
               nil
               (:multiaccount/accounts @re-frame.db/app-db))
              (-> (:multiaccount/accounts @re-frame.db/app-db)
                  first
                  :address))
            password (ethereum/sha3 pin)]
        (if-not typed?
          (let [params (types/clj->json
                        {:account  address
                         :password password
                         :data     (or data (str "0x" hash))})]
            (status/sign-message
             params
             (fn [res]
               (let [signature (-> res
                                   types/json->clj
                                   :result
                                   ethereum/normalized-hex)]
                 (on-success signature)))))
          (status/sign-typed-data
           data
           address
           password
           (fn [res]
             (let [signature (-> res
                                 types/json->clj
                                 :result
                                 ethereum/normalized-hex)]
               (on-success signature)))))))
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
  [{:keys [key-uid multiaccount-data password settings node-config accounts-data]}]
  (status/save-account-and-login
   key-uid
   (types/clj->json multiaccount-data)
   password
   (types/clj->json settings)
   node-config
   (types/clj->json accounts-data)))

(defn login [{:keys [key-uid multiaccount-data password]}]
  (status/login key-uid multiaccount-data password))

(defn send-transaction-with-signature
  [{:keys [transaction on-completed]}]
  (status/send-transaction transaction (ethereum/sha3 (:pin @state)) on-completed))

(defrecord SimulatedKeycard []
  keycard/Keycard
  (keycard/check-nfc-support [this args]
    (log/debug "simulated card check-nfc-support")
    (check-nfc-support args))
  (keycard/check-nfc-enabled [this args]
    (log/debug "simulated card check-nfc-enabled")
    (check-nfc-enabled args))
  (keycard/open-nfc-settings [this]
    (log/debug "simulated card open-nfc-setting")
    (open-nfc-settings))
  (keycard/register-card-events [this args]
    (log/debug "simulated card register-card-event")
    (register-card-events args))
  (keycard/on-card-connected [this callback]
    (log/debug "simulated card on-card-connected")
    (on-card-connected callback))
  (keycard/on-card-disconnected [this callback]
    (log/debug "simulated card on-card-disconnected")
    (on-card-disconnected callback))
  (keycard/remove-event-listener [this event]
    (log/debug "simulated card remove-event-listener")
    (remove-event-listener event))
  (keycard/remove-event-listeners [this]
    (log/debug "simulated card remove-event-listener")
    (remove-event-listeners))
  (keycard/get-application-info [this args]
    (log/debug "simulated card get-application-info")
    (get-application-info args))
  (keycard/install-applet [this args]
    (log/debug "simulated card install-applet")
    (install-applet args))
  (keycard/init-card [this args]
    (log/debug "simulated card init-card")
    (init-card args))
  (keycard/install-applet-and-init-card [this args]
    (log/debug "simulated card install-applet-and-init-card")
    (install-applet-and-init-card args))
  (keycard/pair [this args]
    (log/debug "simulated card pair")
    (pair args))
  (keycard/generate-and-load-key [this args]
    (log/debug "simulated card generate-and-load-key")
    (generate-and-load-key args))
  (keycard/unblock-pin [this args]
    (log/debug "simulated card unblock-pin")
    (unblock-pin args))
  (keycard/verify-pin [this args]
    (log/debug "simulated card verify-pin")
    (verify-pin args))
  (keycard/change-pin [this args]
    (log/debug "simulated card change-pin")
    (change-pin args))
  (keycard/unpair [this args]
    (log/debug "simulated card unpair")
    (unpair args))
  (keycard/delete [this args]
    (log/debug "simulated card delete")
    (delete args))
  (keycard/remove-key [this args]
    (log/debug "simulated card remove-key")
    (remove-key args))
  (keycard/remove-key-with-unpair [this args]
    (log/debug "simulated card remove-key-with-unpair")
    (remove-key-with-unpair args))
  (keycard/export-key [this args]
    (log/debug "simulated card export-key")
    (export-key args))
  (keycard/unpair-and-delete [this args]
    (log/debug "simulated card unpair-and-delete")
    (unpair-and-delete args))
  (keycard/get-keys [this args]
    (log/debug "simulated card get-keys")
    (get-keys args))
  (keycard/sign [this args]
    (log/debug "simulated card sign")
    (sign args))
  (keycard/save-multiaccount-and-login [this args]
    (log/debug "simulated card save-multiaccount-and-login")
    (save-multiaccount-and-login args))
  (keycard/login [this args]
    (log/debug "simulated card login")
    (login args))
  (keycard/send-transaction-with-signature [this args]
    (log/debug "simulated card send-transaction-with-signature")
    (send-transaction-with-signature args)))
