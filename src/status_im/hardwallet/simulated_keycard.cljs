(ns status-im.hardwallet.simulated-keycard
  (:require [status-im.hardwallet.keycard :as keycard]
            [status-im.utils.utils :as utils]))

(def initial-state
  {:card-connected?  false
   :application-info {:initialized? false}})

(defonce state (atom initial-state))

(defn connect-card []
  (swap! state assoc :card-connected? true)
  (doseq [callback (vals (get @state :on-card-connected))]
    (callback)))

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

(defn install-applet [args])
(defn install-cash-applet [args])

(def kk1-password "6d9ZHjn94kFP4bPm")

(defn init-card [{:keys [pin on-success]}]
  (swap! state assoc :application-info
         {:free-pairing-slots     5
          :app-version            "2.2"
          :secure-channel-pub-key "04c3071768912a515c00aeab7ceb8a5bfda91d036f4a4e60b7944cee3ca7fb67b6d118e8df1e2480b87fd636c6615253245bbbc93a6a407f155f2c58f76c96ef0e", :key-uid "", :instance-uid "9c3f27ee5dfc39c2b14f4d6d3379cd68"
          :paired?                false
          :has-master-key?        false
          :initialized?           true})
  (swap! state assoc :pin pin)
  (later
   #(on-success {:password kk1-password
                 :puk       "320612366918"
                 :pin       pin})))

(defn install-applet-and-init-card [args])

(def kk1-pair "ADEol+GCD67EO7zU6ko0DNK7XrNs9w2+h9GxcibNY4yf")

(defn pair [{:keys [password on-success]}]
  (when (= password kk1-password)
    (later #(on-success kk1-pair))))

(defn generate-mnemonic [args])

(defn generate-and-load-key [{:keys [pin pairing on-success]}]
  (when (and (= pin (get @state :pin))
             (= pairing kk1-pair))
    (later
     #(on-success
       {:key-uid                "08f1e42f076b956715dac6b93ad1282e435be877a90c9353f6c6dfe455474047"
        :encryption-public-key  "04a15b33d5c76ff72c3b3863fe2cb2b45c25f87c6accc96fa95457845e3f69ba5fc2d835351d17b5031e1723513824612003facb98f508af2866382ed996125b4d"
        :address                "f75457177cd2b7bdc407a6c4881eb490f66ca3c2"
        :whisper-public-key     "04d25f563a8a2897a7025a1f022eee78ba7c0e182aae04ab640bc9e118698734257647e18cb6c95f825e6d03d8e3550178b13a30dceba722be7c8fcd0adecc0fa9"
        :instance-uid           "1b360b10a9a68b7d494e8f059059f118"
        :wallet-root-public-key "0463187f5c917eef481e04af704c14e57a9e8596516f0ec10a4556561ad49b5aa249976ec545d37d04f4d4c7d1c0d9a2141dc61e458b09631d25fa7858c6323ea3"
        :wallet-root-address    "e034a084d2282e265f83e3fdfa48b42c3d53312a"
        :whisper-address        "87f1c9bbe1c907143413cf018caad355dde16b3c"
        :public-key             "04035d4efe4e96f8fa0e49a94433c972e510f0c8698348b4e1acd3b4d3083c61283b932ec54dd9512566931b26627a5d3122a916577459b7926fce6a278055f899"
        :whisper-private-key    "34bc7d0c258c4f2ac1dac4fd6c55c9478bac1f4a9d8b9f1152c8551ab7187b43"
        :wallet-address         "c8435ef92bbb76bc1861833713e202e18ebd4601"
        :wallet-public-key      "044887a5a2599d722aa1af8cda800a17415d3a071c4706e111ad05465c3bf10fcb6f92c8d74df994160e0ba4aeff71f7a6d256cf36ce8cff3d313b8a0709404886"}))))

(defn unblock-pin [args])

(defn verify-pin [{:keys [pin pairing on-success]}]
  (when (and (= pairing kk1-pair)
             (= pin (get @state :pin)))
    (later #(on-success 3))))

(defn change-pin [args])
(defn unpair [args])
(defn delete [args])
(defn remove-key [args])
(defn remove-key-with-unpair [args])
(defn export-key [args])
(defn unpair-and-delete [args])
(defn get-keys [args])
(defn sign [args])
(defn sign-typed-data [args])

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
  (keycard/generate-mnemonic [this args]
    (generate-mnemonic args))
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
    (sign args)))
