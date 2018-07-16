(ns status-im.native-module.impl.non-status-go-module
  (:require [status-im.native-module.module :as module]
            [status-im.native-module.impl.module :as impl]
            [status-im.constants :as constants]
            [re-frame.core :as re-frame]
            [status-im.thread :as status-im.thread]
            [goog.string :as gstring]
            [goog.string.format]))

(def wrong-password "111")

(defrecord ReactNativeStatus []
  module/IReactNativeStatus
  ;; status-go calls
  (-init-jail [this])
  (-start-node [this config]
    (status-im.thread/dispatch [:signal-event "{\"type\":\"node.started\",\"event\":{}}"])
    (status-im.thread/dispatch [:signal-event "{\"type\":\"node.ready\",\"event\":{}}"]))
  (-stop-node [this])
  (-create-account [this password callback]
    (let [address (str "c9f5c0e2bea0aabb6b0b618e9f45ab0958" (gstring/format "%06d" (rand-int 100000)))]
      (callback (str "{\"address\":\"" address "\",\"pubkey\":\"0x046a313ba760e8853356b42a8732db1e2c339602977a3ac3d57ec2056449439b2c9f28e2e0dd243ac319f5da198b4a96f980d0ab6d4c7220ca7c5e1af2bd1ee8c7\",\"mnemonic\":\"robust rib ramp adult cannon amateur refuse burden review feel scout sell\",\"error\":\"\"}"))))
  (-recover-account [this passphrase password callback])
  (-login [this address password callback]
    (if (not= password wrong-password)
      (callback "{\"error\":\"\"}")
      (callback "{\"error\":\"cannot retrieve a valid key for a given account: could not decrypt key with given passphrase\"}")))
  (-approve-sign-request [this id password callback])
  (-approve-sign-request-with-args [this id password gas gas-price callback])
  (-discard-sign-request [this id])
  (-parse-jail [this chat-id file callback]
    (when (= chat-id constants/console-chat-id)
      (callback "{\"result\":\"{\\\"commands\\\":{\\\"phone,50\\\":{\\\"name\\\":\\\"phone\\\",\\\"title\\\":\\\"Send Phone Number\\\",\\\"description\\\":\\\"Find friends using your number\\\",\\\"has-handler\\\":true,\\\"async-handler\\\":false,\\\"color\\\":\\\"#5bb2a2\\\",\\\"icon\\\":\\\"phone_white\\\",\\\"params\\\":[{\\\"name\\\":\\\"phone\\\",\\\"type\\\":\\\"phone\\\",\\\"placeholder\\\":\\\"Phone number\\\"}],\\\"sequential-params\\\":true,\\\"scope\\\":[\\\"personal-chats\\\",\\\"registered\\\",\\\"dapps\\\"],\\\"scope-bitmask\\\":50},\\\"faucet,50\\\":{\\\"name\\\":\\\"faucet\\\",\\\"title\\\":\\\"Faucet\\\",\\\"description\\\":\\\"Get some ETH\\\",\\\"has-handler\\\":true,\\\"async-handler\\\":false,\\\"color\\\":\\\"#7099e6\\\",\\\"params\\\":[{\\\"name\\\":\\\"url\\\",\\\"type\\\":\\\"text\\\",\\\"placeholder\\\":\\\"Faucet URL\\\"}],\\\"scope\\\":[\\\"personal-chats\\\",\\\"registered\\\",\\\"dapps\\\"],\\\"scope-bitmask\\\":50},\\\"debug,50\\\":{\\\"name\\\":\\\"debug\\\",\\\"title\\\":\\\"Debug mode\\\",\\\"description\\\":\\\"Starts\\/stops a debug mode\\\",\\\"has-handler\\\":true,\\\"async-handler\\\":false,\\\"color\\\":\\\"#7099e6\\\",\\\"params\\\":[{\\\"name\\\":\\\"mode\\\",\\\"type\\\":\\\"text\\\"}],\\\"scope\\\":[\\\"personal-chats\\\",\\\"registered\\\",\\\"dapps\\\"],\\\"scope-bitmask\\\":50}},\\\"responses\\\":{\\\"phone,50\\\":{\\\"name\\\":\\\"phone\\\",\\\"title\\\":\\\"Send Phone Number\\\",\\\"description\\\":\\\"Find friends using your number\\\",\\\"has-handler\\\":true,\\\"async-handler\\\":false,\\\"color\\\":\\\"#5bb2a2\\\",\\\"icon\\\":\\\"phone_white\\\",\\\"params\\\":[{\\\"name\\\":\\\"phone\\\",\\\"type\\\":\\\"phone\\\",\\\"placeholder\\\":\\\"Phone number\\\"}],\\\"sequential-params\\\":true,\\\"scope\\\":[\\\"personal-chats\\\",\\\"registered\\\",\\\"dapps\\\"],\\\"scope-bitmask\\\":50},\\\"confirmation-code,50\\\":{\\\"name\\\":\\\"confirmation-code\\\",\\\"description\\\":\\\"Confirmation code\\\",\\\"has-handler\\\":true,\\\"async-handler\\\":false,\\\"color\\\":\\\"#7099e6\\\",\\\"params\\\":[{\\\"name\\\":\\\"code\\\",\\\"type\\\":\\\"number\\\"}],\\\"sequential-params\\\":true,\\\"scope\\\":[\\\"personal-chats\\\",\\\"registered\\\",\\\"dapps\\\"],\\\"scope-bitmask\\\":50},\\\"password,42\\\":{\\\"name\\\":\\\"password\\\",\\\"description\\\":\\\"Password\\\",\\\"has-handler\\\":true,\\\"async-handler\\\":false,\\\"color\\\":\\\"#7099e6\\\",\\\"icon\\\":\\\"lock_white\\\",\\\"params\\\":[{\\\"name\\\":\\\"password\\\",\\\"type\\\":\\\"password\\\",\\\"placeholder\\\":\\\"Type your password\\\",\\\"hidden\\\":true},{\\\"name\\\":\\\"password-confirmation\\\",\\\"type\\\":\\\"password\\\",\\\"placeholder\\\":\\\"Confirm\\\",\\\"hidden\\\":true}],\\\"sequential-params\\\":true,\\\"scope\\\":[\\\"personal-chats\\\",\\\"anonymous\\\",\\\"dapps\\\"],\\\"scope-bitmask\\\":42},\\\"grant-permissions,58\\\":{\\\"name\\\":\\\"grant-permissions\\\",\\\"description\\\":\\\"Grant permissions\\\",\\\"has-handler\\\":true,\\\"async-handler\\\":false,\\\"color\\\":\\\"#7099e6\\\",\\\"icon\\\":\\\"lock_white\\\",\\\"params\\\":[],\\\"execute-immediately?\\\":true,\\\"scope\\\":[\\\"personal-chats\\\",\\\"anonymous\\\",\\\"registered\\\",\\\"dapps\\\"],\\\"scope-bitmask\\\":58}},\\\"functions\\\":{},\\\"subscriptions\\\":{}}\"}")))
  (-call-jail [this {:keys [callback path] :as params}]
    (cond
      (= path [:responses "password" :preview])
      (callback {:result {:context  {},
                          :messages {},
                          :returned {:markup ["text"
                                              {:style
                                               {:color             "black",
                                                :font-size         8,
                                                :letter-spacing    1,
                                                :margin-bottom     2,
                                                :margin-horizontal 0,
                                                :margin-top        10}}
                                              "●●●●●●●●●●"]}}})
      :else (callback {:result nil})))
  (-call-function! [this params])
  (-call-web3 [this payload callback])
  (-call-web3-private [this payload callback])

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
    (impl/should-move-to-internal-storage? callback))
  (-notify-users [this {:keys [message payload tokens] :as m} callback])
  (-add-peer [this enode callback])
  (-close-application [this])
  (-connection-change [this data])
  (-app-state-change [this state]))
