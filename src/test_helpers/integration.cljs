(ns test-helpers.integration
  (:require
    [cljs.test :refer [is] :as test]
    legacy.status-im.events
    legacy.status-im.subs.root
    [native-module.core :as native-module]
    [promesa.core :as promesa]
    [re-frame.core :as rf]
    status-im.events
    status-im.navigation.core
    status-im.subs.root
    [taoensso.timbre :as log]
    [tests.integration-test.constants :as constants]
    [tests.test-utils :as test-utils]
    [utils.collection :as collection]
    [utils.security.core :as security]
    [utils.transforms :as transforms]))

(defn validate-mnemonic
  [mnemonic]
  (promesa/create
   (fn [p-resolve p-reject]
     (native-module/validate-mnemonic
      (security/safe-unmask-data mnemonic)
      (fn [result]
        (let [{:keys [error keyUID]} (transforms/json->clj result)]
          (if (seq error)
            (p-reject error)
            (p-resolve [mnemonic keyUID]))))))))

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

(defn wallet-loaded?
  []
  (not @(rf/subscribe [:wallet/home-tokens-loading?])))

(defn assert-messenger-started
  []
  (is (messenger-started)))

(defn assert-wallet-loaded
  []
  (is (wallet-loaded?)))

(defn logout
  []
  (log/info (str "==== before dispatch logout ===="))
  (rf/dispatch [:profile/logout]))

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
     (promesa/create
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
          (log/info (str "==== inside wait-for after let block ===="))
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
  (test-utils/init!)
  (if (app-initialized)
    (promesa/resolved ::app-initialized)
    (promesa/do!
     (rf/dispatch [:app-started])
     (wait-for [:profile/get-profiles-overview-success]))))

(defn setup-account
  []
  (if (messenger-started)
    (promesa/resolved ::messenger-started)
    (promesa/do!
     (create-multiaccount!)
     (promesa/then (wait-for [:profile.login/messenger-started]) #(assert-messenger-started)))))

(defn- recover-and-login
  [seed-phrase]
  (rf/dispatch [:profile.recover/recover-and-login
                {:display-name (:name constants/recovery-account)
                 :seed-phrase  seed-phrase
                 :password     constants/password
                 :color        "blue"}]))

(defn enable-testnet!
  []
  (rf/dispatch [:profile.settings/profile-update :test-networks-enabled? true {}])
  (rf/dispatch [:wallet/initialize]))

(defn- recover-multiaccount!
  []
  (promesa/let [masked-seed-phrase (security/mask-data (:seed-phrase constants/recovery-account))
                [mnemonic key-uid] (validate-mnemonic masked-seed-phrase)]
    (rf/dispatch [:onboarding/seed-phrase-validated (security/mask-data mnemonic) key-uid])
    (rf/dispatch [:pop-to-root :screen/profile.profiles])
    (rf/dispatch [:profile/profile-selected key-uid])
    (recover-and-login mnemonic)))

(defn setup-recovered-account
  []
  (if (messenger-started)
    (promesa/resolved ::messenger-started)
    (promesa/do!
     (recover-multiaccount!)
     (promesa/then (wait-for [:profile.login/messenger-started]) #(assert-messenger-started))
     (enable-testnet!)
     (promesa/then (wait-for [:wallet/store-wallet-token]) #(assert-wallet-loaded)))))

(defn test-async
  "Runs `f` inside `cljs.test/async` macro in a restorable re-frame checkpoint.

  `f` will be called with one argument, the `done` function exposed by the
  `cljs.test/async` macro. Normally, you don't need to use `done`, but you can
  call it if you want to early-terminate the current test, so that the test
  runner can execute the next one.

  Option `fail-fast?`, when truthy (defaults to true), will force the test
  runner to terminate on any test failure. Setting it to false can be useful
  during development when you want the rest of the test suite to run due to a
  flaky test. Prefer to fail fast in the CI to save on time & resources.

  When `fail-fast?` is falsey, re-frame's state is automatically restored after
  a test failure, so that the next integration test can run from a pristine
  state.

  Option `timeout-ms` controls the total time allowed to run `f`. The value
  should be high enough to account for some variability, otherwise the test may
  fail more often.
  "
  ([test-name f]
   (test-async
     test-name
     {:fail-fast? true
      :timeout-ms default-integration-test-timeout-ms}
     f))
  ([test-name {:keys [fail-fast? timeout-ms]} f]
   (test/async
     done
     (let [restore-fn (rf/make-restore-fn)]
       (log-headline test-name)
       (-> (promesa/do (f done))
           (promesa/timeout timeout-ms)
           (promesa/catch (fn [error]
                            (is (nil? error))
                            (when fail-fast?
                              (js/process.exit 1))))
           (promesa/finally (fn []
                              (restore-fn)
                              (done))))))))

(defn test-app-initialization
  []
  (test-async ::initialize-app
    (fn []
      (promesa/do
        (test-utils/init!)
        (rf/dispatch [:app-started])
        ;; Use initialize-view because it has the longest avg. time and is
        ;; dispatched by initialize-multiaccounts (last non-view event).
        (wait-for [:profile/get-profiles-overview-success
                   :font/init-font-file-for-initials-avatar])
        (assert-app-initialized)))))

(defn test-account-creation
  []
  (test-async ::create-account
    (fn []
      (promesa/do
        (setup-app)
        (setup-account)
        (logout)
        (log/info (str "==== before wait-for logout ===="))
        (wait-for [:profile.logout/reset-state])
        (log/info (str "==== after wait-for logout ===="))))))

;;;; Fixtures

(defn fixture-session
  "Fixture to set up the application and a logged account before the test runs.
  Log out after the test is done.

  Usage:

      (use-fixtures :each (h/fixture-logged))"
  ([type]
   {:before (if (= :recovered-account type)
              (fn []
                (test/async done
                  (promesa/do (setup-app)
                              (setup-recovered-account)
                              (done))))
              (fn []
                (test/async done
                  (promesa/do (setup-app)
                              (setup-account)
                              (done)))))
    :after  (fn []
              (test/async done
                (promesa/do (logout)
                            (log/info (str "==== before wait-for logout ===="))
                            (wait-for [:profile.logout/reset-state])
                            (log/info (str "==== after wait-for logout ===="))
                            (done))))})
  ([] (fixture-session [:new-account])))


