(ns status-im.contexts.communities.events-test
  (:require
    [cljs.test :refer [deftest is testing]]
    matcher-combinators.test
    [status-im.contexts.communities.events :as events]))

(def community-id "community-id")

(deftest fetch-community-test
  (testing "with community id"
    (testing "update fetching indicator in db"
      (is (match?
           {:db {:communities/fetching-communities {community-id true}}}
           (events/fetch-community {} [{:community-id community-id}]))))
    (testing "call the fetch community rpc method with correct community id"
      (is (match?
           {:json-rpc/call [{:method "wakuext_fetchCommunity"
                             :params [{:CommunityKey    community-id
                                       :TryDatabase     true
                                       :WaitForResponse true}]}]}
           (events/fetch-community {} [{:community-id community-id}])))))
  (testing "with no community id"
    (testing "do nothing"
      (is (match?
           nil
           (events/fetch-community {} [{}]))))))

(deftest community-failed-to-fetch-test
  (testing "given a community id"
    (testing "remove community id from fetching indicator in db"
      (is (match?
           nil
           (get-in (events/community-failed-to-fetch {:db {:communities/fetching-communities
                                                           {community-id true}}}
                                                     [community-id])
                   [:db :communities/fetching-communities community-id]))))))

(deftest community-fetched-test
  (testing "given a community"
    (let [cofx {:db {:communities/fetching-communities {community-id true}}}
          arg  [community-id {:id community-id}]]
      (testing "remove community id from fetching indicator in db"
        (is (match?
             nil
             (get-in (events/community-fetched cofx arg)
                     [:db :communities/fetching-communities community-id]))))
      (testing "dispatch fxs"
        (is (match?
             {:fx [[:dispatch [:communities/handle-community {:id community-id}]]
                   [:dispatch [:chat.ui/spectate-community community-id]]]}
             (events/community-fetched cofx arg))))))
  (testing "given a joined community"
    (let [cofx {:db {:communities/fetching-communities {community-id true}}}
          arg  [community-id {:id community-id :joined true}]]
      (testing "dispatch fxs, do not spectate community"
        (is (match?
             {:fx [[:dispatch [:communities/handle-community {:id community-id}]]
                   [:dispatch [:chat.ui/spectate-community community-id]]]}
             (events/community-fetched cofx arg))))))
  (testing "given a token-gated community"
    (let [cofx {:db {:communities/fetching-communities {community-id true}}}
          arg  [community-id {:id community-id :tokenPermissions [1]}]]
      (testing "dispatch fxs, do not spectate community"
        (is (match?
             {:fx [[:dispatch [:communities/handle-community {:id community-id}]]
                   [:dispatch [:chat.ui/spectate-community community-id]]]}
             (events/community-fetched cofx arg))))))
  (testing "given nil community"
    (testing "do nothing"
      (is (match?
           nil
           (events/community-fetched {} [community-id nil]))))))

(deftest spectate-community-test
  (testing "given a joined community"
    (testing "do nothing"
      (is (match?
           nil
           (events/spectate-community {:db {:communities {community-id {:joined true}}}}
                                      [community-id])))))
  (testing "given a spectated community"
    (testing "do nothing"
      (is (match?
           nil
           (events/spectate-community {:db {:communities {community-id {:spectated true}}}}
                                      [community-id])))))

  (testing "given a community"
    (testing "call spectate community rpc with correct community id"
      (is (match?
           {:json-rpc/call [{:method "wakuext_spectateCommunity"
                             :params [community-id]}]}
           (events/spectate-community {:db {:communities {community-id {}}}} [community-id]))))))

(deftest spectate-community-success-test
  (let [community-id-1 1
        community-id-2 2]
    (testing "given communities"
      (testing "mark first community spectated true"
        (is (match?
             {:db {:communities {community-id-1 {:spectated true}}}}
             (events/spectate-community-success {}
                                                (clj->js [{:communities [{:id community-id-1}
                                                                         {:id community-id-2}]}])))))
      (testing "dispatch fxs for first community"
        ;; Convert the JavaScript object back to a Clojure map because `match?` does not work well
        ;; with JavaScript objects.
        (is (match?
             {:fx [[:dispatch [:communities/handle-community {:id community-id-1}]]]}
             (let [result (events/spectate-community-success {}
                                                             (clj->js [{:communities
                                                                        [{:id community-id-1}
                                                                         {:id community-id-2}]}]))]
               (update-in result [:fx 0 1] #(js->clj % :keywordize-keys true)))))))
    (testing "given empty community"
      (testing "do nothing"
        (is (match?
             nil
             (events/spectate-community-success {}
                                                (clj->js [{:communities []}]))))))
    (testing "given nil community"
      (testing "do nothing"
        (is (match?
             nil
             (events/spectate-community-success {}
                                                (clj->js []))))))))


(deftest get-revealed-accounts-test
  (let [community {:id community-id}]
    (testing "given a unjoined community"
      (is (match?
           nil
           (events/get-revealed-accounts {:db {:communities {community-id community}}} [community-id]))))

    (testing "given a already :fetching-revealed-accounts community"
      (is (match?
           nil
           (events/get-revealed-accounts
            {:db {:communities {community-id (assoc community :fetching-revealed-accounts true)}}}
            [community-id]))))

    (testing "given joined community"
      (let [community (assoc community :joined true)
            db        {:communities     {community-id community}
                       :profile/profile {:public-key "profile-public-key"}}
            effects   (events/get-revealed-accounts {:db db} [community-id])]
        (is (match? (assoc-in db [:communities community-id :fetching-revealed-accounts] true)
                    (:db effects)))
        (is (match? {:method "wakuext_latestRequestToJoinForCommunity"
                     :params [community-id]}
                    (-> effects :json-rpc/call first (select-keys [:method :params]))))))))

(deftest handle-community-test
  (let [community #js {:id community-id :clock 2}]
    (testing "given a unjoined community"
      (let [effects (events/handle-community {} [community])]
        (is (match? community-id
                    (-> effects :db :communities (get community-id) :id)))
        (is (match?
             [[:dispatch
               [:communities/check-permissions-to-join-community-with-all-addresses community-id]]
              [:dispatch [:communities/check-permissions-to-join-community community-id]]]
             (filter some? (:fx effects))))))

    (testing "given a joined community"
      (let [community #js {:id community-id :clock 2 :joined true}
            effects   (events/handle-community {} [community])]
        (is (match?
             [[:dispatch
               [:communities/check-permissions-to-join-community-with-all-addresses community-id]]
              [:dispatch [:communities/check-permissions-to-join-community community-id]]]
             (filter some? (:fx effects))))))

    (testing "given a community with token-permissions-check"
      (let [community #js
                       {:id community-id :clock 2 :token-permissions-check :fake-token-permissions-check}
            effects   (events/handle-community {} [community])]
        (is (match?
             [[:dispatch
               [:communities/check-permissions-to-join-community-with-all-addresses community-id]]]
             (filter some? (:fx effects))))))

    (testing "given a community with lower clock"
      (let [effects (events/handle-community {:db {:communities {community-id {:clock 3}}}} [community])]
        (is (nil? effects))))))
