(ns status-im.native-module.impl.non-status-go-module
  (:require [status-im.native-module.module :as module]
            [status-im.native-module.impl.module :as impl]
            [status-im.constants :as constants]
            [re-frame.core :as re-frame]
            [goog.string :as gstring]
            [goog.string.format]))

(def wrong-password "111")

(defrecord ReactNativeStatus []
  module/IReactNativeStatus
  ;; status-go calls
  (-init-jail [this])
  (-start-node [this callback]
    (re-frame/dispatch [:signal-event "{\"type\":\"node.started\",\"event\":{}}"])
    (re-frame/dispatch [:signal-event "{\"type\":\"node.ready\",\"event\":{}}"]))
  (-stop-rpc-server [this])
  (-start-rpc-server [this])
  (-restart-rpc [this])
  (-create-account [this password callback]
    (let [address (str "c9f5c0e2bea0aabb6b0b618e9f45ab0958" (gstring/format "%06d" (rand-int 100000)))]
      (callback (str "{\"address\":\"" address "\",\"pubkey\":\"0x046a313ba760e8853356b42a8732db1e2c339602977a3ac3d57ec2056449439b2c9f28e2e0dd243ac319f5da198b4a96f980d0ab6d4c7220ca7c5e1af2bd1ee8c7\",\"mnemonic\":\"robust rib ramp adult cannon amateur refuse burden review feel scout sell\",\"error\":\"\"}"))))
  (-recover-account [this passphrase password callback])
  (-login [this address password callback]
    (if (not= password wrong-password)
      (callback "{\"error\":\"\"}")
      (callback "{\"error\":\"cannot retrieve a valid key for a given account: could not decrypt key with given passphrase\"}")))
  (-complete-transactions [this hashes password callback])
  (-discard-transaction [this id])
  (-parse-jail [this chat-id file callback]
    (when (= chat-id constants/console-chat-id)
      (callback "{\"result\": {\"commands\":{\"debug\":{\"color\":\"#7099e6\",\"description\":\"Starts/stops a debug mode\",\"has-handler\":false,\"name\":\"debug\",\"params\":[{\"name\":\"mode\",\"type\":\"text\"}],\"registered-only\":true,\"title\":\"Debug mode\"},\"faucet\":{\"color\":\"#7099e6\",\"description\":\"Get some ETH\",\"has-handler\":false,\"name\":\"faucet\",\"params\":[{\"name\":\"url\",\"placeholder\":\"Faucet URL\",\"type\":\"text\"}],\"registered-only\":true,\"title\":\"Faucet\"},\"phone\":{\"color\":\"#5bb2a2\",\"description\":\"Find friends using your number\",\"has-handler\":false,\"icon\":\"phone_white\",\"name\":\"phone\",\"params\":[{\"name\":\"phone\",\"placeholder\":\"Phone number\",\"type\":\"phone\"}],\"registered-only\":true,\"sequential-params\":true,\"title\":\"Send Phone Number\"}},\"functions\":{},\"responses\":{\"confirmation-code\":{\"color\":\"#7099e6\",\"description\":\"Confirmation code\",\"has-handler\":false,\"name\":\"confirmation-code\",\"params\":[{\"name\":\"code\",\"type\":\"number\"}],\"sequential-params\":true},\"grant-permissions\":{\"color\":\"#7099e6\",\"description\":\"Grant permissions\",\"execute-immediately?\":true,\"has-handler\":false,\"icon\":\"lock_white\",\"name\":\"grant-permissions\",\"params\":[]},\"password\":{\"color\":\"#7099e6\",\"description\":\"Password\",\"has-handler\":false,\"icon\":\"lock_white\",\"name\":\"password\",\"params\":[{\"hidden\":true,\"name\":\"password\",\"placeholder\":\"Type your password\",\"type\":\"password\"},{\"hidden\":true,\"name\":\"password-confirmation\",\"placeholder\":\"Confirm\",\"type\":\"password\"}],\"sequential-params\":true},\"phone\":{\"color\":\"#5bb2a2\",\"description\":\"Find friends using your number\",\"has-handler\":false,\"icon\":\"phone_white\",\"name\":\"phone\",\"params\":[{\"name\":\"phone\",\"placeholder\":\"Phone number\",\"type\":\"phone\"}],\"registered-only\":true,\"sequential-params\":true,\"title\":\"Send Phone Number\"}},\"subscriptions\":{}}}")))
  (-call-jail [this {:keys [callback path] :as params}]
    (cond
      (= path [:responses "password" :preview])
      (callback {:result {:context  {},
                          :messages [],
                          :returned {:markup ["text"
                                              {:style
                                               {:color            "black",
                                                :fontSize         8,
                                                :letterSpacing    1,
                                                :marginBottom     2,
                                                :marginHorizontal 0,
                                                :marginTop        10}}
                                              "●●●●●●●●●●"]}}})
      :else (callback {:result nil})))
  (-call-function! [this params])
  (-call-web3 [this host payload callback])

  ;; other calls
  (-move-to-internal-storage [this callback]
    (impl/move-to-internal-storage callback))
  (-set-soft-input-mode [this mode]
    (impl/set-soft-input-mode mode))
  (-clear-web-data [this]
    (impl/clear-web-data))
  (-module-initialized! [this]
    (impl/module-initialized!))
  (-should-move-to-internal-storage? [this callback]
    (impl/should-move-to-internal-storage? callback)))
