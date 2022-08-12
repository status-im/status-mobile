(ns status-im.integration-test
  (:require [cljs.test :refer [deftest is run-tests]]
            [day8.re-frame.test :as rf-test]
            [re-frame.core :as rf]
            status-im.events
            [status-im.multiaccounts.logout.core :as logout]
            [status-im.transport.core :as transport]
            [status-im.utils.test :as utils.test]))

(def password "testabc")

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

;; This test is only to make sure running multiple integration tests doesn't hang
;; can be removed as soon as another test is added
(deftest create-account-test-safety-check
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
      (rf-test/wait-for [::logout/logout-method]
                        (assert-logout)))))))

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
