(ns status-im.test.chat.events
  (:require [cljs.test :refer [deftest is testing]]
            reagent.core
            [re-frame.core :as rf]
            [day8.re-frame.test :refer [run-test-sync]]
            [status-im.constants :as const]
            [status-im.chat.sign-up :as sign-up]
            [status-im.chat.events :as chat-events]
            [clojure.test.check.properties :as tc-props :include-macros true]
            [clojure.test.check.clojure-test :as tc-test :include-macros true]
            [clojure.spec.alpha :as s]
            [clojure.test.check.generators :as gen]
            [clojure.set :as set]
            [status-im.model :as model]))

(def contact
  {:address          "c296367a939e0957500a25ca89b70bd64b03004e"
   :whisper-identity "0x04f5722fba79eb36d73263417531007f43d13af76c6233573a8e3e60f667710611feba0785d751b50609bfc0b7cef35448875c5392c0a91948c95798a0ce600847"
   :name             "testuser"
   :photo-path       "contacts://testuser"
   :dapp?            false})

(deftest init-console-chat
  (testing "initialising console if console is already added to chats, should not modify anything"
    (let [db {:chats {const/console-chat-id sign-up/console-chat}}
          fx (chat-events/init-console-chat db)]
      (is (= db (:db fx)))
      (is (= #{:db} (-> fx keys set)))))

  (testing "initialising console without existing account and console chat not initialisated"
    (let [fresh-db {:chats                       {}
                    :accounts/current-account-id nil}
          {:keys [db dispatch-n]} (chat-events/init-console-chat fresh-db)]
      (is (= (:current-chat-id db)
             (:chat-id sign-up/console-chat)))
      (is (= (:current-chat-id db)
             const/console-chat-id))
      (is (= dispatch-n
             (concat [[:add-contacts [sign-up/console-contact]]]
                     sign-up/intro-events)))))

  (testing "initialising console with existing account and console chat not initialisated"
    (let [fresh-db {:chats                       {}
                    :accounts/current-account-id (:whisper-identity contact)}
          {:keys [db dispatch-n]} (chat-events/init-console-chat fresh-db)]
      (is (= (:current-chat-id db)
             (:chat-id sign-up/console-chat)))
      (is (= (:current-chat-id db)
             const/console-chat-id))
      (is (= dispatch-n [[:add-contacts [sign-up/console-contact]]])))))

(def init-console-chat:prop:current-chat-is-console-chat
  (tc-props/for-all*
    [(gen/one-of [(gen/return nil)
                  (s/gen :status-im.model/contact)])]
    (fn [contact]
      (let [contact-current (when contact (model/model->current-model ::model/contact contact))
            fresh-db {:chats                       {}
                      :accounts/current-account-id (when contact (:whisper-identity contact-current))}
            {:keys [db dispatch-n]} (chat-events/init-console-chat fresh-db)
            prop-result (and (= (:current-chat-id db)
                                (:chat-id sign-up/console-chat))
                             (= (:current-chat-id db)
                                const/console-chat-id))]
        prop-result))))

(tc-test/defspec init-console-chat:prop:current-chat-is-console-chat-test
                 init-console-chat:prop:current-chat-is-console-chat)

(def init-console-chat:prop:add-contact-console-when-console-not-in-chats
  (tc-props/for-all*
    [gen/boolean
     (gen/one-of [(gen/return nil)
                  (s/gen :status-im.model/contact)])]
    (fn [console-already-in-chats? contact]
      (let [contact-current (when contact (model/model->current-model ::model/contact contact))
            fresh-db (cond-> {:chats (cond-> {}
                                             console-already-in-chats? (assoc const/console-chat-id sign-up/console-chat))}
                             contact (assoc :accounts/current-account-id (when contact-current (:whisper-identity contact))))
            ret (chat-events/init-console-chat fresh-db)
            initialized? (boolean (some #{[:add-contacts [sign-up/console-contact]]} (:dispatch-n ret)))
            prop-result (if console-already-in-chats?
                          (not initialized?)
                          initialized?)]
        prop-result))))

(tc-test/defspec init-console-chat:prop:add-contact-console-when-console-not-in-chats-test
                 init-console-chat:prop:add-contact-console-when-console-not-in-chats)

(def init-console-chat:prop:intro-events-when-when-initializing-without-existing-account
  (tc-props/for-all*
    [(gen/one-of [(gen/return nil)
                  (s/gen :status-im.model/contact)])]
    (fn [contact]
      (let [contact-current (when contact (model/model->current-model ::model/contact contact))
            fresh-db {:chats                       {}
                      :accounts/current-account-id (when contact-current (:whisper-identity contact-current))}
            ret (chat-events/init-console-chat fresh-db)
            intro-events? (set/subset? (set sign-up/intro-events) (set (:dispatch-n ret)))
            prop-result (if contact
                          (not intro-events?)
                          intro-events?)]
        prop-result))))

(tc-test/defspec init-console-chat:prop:intro-events-when-when-initializing-without-existing-account-test
                 init-console-chat:prop:intro-events-when-when-initializing-without-existing-account)
