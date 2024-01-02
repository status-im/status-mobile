(ns status-im.integration-test.profile-test
  (:require
    [cljs.test :refer [deftest is]]
    [day8.re-frame.test :as rf-test]
    [status-im.contexts.profile.utils :as profile.utils]
    [test-helpers.integration :as h]
    [utils.re-frame :as rf]))

(deftest edit-profile-name-test
  (h/log-headline :edit-profile-name-test)
  (let [new-name "John Doe"]
    (rf-test/run-test-async
     (h/with-app-initialized
      (h/with-account
       (rf/dispatch [:profile/edit-name new-name])
       (rf-test/wait-for
         [:navigate-back]
         (rf-test/wait-for
           [:toasts/upsert]
           (let [profile      (rf/sub [:profile/profile])
                 display-name (profile.utils/displayed-name profile)]
             (is (= new-name display-name))))))))))
