(ns tests.integration-test.profile-test
  (:require
    [cljs.test :refer [deftest is use-fixtures]]
    [legacy.status-im.utils.test :as utils.test]
    [promesa.core :as p]
    [status-im.contexts.profile.utils :as profile.utils]
    [test-helpers.integration :as h]
    [utils.re-frame :as rf]))

(use-fixtures :each (h/fixture-logged))

(deftest edit-profile-name-test
  (h/rf-test-async
   (fn []
     (h/log-headline ::edit-profile-name-test)
     (let [new-name "John Doe"]
       (p/do
         (rf/dispatch [:profile/edit-name new-name])
         (h/wait-for [:navigate-back :toasts/upsert])
         (let [profile      (rf/sub [:profile/profile])
               display-name (profile.utils/displayed-name profile)]
           (is (= new-name display-name))))))))

(deftest edit-profile-picture-test
  (h/rf-test-async
   (fn []
     (h/log-headline ::edit-profile-picture-test)
     (let [mock-image    "resources/images/mock2/monkey.png"
           absolute-path (.resolve utils.test/path mock-image)]
       (p/do
         (rf/dispatch [:profile/edit-picture absolute-path 80 80])
         (h/wait-for [:profile/update-local-picture :toasts/upsert])
         (let [profile (rf/sub [:profile/profile])]
           (is (not (nil? (:images profile))))))))))

(deftest delete-profile-picture-test
  (h/rf-test-async
   (fn []
     (h/log-headline ::delete-profile-picture-test)
     (p/do
       (rf/dispatch [:profile/delete-picture])
       (h/wait-for [:profile/update-local-picture :toasts/upsert])
       (let [profile (rf/sub [:profile/profile])]
         (is (nil? (:image profile))))))))

(deftest edit-profile-bio-test
  (h/rf-test-async
   (fn []
     (h/log-headline ::edit-profile-bio-test)
     (let [new-bio "New bio text"]
       (p/do
         (rf/dispatch [:profile/edit-bio new-bio])
         (h/wait-for [:navigate-back :toasts/upsert])
         (let [profile (rf/sub [:profile/profile])
               bio     (:bio profile)]
           (is (= new-bio bio))))))))
