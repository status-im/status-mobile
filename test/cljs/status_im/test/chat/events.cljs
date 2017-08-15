(ns status-im.test.chat.events
  (:require [cljs.test :refer [deftest is testing]]
            reagent.core
            [re-frame.core :as rf]
            [day8.re-frame.test :refer [run-test-sync]]
            [status-im.constants :as const]
            [status-im.chat.sign-up :as sign-up]
            [status-im.chat.events :as chat-events]))

(def contact
  {:address "c296367a939e0957500a25ca89b70bd64b03004e"
   :whisper-identity "0x04f5722fba79eb36d73263417531007f43d13af76c6233573a8e3e60f667710611feba0785d751b50609bfc0b7cef35448875c5392c0a91948c95798a0ce600847"
   :name "testuser"
   :photo-path "contacts://testuser"
   :dapp? false})

(deftest init-console-chat
  (testing "initialising console if console is already added to chats, should not modify anything"
    (let [db {:chats {const/console-chat-id sign-up/console-chat}}
          fx (chat-events/init-console-chat db false)]
      (is (= db (:db fx)))
      (is (= #{:db} (-> fx keys set)))))

  (testing "initialising console without existing account and console chat not initialisated"
    (let [fresh-db {:chats {}
                    :accounts/current-account-id nil}
          {:keys [db dispatch-n]} (chat-events/init-console-chat fresh-db false)]
      (is (= (:current-chat-id db)
             (:chat-id sign-up/console-chat)))
      (is (= (:new-chat db)
             sign-up/console-chat))
      (is (= (:current-chat-id db)
             const/console-chat-id))
      (is (= dispatch-n
             (concat [[:add-contacts [sign-up/console-contact]]]
                     sign-up/intro-events)))))

  (testing "initialising console with existing account and console chat not initialisated"
    (let [fresh-db {:chats {}
                    :accounts/current-account-id (:whisper-identity contact)}
          {:keys [db dispatch-n]} (chat-events/init-console-chat fresh-db false)]
      (is (= (:current-chat-id db)
             (:chat-id sign-up/console-chat)))
      (is (= (:new-chat db)
             sign-up/console-chat))
      (is (= (:current-chat-id db)
             const/console-chat-id))
      (is (= dispatch-n [[:add-contacts [sign-up/console-contact]]])))))
