(ns status-im.integration-test
  (:require [cljs.test :refer [deftest is run-tests]]
            [day8.re-frame.test :as rf-test]
            [re-frame.core :as rf]
            status-im.events
            [status-im.transport.core :as transport]
            [status-im.utils.test :as utils.test]))

(def password "testabc")

(utils.test/init!)

(def initialize-app!
  #(rf/dispatch-sync [:init/app-started]))

(def generate-and-derive-addresses!
  #(rf/dispatch [:generate-and-derive-addresses]))

(def create-multiaccount!
  #(rf/dispatch [:create-multiaccount password]))

(def assert-app-initialized
  #(let [app-state @(rf/subscribe [:app-state])
         multiaccounts-loading? @(rf/subscribe [:multiaccounts/loading])]
     (is (= "active" app-state))
     (is (false? multiaccounts-loading?))))

(def assert-multiaccounts-generated
  #(let [wizard-state @(rf/subscribe [:intro-wizard/choose-key])]
     (is (= 5 (count (:multiaccounts wizard-state))))))

(defn messenger-started []
  @(rf/subscribe [:messenger/started?]))

(def assert-messenger-started
  #(is (messenger-started)))

(deftest initialize-app-test
  (rf-test/run-test-sync
   (rf/dispatch [:init/app-started])
   (assert-app-initialized)))

(defn assert-multiaccount-loaded []
  (is (false? @(rf/subscribe [:multiaccounts/loading]))))

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
      (assert-messenger-started))))))

(comment
  (run-tests))
