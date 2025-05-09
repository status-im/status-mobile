(ns tests.integration-test.profile-test
  (:require
    [cljs.test :refer [deftest is use-fixtures]]
    [promesa.core :as promesa]
    [status-im.contexts.profile.utils :as profile.utils]
    [test-helpers.integration :as h]
    [tests.test-utils :as test-utils]
    [utils.re-frame :as rf]))

(use-fixtures :each (h/fixture-session))

(deftest edit-profile-name-test
  (h/test-async ::edit-profile-name
    (fn []
      (let [new-name "John Doe"]
        (promesa/do
          (rf/dispatch [:profile/edit-name {:display-name new-name}])
          (h/wait-for [:navigate-back :toasts/upsert])
          (let [profile      (rf/sub [:profile/profile])
                display-name (profile.utils/displayed-name profile)]
            (is (= new-name display-name))))))))

(deftest edit-profile-picture-test
  (h/test-async ::edit-profile-picture
    (fn []
      (let [mock-image    "resources/images/mock2/monkey.png"
            absolute-path (.resolve test-utils/path mock-image)]
        (promesa/do
          (rf/dispatch [:profile/edit-picture
                        {:picture     absolute-path
                         :crop-width  80
                         :crop-height 80}])
          (h/wait-for [:profile/update-local-picture :toasts/upsert])
          (let [profile (rf/sub [:profile/profile])]
            (is (not (nil? (:images profile))))))))))

(deftest delete-profile-picture-test
  (h/test-async ::delete-profile-picture
    (fn []
      (promesa/do
        (rf/dispatch [:profile/delete-picture])
        (h/wait-for [:profile/update-local-picture :toasts/upsert])
        (let [profile (rf/sub [:profile/profile])]
          (is (nil? (:image profile))))))))

(deftest edit-profile-bio-test
  (h/test-async ::edit-profile-bio
    (fn []
      (let [new-bio "New bio text"]
        (promesa/do
          (rf/dispatch [:profile/edit-bio new-bio])
          (h/wait-for [:navigate-back :toasts/upsert])
          (let [profile (rf/sub [:profile/profile])
                bio     (:bio profile)]
            (is (= new-bio bio))))))))
