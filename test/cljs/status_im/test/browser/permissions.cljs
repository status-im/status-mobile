(ns status-im.test.browser.permissions
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.browser.permissions :as permissions]
            [status-im.utils.types :as types]
            [status-im.browser.core :as browser]
            [status-im.test.browser.core :as core.tests]))

(deftest permissions-test
  (let [dapp-name  "test.com"
        dapp-name2 "test2.org"
        cofx       {:db (assoc-in (:db (browser/open-url {:db {}} dapp-name))
                                  [:multiaccount :public-key] "public-key")}
        dapp-id (core.tests/get-dapp-id cofx dapp-name)]
    (testing "dapps permissions are initialized"
      (is (zero? (count (get-in cofx [:db :dapps/permissions]))))
      (is (= dapp-id (get-in cofx [:db :browser/options :browser-id]))))

    (testing "receiving an unsupported permission"
      (let [result-ask (browser/process-bridge-message cofx
                                                       (types/clj->json {:type       "api-request"
                                                                         :host       dapp-name
                                                                         :messageId  0
                                                                         :permission "FAKE_PERMISSION"}))]
        (is (not (get-in result-ask [:browser/send-to-bridge :isAllowed])))))

    (testing "receiving a supported permission"
      (let [result-ask (browser/process-bridge-message cofx
                                                       (types/clj->json {:type       "api-request"
                                                                         :host       dapp-name
                                                                         :messageId  1
                                                                         :permission "contact-code"}))]
        (is (= (get-in result-ask [:db :browser/options :show-permission])
               {:requested-permission "contact-code" :dapp-name "test.com" :message-id 1 :yield-control? nil}))
        (is (zero? (count (get-in result-ask [:db :dapps/permissions]))))

        (testing "then user accepts the supported permission"
          (let [accept-result (permissions/allow-permission {:db (:db result-ask)})]
            (is (= (get accept-result :browser/send-to-bridge)
                   {:type       "api-response"
                    :messageId  1
                    :isAllowed  true
                    :data       "public-key"
                    :permission "contact-code"})
                "the data should have been sent to the bridge")
            (is (= (get-in accept-result [:db :dapps/permissions])
                   {"test.com" {:dapp "test.com", :permissions ["contact-code"]}})
                "the dapp should now have CONTACT_CODE permission")

            (testing "then dapp asks for permission again"
              (let [result-ask-again (browser/process-bridge-message {:db (:db accept-result)}
                                                                     (types/clj->json {:type       "api-request"
                                                                                       :host       dapp-name
                                                                                       :messageId  2
                                                                                       :permission "contact-code"}))]
                (is (= (get result-ask-again :browser/send-to-bridge)
                       {:type       "api-response"
                        :isAllowed  true
                        :messageId  2
                        :data       "public-key"
                        :permission "contact-code"})
                    "the response should be immediatly sent to the bridge")))

            (testing "then user switch to another dapp that asks for permissions"
              (let [new-dapp    (browser/open-url {:db (:db accept-result)} dapp-name2)
                    result-ask2 (browser/process-bridge-message {:db (:db new-dapp)}
                                                                (types/clj->json {:type       "api-request"
                                                                                  :host       dapp-name2
                                                                                  :messageId  3
                                                                                  :permission "contact-code"}))]
                (is (= (get-in result-ask2 [:db :dapps/permissions])
                       {"test.com" {:dapp "test.com", :permissions ["contact-code"]}})
                    "there should only be permissions for dapp-name at that point")
                (is (nil? (get result-ask2 :browser/send-to-bridge))
                    "no message should be sent to the bridge")

                (testing "then user accepts permission for dapp-name2"
                  (let [accept-result2 (permissions/allow-permission {:db (:db result-ask2)})]
                    (is (= (get-in accept-result2 [:db :dapps/permissions])
                           {"test.com"  {:dapp "test.com" :permissions ["contact-code"]}
                            "test2.org" {:dapp "test2.org" :permissions ["contact-code"]}})
                        "there should be permissions for both dapps now")
                    (is (= (get accept-result2 :browser/send-to-bridge)
                           {:type       "api-response"
                            :isAllowed  true
                            :messageId  3
                            :data       "public-key"
                            :permission "contact-code"})
                        "the response should be sent to the bridge")))))))))))
