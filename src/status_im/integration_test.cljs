(ns status-im.integration-test
  (:require [cljs.test :refer [deftest is run-tests]]
            [day8.re-frame.test :as rf-test]
            [re-frame.core :as rf]
            status-im.events
            [status-im.multiaccounts.logout.core :as logout]
            [status-im.transport.core :as transport]
            status-im.subs ;;so integration tests can run independently
            [status-im.ethereum.core :as ethereum]
            [status-im.utils.test :as utils.test]))

(def password "testabc")

(def community {:membership 1 :name "foo" :description "bar"})

(def account-name "account-abc")

(utils.test/init!)

(defn initialize-app! []
  (rf/dispatch-sync [:init/app-started]))

(defn generate-and-derive-addresses! []
  (rf/dispatch [:generate-and-derive-addresses]))

(defn create-multiaccount! []
  (rf/dispatch [:create-multiaccount password]))

(defn assert-app-initialized []
  (let [app-state @(rf/subscribe [:app-state])
        multiaccounts-loading? @(rf/subscribe [:multiaccounts/loading])]
    (is (= "active" app-state))
    (is (false? multiaccounts-loading?))))

(defn assert-logout []
  (let [multiaccounts-loading? @(rf/subscribe [:multiaccounts/loading])]
    (is multiaccounts-loading?)))

(defn assert-multiaccounts-generated []
  (let [wizard-state @(rf/subscribe [:intro-wizard/choose-key])]
    (is (= 5 (count (:multiaccounts wizard-state))))))

(defn messenger-started []
  @(rf/subscribe [:messenger/started?]))

(defn assert-messenger-started []
  (is (messenger-started)))

(deftest initialize-app-test
  (rf-test/run-test-sync
   (rf/dispatch [:init/app-started])
   (assert-app-initialized)))

(defn assert-multiaccount-loaded []
  (is (false? @(rf/subscribe [:multiaccounts/loading]))))

(defn assert-community-created []
  (is (= @(rf/subscribe [:communities/create]) community)))

(defn create-new-account! []
  (rf/dispatch-sync [:wallet.accounts/start-adding-new-account {:type :generate}])
  (rf/dispatch-sync [:set-in [:add-account :account :name] account-name])
  (rf/dispatch [:wallet.accounts/add-new-account (ethereum/sha3 password)]))

(defn assert-new-account-created []
  (is (true? (some #(= (:name %) account-name)
                   @(rf/subscribe [:multiaccount/accounts])))))

(defn logout! []
  (rf/dispatch [:logout]))

(deftest create-account-test
  (rf-test/run-test-async
   (initialize-app!) ; initialize app
   (rf-test/wait-for
    [:status-im.init.core/initialize-multiaccounts] ; wait so we load accounts.db
    (generate-and-derive-addresses!) ; generate 5 new keys
    (rf-test/wait-for
     [:multiaccount-generate-and-derive-addresses-success] ; wait for the keys
     (assert-multiaccount-loaded) ; assert keys are generated
     (create-multiaccount!) ; create a multiaccount
     (rf-test/wait-for ; wait for login
      [::transport/messenger-started]
      (assert-messenger-started)
      (logout!)
      (rf-test/wait-for [::logout/logout-method] ; we need to logout to make sure the node is not in an inconsistent state between tests
                        (assert-logout)))))))

(deftest create-community-test
  (rf-test/run-test-async
   (initialize-app!) ; initialize app
   (rf-test/wait-for
    [:status-im.init.core/initialize-multiaccounts]
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
  (rf-test/run-test-async
   (initialize-app!) ; initialize app
   (rf-test/wait-for
    [:status-im.init.core/initialize-multiaccounts]
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
       (rf-test/wait-for [::logout/logout-method] ; we need to logout to make sure the node is not in an inconsistent state between tests
                         (assert-logout))))))))

(def chat-id "0x0402905bed83f0bbf993cee8239012ccb1a8bc86907ead834c1e38476a0eda71414eed0e25f525f270592a2eebb01c9119a4ed6429ba114e51f5cb0a28dae1adfd")

(deftest one-to-one-chat-test
  (rf-test/run-test-async
   (initialize-app!)
   (rf-test/wait-for
    [:status-im.init.core/initialize-multiaccounts]
    (generate-and-derive-addresses!)
    (rf-test/wait-for
     [:multiaccount-generate-and-derive-addresses-success] ; wait for the keys
     (assert-multiaccount-loaded)
     (create-multiaccount!)
     (rf-test/wait-for
      [:status-im.transport.core/messenger-started]
      (assert-messenger-started)
      (rf/dispatch-sync [:chat.ui/start-chat chat-id]) ;; start a new chat
      (rf-test/wait-for
       [:status-im.chat.models/one-to-one-chat-created]
       (rf/dispatch-sync [:chat.ui/navigate-to-chat chat-id])
       (is (= chat-id
              @(rf/subscribe [:chats/current-chat-id])))))))))

(comment
  (run-tests))
