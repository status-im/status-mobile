(ns status-im2.contexts.communities.discover.events-test
  (:require [cljs.test :refer-macros [deftest are]]
            [status-im2.contexts.communities.discover.events :as events]))

(deftest rename-contract-community-key-test
  (are [i e] (= (events/rename-contract-community-key i) e)
   :requestedAccessAt           :requested-access-at
   :canDeleteMessageForEveryone :can-delete-message-for-everyone?
   :name                        :name
   :0x025d27e58                 "0x025d27e58"
   :093b4684-92f0               "093b4684-92f0"
   :3f9e77b8-97c7               "3f9e77b8-97c7"))

(deftest rename-contract-community-keys-test
  (are [i e] (= (events/rename-contract-community-keys i) e)

   {:contractCommunities
    ["0x032aa2439b14eb2caaf724223951da89de1530fbfa5d5a639b93b82cd5341713fd"
     "0x025d27e58f1bece7d9675e6fe907da6e3b5007f06a327dd20db04d68851b9c153d"
     "0x03769999b26cfd88788c882b56232fa5f58735795bdd463820d127b9a23d4415d4"]
    :contractFeaturedCommunities
    ["0x03769999b26cfd88788c882b56232fa5f58735795bdd463820d127b9a23d4415d4"]
    :communities
    {:0x025d27e58f1bece7d9675e6fe907da6e3b5007f06a327dd20db04d68851b9c153d
     {:name                        "foo"
      :tokenPermissions            nil
      :isMember                    false
      :canDeleteMessageForEveryone false}
     :0x032aa2439b14eb2caaf724223951da89de1530fbfa5d5a639b93b82cd5341713fd
     {:name "bar"
      :adminSettings {:pinMessageAllMembersEnabled false}
      :chats
      {:8aa21892-a768-488a-8655-7ed31f527642
       {:name       "web"
        :categoryID "d87621f7-315a-4579-a800-fde42d86ddf2"
        :canPost    false}
       :6efc1767-22e5-46c1-9865-87206e04c9f9
       {:name       "mobile-ui"
        :categoryID "7445461a-bd10-4c54-86fe-70403913faf4"
        :canPost    false}}}
     :0x03769999b26cfd88788c882b56232fa5f58735795bdd463820d127b9a23d4415d4
     {:name "baz"
      :categories
      {:18444776-8720-4955-b200-a55758889f43 {:name "bar" :position 0}
       :093b4684-92f0-42dd-977c-05affc451ec8 {:name "foo" :position 1}}
      :members
      {:0x04a25e0a58ab97c6f93298ad87ab73aae5995365088a1ba150633ea25ff0b80cc1ddba18b122d117e477963cec1962219362a9a77226a6bf9a15d8d02c35fe8c9b
       {}
       :0x0490d2bb47388504e4b615052566e5830662bf202eb179251e9118587ce628c6c76e1f4550f9cd52058cf9dbdb5b788eea10b7c765cd7565675daa5f822acab8f4
       {}}}}
    :unknownCommunities nil}

    {:contract-communities
     ["0x032aa2439b14eb2caaf724223951da89de1530fbfa5d5a639b93b82cd5341713fd"
      "0x025d27e58f1bece7d9675e6fe907da6e3b5007f06a327dd20db04d68851b9c153d"
      "0x03769999b26cfd88788c882b56232fa5f58735795bdd463820d127b9a23d4415d4"]
     :contract-featured-communities
     ["0x03769999b26cfd88788c882b56232fa5f58735795bdd463820d127b9a23d4415d4"]
     :communities
     {"0x025d27e58f1bece7d9675e6fe907da6e3b5007f06a327dd20db04d68851b9c153d"
      {:name                             "foo"
       :token-permissions                nil
       :is-member?                       false
       :can-delete-message-for-everyone? false}
      "0x032aa2439b14eb2caaf724223951da89de1530fbfa5d5a639b93b82cd5341713fd"
      {:name "bar"
       :admin-settings {:pin-message-all-members-enabled false}
       :chats
       {"8aa21892-a768-488a-8655-7ed31f527642"
        {:name        "web"
         :category-id "d87621f7-315a-4579-a800-fde42d86ddf2"
         :can-post?   false}
        "6efc1767-22e5-46c1-9865-87206e04c9f9"
        {:name        "mobile-ui"
         :category-id "7445461a-bd10-4c54-86fe-70403913faf4"
         :can-post?   false}}}
      "0x03769999b26cfd88788c882b56232fa5f58735795bdd463820d127b9a23d4415d4"
      {:name "baz"
       :categories
       {"18444776-8720-4955-b200-a55758889f43" {:name "bar" :position 0}
        "093b4684-92f0-42dd-977c-05affc451ec8" {:name "foo" :position 1}}
       :members
       {"0x04a25e0a58ab97c6f93298ad87ab73aae5995365088a1ba150633ea25ff0b80cc1ddba18b122d117e477963cec1962219362a9a77226a6bf9a15d8d02c35fe8c9b"
        {}
        "0x0490d2bb47388504e4b615052566e5830662bf202eb179251e9118587ce628c6c76e1f4550f9cd52058cf9dbdb5b788eea10b7c765cd7565675daa5f822acab8f4"
        {}}}}
     :unknown-communities nil}))
