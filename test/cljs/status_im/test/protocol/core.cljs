(ns status-im.test.protocol.core
  (:require [cljs.test :refer-macros [deftest is testing run-tests async]]
            [cljs.nodejs :as nodejs]
            [status-im.protocol.web3.utils :as web3.utils]
            [status-im.test.protocol.node :as node]
            [status-im.test.protocol.utils :as utils]
            [status-im.protocol.core :as protocol]))

;; NOTE(oskarth): All these tests are evaluated in NodeJS

(nodejs/enable-util-print!)

;; NOTE(oskarth): If status-go has already been built correctly, comment this out
(node/prepare-env!)

(def rpc-url "http://localhost:8645")
(def Web3 (js/require "web3"))
(defn make-web3 []
  (Web3. (Web3.providers.HttpProvider. rpc-url)))

(defn make-callback [identity done]
  (fn [& args]
    (is (contains? #{:sent :pending} (first args)))
    (when (= (first args) :sent)
      (protocol/reset-all-pending-messages!)
      (protocol/stop-watching-all!)
      (node/stop!)
      (done)
      (utils/exit!))))

(defn post-error-callback [identity]
  (fn [& args]
    (.log js/console (str :post-error " " identity "\n" args))))

(defn id-specific-config
  [id {:keys [private public]} contacts done]
  {:identity            id
   :callback            (make-callback id done)
   :profile-keypair     {:public  public
                         :private private}
   :contacts            contacts
   :post-error-callback (post-error-callback id)})

;; NOTE(oskarth): Test assumes callback is always called, otherwise it won't terminate
;; TODO(oskarth): Add a timeout async function where we fail test if it takes too long to finish
;; TODO(oskarth): Fix this test, issue with private key not being persisted
#_(deftest test-send-message!
  (async done
    (node/start!)
    (let [web3          (make-web3)
          id1-keypair   (protocol/new-keypair!)
          common-config {:web3                        web3
                         :groups                      []
                         :ack-not-received-s-interval 125
                         :default-ttl                 120
                         :send-online-s-interval      180
                         :ttl-config                  {:public-group-message 2400}
                         :max-attempts-number         3
                         :delivery-loop-ms-interval   500
                         :hashtags                    []
                         :pending-messages            []}
          id1-config    (id-specific-config node/identity-1 id1-keypair [] done)]
      (protocol/init-whisper! (merge common-config id1-config))
      (protocol/send-message!
        {:web3    web3
         :message {:message-id "123"
                   :from       node/identity-1
                   :to         node/identity-2
                   :payload    {:type         :message
                                :content-type "text/plain"
                                :content      "123"
                                :timestamp    1498723691404}}}))))

;; TODO(oskarth): Add fixtures to start node before running tests against node
(deftest test-whisper-version!
  (testing "Whisper version supported"
    (async done
           (node/start!)
           (let [web3 (make-web3)
                 shh  (web3.utils/shh web3)]
             (.version shh
                       (fn [& args]
                         (is (= "5.0" (second args)))
                         (done)))))))
