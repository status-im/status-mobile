(ns status-im.test.browser.permissions
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.browser.permissions :as permissions]
            [status-im.utils.types :as types]
            [status-im.browser.core :as browser]))

(deftest permissions-test
  (let [dapp-name         "test.com"
        dapp-name2        "test2.org"
        cofx {:db (assoc-in (:db (browser/open-url {:db {}} dapp-name))
                            [:account/account :public-key] "public-key")}]
    (testing "dapps permissions are initialized"
      (is (zero? (count (get-in cofx [:db :dapps/permissions]))))
      (is (= dapp-name (get-in cofx [:db :browser/options :browser-id]))))

    (testing "receiving an unsupported permission"
      (is (nil? (:browser/send-to-bridge (browser/process-bridge-message cofx
                                                                         (types/clj->json {:type        "status-api-request"
                                                                                           :host        dapp-name
                                                                                           :permissions ["FAKE_PERMISSION"]}))))
          "nothing should happen"))

    (testing "receiving a supported permission and an unsupported one"
      (let [result-ask (browser/process-bridge-message cofx
                                                       (types/clj->json {:type        "status-api-request"
                                                                         :host        dapp-name
                                                                         :permissions ["CONTACT_CODE" "FAKE_PERMISSION"]}))]
        (is (= (get-in result-ask [:db :browser/options :show-permission])
               {:requested-permission "CONTACT_CODE", :dapp-name "test.com"}))
        (is (zero? (count (get-in result-ask [:db :dapps/permissions]))))

        (testing "then user accepts the supported permission"
          (let [accept-result (permissions/allow-permission {:db (:db result-ask)} dapp-name "CONTACT_CODE")]
            (is (= (get-in accept-result [:browser/send-to-bridge :message])
                   {:type "status-api-success"
                    :data {"CONTACT_CODE" "public-key"}
                    :keys ["CONTACT_CODE"]})
                "the data should have been sent to the bridge")
            (is (= (get-in accept-result [:db :dapps/permissions])
                   {"test.com" {:dapp "test.com", :permissions ["CONTACT_CODE"]}})
                "the dapp should now have CONTACT_CODE permission")

            (testing "then dapps asks for permission again"
              (let [result-ask-again (browser/process-bridge-message {:db (:db accept-result)}
                                                                     (types/clj->json {:type        "status-api-request"
                                                                                       :host        dapp-name
                                                                                       :permissions ["CONTACT_CODE"]}))]
                (is (= (get-in result-ask-again
                               [:browser/send-to-bridge :message])
                       {:type "status-api-success"
                        :data {"CONTACT_CODE" "public-key"}
                        :keys ["CONTACT_CODE"]})
                    "the response should be immediatly sent to the bridge")))

            (testing "then user switch to another dapp that asks for permissions"
              (let [new-dapp (browser/open-url {:db (:db accept-result)} dapp-name2)
                    result-ask2 (browser/process-bridge-message {:db (:db new-dapp)}
                                                                (types/clj->json {:type        "status-api-request"
                                                                                  :host        dapp-name2
                                                                                  :permissions ["CONTACT_CODE" "FAKE_PERMISSION"]}))]
                (is (= (get-in result-ask2 [:db :dapps/permissions])
                       {"test.com" {:dapp "test.com", :permissions ["CONTACT_CODE"]}})
                    "there should only be permissions for dapp-name at that point")
                (is (nil? (get-in result-ask2
                                  [:browser/send-to-bridge :message]))
                    "no message should be sent to the bridge")

                (testing "then user accepts permission for dapp-name2"
                  (let [accept-result2 (permissions/allow-permission {:db (:db result-ask2)} dapp-name2 "CONTACT_CODE")]
                    (is (= (get-in accept-result2 [:db :dapps/permissions])
                           {"test.com" {:dapp "test.com" :permissions ["CONTACT_CODE"]}
                            "test2.org" {:dapp "test2.org" :permissions ["CONTACT_CODE"]}})
                        "there should be permissions for both dapps now")
                    (is (= (get-in accept-result2
                                   [:browser/send-to-bridge :message])
                           {:type "status-api-success"
                            :data {"CONTACT_CODE" "public-key"}
                            :keys ["CONTACT_CODE"]})
                        "the response should be sent to the bridge")))))

            (testing "then user refuses the permission"
              (let [result-refuse (permissions/process-next-permission {:db (:db result-ask)} dapp-name)]
                (is (zero? (count (get-in result-refuse [:db :dapps/permissions])))
                    "no permissions should be granted")
                (is (nil? (get-in result-refuse [:browser/send-to-bridge :message]))
                    "no message should be sent to bridge")))))))))
