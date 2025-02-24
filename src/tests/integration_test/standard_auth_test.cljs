(ns tests.integration-test.standard-auth-test
  (:require
    [cljs.test :refer [deftest is testing]]
    [day8.re-frame.test :as rf-test]
    re-frame.core
    [test-helpers.integration :as h]
    [utils.re-frame :as rf]))

(def default-args
  {:on-auth-success       identity
   :on-auth-fail          identity
   :on-close              identity
   :auth-button-label     "test"
   :auth-button-icon-left :test-icon
   :blur?                 false
   :theme                 :light})

(defn auth-success-fixtures
  []
  (rf/reg-fx :effects.standard-auth/on-auth-success
   (fn [on-auth-success] (on-auth-success)))
  (rf/reg-fx :effects.biometric/check-if-available
   (fn [{:keys [on-success]}] (on-success)))
  (rf/reg-event-fx :biometric/authenticate
   (fn [_ [{:keys [on-success]}]] (on-success)))
  (rf/reg-fx :keychain/get-user-password
   (fn [[_ on-success]] (on-success))))

(deftest standard-auth-biometric-authorize-success-test
  (testing "calling success callback when completing biometric authentication"
    (h/log-headline :standard-auth-authorize-success)
    (rf-test/run-test-async
     (auth-success-fixtures)
     (let [on-success-called? (atom false)
           args               (assoc default-args :on-auth-success #(reset! on-success-called? true))]
       (rf/dispatch [:standard-auth/authorize args])
       (rf-test/wait-for [:standard-auth/migrate-partially-operable-accounts]
         (is @on-success-called?))))))

(defn auth-cancel-fixtures
  []
  (rf/reg-fx :effects.biometric/check-if-available
   (fn [{:keys [on-success]}] (on-success)))
  (rf/reg-event-fx :biometric/authenticate
   (fn [_ [{:keys [on-cancel]}]] (on-cancel)))
  (rf/reg-event-fx :show-bottom-sheet identity))

(deftest standard-auth-biometric-authorize-cancel-test
  (testing "falling back to password authorization when biometrics canceled"
    (h/log-headline :standard-auth-authorize-cancel)
    (rf-test/run-test-async
     (auth-cancel-fixtures)
     (rf/dispatch [:standard-auth/authorize default-args])
     (rf-test/wait-for [:show-bottom-sheet]
       (is true)))))

(defn auth-fail-fixtures
  [expected-error-cause]
  (rf/reg-fx :effects.biometric/check-if-available
   (fn [{:keys [on-success]}] (on-success)))
  (rf/reg-event-fx :biometric/authenticate
   (fn [_ [{:keys [on-fail]}]] (on-fail (ex-info "error" {} expected-error-cause))))
  (rf/reg-event-fx :biometric/show-message identity))

(deftest standard-auth-biometric-authorize-fail-test
  (testing "showing biometric error message when authorization failed"
    (h/log-headline :standard-auth-authorize-fail)
    (rf-test/run-test-async
     (let [on-fail-called?      (atom false)
           expected-error-cause :bad-error
           error                (atom nil)
           args                 (assoc default-args
                                       :on-auth-fail
                                       (fn [err]
                                         (reset! on-fail-called? true)
                                         (reset! error err)))]
       (auth-fail-fixtures expected-error-cause)
       (rf/dispatch [:standard-auth/authorize args])
       (rf-test/wait-for [:biometric/show-message]
         (is @on-fail-called?)
         (is (= expected-error-cause (ex-cause @error))))))))

(defn auth-password-fallback-fixtures
  []
  (rf/reg-fx :effects.biometric/check-if-available
   (fn [{:keys [on-fail]}] (on-fail)))
  (rf/reg-event-fx :show-bottom-sheet identity))

(deftest standard-auth-password-authorize-fallback-test
  (testing "falling back to password when biometrics is not available"
    (h/log-headline :standard-auth-password-authorize-fallback)
    (rf-test/run-test-async
     (auth-password-fallback-fixtures)
     (rf/dispatch [:standard-auth/authorize default-args])
     (rf-test/wait-for [:standard-auth/authorize-with-password]
       (is true)))))
