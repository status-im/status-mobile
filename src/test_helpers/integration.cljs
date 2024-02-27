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
    [re-frame.interop :as rf.interop]
    status-im.events
    status-im.navigation.core
    status-im.subs.root
    [taoensso.timbre :as log]
    [tests.integration-test.constants :as constants]
    [utils.collection :as collection]))

(def default-re-frame-wait-for-timeout-ms
  "Controls the maximum time allowed to wait for all events to be processed by
  re-frame on every call to `wait-for`.

  Take into consideration that some endpoints/signals may take significantly
  more time to finish/arrive."
  (* 10 1000))

(def default-integration-test-timeout-ms
  "Use a high-enough value in milliseconds to timeout integration tests. Not too
  small, which would cause sporadic failures, and not too high as to make you
  sleepy."
  (* 60 1000))

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

  If an event ID that is expected in `event-ids` occurs in a different order,
  the promise will be rejected."
  ([event-ids]
   (wait-for event-ids default-re-frame-wait-for-timeout-ms))
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

(defn setup-app
  []
  (legacy-test/init!)
  (if (app-initialized)
    (p/resolved ::app-initialized)
    (do
      (rf/dispatch [:app-started])
      (wait-for [:profile/get-profiles-overview-success]))))

(defn setup-account
  []
  (if (messenger-started)
    (p/resolved ::messenger-started)
    (do
      (create-multiaccount!)
      (-> (wait-for [:messenger-started])
          (.then #(assert-messenger-started))))))

(defn integration-test
  "Runs `f` inside `cljs.test/async` macro in a restorable re-frame checkpoint.

  Option `fail-fast?`, when truthy (defaults to true), will force the test
  runner to terminate on any test failure. Setting it to false can be useful
  when you want the rest of the test suite to run due to a flaky test.

  When `fail-fast?` is falsey, re-frame's state is automatically restored after
  a test failure, so that the next integration test can run from a pristine
  state.

  Option `timeout-ms` controls the total time allowed to run `f`. The value
  should be high enough to account for some variability, otherwise the test may
  fail more often.
  "
  ([test-name f]
   (integration-test test-name
                     {:fail-fast? true
                      :timeout-ms default-integration-test-timeout-ms}
                     f))
  ([test-name {:keys [fail-fast? timeout-ms]} f]
   (test/async
     done
     (let [restore-fn (rf/make-restore-fn)]
       (log-headline test-name)
       (-> (p/do (f done))
           (p/timeout timeout-ms)
           (p/catch (fn [error]
                      (is (nil? error))
                      (when fail-fast?
                        (js/process.exit 1))))
           (p/finally (fn []
                        (restore-fn)
                        (done))))))))

;;;; Fixtures

(defn fixture-logged
  "Fixture to set up the application and a logged account before the test runs.
  Log out after the test is done.

  Usage:

      (use-fixtures :each (h/fixture-logged))"
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

(defn fixture-silence-reframe
  "Fixture to disable most re-frame warnings.

  Avoid using this fixture for non-dev purposes because in the CI output it's
  desirable to have more data to debug, not less.

  Example messages disabled:

  - Warning about subscriptions being used in non-reactive contexts.
  - Debug message \"Handling re-frame event: XYZ\".

  Usage:

      (use-fixtures :once (h/fixture-silence-re-frame))
  "
  []
  {:before (fn []
             (set! rf.interop/debug-enabled? false))
   :after  (fn []
             (set! rf.interop/debug-enabled? true))})
