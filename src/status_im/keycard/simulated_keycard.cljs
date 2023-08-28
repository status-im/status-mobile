(ns status-im.keycard.simulated-keycard
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [re-frame.db :as re-frame.db]
            [status-im2.constants :as constants]
            [status-im.ethereum.core :as ethereum]
            [utils.i18n :as i18n]
            [status-im.keycard.keycard :as keycard]
            [status-im.multiaccounts.create.core :as multiaccounts.create]
            [native-module.core :as native-module]
            [status-im.node.core :as node]
            [status-im.utils.types :as types]
            [status-im.utils.utils :as utils]
            [taoensso.timbre :as log]))

(def kk1-password "000000")
(def default-pin "111111")
(def default-puk "000000000000")
(def account-password (ethereum/sha3 "no password"))

(def initial-state
  {:card-connected?  false
   :application-info {:initialized? false}})

(defonce state (atom initial-state))

(defn connect-card
  []
  (swap! state assoc :card-connected? true)
  (doseq [callback (vals (get @state :on-card-connected))]
    (callback)))

(defn connect-selected-card
  []
  (swap! state assoc
    :application-info
    {:free-pairing-slots 5
     :app-version "3.0"
     :secure-channel-pub-key
     "04c3071768912a515c00aeab7ceb8a5bfda91d036f4a4e60b7944cee3ca7fb67b6d118e8df1e2480b87fd636c6615253245bbbc93a6a407f155f2c58f76c96ef0e"
     :instance-uid "1b360b10a9a68b7d494e8f059059f118"
     :paired? true
     :has-master-key? true
     :initialized? true
     :pin-retry-counter 3
     :puk-retry-counter 5
     :key-uid (get-in @re-frame.db/app-db [:profile/login :key-uid])})
  (swap! state assoc :pin default-pin :puk default-puk :password kk1-password)
  (connect-card))

(def initialization (atom false))

(defn connect-pairing-card
  []
  (reset! initialization false)
  (swap! state assoc
    :application-info
    {:free-pairing-slots 5
     :app-version "3.0"
     :secure-channel-pub-key
     "04c3071768912a515c00aeab7ceb8a5bfda91d036f4a4e60b7944cee3ca7fb67b6d118e8df1e2480b87fd636c6615253245bbbc93a6a407f155f2c58f76c96ef0e"
     :instance-uid "1b360b10a9a68b7d494e8f059059f118"
     :paired? false
     :has-master-key? true
     :initialized? true
     :pin-retry-counter 3
     :puk-retry-counter 5
     :key-uid "0x16839e8b1b8a395acb18100c7e2b161701c225adf31eefa02114099b4d81a30b"})
  (swap! state assoc :pin default-pin :puk default-puk :password kk1-password)
  (connect-card))

(defn disconnect-card
  []
  (swap! state assoc :card-connected? false)
  (doseq [callback (vals (get @state :on-card-disconnected))]
    (callback)))

(defn reset-state
  []
  (swap! state assoc :application-info {:initialized? false}))

(defn- later
  [f]
  (when f
    (utils/set-timeout f 500)))

(defn pin-error
  []
  #js
   {:code    "EUNSPECIFIED"
    :message (str "Unexpected error SW, 0x63C" (get-in @state [:application-info :pin-retry-counter]))})

(defn puk-error
  []
  #js
   {:code    "EUNSPECIFIED"
    :message (str "Unexpected error SW, 0x63C" (get-in @state [:application-info :puk-retry-counter]))})

(defn with-pin
  [pin on-failure on-valid]
  (cond
    (= (get-in @state [:application-info :pin-retry-counter]) 0)
    (later #(on-failure (pin-error)))

    (= pin (get @state :pin))
    (do
      (swap! state update
        :application-info  assoc
        :pin-retry-counter 3
        :puk-retry-counter 5)
      (later on-valid))

    :else
    (do
      (swap! state update-in
        [:application-info :pin-retry-counter]
        (fnil dec 3))
      (later #(on-failure (pin-error))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn start-nfc
  [{:keys [on-success]}]
  (when (get @state :card-connected?) (connect-card))
  (later #(on-success true)))

(defn stop-nfc
  [{:keys [on-success]}]
  (later #(on-success true)))

(defn set-nfc-message
  [{:keys [on-success]}]
  (later #(on-success true)))

(defn check-nfc-support
  [{:keys [on-success]}]
  (later #(on-success true)))

(defn check-nfc-enabled
  [{:keys [on-success]}]
  (later #(on-success true)))

(defn open-nfc-settings [])

(defn on-card-connected
  [callback]
  (let [id (random-uuid)]
    (swap! state update :on-card-connected assoc id callback)
    id))

(defn on-card-disconnected
  [callback]
  (let [id (random-uuid)]
    (swap! state update :on-card-disconnected assoc id callback)
    id))

(defn register-card-events
  [args]
  (log/debug "register-card-events")
  (on-card-connected (:on-card-connected args))
  (on-card-disconnected (:on-card-disconnected args)))

(defn set-pairings
  [args]
  (log/warn "set-pairings not implemented" args))

(defn remove-event-listener
  [id]
  (log/debug "remove-event-listener")
  (swap! state update :on-card-connected dissoc id)
  (swap! state update :on-card-disconnected dissoc id))

(defn remove-event-listeners
  []
  (log/debug "remove-event-listeners")
  (swap! state dissoc :on-card-connected)
  (swap! state dissoc :on-card-disconnected))

(defn get-application-info
  [{:keys [on-success]}]
  (log/debug "get-application-info")
  (later #(on-success (get @state :application-info))))

(defn factory-reset
  [args]
  (log/debug "factory-reset")
  (reset-state)
  (get-application-info args))

(defn install-applet [_])
(defn install-cash-applet [_])

(defn init-card
  [{:keys [pin on-success]}]
  (reset! initialization true)
  (swap! state assoc
    :application-info
    {:free-pairing-slots 5
     :app-version "3.0"
     :secure-channel-pub-key
     "04c3071768912a515c00aeab7ceb8a5bfda91d036f4a4e60b7944cee3ca7fb67b6d118e8df1e2480b87fd636c6615253245bbbc93a6a407f155f2c58f76c96ef0e"
     :key-uid ""
     :instance-uid "1b360b10a9a68b7d494e8f059059f118"
     :paired? false
     :has-master-key? false
     :pin-retry-counter 3
     :puk-retry-counter 5
     :initialized? true})
  (swap! state assoc :pin pin :puk default-puk :password kk1-password)
  (later
   #(on-success {:password kk1-password
                 :puk      default-puk
                 :pin      pin})))

(defn install-applet-and-init-card [_])

(def derived-acc (atom nil))

(defn multiaccount->keys
  [data]
  (let [{:keys [selected-id multiaccounts root-key derived]}
        data

        {:keys [id address public-key derived key-uid]}
        (or (->> multiaccounts
                 (filter #(= (:id %) selected-id))
                 first)
            (assoc root-key :derived derived))

        whisper (get derived constants/path-whisper-keyword)
        wallet (get derived constants/path-default-wallet-keyword)
        wallet-root (get derived constants/path-wallet-root-keyword)
        password account-password
        response {:key-uid key-uid
                  :encryption-public-key password
                  :address address
                  :whisper-public-key (:public-key whisper)
                  :instance-uid "1b360b10a9a68b7d494e8f059059f118"
                  :wallet-root-public-key (:public-key wallet-root)
                  :wallet-root-address (:address wallet-root)
                  :whisper-address (:address whisper)
                  :public-key public-key
                  :whisper-private-key "34bc7d0c258c4f2ac1dac4fd6c55c9478bac1f4a9d8b9f1152c8551ab7187b43"
                  :wallet-address (:address wallet)
                  :wallet-public-key (:public-key wallet)}]
    [id response]))

(defn pair
  [{:keys [password on-success]}]
  (when (and (not @initialization)
             (not @derived-acc))
    (native-module/multiaccount-import-mnemonic
     "night fortune spider version version armed amused winner matter tonight cave flag"
     nil
     (fn [result]
       (let [{:keys [id] :as root-data}
             (multiaccounts.create/normalize-multiaccount-data-keys
              (types/json->clj result))]
         (native-module.core/multiaccount-derive-addresses
          id
          [constants/path-wallet-root
           constants/path-eip1581
           constants/path-whisper
           constants/path-default-wallet]
          (fn [result]
            (let [derived-data (multiaccounts.create/normalize-derived-data-keys
                                (types/json->clj result))
                  public-key   (get-in derived-data [constants/path-whisper-keyword :public-key])]
              (native-module/gfycat-identicon-async
               public-key
               (fn [name _]
                 (let [derived-data-extended
                       (update derived-data constants/path-whisper-keyword assoc :name name)]
                   (reset! derived-acc
                     {:root-key root-data
                      :derived  derived-data-extended})))))))))))
  (when (= password (get @state :password))
    (swap! state assoc-in [:application-info :paired?] true)
    (later #(on-success (str (rand-int 10000000))))))

(defn generate-and-load-key
  [{:keys [pin on-success key-uid delete-multiaccount?]}]
  (when (= pin (get @state :pin))
    (let [[id response] (multiaccount->keys
                         (:intro-wizard @re-frame.db/app-db))]
      (log/debug "[simulated kk] generate-and-load-key response" response)
      (swap! state assoc-in
        [:application-info :key-uid]
        (:key-uid response))
      (let [deletion-wrapper
            (if delete-multiaccount?
              (fn [on-deletion-success]
                (js/alert "OH SHEET")
                (native-module/delete-multiaccount
                 key-uid
                 (fn [result]
                   (let [{:keys [error]} (types/json->clj result)]
                     (if-not (string/blank? error)
                       (log/error error)
                       (on-deletion-success))))))
              (fn [cb] (cb)))]
        (deletion-wrapper
         (fn []
           (native-module/multiaccount-store-derived
            id
            (:key-uid response)
            [constants/path-wallet-root
             constants/path-eip1581
             constants/path-whisper
             constants/path-default-wallet]
            account-password
            #(on-success response))))))))

(defn unblock-pin
  [{:keys [puk new-pin on-success on-failure]}]
  (cond
    (= (get-in @state [:application-info :puk-retry-counter]) 0)
    (later #(on-failure (puk-error)))

    (= puk (get @state :puk))
    (do
      (swap! state update
        :application-info  assoc
        :pin-retry-counter 3
        :puk-retry-counter 5)
      (swap! state assoc :pin new-pin)
      (later #(on-success true)))

    :else
    (do
      (swap! state update-in
        [:application-info :puk-retry-counter]
        (fnil dec 5))
      (later
       #(on-failure (puk-error))))))

(defn verify-pin
  [{:keys [pin on-success on-failure]}]
  (with-pin pin on-failure #(on-success 3)))

(defn change-pin
  [{:keys [current-pin new-pin on-success on-failure]}]
  (with-pin current-pin
            on-failure
            (fn []
              (swap! state assoc :pin new-pin)
              (on-success))))

(defn change-puk
  [{:keys [pin puk on-success on-failure]}]
  (with-pin pin
            on-failure
            (fn []
              (swap! state assoc :puk puk)
              (on-success))))

(defn change-pairing
  [{:keys [pin pairing on-success on-failure]}]
  (with-pin pin
            on-failure
            (fn []
              (swap! state assoc :password pairing)
              (on-success))))

(defn unpair
  [args]
  (log/warn "unpair not implemented" args))
(defn delete
  [args]
  (log/warn "delete not implemented" args))
(defn remove-key
  [args]
  (log/warn "remove-key not implemented" args))
(defn remove-key-with-unpair
  [args]
  (log/warn "remove-key-with-unpair not implemented" args))

(defn normalize-path
  [path]
  (if (string/starts-with? path "m/")
    (str constants/path-wallet-root
         "/"
         (last (string/split path "/")))
    path))

(defn export-key
  [{:keys [pin on-success on-failure]}]
  (with-pin
   pin
   on-failure
   #(let [{:keys [key-uid wallet-root-address]}
          (get @re-frame.db/app-db :profile/profile)
          accounts (get @re-frame.db/app-db :profile/wallet-accounts)
          hashed-password account-password
          path-num (inc (get-in @re-frame.db/app-db
                                [:profile/profile :latest-derived-path]))
          path (str "m/" path-num)]
      (native-module/multiaccount-load-account
       wallet-root-address
       hashed-password
       (fn [value]
         (let [{:keys [id error]} (types/json->clj value)]
           (if error
             (re-frame/dispatch [::new-account-error :password-error error])
             (native-module/multiaccount-derive-addresses
              id
              [path]
              (fn [derived]
                (let [derived-address (get-in (types/json->clj derived) [(keyword path) :address])]
                  (if (some (fn [a] (= derived-address (get a :address))) accounts)
                    (re-frame/dispatch [::new-account-error :account-error
                                        (i18n/label :t/account-exists-title)])
                    (native-module/multiaccount-store-derived
                     id
                     key-uid
                     [path]
                     hashed-password
                     (fn [result]
                       (let [{:keys [error] :as result} (types/json->clj result)
                             {:keys [publicKey]}        (get result (keyword path))]
                         (if error
                           (on-failure error)
                           (on-success publicKey))))))))))))))))

(defn unpair-and-delete [_])

(defn get-keys
  [{:keys [on-success on-failure pin]}]
  (with-pin pin
            on-failure
            (if @derived-acc
              (let [[id account-keys] (multiaccount->keys @derived-acc)]
                (swap! state assoc-in
                  [:application-info :key-uid]
                  (:key-uid account-keys))
                (native-module/multiaccount-store-derived
                 id
                 (:key-uid account-keys)
                 [constants/path-wallet-root
                  constants/path-eip1581
                  constants/path-whisper
                  constants/path-default-wallet]
                 account-password
                 #(on-success account-keys)))
              #(on-success
                {:key-uid               (get-in @state [:application-info :key-uid])
                 :instance-uid          (get-in @state [:application-info :instance-uid])
                 :encryption-public-key account-password}))))

(def import-keys get-keys)

(defn sign
  [{:keys [pin data path typed? on-success on-failure] :as card}]
  (with-pin pin
            on-failure
            #(let [address
                   (if path
                     (reduce
                      (fn [_ {:keys [address] :as acc}]
                        (when (= path (:path acc))
                          (reduced address)))
                      nil
                      (:profile/wallet-accounts @re-frame.db/app-db))
                     (-> (:profile/wallet-accounts @re-frame.db/app-db)
                         first
                         :address))
                   password account-password]
               (if-not typed?
                 (let [params (types/clj->json
                               {:account  address
                                :password password
                                :data     (or data (str "0x" (:hash card)))})]
                   (native-module/sign-message
                    params
                    (fn [res]
                      (let [signature (-> res
                                          types/json->clj
                                          :result
                                          ethereum/normalized-hex)]
                        (on-success signature)))))
                 (native-module/sign-typed-data
                  data
                  address
                  password
                  (fn [res]
                    (let [signature (-> res
                                        types/json->clj
                                        :result
                                        ethereum/normalized-hex)]
                      (on-success signature))))))))

(defn sign-typed-data
  [args]
  (log/warn "sign-typed-data not implemented" args))

(defn save-multiaccount-and-login
  [{:keys [key-uid multiaccount-data password settings node-config accounts-data]}]
  (native-module/save-account-and-login
   key-uid
   (types/clj->json multiaccount-data)
   password
   (types/clj->json settings)
   node-config
   (types/clj->json accounts-data)))

(defn login
  [{:keys [key-uid multiaccount-data password]}]
  (native-module/login-with-config key-uid multiaccount-data password node/login-node-config))

(defn send-transaction-with-signature
  [{:keys [transaction on-completed]}]
  (native-module/send-transaction transaction account-password on-completed))

(defn delete-multiaccount-before-migration
  [{:keys [on-success]}]
  (on-success))

(defrecord SimulatedKeycard []
  keycard/Keycard
    (keycard/start-nfc [_this args]
      (log/debug "simulated card start-nfc")
      (start-nfc args))
    (keycard/stop-nfc [_this args]
      (log/debug "simulated card stop-nfc")
      (stop-nfc args))
    (keycard/set-nfc-message [_this args]
      (log/debug "simulated card set-nfc-message")
      (set-nfc-message args))
    (keycard/check-nfc-support [_this args]
      (log/debug "simulated card check-nfc-support")
      (check-nfc-support args))
    (keycard/check-nfc-enabled [_this args]
      (log/debug "simulated card check-nfc-enabled")
      (check-nfc-enabled args))
    (keycard/open-nfc-settings [_this]
      (log/debug "simulated card open-nfc-setting")
      (open-nfc-settings))
    (keycard/register-card-events [_this args]
      (log/debug "simulated card register-card-event")
      (register-card-events args))
    (keycard/set-pairings [_this args]
      (log/debug "simulated card set-pairings")
      (set-pairings args))
    (keycard/on-card-connected [_this callback]
      (log/debug "simulated card on-card-connected")
      (on-card-connected callback))
    (keycard/on-card-disconnected [_this callback]
      (log/debug "simulated card on-card-disconnected")
      (on-card-disconnected callback))
    (keycard/remove-event-listener [_this event]
      (log/debug "simulated card remove-event-listener")
      (remove-event-listener event))
    (keycard/remove-event-listeners [_this]
      (log/debug "simulated card remove-event-listener")
      (remove-event-listeners))
    (keycard/get-application-info [_this args]
      (log/debug "simulated card get-application-info")
      (get-application-info args))
    (keycard/factory-reset [_this args]
      (log/debug "simulated card factory-reset")
      (factory-reset args))
    (keycard/install-applet [_this args]
      (log/debug "simulated card install-applet")
      (install-applet args))
    (keycard/init-card [_this args]
      (log/debug "simulated card init-card")
      (init-card args))
    (keycard/install-applet-and-init-card [_this args]
      (log/debug "simulated card install-applet-and-init-card")
      (install-applet-and-init-card args))
    (keycard/pair [_this args]
      (log/debug "simulated card pair")
      (pair args))
    (keycard/generate-and-load-key [_this args]
      (log/debug "simulated card generate-and-load-key")
      (generate-and-load-key args))
    (keycard/unblock-pin [_this args]
      (log/debug "simulated card unblock-pin")
      (unblock-pin args))
    (keycard/verify-pin [_this args]
      (log/debug "simulated card verify-pin")
      (verify-pin args))
    (keycard/change-pin [_this args]
      (log/debug "simulated card change-pin")
      (change-pin args))
    (keycard/change-puk [_this args]
      (log/debug "simulated card change-puk")
      (change-puk args))
    (keycard/change-pairing [_this args]
      (log/debug "simulated card change-pairing")
      (change-pairing args))
    (keycard/unpair [_this args]
      (log/debug "simulated card unpair")
      (unpair args))
    (keycard/delete [_this args]
      (log/debug "simulated card delete")
      (delete args))
    (keycard/remove-key [_this args]
      (log/debug "simulated card remove-key")
      (remove-key args))
    (keycard/remove-key-with-unpair [_this args]
      (log/debug "simulated card remove-key-with-unpair")
      (remove-key-with-unpair args))
    (keycard/export-key [_this args]
      (log/debug "simulated card export-key")
      (export-key args))
    (keycard/unpair-and-delete [_this args]
      (log/debug "simulated card unpair-and-delete")
      (unpair-and-delete args))
    (keycard/import-keys [_this args]
      (log/debug "simulated card import-keys")
      (import-keys args))
    (keycard/get-keys [_this args]
      (log/debug "simulated card get-keys")
      (get-keys args))
    (keycard/sign [_this args]
      (log/debug "simulated card sign")
      (sign args))
    (keycard/save-multiaccount-and-login [_this args]
      (log/debug "simulated card save-multiaccount-and-login")
      (save-multiaccount-and-login args))
    (keycard/login [_this args]
      (log/debug "simulated card login")
      (login args))
    (keycard/send-transaction-with-signature [_this args]
      (log/debug "simulated card send-transaction-with-signature")
      (send-transaction-with-signature args))
    (keycard/delete-multiaccount-before-migration [_ args]
      (log/debug "simulated card delete-multiaccount-before-migration")
      (delete-multiaccount-before-migration args)))
