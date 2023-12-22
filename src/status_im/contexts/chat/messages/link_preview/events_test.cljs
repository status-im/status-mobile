(ns status-im.contexts.chat.messages.link-preview.events-test
  (:require [cljs.test :as t]
            [legacy.status-im.mailserver.core :as mailserver]
            matcher-combinators.test
            [status-im.contexts.chat.messages.link-preview.events :as sut]))

(t/deftest fetch-community
  (t/testing "with community id"
    (t/testing "update resolving indicator in db"
      (t/is (match?
             {:db {:communities/resolve-community-info {"community-id" true}}}
             (sut/fetch-community {} ["community-id"]))))
    (t/testing "call the fetch community rpc method with correct community id"
      (t/is (match?
             {:json-rpc/call [{:method "wakuext_fetchCommunity"
                               :params [{:CommunityKey    "community-id"
                                         :TryDatabase     true
                                         :WaitForResponse true}]}]}
             (sut/fetch-community {} ["community-id"])))))
  (t/testing "with nil community id"
    (t/testing "do nothing"
      (t/is (match?
             nil
             (sut/fetch-community {} nil))))))

(t/deftest community-failed-to-resolve
  (t/testing "given a community id"
    (t/testing "remove community id from resolving indicator in db"
      (t/is (match?
             nil
             (get-in (sut/community-failed-to-resolve {:db {:communities/resolve-community-info
                                                            {"community-id" true}}}
                                                      ["community-id"])
                     [:db :communities/resolve-community-info "community-id"]))))))

(t/deftest community-resolved
  (with-redefs [sut/community-link (fn [id] (str "community-link+" id))]
    (t/testing "given a community"
      (let [cofx {:db {:communities/resolve-community-info {"community-id" true}}}
            arg  ["community-id" {:id "community-id"}]]
        (t/testing "remove community id from resolving indicator in db"
          (t/is (match?
                 nil
                 (get-in (sut/community-resolved cofx arg)
                         [:db :communities/resolve-community-info "community-id"]))))
        (t/testing "dispatch fxs"
          (t/is (match?
                 {:fx [[:dispatch [:communities/handle-community {:id "community-id"}]]
                       [:dispatch
                        [:chat.ui/cache-link-preview-data "community-link+community-id"
                         {:id "community-id"}]]]}
                 (sut/community-resolved cofx arg))))))
    (t/testing "given a joined community"
      (let [cofx {:db {:communities/resolve-community-info {"community-id" true}}}
            arg  ["community-id" {:id "community-id" :joined true}]]
        (t/testing "dispatch fxs, do not spectate community"
          (t/is (match?
                 {:fx [[:dispatch [:communities/handle-community {:id "community-id"}]]
                       [:dispatch
                        [:chat.ui/cache-link-preview-data "community-link+community-id"
                         {:id "community-id"}]]]}
                 (sut/community-resolved cofx arg))))))
    (t/testing "given a token-gated community"
      (let [cofx {:db {:communities/resolve-community-info {"community-id" true}}}
            arg  ["community-id" {:id "community-id" :tokenPermissions [1]}]]
        (t/testing "dispatch fxs, do not spectate community"
          (t/is (match?
                 {:fx [[:dispatch [:communities/handle-community {:id "community-id"}]]
                       [:dispatch
                        [:chat.ui/cache-link-preview-data "community-link+community-id"
                         {:id "community-id"}]]]}
                 (sut/community-resolved cofx arg))))))
    (t/testing "given nil community"
      (t/testing "do nothing"
        (t/is (match?
               nil
               (sut/community-resolved {} ["community-id" nil])))))))

(t/deftest spectate-community
  (t/testing "given a joined community"
    (t/testing "do nothing"
      (t/is (match?
             nil
             (sut/spectate-community {:db {:communities {"community-id" {:joined true}}}}
                                     ["community-id"])))))
  (t/testing "given a spectated community"
    (t/testing "do nothing"
      (t/is (match?
             nil
             (sut/spectate-community {:db {:communities {"community-id" {:spectated true}}}}
                                     ["community-id"])))))
  (t/testing "given a spectating community"
    (t/testing "do nothing"
      (t/is (match?
             nil
             (sut/spectate-community {:db {:communities {"community-id" {:spectating true}}}}
                                     ["community-id"])))))
  (t/testing "given a community"
    (t/testing "mark community spectating"
      (t/is (match?
             {:db {:communities {"community-id" {:spectating true}}}}
             (sut/spectate-community {:db {:communities {"community-id" {}}}} ["community-id"]))))
    (t/testing "call spectate community rpc with correct community id"
      (t/is (match?
             {:json-rpc/call [{:method "wakuext_spectateCommunity"
                               :params ["community-id"]}]}
             (sut/spectate-community {:db {:communities {"community-id" {}}}} ["community-id"]))))))

(t/deftest spectate-community-failed
  (t/testing "mark community spectating false"
    (t/is (match?
           {:db {:communities {"community-id" {:spectating false}}}}
           (sut/spectate-community-failed {} ["community-id"])))))

(t/deftest spectate-community-success
  (t/testing "given communities"
    (t/testing "mark first community spectating false"
      (t/is (match?
             {:db {:communities {"community-id" {:spectating false}}}}
             (sut/spectate-community-success {} [{:communities [{:id "community-id"}]}]))))
    (t/testing "mark first community spectated true"
      (t/is (match?
             {:db {:communities {"community-id" {:spectated true}}}}
             (sut/spectate-community-success {} [{:communities [{:id "community-id"}]}]))))
    (t/testing "dispatch fxs for first community"
      (t/is (match?
             {:fx [[:dispatch [:communities/handle-community {:id "community-id"}]]
                   [:dispatch [::mailserver/request-messages]]]}
             (sut/spectate-community-success {} [{:communities [{:id "community-id"}]}])))))
  (t/testing "given empty community"
    (t/testing "do nothing"
      (t/is (match?
             nil
             (sut/spectate-community-success {} [{:communities []}])))))
  (t/testing "given nil community"
    (t/testing "do nothing"
      (t/is (match?
             nil
             (sut/spectate-community-success {} []))))))
