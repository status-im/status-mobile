(ns status-im2.integration-test.chat-test
  (:require
    [cljs.test :refer [deftest is]]
    [day8.re-frame.test :as rf-test]
    legacy.status-im.events
    [legacy.status-im.multiaccounts.logout.core :as logout]
    legacy.status-im.subs.root
    [re-frame.core :as rf]
    [status-im2.constants :as constants]
    status-im2.events
    status-im2.navigation.core
    status-im2.subs.root
    [test-helpers.integration :as h]))

(def chat-id
  "0x0402905bed83f0bbf993cee8239012ccb1a8bc86907ead834c1e38476a0eda71414eed0e25f525f270592a2eebb01c9119a4ed6429ba114e51f5cb0a28dae1adfd")

(deftest one-to-one-chat-test
  (h/log-headline :one-to-one-chat-test)
  (rf-test/run-test-async
   (h/with-app-initialized
    (h/with-account
     (rf/dispatch-sync [:chat.ui/start-chat chat-id]) ;; start a new chat
     (rf-test/wait-for
       [:chat/one-to-one-chat-created]
       (rf/dispatch-sync [:chat/navigate-to-chat chat-id])
       (is (= chat-id @(rf/subscribe [:chats/current-chat-id])))
       (h/logout)
       (rf-test/wait-for [::logout/logout-method]))))))

(deftest delete-chat-test
  (h/log-headline :delete-chat-test)
  (rf-test/run-test-async
   (h/with-app-initialized
    (h/with-account
     (rf/dispatch-sync [:chat.ui/start-chat chat-id]) ;; start a new chat
     (rf-test/wait-for
       [:chat/one-to-one-chat-created]
       (rf/dispatch-sync [:chat/navigate-to-chat chat-id])
       (is (= chat-id @(rf/subscribe [:chats/current-chat-id])))
       (is @(rf/subscribe [:chats/chat chat-id]))
       (rf/dispatch-sync [:chat.ui/show-remove-confirmation chat-id])
       (rf/dispatch-sync [:chat.ui/remove-chat chat-id])
       (h/logout)
       (rf-test/wait-for [::logout/logout-method]))))))

(deftest mute-chat-test
  (h/log-headline :mute-chat-test)
  (rf-test/run-test-async
   (h/with-app-initialized
    (h/with-account
     (rf/dispatch-sync [:chat.ui/start-chat chat-id]) ;; start a new chat
     (rf-test/wait-for
       [:chat/one-to-one-chat-created]
       (rf/dispatch-sync [:chat/navigate-to-chat chat-id])
       (is (= chat-id @(rf/subscribe [:chats/current-chat-id])))
       (is @(rf/subscribe [:chats/chat chat-id]))
       (rf/dispatch-sync [:chat.ui/mute chat-id true constants/mute-till-unmuted])
       (rf-test/wait-for
         [:chat/mute-successfully]
         (is @(rf/subscribe [:chats/muted chat-id]))
         (rf/dispatch-sync [:chat.ui/mute chat-id false])
         (rf-test/wait-for
           [:chat/mute-successfully]
           (is (not @(rf/subscribe [:chats/muted chat-id])))
           (h/logout)
           (rf-test/wait-for [::logout/logout-method]))))))))

(deftest add-contact-test
  (h/log-headline :add-contact-test)
  (let
    [compressed-key "zQ3shMwgSMKHVznoowceZMxWde9HUnkQEVSGvvex8UFpFNErL"
     public-key     (str "0x0407e9dc435fe366cb0b4c4f35cbd925438c0f46fe0"
                         "ed2a86050325bc8856e26898c17e31dee2602b9429c91"
                         "ecf65a41d62ac1f2f0823c0710dcb536e79af2763c")
     primary-name   "zQ3...pFNErL"]
    (rf-test/run-test-async
     (h/with-app-initialized
      (h/with-account
       ;; search for contact using compressed key
       (rf/dispatch [:contacts/set-new-identity compressed-key])
       (rf-test/wait-for
         [:contacts/set-new-identity-success]
         (let [new-identity @(rf/subscribe [:contacts/new-identity])]
           (is (= public-key (:public-key new-identity)))
           (is (= :valid (:state new-identity))))
         ;; click 'view profile' button
         (rf/dispatch [:chat.ui/show-profile public-key])
         (rf-test/wait-for
           [:contacts/build-contact]
           (rf-test/wait-for
             [:contacts/contact-built]
             (let [contact @(rf/subscribe [:contacts/current-contact])]
               (is (= primary-name (:primary-name contact))))
             (h/logout)
             (rf-test/wait-for [::logout/logout-method])))))))))
