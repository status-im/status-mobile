(ns legacy.status-im.data-store.communities
  (:require
    [clojure.set :as set]
    [status-im.constants :as constants]
    [utils.transforms :as transforms]))

(defn <-request-to-join-community-rpc
  [r]
  (set/rename-keys r
                   {:communityId :community-id
                    :publicKey   :public-key
                    :chatId      :chat-id}))

(defn <-requests-to-join-community-rpc
  [requests key-fn]
  (reduce #(assoc %1 (key-fn %2) (<-request-to-join-community-rpc %2)) {} requests))

(defn <-chats-rpc
  "This transformation from RPC is optimized differently because there can be
  thousands of members in all chats and we don't want to transform them from JS
  to CLJS because they will only be used to list community members or community
  chat members."
  [chats-js]
  (let [chat-key-fn (fn [k]
                      (case k
                        "tokenGated"              :token-gated?
                        "canPost"                 :can-post?
                        "can-view"                :can-view?
                        "hideIfPermissionsNotMet" :hide-if-permissions-not-met?
                        (keyword k)))
        chat-val-fn (fn [k v]
                      (if (= "members" k)
                        v
                        (transforms/js->clj v)))]
    (transforms/<-js-map
     chats-js
     {:val-fn (fn [_ v]
                (transforms/<-js-map v {:key-fn chat-key-fn :val-fn chat-val-fn}))})))

(defn <-categories-rpc
  [categ]
  (reduce-kv #(assoc %1 (name %2) %3) {} categ))

(defn role-permission?
  [token-permission]
  (contains? constants/community-role-permissions (:type token-permission)))

(defn membership-permission?
  [token-permission]
  (= (:type token-permission) constants/community-token-permission-become-member))

(defn- rename-community-key
  [k]
  (case k
    "canRequestAccess"            :can-request-access?
    "canManageUsers"              :can-manage-users?
    "canDeleteMessageForEveryone" :can-delete-message-for-everyone?
    ;; This flag is misleading based on its name alone
    ;; because it should not be used to decide if the user
    ;; is *allowed* to join. Allowance is based on token
    ;; permissions. Still, the flag can be used to know
    ;; whether or not the user will have to wait until an
    ;; admin approves a join request.
    "canJoin"                     :can-join?
    "requestedToJoinAt"           :requested-to-join-at
    "isMember"                    :is-member?
    "outroMessage"                :outro-message
    "adminSettings"               :admin-settings
    "tokenPermissions"            :token-permissions
    "communityTokensMetadata"     :tokens-metadata
    "introMessage"                :intro-message
    "muteTill"                    :muted-till
    "lastOpenedAt"                :last-opened-at
    "joinedAt"                    :joined-at
    (keyword k)))

(defn <-rpc
  [c-js]
  (let [community (transforms/<-js-map
                   c-js
                   {:key-fn rename-community-key
                    :val-fn (fn [k v]
                              (case k
                                "members" v
                                "chats"   (<-chats-rpc v)
                                (transforms/js->clj v)))})]
    (-> community
        (update :admin-settings
                set/rename-keys
                {:pinMessageAllMembersEnabled :pin-message-all-members-enabled?})
        (update :token-permissions seq)
        (update :categories <-categories-rpc)
        (assoc :role-permissions?
               (->> community
                    :token-permissions
                    vals
                    (some role-permission?)))
        (assoc :membership-permissions?
               (->> community
                    :token-permissions
                    vals
                    (some membership-permission?)))
        (assoc :token-images
               (reduce (fn [acc {sym :symbol image :image}]
                         (assoc acc sym image))
                       {}
                       (:tokens-metadata community))))))
