(ns status-im.subs.community.account-selection-test
  (:require
    [cljs.test :refer [is]]
    matcher-combinators.test
    [re-frame.db :as rf-db]
    status-im.subs.communities
    [test-helpers.unit :as h]
    [utils.re-frame :as rf]))

(def community-id "0x1")

(h/deftest-sub :communities/for-context-tag
  [sub-name]
  (reset! rf-db/app-db
    {:communities
     {community-id {:name   "foo"
                    :images {:thumbnail {:uri "data:image/png;"}}
                    :color  :orange}}})

  (is (match? {:name  "foo"
               :logo  "data:image/png;"
               :color :orange}
              (rf/sub [sub-name community-id]))))

(h/deftest-sub :communities/airdrop-account
  [sub-name]
  (let [airdrop-account {:address "0xA" :position 1}]
    (reset! rf-db/app-db
      {:communities/all-airdrop-addresses {community-id "0xA"}
       :wallet                            {:accounts {"0xB" {:address "0xB" :position 0}
                                                      "0xA" airdrop-account}}})

    (is (match? airdrop-account (rf/sub [sub-name community-id])))))

(h/deftest-sub :communities/accounts-to-reveal
  [sub-name]
  (reset! rf-db/app-db
    {:communities/all-addresses-to-reveal {community-id #{"0xC" "0xB"}}
     :wallet                              {:accounts {"0xB" {:address "0xB" :position 0}
                                                      "0xA" {:address "0xA" :position 1}
                                                      "0xC" {:address "0xC" :position 2}}}})

  (is (match? [{:address "0xB" :position 0}
               {:address "0xC" :position 2}]
              (rf/sub [sub-name community-id]))))
