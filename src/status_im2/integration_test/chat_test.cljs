(ns status-im2.integration-test.chat-test
  (:require
    [cljs.test :refer [deftest is]]
    [day8.re-frame.test :as rf-test]
    [re-frame.core :as rf]
    status-im.events
    [status-im.multiaccounts.logout.core :as logout]
    status-im.subs.root
    [status-im.utils.test :as utils.test]
    [status-im2.constants :as constants]
    status-im2.events
    status-im2.navigation.core
    status-im2.subs.root
    [test-helpers.integration :as h]))

(utils.test/init!)

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
    [compressed-key "zQ3shWj4WaBdf2zYKCkXe6PHxDxNTzZyid1i75879Ue9cX9gA"
     public-key     (str "0x048a6773339d11ccf5fd81677b7e54daeec544a1287"
                         "bd92b725047ad6faa9a9b9f8ea86ed5a226d2a994f5f4"
                         "6d0b43321fd8de7b7997a166e67905c8c73cd37cea")
     primary-name   "zQ3...9cX9gA"]
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
