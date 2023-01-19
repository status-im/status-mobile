(ns status-im.integration-test
  (:require [cljs.test :refer [deftest is run-tests]]
            [clojure.string :as string]
            [day8.re-frame.test :as rf-test]
            [re-frame.core :as rf]
            [status-im.ethereum.core :as ethereum]
            status-im.events
            status-im2.events
            [status-im.multiaccounts.logout.core :as logout]
            [status-im.transport.core :as transport]
            [status-im.utils.test :as utils.test]
            status-im2.navigation.core
            status-im2.subs.root ;;so integration tests can run independently
            [taoensso.timbre :as log]
            [utils.security.core :as security]))

(def password "testabc")

(def community {:membership 1 :name "foo" :description "bar"})

(def account-name "account-abc")

(utils.test/init!)

(defn initialize-app!
  []
  (rf/dispatch [:setup/app-started]))

(defn generate-and-derive-addresses!
  []
  (rf/dispatch [:generate-and-derive-addresses]))

(defn create-multiaccount!
  []
  (rf/dispatch [:create-multiaccount password]))

(defn assert-app-initialized
  []
  (let [app-state              @(rf/subscribe [:app-state])
        multiaccounts-loading? @(rf/subscribe [:multiaccounts/loading])]
    (is (= "active" app-state))
    (is (false? multiaccounts-loading?))))

(defn assert-logout
  []
  (let [multiaccounts-loading? @(rf/subscribe [:multiaccounts/loading])]
    (is multiaccounts-loading?)))

(defn assert-multiaccounts-generated
  []
  (let [wizard-state @(rf/subscribe [:intro-wizard/choose-key])]
    (is (= 5 (count (:multiaccounts wizard-state))))))

(defn messenger-started
  []
  @(rf/subscribe [:messenger/started?]))

(defn assert-messenger-started
  []
  (is (messenger-started)))

(defn assert-multiaccount-loaded
  []
  (is (false? @(rf/subscribe [:multiaccounts/loading]))))

(defn assert-community-created
  []
  (is (= @(rf/subscribe [:communities/create]) community)))

(defn create-new-account!
  []
  (rf/dispatch-sync [:wallet.accounts/start-adding-new-account {:type :generate}])
  (rf/dispatch-sync [:set-in [:add-account :account :name] account-name])
  (rf/dispatch [:wallet.accounts/add-new-account (ethereum/sha3 password)]))

(defn assert-new-account-created
  []
  (is (true? (some #(= (:name %) account-name)
                   @(rf/subscribe [:multiaccount/accounts])))))

(defn logout!
  []
  (rf/dispatch [:logout]))

(deftest initialize-app-test
  (log/info "========= initialize-app-test ==================")
  (rf-test/run-test-async
   (rf/dispatch [:setup/app-started])
   (rf-test/wait-for
     ;; use initialize-view because it has the longest avg. time and
     ;; is dispatched by initialize-multiaccounts (last non-view event)
     [:setup/initialize-view]
     (assert-app-initialized))))

(deftest create-account-test
  (log/info "====== create-account-test ==================")
  (rf-test/run-test-async
   (initialize-app!) ; initialize app
   (rf-test/wait-for
     [:setup/initialize-view]
     (generate-and-derive-addresses!) ; generate 5 new keys
     (rf-test/wait-for
       [:multiaccount-generate-and-derive-addresses-success] ; wait for the keys
       (assert-multiaccount-loaded) ; assert keys are generated
       (create-multiaccount!) ; create a multiaccount
       (rf-test/wait-for ; wait for login
         [::transport/messenger-started]
         (assert-messenger-started)
         (logout!)
         (rf-test/wait-for [::logout/logout-method] ; we need to logout to make sure the node is not in
                                                    ; an
                                                    ; inconsistent state between tests
           (assert-logout)))))))

(deftest create-community-test
  (log/info "====== create-community-test ==================")
  (rf-test/run-test-async
   (initialize-app!) ; initialize app
   (rf-test/wait-for
     [:setup/initialize-view]
     (generate-and-derive-addresses!) ; generate 5 new keys
     (rf-test/wait-for
       [:multiaccount-generate-and-derive-addresses-success]
       (assert-multiaccount-loaded) ; assert keys are generated
       (create-multiaccount!) ; create a multiaccount
       (rf-test/wait-for ; wait for login
         [::transport/messenger-started]
         (assert-messenger-started)
         (rf/dispatch-sync [:status-im.communities.core/open-create-community])
         (doseq [[k v] (dissoc community :membership)]
           (rf/dispatch-sync [:status-im.communities.core/create-field k v]))
         (rf/dispatch [:status-im.communities.core/create-confirmation-pressed])
         (rf-test/wait-for
           [:status-im.communities.core/community-created]
           (assert-community-created)
           (logout!)
           (rf-test/wait-for [::logout/logout-method]
             (assert-logout))))))))

(deftest create-wallet-account-test
  (log/info "====== create-wallet-account-test ==================")
  (rf-test/run-test-async
   (initialize-app!)
   (rf-test/wait-for
     [:setup/initialize-view]
     (generate-and-derive-addresses!) ; generate 5 new keys
     (rf-test/wait-for
       [:multiaccount-generate-and-derive-addresses-success]
       (assert-multiaccount-loaded) ; assert keys are generated
       (create-multiaccount!) ; create a multiaccount
       (rf-test/wait-for ; wait for login
         [::transport/messenger-started]
         (assert-messenger-started)
         (create-new-account!) ; create a new account
         (rf-test/wait-for
           [:wallet.accounts/account-stored]
           (assert-new-account-created) ; assert account was created
           (logout!)
           (rf-test/wait-for [::logout/logout-method] ; we need to logout to make sure the node is not in
                                                      ; an
                                                      ; inconsistent state between tests
             (assert-logout))))))))

(deftest back-up-seed-phrase-test
  (log/info "========= back-up-seed-phrase-test ==================")
  (rf-test/run-test-async
   (initialize-app!)
   (rf-test/wait-for
     [:setup/initialize-view]
     (generate-and-derive-addresses!)
     (rf-test/wait-for
       [:multiaccount-generate-and-derive-addresses-success]
       (assert-multiaccount-loaded)
       (create-multiaccount!)
       (rf-test/wait-for
         [::transport/messenger-started]
         (assert-messenger-started)
         (rf/dispatch-sync [:set-in [:my-profile/seed :step] :12-words]) ; display seed phrase to user
         (rf/dispatch-sync [:my-profile/enter-two-random-words]) ; begin prompting user for seed words
         (let [ma    @(rf/subscribe [:multiaccount])
               seed  @(rf/subscribe [:my-profile/seed])
               word1 (second (:first-word seed))
               word2 (second (:second-word seed))]
           (is (= 12 (count (string/split (:mnemonic ma) #" ")))) ; assert 12-word seed phrase
           (rf/dispatch-sync [:set-in [:my-profile/seed :word] word1])
           (rf/dispatch-sync [:my-profile/set-step :second-word])
           (rf/dispatch-sync [:set-in [:my-profile/seed :word] word2])
           ;; TODO: refactor (defn next-handler) & (defn enter-word) to improve test coverage?
           (rf/dispatch [:my-profile/finish])
           (rf-test/wait-for
             [:my-profile/finish-success]
             (is (nil? @(rf/subscribe [:mnemonic]))) ; assert seed phrase has been removed
             (logout!)
             (rf-test/wait-for [::logout/logout-method] ; we need to logout to make sure the node is not
                                                        ; in
                                                        ; an inconsistent state between tests
               (assert-logout)))))))))

(def multiaccount-name "Narrow Frail Lemming")
(def multiaccount-mnemonic
  "tattoo ramp health green tongue universe style vapor become tape lava reason")
(def multiaccount-key-uid "0x694b8229524820a3a00a6e211141561d61b251ad99d6b65daf82a73c9a57697b")

(deftest recover-multiaccount-test
  (log/info "========= recover-multiaccount-test ==================")
  (rf-test/run-test-async
   (initialize-app!)
   (rf-test/wait-for
     [:setup/initialize-view]
     (rf/dispatch-sync [:init-root :onboarding])
     (rf/dispatch-sync [:multiaccounts.recover.ui/recover-multiaccount-button-pressed])
     (rf/dispatch-sync [:status-im.multiaccounts.recover.core/enter-phrase-pressed])
     (rf/dispatch-sync [:multiaccounts.recover/enter-phrase-input-changed
                        (security/mask-data multiaccount-mnemonic)])
     (rf/dispatch [:multiaccounts.recover/enter-phrase-next-pressed])
     (rf-test/wait-for
       [:status-im.multiaccounts.recover.core/import-multiaccount-success]
       (rf/dispatch-sync [:multiaccounts.recover/re-encrypt-pressed])
       (rf/dispatch [:multiaccounts.recover/enter-password-next-pressed password])
       (rf-test/wait-for
         [:status-im.multiaccounts.recover.core/store-multiaccount-success]
         (let [multiaccount @(rf/subscribe [:multiaccount])] ; assert multiaccount is recovered
           (is (= multiaccount-key-uid (:key-uid multiaccount)))
           (is (= multiaccount-name (:name multiaccount))))
         (rf-test/wait-for ; wait for login
           [::transport/messenger-started]
           (assert-messenger-started)
           (logout!)
           (rf-test/wait-for [::logout/logout-method] ; we need to logout to make sure the node is not in
                                                      ; an
                                                      ; inconsistent state between tests
             (assert-logout))))))))

(def chat-id
  "0x0402905bed83f0bbf993cee8239012ccb1a8bc86907ead834c1e38476a0eda71414eed0e25f525f270592a2eebb01c9119a4ed6429ba114e51f5cb0a28dae1adfd")

(deftest one-to-one-chat-test
  (log/info "========= one-to-one-chat-test ==================")
  (rf-test/run-test-async
   (initialize-app!)
   (rf-test/wait-for
     [:setup/initialize-view]
     (generate-and-derive-addresses!)
     (rf-test/wait-for
       [:multiaccount-generate-and-derive-addresses-success] ; wait for the keys
       (assert-multiaccount-loaded)
       (create-multiaccount!)
       (rf-test/wait-for
         [::transport/messenger-started]
         (assert-messenger-started)
         (rf/dispatch-sync [:chat.ui/start-chat chat-id]) ;; start a new chat
         (rf-test/wait-for
           [:chat/one-to-one-chat-created]
           (rf/dispatch-sync [:chat/navigate-to-chat chat-id])
           (is (= chat-id @(rf/subscribe [:chats/current-chat-id])))
           (logout!)
           (rf-test/wait-for [::logout/logout-method] ; we need to logout to make sure the node is not in
                                                      ; an
                                                      ; inconsistent state between tests
             (assert-logout))))))))

(deftest delete-chat-test
  (log/info "========= delete-chat-test ==================")
  (rf-test/run-test-async
   (initialize-app!)
   (rf-test/wait-for
     [:setup/initialize-view]
     (generate-and-derive-addresses!)
     (rf-test/wait-for
       [:multiaccount-generate-and-derive-addresses-success] ; wait for the keys
       (assert-multiaccount-loaded)
       (create-multiaccount!)
       (rf-test/wait-for
         [::transport/messenger-started]
         (assert-messenger-started)
         (rf/dispatch-sync [:chat.ui/start-chat chat-id]) ;; start a new chat
         (rf-test/wait-for
           [:chat/one-to-one-chat-created]
           (rf/dispatch-sync [:chat/navigate-to-chat chat-id])
           (is (= chat-id @(rf/subscribe [:chats/current-chat-id])))
           (is @(rf/subscribe [:chats/chat chat-id]))
           (rf/dispatch-sync [:chat.ui/show-remove-confirmation chat-id])
           (rf/dispatch-sync [:chat.ui/remove-chat chat-id])
           (logout!)
           (rf-test/wait-for [::logout/logout-method] ; we need to logout to make sure the node is not
                            ; in an
                            ; inconsistent state between tests
                            (assert-logout))))))))

(deftest mute-chat-test
  (log/info "========= mute-chat-test ==================")
  (rf-test/run-test-async
   (initialize-app!)
   (rf-test/wait-for
     [:setup/initialize-view]
     (generate-and-derive-addresses!)
     (rf-test/wait-for
       [:multiaccount-generate-and-derive-addresses-success] ; wait for the keys
       (assert-multiaccount-loaded)
       (create-multiaccount!)
       (rf-test/wait-for
         [::transport/messenger-started]
         (assert-messenger-started)
         (rf/dispatch-sync [:chat.ui/start-chat chat-id]) ;; start a new chat
         (rf-test/wait-for
           [:chat/one-to-one-chat-created]
           (rf/dispatch-sync [:chat/navigate-to-chat chat-id])
           (is (= chat-id @(rf/subscribe [:chats/current-chat-id])))
           (is @(rf/subscribe [:chats/chat chat-id]))
           (rf/dispatch-sync [:chat.ui/mute chat-id true])
           (rf-test/wait-for
             [:chat/mute-successfully]
             (is @(rf/subscribe [:chats/muted chat-id]))
             (rf/dispatch-sync [:chat.ui/mute chat-id false])
             (rf-test/wait-for
               [:chat/mute-successfully]
               (is (not @(rf/subscribe [:chats/muted chat-id])))
               (logout!)
               (rf-test/wait-for [::logout/logout-method] ; we need to logout to make sure the node is
                                                          ; not in
                                                          ; an inconsistent state between tests
                 (assert-logout))))))))))

(comment
  (run-tests))
