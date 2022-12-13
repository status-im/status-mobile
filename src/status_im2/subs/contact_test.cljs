(ns status-im2.subs.contact-test
  (:require [status-im.test-helpers :as h]
            [re-frame.db :as rf-db]
            [utils.re-frame :as rf]
            status-im2.subs.root
            [cljs.test :refer [is testing]]))


(h/deftest-sub :contacts/add-members-sections
               [sub-name]
               (testing "return group members concatenated with contacts, uniquely"
                        (swap! rf-db/app-db assoc
                               :contacts/active ({:public-key "0x1" :alias "Panda"})
                               :contacts/current-chat-contacts ({:public-key "0x2" :alias "Squirrel"}))
                        (is (= ({:title "P" :data [{:public-key "0x1" :alias "Panda"}]} {:title "S" :data [{:public-key "0x2" :alias "Squirrel"}]}) (rf/sub [sub-name])))))
