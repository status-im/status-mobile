(ns status-im.test.pairing.core
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.utils.identicon :as identicon]
            [status-im.transport.message.pairing :as transport.pairing]
            [status-im.utils.pairing :as pairing.utils]
            [status-im.utils.config :as config]
            [status-im.pairing.core :as pairing]))

(deftest merge-contact-test
  (testing "vanilla contacts"
    (let [contact-1 {:system-tags #{:contact/added}
                     :this-should-be-kept true
                     :last-updated 1
                     :name "name-v1"
                     :photo-path "photo-v1"}
          contact-2 {:system-tags #{:contact/added}
                     :last-updated 2
                     :name "name-v2"
                     :photo-path "photo-v2"}
          expected  {:system-tags #{:contact/added}
                     :this-should-be-kept true
                     :last-updated 2
                     :device-info nil
                     :name "name-v2"
                     :photo-path "photo-v2"}]
      (is (= expected (pairing/merge-contact contact-1 contact-2)))))
  (testing "without last-updated"
    (let [contact-1 {:system-tags #{:contact/added}
                     :name "name-v1"
                     :photo-path "photo-v1"}
          contact-2 {:system-tags #{:contact/added}
                     :last-updated 2
                     :name "name-v2"
                     :photo-path "photo-v2"}
          expected  {:system-tags #{:contact/added}
                     :last-updated 2
                     :device-info nil
                     :name "name-v2"
                     :photo-path "photo-v2"}]
      (is (= expected (pairing/merge-contact contact-1 contact-2)))))
  (testing "nil contact"
    (let [contact-1 nil
          contact-2 {:system-tags #{:contact/added}
                     :last-updated 2
                     :name "name-v2"
                     :photo-path "photo-v2"}
          expected  {:system-tags #{:contact/added}
                     :last-updated 2
                     :device-info nil
                     :name "name-v2"
                     :photo-path "photo-v2"}]
      (is (= expected (pairing/merge-contact contact-1 contact-2)))))
  (testing "added in one device but updated less recently"
    (let [contact-1 {:system-tags #{:contact/added}
                     :last-updated 1
                     :name "name-v1"
                     :photo-path "photo-v1"}
          contact-2 {:system-tags #{}
                     :last-updated 2
                     :name "name-v2"
                     :photo-path "photo-v2"}
          expected  {:system-tags #{}
                     :last-updated 2
                     :device-info nil
                     :name "name-v2"
                     :photo-path "photo-v2"}]
      (is (= expected (pairing/merge-contact contact-1 contact-2)))))
  (testing "added in one device updated more recently"
    (let [contact-1 {:system-tags #{}
                     :last-updated 1
                     :name "name-v1"
                     :photo-path "photo-v1"}
          contact-2 {:system-tags #{:contact/added}
                     :last-updated 2
                     :name "name-v2"
                     :photo-path "photo-v2"}
          expected  {:system-tags #{:contact/added}
                     :last-updated 2
                     :device-info nil
                     :name "name-v2"
                     :photo-path "photo-v2"}]
      (is (= expected (pairing/merge-contact contact-1 contact-2)))))
  (testing "pending in one device and nil"
    (let [contact-1 nil
          contact-2 {:system-tags #{}
                     :last-updated 2
                     :name "name-v2"
                     :photo-path "photo-v2"}
          expected  {:system-tags #{}
                     :last-updated 2
                     :device-info nil
                     :name "name-v2"
                     :photo-path "photo-v2"}]
      (is (= expected (pairing/merge-contact contact-1 contact-2)))))
  (testing "device-info"
    (let [contact-1 {:system-tags #{:contact/added}
                     :last-updated 1
                     :name "name-v1"
                     :device-info {"1" {:timestamp 1
                                        :fcm-token "token-1"
                                        :id "1"}
                                   "2" {:timestamp 1
                                        :fcm-token "token-2"
                                        :id "2"}}
                     :photo-path "photo-v1"}
          contact-2 {:system-tags #{:contact/added}
                     :last-updated 2
                     :name "name-v2"
                     :device-info {"2" {:timestamp 2
                                        :fcm-token "token-2"
                                        :id "2"}
                                   "3" {:timestamp 2
                                        :fcm-token "token-3"
                                        :id "3"}}
                     :photo-path "photo-v2"}
          expected  {:system-tags #{:contact/added}
                     :last-updated 2
                     :device-info {"1" {:timestamp 1
                                        :fcm-token "token-1"
                                        :id "1"}
                                   "2" {:timestamp 2
                                        :fcm-token "token-2"
                                        :id "2"}
                                   "3" {:timestamp 2
                                        :fcm-token "token-3"
                                        :id "3"}}
                     :name "name-v2"
                     :photo-path "photo-v2"}]
      (is (= expected (pairing/merge-contact contact-1 contact-2))))))

(deftest handle-sync-installation-test
  (with-redefs [gfycat/generate-gfy (constantly "generated")
                identicon/identicon (constantly "generated")]
    (testing "syncing contacts"
      (let [old-contact-1   {:name "old-contact-one"
                             :public-key "contact-1"
                             :last-updated 0
                             :photo-path "old-contact-1"
                             :system-tags #{}}
            new-contact-1   {:name "new-contact-one"
                             :public-key "contact-1"
                             :last-updated 1
                             :photo-path "new-contact-1"
                             :system-tags #{:contact/added}}
            old-contact-2   {:name "old-contact-2"
                             :public-key "contact-2"
                             :last-updated 0
                             :photo-path "old-contact-2"
                             :system-tags #{:contact/added}}
            new-contact-2   {:name "new-contact-2"
                             :public-key "contact-2"
                             :last-updated 1
                             :photo-path "new-contact-2"
                             :system-tags #{:contact/added}}
            contact-3        {:name "contact-3"
                              :public-key "contact-3"
                              :photo-path "contact-3"
                              :system-tags #{:contact/added}}
            contact-4        {:name "contact-4"
                              :public-key "contact-4"
                              :system-tags #{}}
            local-contact-5  {:name "contact-5"
                              :photo-path "local"
                              :public-key "contact-5"
                              :system-tags #{}
                              :last-updated 1}
            remote-contact-5 {:name "contact-5"
                              :public-key "contact-5"
                              :photo-path "remote"
                              :system-tags #{}
                              :last-updated 1}
            cofx {:db {:multiaccount {:public-key "us"}
                       :contacts/contacts {"contact-1" old-contact-1
                                           "contact-2" new-contact-2
                                           "contact-3" contact-3
                                           "contact-5" local-contact-5}}}
            sync-message {:contacts {"contact-1" new-contact-1
                                     "contact-2" old-contact-2
                                     "contact-4" contact-4
                                     "contact-5" remote-contact-5}}
            expected {"contact-1" (assoc new-contact-1 :device-info nil)
                      "contact-2" (assoc new-contact-2 :device-info nil)
                      "contact-3" contact-3
                      "contact-4" (assoc contact-4
                                         :photo-path "generated")
                      "contact-5" (assoc local-contact-5 :device-info nil)}]
        (testing "not coming from us"
          (is (not (:db (pairing/handle-sync-installation cofx sync-message "not-us")))))
        (testing "coming from us"
          (is (= expected (get-in
                           (pairing/handle-sync-installation cofx sync-message "us")
                           [:db :contacts/contacts]))))))
    (testing "syncing multiaccount"
      (let [old-multiaccount   {:name "old-name"
                                :public-key "us"
                                :photo-path "old-photo-path"
                                :last-updated 0}
            new-multiaccount   {:name "new-name"
                                :public-key "us"
                                :photo-path "new-photo-path"
                                :last-updated 1}]
        (testing "newer update"
          (let [cofx {:db {:multiaccount old-multiaccount}}
                sync-message    {:account new-multiaccount}]
            (is (= new-multiaccount (get-in (pairing/handle-sync-installation cofx sync-message "us")
                                            [:db :multiaccount])))))
        (testing "older update"
          (let [cofx {:db {:multiaccount new-multiaccount}}
                sync-message    {:multiaccount old-multiaccount}]
            (is (= new-multiaccount (get-in (pairing/handle-sync-installation cofx sync-message "us")
                                            [:db :multiaccount])))))))
    (testing "syncing public chats"
      (let [cofx {:db {:multiaccount {:public-key "us"}}}]
        (testing "a new chat"
          (let [sync-message {:chat {:public? true
                                     :chat-id "status"
                                     :is-active true}}]
            (is (get-in (pairing/handle-sync-installation cofx sync-message "us")
                        [:db :chats "status"]))))))))

(deftest handle-pair-installation-test
  (let [cofx {:db {:multiaccount {:public-key "us"}
                   :pairing/installations {"1" {:has-bundle? true
                                                :installation-id "1"}
                                           "2" {:has-bundle? false
                                                :installation-id "2"}}}}
        pair-message {:device-type "ios"
                      :fcm-token "fcm-token"
                      :name "name"
                      :installation-id "1"}]
    (testing "not coming from us"
      (is (not (:db (pairing/handle-pair-installation cofx pair-message 1 "not-us")))))
    (testing "coming from us"
      (is (=  [["1"
                {:name "name"
                 :fcmToken "fcm-token"
                 :deviceType "ios"}]]
              (:pairing/set-installation-metadata
               (pairing/handle-pair-installation cofx pair-message 1 "us")))))))

(deftest sync-installation-messages-test
  (testing "it creates a sync installation message"
    (let [cofx {:db {:multiaccount {:public-key "us"
                                    :name "name"
                                    :photo-path "photo-path"
                                    :last-updated 1}
                     :chats             {"status" {:public? true
                                                   :is-active true
                                                   :chat-id "status"}}
                     :contacts/contacts {"contact-1" {:name "contact-1"
                                                      :public-key "contact-1"
                                                      :system-tags #{:contact/added}}
                                         "contact-2" {:name "contact-2"
                                                      :public-key "contact-2"
                                                      :system-tags #{:contact/added}}
                                         "contact-3" {:name "contact-3"
                                                      :public-key "contact-3"
                                                      :system-tags #{:contact/added}}
                                         "contact-4" {:name "contact-4"
                                                      :public-key "contact-4"
                                                      :system-tags #{:contact/added}}
                                         "contact-5" {:name "contact-5"
                                                      :public-key "contact-5"
                                                      :system-tags #{:contact/added}}
                                         "contact-6" {:name "contact-6"
                                                      :public-key "contact-6"
                                                      :system-tags #{:contact/blocked}}}}}
          expected [(transport.pairing/SyncInstallation. {"contact-1" {:name "contact-1"
                                                                       :public-key "contact-1"
                                                                       :pending? false
                                                                       :system-tags #{:contact/added}}
                                                          "contact-2" {:name "contact-2"
                                                                       :pending? false
                                                                       :public-key "contact-2"
                                                                       :system-tags #{:contact/added}}
                                                          "contact-3" {:name "contact-3"
                                                                       :pending? false
                                                                       :public-key "contact-3"
                                                                       :system-tags #{:contact/added}}
                                                          "contact-4" {:name "contact-4"
                                                                       :pending? false
                                                                       :public-key "contact-4"
                                                                       :system-tags #{:contact/added}}}
                                                         {} {})
                    (transport.pairing/SyncInstallation. {"contact-5" {:name "contact-5"
                                                                       :public-key "contact-5"
                                                                       :pending? false
                                                                       :system-tags #{:contact/added}}} {} {})
                    (transport.pairing/SyncInstallation. {} {:photo-path "photo-path"
                                                             :name "name"
                                                             :last-updated 1} {})
                    (transport.pairing/SyncInstallation. {} {} {:public? true
                                                                :chat-id "status"})]]
      (is (= expected (pairing/sync-installation-messages cofx))))))

(deftest handle-bundles-added-test
  (let [installation-1 {:has-bundle? false
                        :installation-id "installation-1"}
        cofx {:db {:multiaccount {:public-key "us"}
                   :pairing/installations {"installation-1" installation-1}}}]
    (testing "new installations"
      (let [new-installation {:identity "us" :installationID "installation-2"}
            expected {"installation-1" installation-1
                      "installation-2" {:has-bundle? true
                                        :installation-id "installation-2"}}]
        (is ((into #{} (keys (pairing/handle-bundles-added cofx new-installation)))
             :pairing/get-our-installations))))
    (testing "not from us"
      (let [new-installation {:identity "not-us" :installationID "does-not-matter"}]
        (is (not (pairing/handle-bundles-added cofx new-installation)))))))

(deftest has-paired-installations-test
  (testing "no paired devices"
    (is (not (pairing.utils/has-paired-installations? {:db {:multiaccount {:installation-id "1"}
                                                            :pairing/installations {"1" {:installation-id "1"
                                                                                         :enabled? true}
                                                                                    "2" {:installation-id "2"}
                                                                                    "3" {:installation-id "3"}}}}))))
  (testing "has paired devices"
    (is (pairing.utils/has-paired-installations? {:db {:pairing/installations {:multiaccount {:instllation-id "1"}
                                                                               "1" {:installation-id "1"
                                                                                    :enabled? true}
                                                                               "2" {:installation-id "2"}
                                                                               "3" {:installation-id "3"
                                                                                    :enabled? true}}}}))))

(deftest sort-installations
  (let [id "0"
        expected [{:installation-id id
                   :timestamp (rand-int 200)
                   :enabled? false}
                  {:installation-id "1"
                   :timestamp 3
                   :enabled? true}
                  {:installation-id "2"
                   :timestamp 2
                   :enabled? true}
                  {:installation-id "3"
                   :timestamp 10
                   :enabled? false}
                  {:installation-id "4"
                   :timestamp 9
                   :enabled? false}]]
    (is (= expected (pairing/sort-installations id (shuffle expected))))))
