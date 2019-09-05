(ns status-im.test.protocol.core
  (:require [cljs.test :refer-macros [deftest is testing async]]
            [cljs.nodejs :as nodejs]
            re-frame.db
            status-im.ui.screens.events
            [re-frame.core :as rf]
            [cljs.core.async :as async]
            [status-im.transport.utils :as transport.utils]
            [day8.re-frame.test :refer-macros [run-test-async run-test-sync] :as rf-test]
            [status-im.test.protocol.node :as node]
            [status-im.transport.message.contact :as transport.contact]
            [status-im.test.protocol.utils :as utils]))

;; NOTE(oskarth): All these tests are evaluated in NodeJS

(nodejs/enable-util-print!)

(def contact-public-key "0x048f7d5d4bda298447bbb5b021a34832509bd1a8dbe4e06f9b7223d00a59b6dc14f6e142b21d3220ceb3155a6d8f40ec115cd96394d3cc7c55055b433a1758dc74")
(def rpc-url (aget nodejs/process.env "WNODE_ADDRESS"))

(def Web3 (js/require "web3"))
(defn make-web3 []
  (Web3. (Web3.providers.HttpProvider. rpc-url)))

(defn create-keys [shh]
  (let [keypair-promise (.newKeyPair shh)]
    (.getPublicKey shh keypair-promise)))

(deftest test-send-message!
  (testing "send contact request & message"
    (run-test-async
     (let [web3 (make-web3)
           shh  (.-shh web3)
           from (create-keys shh)]
       (reset! re-frame.db/app-db {:web3 web3
                                   :multiaccount {:public-key from}})

       (rf/dispatch [:contact.ui/send-message-pressed {:public-key contact-public-key}])
       (rf-test/wait-for [::transport.contact/send-new-sym-key]
                         (rf/dispatch [:set-chat-input-text "test message"])
                         (rf/dispatch [:send-current-message])
                         (rf-test/wait-for [:update-message-status :transport/send-status-message-error]
                                           (is true)))))))

(deftest test-whisper-version!
  (testing "Whisper version supported"
    (async done
           (let [web3 (make-web3)
                 shh  (.-shh web3)]
             (.version shh
                       (fn [& args]
                         (is (= "6.0" (second args)))
                         (done)))))))
