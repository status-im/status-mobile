(ns status-im.integration-test.standard-auth-test
  (:require
    [cljs.test :refer [deftest testing is use-fixtures]]
    [day8.re-frame.test :as rf-test]
    re-frame.core
    [test-helpers.integration :as h]
    [utils.re-frame :as rf]))

(defn fixture-re-frame
  []
  (let [restore-re-frame (atom nil)]
    {:before #(reset! restore-re-frame (re-frame.core/make-restore-fn))
     :after  #(@restore-re-frame)}))

(use-fixtures :each (fixture-re-frame))

(def default-args
  {:on-auth-success       identity
   :on-auth-fail          identity
   :on-close              identity
   :auth-button-label     "test"
   :auth-button-icon-left :test-icon
   :blur?                 false
   :theme                 :light})

(deftest standard-auth-biometric-authorize-success
  (testing "calling success callback when completing biometric authentication"
    (h/log-headline :standard-auth-authorize-success)
    (rf-test/run-test-async
     (do
       (rf/reg-fx :biometric/check-if-available
        (fn [{:keys [on-success]}]
          (rf/dispatch on-success)))
       (rf/reg-event-fx :biometric/authenticate
        (fn [_ [{:keys [on-success]}]]
          (rf/dispatch on-success)))
       (rf/reg-fx :keychain/get-user-password
        (fn [[_ on-success]]
          (on-success))))
     (let [on-success-called? (atom false)
           args               (assoc default-args :on-auth-success #(reset! on-success-called? true))]
       (rf/dispatch [:standard-auth/authorize args])
       (rf-test/wait-for [:standard-auth/on-biometric-success]
         (is @on-success-called?))))))

(deftest standard-auth-biometric-authorize-cancel
  (testing "falling back to password authorization when biometrics canceled"
    (h/log-headline :standard-auth-authorize-cancel)
    (rf-test/run-test-async
     (do
       (rf/reg-fx :biometric/check-if-available
        (fn [{:keys [on-success]}]
          (rf/dispatch on-success)))
       (rf/reg-event-fx :biometric/authenticate
        (fn [_ [{:keys [on-cancel]}]]
          (rf/dispatch on-cancel)))
       (rf/reg-event-fx :show-bottom-sheet
        (fn [_])))
     (rf/dispatch [:standard-auth/authorize default-args])
     (rf-test/wait-for [:show-bottom-sheet]
       (is true)))))

(deftest standard-auth-biometric-authorize-fail
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
       (rf/reg-fx :biometric/check-if-available
        (fn [{:keys [on-success]}]
          (rf/dispatch on-success)))
       (rf/reg-event-fx :biometric/authenticate
        (fn [_ [{:keys [on-fail]}]]
          (rf/dispatch (conj on-fail (ex-info "error" {} expected-error-cause)))))
       (rf/reg-fx :biometric/show-message
        (fn [_]))
       (rf/dispatch [:standard-auth/authorize args])
       (rf-test/wait-for [:biometric/show-message]
         (is @on-fail-called?)
         (is (= expected-error-cause (ex-cause @error))))))))

(deftest standard-auth-password-authorize-fallback
  (testing "falling back to password when biometrics is not available"
    (h/log-headline :standard-auth-password-authorize-fallback)
    (rf-test/run-test-async
     (do
       (rf/reg-fx :biometric/check-if-available
        (fn [{:keys [on-fail]}]
          (rf/dispatch on-fail)))
       (rf/reg-event-fx :show-bottom-sheet
        (fn [_])))
     (rf/dispatch [:standard-auth/authorize default-args])
     (rf-test/wait-for [:standard-auth/authorize-with-password]
       (is true)))))
