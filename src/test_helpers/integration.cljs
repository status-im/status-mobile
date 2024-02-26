(ns test-helpers.integration
  (:require
    [cljs.test :refer [is] :as test]
    legacy.status-im.events
    legacy.status-im.subs.root
    [legacy.status-im.utils.test :as legacy-test]
    [native-module.core :as native-module]
    [re-frame.core :as rf]
    status-im.events
    status-im.navigation.core
    status-im.subs.root
    [taoensso.timbre :as log]
    [tests.integration-test.constants :as constants]))

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

(defn wait-for
  ([target-event-ids]
   (wait-for target-event-ids 10000))
  ([target-event-ids timeout-ms]
   (let [waiting-for (atom (set target-event-ids))]
     (js/Promise.
      (fn [promise-resolve promise-reject]
        (let [cb-id    (gensym "post-event-callback")
              timer-id (js/setTimeout (fn []
                                        (rf/remove-post-event-callback cb-id)
                                        (promise-reject (ex-info "some events did not run"
                                                                 {:event-ids   target-event-ids
                                                                  :timeout-ms  timeout-ms
                                                                  :waiting-for @waiting-for}
                                                                 ::timeout)))
                                      timeout-ms)]
          (rf/add-post-event-callback
           cb-id
           (fn [[event-id & _]]
             (when (contains? @waiting-for event-id)
               (swap! waiting-for disj event-id)
               (when (empty? @waiting-for)
                 (js/clearTimeout timer-id)
                 (rf/remove-post-event-callback cb-id)
                 (promise-resolve)))))))))))

(defn rf-test-async
  [f]
  (test/async
   done
   (let [restore-fn (rf/make-restore-fn)]
     (-> (f done)
         (.catch (fn [error]
                   (is (true? false) (str "async test failed" error))))
         (.finally (fn []
                     (restore-fn)
                     (done)))))))

(defn with-app-initialized
  []
  (legacy-test/init!)
  (if (app-initialized)
    (js/Promise.resolve)
    (do
      (rf/dispatch [:app-started])
      (wait-for [:profile/get-profiles-overview-success]))))

(defn with-account
  []
  (if (messenger-started)
    (js/Promise.resolve)
    (do
      (create-multiaccount!)
      (-> (wait-for [:messenger-started])
          (.then #(assert-messenger-started))))))
