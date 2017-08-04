(ns status-im.test.contacts.handlers
  (:require [cljs.test :refer-macros [deftest is testing]]
            reagent.core
            [re-frame.core :as rf]
            [day8.re-frame.test :refer-macros [run-test-sync]]
            status-im.specs
            status-im.db
            [status-im.contacts.events :as contacts-events]
            [status-im.group.events :as group-events]
            [status-im.handlers :as handlers]
            status-im.subs
            [status-im.utils.js-resources :as js-res]
            [status-im.utils.datetime :as datetime]))

(def browse-contact-from-realm-db
  {:last-updated     0
   :address          nil
   :name             "Browse"

   :global-command
                     {
                      :description         "Launch the browser"
                      :sequential-params   false
                      :color               "#ffa500"
                      :name                "global"
                      :params
                                           {
                                            :0
                                            {
                                             :name        "url"
                                             :type        "text"
                                             :placeholder "URL"}}
                      :icon                nil
                      :title               "Browser"
                      :has-handler         false
                      :fullscreen          true
                      :suggestions-trigger "on-change"}
   :dapp-url         nil
   :dapp-hash        nil

   :commands
                     {
                      :location
                      {
                       :description         "Share your location"
                       :sequential-params   true
                       :color               nil
                       :name                "location"

                       :params
                                            {
                                             :0
                                             {
                                              :name        "address"
                                              :type        "text"
                                              :placeholder "address"}}
                       :icon                nil
                       :title               "Location"
                       :has-handler         false
                       :fullscreen          true
                       :owner-id            "browse"
                       :suggestions-trigger "on-change"}}
   :photo-path       nil
   :debug?           false
   :status           nil
   :bot-url          "local://browse-bot"
   :responses        {}
   :pending?         false
   :whisper-identity "browse"
   :last-online      0
   :dapp?            true
   :unremovable?     true
   :private-key      nil
   :public-key       nil})

(def browse-global-commands
  {:browse
   {
    :description         "Launch the browser"
    :bot                 "browse"
    :color               "#ffa500"
    :name                "global"
    :params
                         [
                          {
                           :name        "url"
                           :placeholder "URL"
                           :type        "text"}]
    :type                :command
    :title               "Browser"
    :sequential-params   false
    :icon                nil
    :has-handler         false
    :fullscreen          true
    :suggestions-trigger "on-change"}})

(def dapps-contact-group
  {:group-id "dapps"
   :name "ÐApps"
   :order 0
   :timestamp 0
   :contacts [{:identity "wallet"}
              {:identity "oaken-water-meter"}
              {:identity "melonport"}
              {:identity "bchat"}
              {:identity "Dentacoin"}
              {:identity "Augur"}
              {:identity "Ethlance"}
              {:identity "Commiteth"}]
   :pending? false})

(def wallet-contact
  {:address nil
   :name "Wallet"
   :global-command nil
   :dapp-url "https://status.im/dapps/wallet/"
   :dapp-hash nil
   :photo-path "icon_wallet_avatar"
   :bot-url nil
   :pending? false
   :whisper-identity "wallet"
   :dapp? true
   :unremovable? true
   :public-key nil})

(def contacts-browse-wallet
  {"browse" browse-contact-from-realm-db
   "wallet" wallet-contact})

(defn test-fixtures []
  (rf/reg-fx ::handlers/init-store #())

  (rf/reg-fx ::contacts-events/save-contacts! #())
  (rf/reg-fx ::contacts-events/save-contact #())
  (rf/reg-fx ::contacts-events/watch-contact #())
  (rf/reg-fx ::contacts-events/stop-watching-contact #())
  (rf/reg-fx ::contacts-events/send-contact-request-fx #())

  (rf/reg-fx ::group-events/save-contact-groups #())
  (rf/reg-fx ::group-events/add-contacts-to-contact-group #())

  (rf/reg-cofx
    ::contacts-events/get-all-contacts
    (fn [coeffects _]
      (assoc coeffects :all-contacts [browse-contact-from-realm-db])))

  ;;TODO implement tests later for :add-chat? and :bot-url
  (rf/reg-cofx
    ::contacts-events/get-default-contacts-and-groups
    (fn [coeffects _]
      (assoc coeffects :default-contacts (update (select-keys js-res/default-contacts [:wallet])
                                                 :wallet
                                                 dissoc :add-chat? :bot-url)
                       :default-groups (select-keys js-res/default-contact-groups [:dapps])))))

(deftest contacts-events
  "load-contacts
   load-default-contacts (add-contact-groups, add-contacts, add-contacts-to-group ;TODO add-chat, load-commands!)
   add-contact-handler (add-new-contact-and-open-chat, status-im.contacts.events/add-new-contact,
                        status-im.contacts.events/send-contact-request ;TODO start-chat)
   contact-request-received (update-contact, watch-contact ;TODO :update-chat!)
   contact-update-received (update-contact ;TODO :update-chat!)
   hide-contact (update-contact ;TODO :account-update-keys)
   add-contact-handler (add-pending-contact, status-im.contacts.events/add-new-contact
                        status-im.contacts.events/send-contact-request ;TODO :discoveries-send-portions)"

  (run-test-sync

    (test-fixtures)

    (rf/dispatch [:initialize-db])

    (let [contacts        (rf/subscribe [:get-contacts])
          global-commands (rf/subscribe [:get :global-commands])
          contact-groups  (rf/subscribe [:get-contact-groups])]

      (testing ":load-contacts event"

        ;;Assert the initial state
        (is (and (map? @contacts) (empty? @contacts)))
        (is (nil? @global-commands))

        (rf/dispatch [:load-contacts])

        (is (= {"browse" browse-contact-from-realm-db} @contacts))
        (is (= browse-global-commands @global-commands)))

      (testing ":load-default-contacts! event"

        ;;Assert the initial state
        (is (and (map? @contact-groups) (empty? @contact-groups)))

        ;; :load-default-contacts! event dispatches next 5 events
        ;;
        ;; :add-contact-groups
        ;; :add-contacts
        ;; :add-contacts-to-group
        ;;TODO :add-chat
        ;;TODO :load-commands!
        (rf/dispatch [:load-default-contacts!])

        (is (= {"dapps" dapps-contact-group} (update @contact-groups "dapps" assoc :timestamp 0)))

        (is (= contacts-browse-wallet
               @contacts)))

      (let [new-contact-public-key "0x048f7d5d4bda298447bbb5b021a34832509bd1a8dbe4e06f9b7223d00a59b6dc14f6e142b21d3220ceb3155a6d8f40ec115cd96394d3cc7c55055b433a1758dc74"
            new-contact-address "5392ccb49f2e9fef8b8068b3e3b5ba6c020a9aca"
            new-contact {:name ""
                         :photo-path ""
                         :whisper-identity new-contact-public-key
                         :address new-contact-address}
            contact (rf/subscribe [:contact-by-identity new-contact-public-key])]

        (testing ":add-contact-handler event - new contact"

          ;; :add-contact-handler event dispatches next 4 events for new contact
          ;;
          ;; :add-new-contact-and-open-chat
          ;; :status-im.contacts.events/add-new-contact
          ;; :status-im.contacts.events/send-contact-request
          ;;TODO :start-chat
          (rf/reg-event-db :start-chat (fn [db _] db))

          (rf/dispatch [:add-contact-handler new-contact-public-key])

          (is (= new-contact (assoc @contact :photo-path "" :name "")))

          (is (= (assoc contacts-browse-wallet new-contact-public-key new-contact)
                 (update @contacts new-contact-public-key assoc :photo-path "" :name ""))))

        (testing ":contact-request-received event"

          ;; :contact-request-received event dispatches next 3 events
          ;;
          ;; :update-contact!
          ;; :watch-contact
          ;;TODO :update-chat!
          (rf/reg-event-db :update-chat! (fn [db _] db))

          (let [recieved-contact {:name "test"
                                  :profile-image ""
                                  :address new-contact-address
                                  :status "test status"}
                recieved-contact' (merge new-contact
                                         (dissoc recieved-contact :profile-image)
                                         {:public-key "" :private-key ""})]

            (rf/dispatch [:contact-request-received {:from new-contact-public-key
                                                     :payload {:contact recieved-contact
                                                               :keypair {:public ""
                                                                         :private ""}}}])

            (is (= (assoc contacts-browse-wallet new-contact-public-key recieved-contact')
                   @contacts))

            (testing ":contact-update-received event"

              ;; :contact-update-received event dispatches next 2 events
              ;;
              ;; :update-contact!
              ;;TODO :update-chat!
              (let [timestamp (datetime/now-ms)
                    recieved-contact'' (assoc recieved-contact' :last-updated timestamp
                                                                :status "new status"
                                                                :name "new name")]

                (rf/dispatch [:contact-update-received {:from new-contact-public-key
                                                        :payload {:content {:profile {:profile-image ""
                                                                                      :status "new status"
                                                                                      :name "new name"}}
                                                                  :timestamp timestamp}}])

                (is (= (assoc contacts-browse-wallet new-contact-public-key recieved-contact'')
                       @contacts))

                (testing ":hide-contact event"

                  ;; :hide-contact event dispatches next 2 events
                  ;;
                  ;; :update-contact!
                  ;;TODO :account-update-keys
                  (rf/reg-event-db :account-update-keys (fn [db _] db))

                  (rf/dispatch [:hide-contact @contact])

                  (is (= (assoc contacts-browse-wallet new-contact-public-key (assoc recieved-contact''
                                                                                :pending? true))
                         @contacts)))

                (testing ":add-contact-handler event - :add-pending-contact"

                  ;; :add-contact-handler event dispatches next 4 events
                  ;;
                  ;; :add-pending-contact
                  ;; :status-im.contacts.events/add-new-contact
                  ;; :status-im.contacts.events/send-contact-request
                  ;;TODO :discoveries-send-portions
                  (rf/reg-event-db :discoveries-send-portions (fn [db _] db))

                  (rf/dispatch [:add-contact-handler new-contact-public-key])

                  (is (= (assoc contacts-browse-wallet new-contact-public-key (assoc recieved-contact''
                                                                                :pending? false))
                         @contacts)))))))))))