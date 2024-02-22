(ns tests.integration-test.profile-test
  (:require
    [cljs.test :refer [deftest is]]
    [day8.re-frame.test :as rf-test]
    [legacy.status-im.multiaccounts.logout.core :as logout]
    [legacy.status-im.utils.test :as utils.test]
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
             (is (= new-name display-name)))
           (h/logout)
           (rf-test/wait-for [::logout/logout-method]))))))))

(deftest edit-profile-picture-test
  (h/log-headline :edit-profile-picture-test)
  (let [mock-image    "resources/images/mock2/monkey.png"
        absolute-path (.resolve utils.test/path mock-image)]
    (rf-test/run-test-async
     (h/with-app-initialized
      (h/with-account
       (rf/dispatch [:profile/edit-picture absolute-path 80 80])
       (rf-test/wait-for
         [:profile/update-local-picture]
         (rf-test/wait-for
           [:toasts/upsert]
           (let [profile (rf/sub [:profile/profile])]
             (is (not (nil? (:images profile)))))
           (h/logout)
           (rf-test/wait-for [::logout/logout-method]))))))))

(deftest delete-profile-picture-test
  (h/log-headline :delete-profile-picture-test)
  (rf-test/run-test-async
   (h/with-app-initialized
    (h/with-account
     (rf/dispatch [:profile/delete-picture])
     (rf-test/wait-for
       [:profile/update-local-picture]
       (rf-test/wait-for
         [:toasts/upsert]
         (let [profile (rf/sub [:profile/profile])]
           (is (nil? (:image profile))))
         (h/logout)
         (rf-test/wait-for [::logout/logout-method])))))))

(deftest edit-profile-bio-test
  (h/log-headline :edit-profile-bio-test)
  (let [new-bio "New bio text"]
    (rf-test/run-test-async
     (h/with-app-initialized
      (h/with-account
       (rf/dispatch [:profile/edit-bio new-bio])
       (rf-test/wait-for
         [:navigate-back]
         (rf-test/wait-for
           [:toasts/upsert]
           (let [profile (rf/sub [:profile/profile])
                 bio     (:bio profile)]
             (is (= new-bio bio)))
           (h/logout)
           (rf-test/wait-for [::logout/logout-method]))))))))
