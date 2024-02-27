(ns test-helpers.integration
  (:require-macros [test-helpers.integration])
  (:require
    [cljs.test :refer [is] :as test]
    legacy.status-im.events
    [legacy.status-im.multiaccounts.logout.core :as logout]
    legacy.status-im.subs.root
    [legacy.status-im.utils.test :as legacy-test]
    [native-module.core :as native-module]
    [promesa.core :as p]
    [re-frame.core :as rf]
    status-im.events
    status-im.navigation.core
    status-im.subs.root
    [taoensso.timbre :as log]
    [tests.integration-test.constants :as constants]
    [utils.collection :as collection]))

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
  (log/info (str "==== " test-name " ====")))

(defn wait-for
  "Returns a promise that resolves when all `event-ids` are processed by re-frame,
  otherwise rejects after `timeout-ms`.

  If an event ID that is expected in `event-ids` happens in a different order,
  the promise will be rejected."
  ([event-ids]
   (wait-for event-ids 10000))
  ([event-ids timeout-ms]
   (let [waiting-ids (atom event-ids)]
     (js/Promise.
      (fn [promise-resolve promise-reject]
        (let [cb-id    (gensym "post-event-callback")
              timer-id (js/setTimeout (fn []
                                        (rf/remove-post-event-callback cb-id)
                                        (promise-reject (ex-info
                                                         "timed out waiting for all event-ids to run"
                                                         {:event-ids   event-ids
                                                          :waiting-ids @waiting-ids
                                                          :timeout-ms  timeout-ms}
                                                         ::timeout)))
                                      timeout-ms)]
          (rf/add-post-event-callback
           cb-id
           (fn [[event-id & _]]
             (when-let [idx (collection/first-index #(= % event-id) @waiting-ids)]
               ;; All `event-ids` should be processed in their original order.
               (if (zero? idx)
                 (do
                   (swap! waiting-ids rest)
                   ;; When there's nothing else to wait for, clean up resources.
                   (when (empty? @waiting-ids)
                     (js/clearTimeout timer-id)
                     (rf/remove-post-event-callback cb-id)
                     (promise-resolve)))
                 (do
                   (js/clearTimeout timer-id)
                   (rf/remove-post-event-callback cb-id)
                   (promise-reject (ex-info "event happened in unexpected order"
                                            {:event-ids   event-ids
                                             :waiting-for @waiting-ids}
                                            ::out-of-order-event-id)))))))))))))

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

(defn setup-app
  []
  (legacy-test/init!)
  (if (app-initialized)
    (js/Promise.resolve)
    (do
      (rf/dispatch [:app-started])
      (wait-for [:profile/get-profiles-overview-success]))))

(defn setup-account
  []
  (if (messenger-started)
    (js/Promise.resolve)
    (do
      (create-multiaccount!)
      (-> (wait-for [:messenger-started])
          (.then #(assert-messenger-started))))))

;;;; Fixtures

(defn fixture-logged
  []
  {:before (fn []
             (test/async done
               (p/do (setup-app)
                     (setup-account)
                     (done))))
   :after  (fn []
             (test/async done
               (p/do (logout)
                     (wait-for [::logout/logout-method])
                     (done))))})
