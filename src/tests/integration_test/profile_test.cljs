(ns tests.integration-test.profile-test
  (:require
    [cljs.test :refer [deftest is]]
    [legacy.status-im.multiaccounts.logout.core :as logout]
    [legacy.status-im.utils.test :as utils.test]
    [status-im.contexts.profile.utils :as profile.utils]
    [test-helpers.integration :as h]
    [utils.re-frame :as rf]))

(deftest edit-profile-name-test
  (h/rf-test-async
   (fn []
     (h/log-headline ::edit-profile-name-test)
     (let [new-name "John Doe"]
       (-> (h/with-app-initialized)
           (.then h/with-account)
           (.then #(rf/dispatch [:profile/edit-name new-name]))
           (.then #(h/wait-for [:navigate-back :toasts/upsert]))
           (.then (fn []
                    (let [profile      (rf/sub [:profile/profile])
                          display-name (profile.utils/displayed-name profile)]
                      (is (= new-name display-name)))
                    (h/logout)))
           (.then #(h/wait-for [::logout/logout-method])))))))

(deftest edit-profile-picture-test
  (h/rf-test-async
   (fn []
     (h/log-headline ::edit-profile-picture-test)
     (let [mock-image    "resources/images/mock2/monkey.png"
           absolute-path (.resolve utils.test/path mock-image)]
       (-> (h/with-app-initialized)
           (.then h/with-account)
           (.then #(rf/dispatch [:profile/edit-picture absolute-path 80 80]))
           (.then #(h/wait-for [:profile/update-local-picture :toasts/upsert]))
           (.then (fn []
                    (let [profile (rf/sub [:profile/profile])]
                      (is (not (nil? (:images profile)))))
                    (h/logout)))
           (.then #(h/wait-for [::logout/logout-method])))))))

(deftest delete-profile-picture-test
  (h/rf-test-async
   (fn []
     (h/log-headline ::delete-profile-picture-test)
     (-> (h/with-app-initialized)
         (.then h/with-account)
         (.then #(rf/dispatch [:profile/delete-picture]))
         (.then #(h/wait-for [:profile/update-local-picture :toasts/upsert]))
         (.then (fn []
                  (let [profile (rf/sub [:profile/profile])]
                    (is (nil? (:image profile))))
                  (h/logout)))
         (.then #(h/wait-for [::logout/logout-method]))))))

(deftest edit-profile-bio-test
  (h/rf-test-async
   (fn []
     (h/log-headline ::edit-profile-bio-test)
     (let [new-bio "New bio text"]
       (-> (h/with-app-initialized)
           (.then h/with-account)
           (.then #(rf/dispatch [:profile/edit-bio new-bio]))
           (.then #(h/wait-for [:navigate-back :toasts/upsert]))
           (.then (fn []
                    (let [profile (rf/sub [:profile/profile])
                          bio     (:bio profile)]
                      (is (= new-bio bio)))
                    (h/logout)))
           (.then #(h/wait-for [::logout/logout-method])))))))
