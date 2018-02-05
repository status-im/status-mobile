(ns status-im.test.contacts.events
  (:require [cljs.test :refer-macros [deftest is testing]]
            reagent.core
            [re-frame.core :as rf]
            [day8.re-frame.test :refer-macros [run-test-sync]]
            status-im.ui.screens.db
            [status-im.ui.screens.contacts.events :as contacts-events]
            [status-im.ui.screens.group.events :as group-events]
            status-im.ui.screens.subs
            [status-im.ui.screens.events :as events]
            [status-im.utils.js-resources :as js-res]
            [status-im.utils.datetime :as datetime]))

(def test-contact-group
  {:group-id "1501682106404-685e041e-38e7-593e-b42c-fb4cabd7faa4"
   :name "Test"
   :timestamp 0
   :order 0
   :pending? false
   :contacts (list
               {:identity "bchat"}
               {:identity "Commiteth"}
               {:identity "demo-bot"})})

(def dapps-contact-group
  {:group-id "dapps"
   :name "ÐApps"
   :order 1
   :timestamp 0
   :contacts [{:identity "oaken-water-meter"}
              {:identity "melonport"}
              {:identity "bchat"}
              {:identity "Dentacoin"}
              {:identity "Augur"}
              {:identity "Ethlance"}
              {:identity "Commiteth"}]
   :pending? false})

(def demo-bot-contact
  {:address nil
   :name "Demo bot"
   :description nil
   :hide-contact? false
   :dapp-hash nil
   :photo-path nil
   :dapp-url nil
   :bot-url "local://demo-bot"
   :whisper-identity "demo-bot"
   :dapp? true
   :pending? false
   :unremovable? false
   :public-key nil})

(def console-contact
  {:whisper-identity "console"
   :name             "Console"
   :photo-path       "console"
   :dapp?            true
   :unremovable?     true
   :bot-url          "local://console-bot"
   :status           "intro-status"
   :pending?         false})

(defn test-fixtures []
  (rf/reg-fx ::events/init-store #())

  (rf/reg-fx ::contacts-events/save-contacts! #())
  (rf/reg-fx ::contacts-events/save-contact #())
  (rf/reg-fx ::contacts-events/watch-contact #())
  (rf/reg-fx ::contacts-events/stop-watching-contact #())
  (rf/reg-fx ::contacts-events/send-contact-request-fx #())

  (rf/reg-fx ::group-events/save-contact-group #())
  (rf/reg-fx ::group-events/save-contact-groups #())
  (rf/reg-fx ::group-events/add-contacts-to-contact-group #())
  (rf/reg-fx ::group-events/save-contact-group-property #())
  (rf/reg-fx ::group-events/add-contacts-to-contact-group #())

  (rf/reg-fx :save-chat #())

  (rf/reg-cofx
   ::contacts-events/get-all-contacts
   (fn [coeffects _]
     (assoc coeffects :all-contacts [])))

  (rf/reg-cofx
   :get-local-storage-data
   (fn [cofx]
     (assoc cofx :get-local-storage-data (constantly nil))))

  (rf/reg-cofx
   ::group-events/get-all-contact-groups
   (fn [coeffects _]
     (assoc coeffects :all-groups {(:group-id test-contact-group) test-contact-group})))

  ;;TODO implement tests later for :add-chat? and :bot-url
  (rf/reg-cofx
   ::contacts-events/get-default-contacts-and-groups
   (fn [coeffects _]
     (assoc coeffects
            :default-contacts (select-keys js-res/default-contacts [:demo-bot])
            :default-groups (select-keys js-res/default-contact-groups [:dapps])))))

(deftest contacts-events
  "load-contacts
   load-contact-groups
   load-default-contacts (add-contact-groups, add-contacts, add-contacts-to-group ;TODO add-chat, load-commands!)
   add-contact-handler (add-new-contact-and-open-chat, status-im.contacts.events/add-new-contact,
                        status-im.contacts.events/send-contact-request ;TODO start-chat)
   contact-request-received (update-contact, watch-contact ;TODO :update-chat!)
   contact-update-received (update-contact ;TODO :update-chat!)
   hide-contact (update-contact ;TODO :account-update-keys)
   add-contact-handler (add-pending-contact, status-im.contacts.events/add-new-contact
                        status-im.contacts.events/send-contact-request ;TODO :discoveries-send-portions)

   create-new-contact-group
   set-contact-group-name
   save-contact-group-order
   add-selected-contacts-to-group
   remove-contact-from-group
   add-contacts-to-group
   delete-contact-group"

  (testing "watch-contact"
    (let [contact   {:public-key "public-key"
                     :private-key "private-key"
                     :whisper-identity "whisper-identity"}
          actual-fx (-> {:db {:web3 "web3"}}
                        (contacts-events/watch-contact contact)
                        ::contacts-events/watch-contact)]

      (testing "it adds a ::watch-contact effect"
        (is (not (nil? actual-fx))))

      (testing "it adds web3"
        (is (= "web3" (:web3 actual-fx))))

      (testing "it adds the watched-contact whisper-identity"
        (is (= "whisper-identity" (:whisper-identity actual-fx))))

      (testing "it adds the public key"
        (is (= "public-key" (:public-key actual-fx))))

      (testing "it adds the private key"
        (is (= "private-key" (:private-key actual-fx))))))

  (testing "send-contact-request"
    (let [contact    {:whisper-identity "contact-whisper-identity"}
          account    {:name "name"
                      :photo-path "photo-path"
                      :status "status"
                      :updates-public-key "updates-public-key"
                      :updates-private-key "updates-private-key"}
          db         {:accounts/account account
                      :web3 "web3"
                      :current-public-key "current-public-key"
                      :notifications {:fcm-token "fcm-token"}}
          actual-fx (-> {:db db}
                        (contacts-events/send-contact-request contact)
                        ::contacts-events/send-contact-request-fx)]

      (testing "it adds a ::send-contact-request-fx effect"
        (is (not (nil? actual-fx))))

      (testing "it adds the current-public-key"
        (is (= "current-public-key" (:current-public-key actual-fx))))

      (testing "it adds web3"
        (is (= "web3" (:web3 actual-fx))))

      (testing "it adds the fcm-token"
        (is (= "fcm-token" (:fcm-token actual-fx))))

      (testing "it adds the whisper-identity of the contact"
        (is (= "contact-whisper-identity" (:whisper-identity actual-fx))))

      (testing "it adds the current-account information"
        (is (= account (select-keys actual-fx [:name
                                               :photo-path
                                               :status
                                               :updates-public-key
                                               :updates-private-key]))))))

  (run-test-sync

    (test-fixtures)

    (rf/dispatch [:initialize-db])

    (let [contacts        (rf/subscribe [:get-contacts])
          contact-groups  (rf/subscribe [:get-contact-groups])
          view-id         (rf/subscribe [:get :view-id])]

      (testing ":load-contacts event"

        ;;Assert the initial state
        (is (and (map? @contacts) (empty? @contacts)))

        (rf/dispatch [:load-contacts]))

      (testing ":load-contact-groups event"

        ;;Assert the initial state
        (is (and (map? @contact-groups) (empty? @contact-groups)))

        (rf/dispatch [:load-contact-groups])

        (is (= {(:group-id test-contact-group) test-contact-group}
               @contact-groups)))

      (testing ":load-default-contacts! event"

        ;; :load-default-contacts! event dispatches next 5 events
        ;;
        ;; :add-contact-groups
        ;; :add-contacts
        ;; :add-contacts-to-group
        ;;TODO :add-chat
        ;;TODO :load-commands!
        (rf/dispatch [:load-default-contacts!])

        (rf/dispatch [:set-in [:group/contact-groups "dapps" :timestamp] 0])

        (is (= {"dapps" dapps-contact-group
                (:group-id test-contact-group) test-contact-group}
               @contact-groups))

        (testing "it adds a default contact"
          (is (= demo-bot-contact (get @contacts "demo-bot"))))

        (testing "it adds the console bot"
          (is (= console-contact (get @contacts "console"))))

        (testing "it does not add any other contact"
          (is (= 2 (count (keys @contacts))))))

      (let [new-contact-public-key "0x048f7d5d4bda298447bbb5b021a34832509bd1a8dbe4e06f9b7223d00a59b6dc14f6e142b21d3220ceb3155a6d8f40ec115cd96394d3cc7c55055b433a1758dc74"
            new-contact-address "5392ccb49f2e9fef8b8068b3e3b5ba6c020a9aca"
            new-contact {:name ""
                         :photo-path ""
                         :whisper-identity new-contact-public-key
                         :address new-contact-address}
            contact (rf/subscribe [:contact-by-identity new-contact-public-key])
            current-chat-id (rf/subscribe [:get-current-chat-id])]

        (testing ":add-contact-handler event - new contact"

          (rf/dispatch [:set :view-id nil])
          (rf/dispatch [:set :current-chat-id nil])

          ;; :add-contact-handler event dispatches next 4 events for new contact
          ;;
          ;; :add-new-contact-and-open-chat
          ;; :status-im.contacts.events/add-new-contact
          ;; :status-im.contacts.events/send-contact-request
          ;;TODO :start-chat

          (rf/dispatch [:add-contact-handler new-contact-public-key])

          (testing "it returns the new contact from the contact-by-identity sub"
            (is (= new-contact (assoc @contact :photo-path "" :name ""))))

          (testing "it adds the new contact to the list of contacts"
            (is (= new-contact
                   (-> @contacts
                       (get new-contact-public-key)
                       (assoc :photo-path "" :name "")))))

          (testing "it loads the 1-1 chat"
            (is (= :chat @view-id)))

          (testing "it adds the new contact to the chat"
            (is (= new-contact-public-key @current-chat-id))))

        (testing ":contact-request-received event"

          ;; :contact-request-received event dispatches next 3 events
          ;;
          ;; :update-contact!
          ;; :watch-contact
          ;;TODO :update-chat!
          (rf/reg-event-db :update-chat! (fn [db _] db))

          (let [received-contact {:name "test"
                                  :profile-image ""
                                  :address new-contact-address
                                  :status "test status"
                                  :fcm-token "0xwhatever"}
                received-contact' (merge new-contact
                                         (dissoc received-contact :profile-image)
                                         {:public-key new-contact-public-key
                                          :private-key ""})]

            (rf/dispatch [:contact-request-received {:from new-contact-public-key
                                                     :payload {:contact received-contact
                                                               :keypair {:public new-contact-public-key
                                                                         :private ""}}}])

            (testing "it adds the new contact to the list of contacts"
              (is (= received-contact'
                     (get @contacts new-contact-public-key))))

            (testing ":contact-update-received event"

              ;; :contact-update-received event dispatches next 2 events
              ;;
              ;; :update-contact!
              ;;TODO :update-chat!
              (let [timestamp (datetime/now-ms)
                    received-contact'' (assoc received-contact'
                                              :last-updated timestamp
                                              :status "new status"
                                              :name "new name")]

                (rf/dispatch [:contact-update-received {:from new-contact-public-key
                                                        :payload {:content {:profile {:profile-image ""
                                                                                      :status "new status"
                                                                                      :name "new name"}}
                                                                  :timestamp timestamp}}])

                (testing "it updates the contact and set the :last-updated key"
                  (is (= received-contact''
                         (get @contacts new-contact-public-key))))

                (testing ":hide-contact event"

                  ;; :hide-contact event dispatches next 2 events
                  ;;
                  ;; :update-contact!
                  ;;TODO :account-update-keys
                  (rf/reg-event-db :account-update-keys (fn [db _] db))

                  (rf/dispatch [:hide-contact @contact])

                  (testing "it sets the pending? flag to true"
                    (is (= (assoc received-contact'' :pending? true)
                           (get @contacts new-contact-public-key)))))

                (testing ":add-contact-handler event - :add-pending-contact"

                  ;; :add-contact-handler event dispatches next 4 events
                  ;;
                  ;; :add-pending-contact
                  ;; :status-im.contacts.events/add-new-contact
                  ;; :status-im.contacts.events/send-contact-request
                  ;;TODO :discoveries-send-portions
                  (rf/reg-event-db :discoveries-send-portions (fn [db _] db))

                  (rf/dispatch [:set :view-id nil])
                  (rf/dispatch [:set :current-chat-id nil])

                  (rf/dispatch [:add-contact-handler new-contact-public-key])

                  (testing "it sets the pending? flag to false"
                    (is (= (assoc received-contact'' :pending? false)
                           (get @contacts new-contact-public-key))))

                  (testing "it loads the 1-1 chat"
                    (is (= :chat @view-id)))

                  (testing "it adds the new contact to the chat"
                    (is (= new-contact-public-key @current-chat-id))))

                (testing ":create-new-contact-group event"

                  (let [new-group-name "new group"]

                    (rf/dispatch [:select-contact new-contact-public-key])
                    (rf/dispatch [:select-contact "demo-bot"])
                    (rf/dispatch [:select-contact "browse"])
                    (rf/dispatch [:deselect-contact "browse"])

                    (rf/dispatch [:create-new-contact-group new-group-name])

                    (rf/dispatch [:deselect-contact "demo-bot"])
                    (rf/dispatch [:deselect-contact new-contact-public-key])

                    (let [new-group-id (->> @contact-groups
                                            (vals)
                                            (filter #(= (:name %) new-group-name))
                                            (first)
                                            (:group-id))
                          new-group {:group-id    new-group-id
                                     :name        new-group-name
                                     :order       2
                                     :timestamp   0
                                     :contacts   [{:identity new-contact-public-key}
                                                  {:identity "demo-bot"}]}
                          groups-with-new-group {new-group-id new-group
                                                 "dapps" dapps-contact-group
                                                 (:group-id test-contact-group) test-contact-group}]

                      (rf/dispatch [:set-in [:group/contact-groups new-group-id :timestamp] 0])

                      (is (= groups-with-new-group @contact-groups))

                      (let [groups-with-new-group' (update groups-with-new-group new-group-id assoc :name "new group name")]

                        (testing ":set-contact-group-name event"

                          (rf/reg-event-db ::prepare-group-name
                                           (fn [db _] (assoc db
                                                             :new-chat-name "new group name"
                                                             :group/contact-group-id new-group-id)))

                          (rf/dispatch [::prepare-group-name])
                          (rf/dispatch [:set-contact-group-name])

                          (is (= groups-with-new-group' @contact-groups)))

                        (let [groups-with-new-group'' (-> groups-with-new-group'
                                                          (update new-group-id assoc :order 1)
                                                          (update "dapps" assoc :order 2))]

                          (testing ":save-contact-group-order event"

                            (rf/reg-event-db ::prepare-groups-order
                                             (fn [db _]
                                               (assoc db :group/groups-order
                                                      (->> (vals (:group/contact-groups db))
                                                           (remove :pending?)
                                                           (sort-by :order >)
                                                           (map :group-id)))))

                            (rf/dispatch [::prepare-groups-order])
                            (rf/dispatch [:change-contact-group-order 1 0])
                            (rf/dispatch [:save-contact-group-order])

                            (is (= groups-with-new-group'' @contact-groups)))

                          (testing ":add-selected-contacts-to-group event"

                            (rf/dispatch [:select-contact "browse"])
                            (rf/dispatch [:add-selected-contacts-to-group])
                            (rf/dispatch [:deselect-contact "browse"])

                            (is (= (update groups-with-new-group'' new-group-id assoc :contacts [{:identity new-contact-public-key}
                                                                                                 {:identity "demo-bot"}
                                                                                                 {:identity "browse"}])
                                   @contact-groups)))

                          (testing ":remove-contact-from-group event"

                            (rf/dispatch [:remove-contact-from-group "browse" new-group-id])

                            (is (= groups-with-new-group'' @contact-groups)))


                          (testing ":add-contacts-to-group event"

                            (rf/dispatch [:add-contacts-to-group new-group-id ["browse"]])

                            (is (= (update groups-with-new-group'' new-group-id assoc :contacts [{:identity new-contact-public-key}
                                                                                                 {:identity "demo-bot"}
                                                                                                 {:identity "browse"}])
                                   @contact-groups))

                            (rf/dispatch [:remove-contact-from-group "browse" new-group-id]))

                          (testing ":delete-contact-group event"

                            (rf/dispatch [:delete-contact-group])

                            (is (= (update groups-with-new-group'' new-group-id assoc :pending? true)
                                   @contact-groups))))))))))))))))
