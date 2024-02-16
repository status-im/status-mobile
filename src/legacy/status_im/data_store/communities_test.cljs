(ns legacy.status-im.data-store.communities-test
  (:require
    [cljs.test :refer [deftest is]]
    [legacy.status-im.data-store.communities :as sut]))

(def permissions
  {"community-id-chat-1"
   {:viewOnlyPermissions    {:satisfied   false
                             :permissions {:token-permission-id-01 {:criteria [false]}}}
    :viewAndPostPermissions {:satisfied true :permissions {}}}
   "community-id-chat-2"
   {:viewOnlyPermissions    {:satisfied true :permissions {}}
    :viewAndPostPermissions {:satisfied true :permissions {}}}})

(deftest rpc->channel-permissions-test
  (is (= {"community-id-chat-1"
          {:view-only     {:satisfied?  false
                           :permissions {:token-permission-id-01 {:criteria [false]}}}
           :view-and-post {:satisfied? true :permissions {}}}
          "community-id-chat-2"
          {:view-only     {:satisfied? true :permissions {}}
           :view-and-post {:satisfied? true :permissions {}}}}
         (sut/rpc->channel-permissions permissions))))
