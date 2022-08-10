(ns status-im.integration-test
  (:require [cljs.test :refer [deftest is run-tests]]
            [day8.re-frame.test :as rf-test]
            [re-frame.core :as rf]
            status-im.events
            [status-im.multiaccounts.logout.core :as logout]
            [status-im.transport.core :as transport]
            ;; status-im.subs ;;so integration tests can run independently
            [status-im.utils.test :as utils.test]))

(def password "testabc")

(def community {:membership 1 :name "foo" :description "bar"})

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

(comment
  (run-tests))
