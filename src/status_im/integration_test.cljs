(ns status-im.integration-test
  (:require [cljs.test :refer [deftest is]]
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
            status-im2.subs.root ; so integration tests can run independently
            [taoensso.timbre :as log]
            [status-im2.constants :as constants]))

(def password "testabc")

(def community {:membership 1 :name "foo" :description "bar"})

(def account-name "account-abc")

(utils.test/init!)

(defn initialize-app!
  []
  (rf/dispatch [:app-started]))

(defn create-multiaccount!
  []
  (rf/dispatch [:profile.create/create-and-login
                {:display-name account-name :password password :color "blue"}]))

(defn assert-app-initialized
  []
  (let [app-state @(rf/subscribe [:app-state])]
    (is (= "active" app-state))))

(defn messenger-started
  []
  @(rf/subscribe [:messenger/started?]))

(defn assert-messenger-started
  []
  (is (messenger-started)))

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
                   @(rf/subscribe [:profile/wallet-accounts])))))

(defn logout!
  []
  (rf/dispatch [:logout]))

(deftest initialize-app-test
  (log/info "========= initialize-app-test ==================")
  (rf-test/run-test-async
   (rf/dispatch [:app-started])
   (rf-test/wait-for
     ;; use initialize-view because it has the longest avg. time and
     ;; is dispatched by initialize-multiaccounts (last non-view event)
     [:profile/get-profiles-overview-success]
     (assert-app-initialized))))

(deftest create-account-test
  (log/info "====== create-account-test ==================")
  (rf-test/run-test-async
   (initialize-app!) ; initialize app
   (rf-test/wait-for
     [:profile/get-profiles-overview-success]
     (create-multiaccount!) ; create a multiaccount
     (rf-test/wait-for ; wait for login
       [::transport/messenger-started]
       (assert-messenger-started)
       (logout!)
       (rf-test/wait-for [::logout/logout-method])))))

(deftest create-community-test
  (log/info "====== create-community-test ==================")
  (rf-test/run-test-async
   (initialize-app!) ; initialize app
   (rf-test/wait-for
     [:profile/get-profiles-overview-success]
     (create-multiaccount!) ; create a multiaccount
     (rf-test/wait-for ; wait for login
       [::transport/messenger-started]
       (assert-messenger-started)
       (rf/dispatch-sync [:legacy-only-for-e2e/open-create-community])
       (doseq [[k v] (dissoc community :membership)]
         (rf/dispatch-sync [:status-im.communities.core/create-field k v]))
       (rf/dispatch [:status-im.communities.core/create-confirmation-pressed])
       (rf-test/wait-for
         [:status-im.communities.core/community-created]
         (assert-community-created)
         (logout!)
         (rf-test/wait-for [::logout/logout-method]))))))

(deftest create-wallet-account-test
  (log/info "====== create-wallet-account-test ==================")
  (rf-test/run-test-async
   (initialize-app!)
   (rf-test/wait-for
     [:profile/get-profiles-overview-success]
     (create-multiaccount!) ; create a multiaccount
     (rf-test/wait-for ; wait for login
       [::transport/messenger-started]
       (assert-messenger-started)
       (create-new-account!) ; create a new account
       (rf-test/wait-for
         [:wallet.accounts/account-stored]
         (assert-new-account-created) ; assert account was created
         (logout!)
         (rf-test/wait-for [::logout/logout-method]))))))

(deftest back-up-seed-phrase-test
  (log/info "========= back-up-seed-phrase-test ==================")
  (rf-test/run-test-async
   (initialize-app!)
   (rf-test/wait-for
     [:profile/get-profiles-overview-success]
     (create-multiaccount!)
     (rf-test/wait-for
       [::transport/messenger-started]
       (assert-messenger-started)
       (rf/dispatch-sync [:set-in [:my-profile/seed :step] :12-words]) ; display seed phrase to user
       (rf/dispatch-sync [:my-profile/enter-two-random-words]) ; begin prompting user for seed words
       (let [{:keys [mnemonic]} @(rf/subscribe [:profile/profile])
             seed               @(rf/subscribe [:my-profile/seed])
             word1              (second (:first-word seed))
             word2              (second (:second-word seed))]
         (is (= 12 (count (string/split mnemonic #" ")))) ; assert 12-word seed phrase
         (rf/dispatch-sync [:set-in [:my-profile/seed :word] word1])
         (rf/dispatch-sync [:my-profile/set-step :second-word])
         (rf/dispatch-sync [:set-in [:my-profile/seed :word] word2])
         ;; TODO: refactor (defn next-handler) & (defn enter-word) to improve test coverage?
         (rf/dispatch [:my-profile/finish])
         (rf-test/wait-for
           [:my-profile/finish-success]
           (is (nil? @(rf/subscribe [:mnemonic]))) ; assert seed phrase has been removed
           (logout!)
           (rf-test/wait-for [::logout/logout-method])))))))

(def multiaccount-name "Narrow Frail Lemming")
(def multiaccount-mnemonic
  "tattoo ramp health green tongue universe style vapor become tape lava reason")
(def multiaccount-key-uid "0x694b8229524820a3a00a6e211141561d61b251ad99d6b65daf82a73c9a57697b")

(def chat-id
  "0x0402905bed83f0bbf993cee8239012ccb1a8bc86907ead834c1e38476a0eda71414eed0e25f525f270592a2eebb01c9119a4ed6429ba114e51f5cb0a28dae1adfd")

(deftest one-to-one-chat-test
  (log/info "========= one-to-one-chat-test ==================")
  (rf-test/run-test-async
   (initialize-app!)
   (rf-test/wait-for
     [:profile/get-profiles-overview-success]
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
         (rf-test/wait-for [::logout/logout-method]))))))

(deftest delete-chat-test
  (log/info "========= delete-chat-test ==================")
  (rf-test/run-test-async
   (initialize-app!)
   (rf-test/wait-for
     [:profile/get-profiles-overview-success]
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
         (rf-test/wait-for [::logout/logout-method]))))))

(deftest mute-chat-test
  (log/info "========= mute-chat-test ==================")
  (rf-test/run-test-async
   (initialize-app!)
   (rf-test/wait-for
     [:profile/get-profiles-overview-success]
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
         (rf/dispatch-sync [:chat.ui/mute chat-id true constants/mute-till-unmuted])
         (rf-test/wait-for
           [:chat/mute-successfully]
           (is @(rf/subscribe [:chats/muted chat-id]))
           (rf/dispatch-sync [:chat.ui/mute chat-id false])
           (rf-test/wait-for
             [:chat/mute-successfully]
             (is (not @(rf/subscribe [:chats/muted chat-id])))
             (logout!)
             (rf-test/wait-for [::logout/logout-method]))))))))

(deftest add-contact-test
  (log/info "========= add-contact-test ==================")
  (let
    [compressed-key "zQ3shWj4WaBdf2zYKCkXe6PHxDxNTzZyid1i75879Ue9cX9gA"
     public-key
     "0x048a6773339d11ccf5fd81677b7e54daeec544a1287bd92b725047ad6faa9a9b9f8ea86ed5a226d2a994f5f46d0b43321fd8de7b7997a166e67905c8c73cd37cea"
     three-words-name "Rich Total Pondskater"]
    (rf-test/run-test-async
     (initialize-app!)
     (rf-test/wait-for
       [:profile/get-profiles-overview-success]
       (create-multiaccount!)
       (rf-test/wait-for
         [::transport/messenger-started]
         (assert-messenger-started)
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
                 (is (= three-words-name (:primary-name contact))))
               (logout!)
               (rf-test/wait-for [::logout/logout-method])))))))))
