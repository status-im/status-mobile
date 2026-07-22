(ns status-im.contexts.onboarding.events-test
  (:require [cljs.test :refer [deftest is testing]]
            [status-im.contexts.onboarding.events :as sut]))

(deftest on-delete-profile-success-test
  (testing "clears cached auth credentials when removing a profile"
    (let [key-uid       "profile-key"
          remaining-uid "remaining-profile"
          cofx          {:db {:profile/profiles-overview {key-uid       {:name "deleted"}
                                                          remaining-uid {:name "remaining"}}}}
          result        (sut/on-delete-profile-success cofx key-uid)]
      (is (= {remaining-uid {:name "remaining"}}
             (get-in result [:db :profile/profiles-overview])))
      (is (some #{[:keychain/clear-user-password key-uid]} (:fx result))))))
