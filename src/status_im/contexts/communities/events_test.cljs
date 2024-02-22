(ns status-im.contexts.communities.events-test
  (:require [cljs.test :refer [deftest is testing]]
            [legacy.status-im.mailserver.core :as mailserver]
            matcher-combinators.test
            [status-im.constants :as constants]
            [status-im.contexts.chat.messenger.messages.link-preview.events :as link-preview.events]
            [status-im.contexts.communities.events :as events]))

(def community-id "community-id")

(deftest initialize-permission-addresses-test
  (let [initial-db  {:db {:wallet {:accounts {"0x1" {:address  "0x1"
                                                     :position 0}
                                              "0x2" {:address  "0x2"
                                                     :position 1}}}}}
        expected-db {:db (update-in (:db initial-db)
                                    [:communities community-id]
                                    assoc
                                    :share-all-addresses?          true
                                    :previous-share-all-addresses? true
                                    :previous-permission-addresses #{"0x1" "0x2"}
                                    :selected-permission-addresses #{"0x1" "0x2"}
                                    :airdrop-address               "0x1")}]
    (is (match? expected-db (events/initialize-permission-addresses initial-db [community-id])))))

(deftest toggle-selected-permission-address-test
  (let [initial-db {:db {:communities {community-id {:selected-permission-addresses #{"0x1" "0x2"}}}}}]
    (is (match? {:db {:communities {community-id {:selected-permission-addresses #{"0x1"}}}}}
                (events/toggle-selected-permission-address initial-db ["0x2" community-id])))
    (is (match? {:db {:communities {community-id {:selected-permission-addresses #{"0x1" "0x2" "0x3"}}}}}
                (events/toggle-selected-permission-address initial-db ["0x3" community-id])))))

(deftest update-previous-permission-addresses-test
  (let [wallet {:accounts {"0x1" {:address  "0x1"
                                  :position 0}
                           "0x2" {:address  "0x2"
                                  :position 1}
                           "0x3" {:address  "0x3"
                                  :position 2}}}]
    (let [initial-db  {:db {:wallet      wallet
                            :communities {community-id {:previous-permission-addresses #{"0x1" "0x2"}
                                                        :selected-permission-addresses #{"0x1" "0x2"
                                                                                         "0x3"}
                                                        :share-all-addresses?          true
                                                        :previous-share-all-addresses? false
                                                        :airdrop-address               "0x1"}}}}
          expected-db {:db {:wallet      wallet
                            :communities {community-id {:previous-permission-addresses #{"0x1" "0x2"
                                                                                         "0x3"}
                                                        :selected-permission-addresses #{"0x1" "0x2"
                                                                                         "0x3"}
                                                        :share-all-addresses?          true
                                                        :previous-share-all-addresses? true
                                                        :airdrop-address               "0x1"}}}}]
      (is
       (match? expected-db
               (events/update-previous-permission-addresses initial-db [community-id]))))
    (let [initial-db  {:db {:wallet      wallet
                            :communities {community-id {:previous-permission-addresses #{"0x1" "0x2"}
                                                        :selected-permission-addresses #{"0x2" "0x3"}
                                                        :airdrop-address               "0x1"}}}}
          expected-db {:db {:wallet      wallet
                            :communities {community-id {:previous-permission-addresses #{"0x2" "0x3"}
                                                        :selected-permission-addresses #{"0x2" "0x3"}
                                                        :airdrop-address               "0x2"}}}}]
      (is
       (match? expected-db
               (events/update-previous-permission-addresses initial-db [community-id]))))))

(deftest toggle-share-all-addresses-test
  (let [initial-db  {:db {:wallet      {:accounts {"0x1" {:address  "0x1"
                                                          :position 0}
                                                   "0x2" {:address  "0x2"
                                                          :position 1}}}
                          :communities {community-id {:share-all-addresses?          true
                                                      :previous-share-all-addresses? true
                                                      :previous-permission-addresses #{"0x1" "0x2"}
                                                      :selected-permission-addresses #{"0x1" "0x2"}
                                                      :airdrop-address               "0x1"}}}}
        expected-db (update-in initial-db
                               [:db :communities community-id]
                               assoc
                               :previous-share-all-addresses? true
                               :share-all-addresses?
                               false)]
    (is (match? expected-db (events/toggle-share-all-addresses initial-db [community-id]))))
  (let [initial-db  {:db {:wallet      {:accounts {"0x1" {:address  "0x1"
                                                          :position 0}
                                                   "0x2" {:address  "0x2"
                                                          :position 1}}}
                          :communities {community-id {:share-all-addresses?          false
                                                      :previous-share-all-addresses? false
                                                      :previous-permission-addresses #{"0x1"}
                                                      :selected-permission-addresses #{"0x1"}
                                                      :airdrop-address               "0x1"}}}}
        expected-db (update-in initial-db
                               [:db :communities community-id]
                               assoc
                               :selected-permission-addresses #{"0x1" "0x2"}
                               :previous-share-all-addresses? false
                               :share-all-addresses?          true)]
    (is (match? expected-db (events/toggle-share-all-addresses initial-db [community-id])))))

(deftest fetch-community
  (testing "with community id"
    (testing "update fetching indicator in db"
      (is (match?
           {:db {:communities/fetching-community {community-id true}}}
           (events/fetch-community {} [community-id]))))
    (testing "call the fetch community rpc method with correct community id"
      (is (match?
           {:json-rpc/call [{:method "wakuext_fetchCommunity"
                             :params [{:CommunityKey    community-id
                                       :TryDatabase     true
                                       :WaitForResponse true}]}]}
           (events/fetch-community {} [community-id])))))
  (testing "with no community id"
    (testing "do nothing"
      (is (match?
           nil
           (events/fetch-community {} []))))))

(deftest community-failed-to-fetch
  (testing "given a community id"
    (testing "remove community id from fetching indicator in db"
      (is (match?
           nil
           (get-in (events/community-failed-to-fetch {:db {:communities/fetching-community
                                                           {community-id true}}}
                                                     [community-id])
                   [:db :communities/fetching-community community-id]))))))

(deftest community-fetched
  (with-redefs [link-preview.events/community-link (fn [id] (str "community-link+" id))]
    (testing "given a community"
      (let [cofx {:db {:communities/fetching-community {community-id true}}}
            arg  [community-id {:id community-id}]]
        (testing "remove community id from fetching indicator in db"
          (is (match?
               nil
               (get-in (events/community-fetched cofx arg)
                       [:db :communities/fetching-community community-id]))))
        (testing "dispatch fxs"
          (is (match?
               {:fx [[:dispatch [:communities/handle-community {:id community-id}]]
                     [:dispatch
                      [:chat.ui/cache-link-preview-data "community-link+community-id"
                       {:id community-id}]]]}
               (events/community-fetched cofx arg))))))
    (testing "given a joined community"
      (let [cofx {:db {:communities/fetching-community {community-id true}}}
            arg  [community-id {:id community-id :joined true}]]
        (testing "dispatch fxs, do not spectate community"
          (is (match?
               {:fx [[:dispatch [:communities/handle-community {:id community-id}]]
                     [:dispatch
                      [:chat.ui/cache-link-preview-data "community-link+community-id"
                       {:id community-id}]]]}
               (events/community-fetched cofx arg))))))
    (testing "given a token-gated community"
      (let [cofx {:db {:communities/fetching-community {community-id true}}}
            arg  [community-id {:id community-id :tokenPermissions [1]}]]
        (testing "dispatch fxs, do not spectate community"
          (is (match?
               {:fx [[:dispatch [:communities/handle-community {:id community-id}]]
                     [:dispatch
                      [:chat.ui/cache-link-preview-data "community-link+community-id"
                       {:id community-id}]]]}
               (events/community-fetched cofx arg))))))
    (testing "given nil community"
      (testing "do nothing"
        (is (match?
             nil
             (events/community-fetched {} [community-id nil])))))))

(deftest spectate-community
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
  (testing "given a spectating community"
    (testing "do nothing"
      (is (match?
           nil
           (events/spectate-community {:db {:communities {community-id {:spectating true}}}}
                                      [community-id])))))
  (testing "given a community"
    (testing "mark community spectating"
      (is (match?
           {:db {:communities {community-id {:spectating true}}}}
           (events/spectate-community {:db {:communities {community-id {}}}} [community-id]))))
    (testing "call spectate community rpc with correct community id"
      (is (match?
           {:json-rpc/call [{:method "wakuext_spectateCommunity"
                             :params [community-id]}]}
           (events/spectate-community {:db {:communities {community-id {}}}} [community-id]))))))

(deftest spectate-community-failed
  (testing "mark community spectating false"
    (is (match?
         {:db {:communities {community-id {:spectating false}}}}
         (events/spectate-community-failed {} [community-id])))))

(deftest spectate-community-success
  (testing "given communities"
    (testing "mark first community spectating false"
      (is (match?
           {:db {:communities {community-id {:spectating false}}}}
           (events/spectate-community-success {} [{:communities [{:id community-id}]}]))))
    (testing "mark first community spectated true"
      (is (match?
           {:db {:communities {community-id {:spectated true}}}}
           (events/spectate-community-success {} [{:communities [{:id community-id}]}]))))
    (testing "dispatch fxs for first community"
      (is (match?
           {:fx [[:dispatch [:communities/handle-community {:id community-id}]]
                 [:dispatch [::mailserver/request-messages]]]}
           (events/spectate-community-success {} [{:communities [{:id community-id}]}])))))
  (testing "given empty community"
    (testing "do nothing"
      (is (match?
           nil
           (events/spectate-community-success {} [{:communities []}])))))
  (testing "given nil community"
    (testing "do nothing"
      (is (match?
           nil
           (events/spectate-community-success {} []))))))

(deftest get-revealed-accounts
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
        (is (match? {:method "wakuext_getRevealedAccounts"
                     :params [community-id "profile-public-key"]}
                    (-> effects :json-rpc/call first (select-keys [:method :params]))))))))

(deftest handle-community
  (let [community {:id community-id}]
    (testing "given a unjoined community"
      (let [effects (events/handle-community {} [community])]
        (is (match? community-id
                    (-> effects :db :communities (get community-id) :id)))
        (is (match?
             [[:dispatch [:communities/initialize-permission-addresses community-id]]
              [:dispatch [:chat.ui/spectate-community community-id]]
              [:dispatch [:communities/check-permissions-to-join-community community-id]]]
             (filter some? (:fx effects))))))
    (testing "given a joined community"
      (let [community (assoc community :joined true)
            effects   (events/handle-community {} [community])]
        (is (match?
             [[:dispatch [:communities/initialize-permission-addresses community-id]]
              [:dispatch [:communities/check-permissions-to-join-community community-id]]
              [:dispatch [:communities/get-revealed-accounts community-id]]]
             (filter some? (:fx effects))))))
    (testing "given a community with token-permissions-check"
      (let [community (assoc community :token-permissions-check :fake-token-permissions-check)
            effects   (events/handle-community {} [community])]
        (is (match?
             [[:dispatch [:communities/initialize-permission-addresses community-id]]
              [:dispatch [:chat.ui/spectate-community community-id]]]
             (filter some? (:fx effects))))))
    (testing "given a community with view channel permission"
      (let [community (assoc community
                             :token-permissions
                             [["perm-id" {:type constants/community-token-permission-can-view-channel}]])
            effects   (events/handle-community {} [community])]
        (is (match?
             [[:dispatch [:communities/initialize-permission-addresses community-id]]
              [:dispatch [:chat.ui/spectate-community community-id]]
              [:dispatch [:communities/check-permissions-to-join-community community-id]]
              [:dispatch
               [:communities/check-all-community-channels-permissions community-id]]]
             (filter some? (:fx effects))))))

    (testing "given a community with post in channel permission"
      (let [community (assoc community
                             :token-permissions
                             [["perm-id"
                               {:type constants/community-token-permission-can-view-and-post-channel}]])
            effects   (events/handle-community {} [community])]
        (is (match?
             [[:dispatch [:communities/initialize-permission-addresses community-id]]
              [:dispatch [:chat.ui/spectate-community community-id]]
              [:dispatch [:communities/check-permissions-to-join-community community-id]]
              [:dispatch
               [:communities/check-all-community-channels-permissions community-id]]]
             (filter some? (:fx effects))))))))
