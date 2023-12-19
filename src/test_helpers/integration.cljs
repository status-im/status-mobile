(ns test-helpers.integration
  (:require-macros [test-helpers.integration])
  (:require
    [cljs.test :refer [is]]
    legacy.status-im.events
    legacy.status-im.subs.root
    [native-module.core :as native-module]
    [re-frame.core :as rf]
    status-im2.events
    [status-im2.integration-test.constants :as constants]
    status-im2.navigation.core
    status-im2.subs.root
    [taoensso.timbre :as log]))

(defn initialize-app!
  []
  (rf/dispatch [:app-started]))

(defn create-multiaccount!
  []
  (rf/dispatch [:profile.create/create-and-login
                {:display-name constants/account-name :password constants/password :color "blue"}]))

(defn app-initialized
  []
  (let [app-state @(rf/subscribe [:app-state])]
    (= "active" app-state)))

(defn assert-app-initialized
  []
  (is (app-initialized)))

(defn messenger-started
  []
  @(rf/subscribe [:messenger/started?]))

(defn assert-messenger-started
  []
  (is (messenger-started)))

(defn assert-community-created
  []
  (is (= @(rf/subscribe [:communities/create]) constants/community)))

(defn create-new-account!
  []
  (rf/dispatch-sync [:wallet-legacy.accounts/start-adding-new-account {:type :generate}])
  (rf/dispatch-sync [:set-in [:add-account :account :name] constants/account-name])
  (rf/dispatch [:wallet-legacy.accounts/add-new-account (native-module/sha3 constants/password)]))

(defn assert-new-account-created
  []
  (is (true? (some #(= (:name %) constants/account-name)
                   @(rf/subscribe [:profile/wallet-accounts])))))

(defn logout
  []
  (rf/dispatch [:logout]))

(defn log-headline
  [test-name]
  (log/info (str "========= " (name test-name) " ==================")))
