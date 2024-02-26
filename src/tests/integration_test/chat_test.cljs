(ns tests.integration-test.chat-test
  (:require
    [cljs.test :refer [deftest is use-fixtures]]
    legacy.status-im.events
    legacy.status-im.subs.root
    [promesa.core :as p]
    [re-frame.core :as rf]
    [status-im.constants :as constants]
    status-im.events
    status-im.navigation.core
    status-im.subs.root
    [test-helpers.integration :as h]))

(use-fixtures :each (h/fixture-logged))

(def chat-id
  "0x0402905bed83f0bbf993cee8239012ccb1a8bc86907ead834c1e38476a0eda71414eed0e25f525f270592a2eebb01c9119a4ed6429ba114e51f5cb0a28dae1adfd")

(deftest one-to-one-chat-test
  (h/rf-test-async
   (fn []
     (h/log-headline ::one-to-one-chat-test)
     (p/do
       (rf/dispatch-sync [:chat.ui/start-chat chat-id])
       (h/wait-for [:chat/one-to-one-chat-created])
       (rf/dispatch-sync [:chat/navigate-to-chat chat-id])
       (is (= chat-id @(rf/subscribe [:chats/current-chat-id])))))))

(deftest delete-chat-test
  (h/rf-test-async
   (fn []
     (h/log-headline ::delete-chat-test)
     (p/do
       (rf/dispatch-sync [:chat.ui/start-chat chat-id])
       (h/wait-for [:chat/one-to-one-chat-created])
       (rf/dispatch-sync [:chat/navigate-to-chat chat-id])
       (is (= chat-id @(rf/subscribe [:chats/current-chat-id])))
       (is @(rf/subscribe [:chats/chat chat-id]))
       (rf/dispatch-sync [:chat.ui/show-remove-confirmation chat-id])
       (rf/dispatch-sync [:chat.ui/remove-chat chat-id])))))

(deftest mute-chat-test
  (h/rf-test-async
   (fn []
     (h/log-headline ::mute-chat-test)
     (p/do
       (rf/dispatch-sync [:chat.ui/start-chat chat-id])
       (h/wait-for [:chat/one-to-one-chat-created])

       (rf/dispatch-sync [:chat/navigate-to-chat chat-id])
       (is (= chat-id @(rf/subscribe [:chats/current-chat-id])))
       (is @(rf/subscribe [:chats/chat chat-id]))

       (rf/dispatch-sync [:chat.ui/mute chat-id true constants/mute-till-unmuted])
       (h/wait-for [:chat/mute-successfully])
       (is @(rf/subscribe [:chats/muted chat-id]))

       (rf/dispatch-sync [:chat.ui/mute chat-id false])
       (h/wait-for [:chat/mute-successfully])
       (is (not @(rf/subscribe [:chats/muted chat-id])))))))

(deftest add-contact-test
  (h/rf-test-async
   (fn []
     (h/log-headline :add-contact-test)
     (let [compressed-key "zQ3shMwgSMKHVznoowceZMxWde9HUnkQEVSGvvex8UFpFNErL"
           public-key     (str "0x0407e9dc435fe366cb0b4c4f35cbd925438c0f46fe0"
                               "ed2a86050325bc8856e26898c17e31dee2602b9429c91"
                               "ecf65a41d62ac1f2f0823c0710dcb536e79af2763c")
           primary-name   "zQ3...pFNErL"]
       (p/do
         ;; Search for contact using compressed key
         (rf/dispatch [:contacts/set-new-identity {:input compressed-key}])
         (h/wait-for [:contacts/set-new-identity-success])
         (let [new-identity @(rf/subscribe [:contacts/new-identity])]
           (is (= public-key (:public-key new-identity)))
           (is (= :valid (:state new-identity))))

         ;; Click 'view profile' button
         (rf/dispatch [:chat.ui/show-profile public-key])
         (h/wait-for [:contacts/build-contact :contacts/build-contact-success])
         (let [contact @(rf/subscribe [:contacts/current-contact])]
           (is (= primary-name (:primary-name contact)))))))))
