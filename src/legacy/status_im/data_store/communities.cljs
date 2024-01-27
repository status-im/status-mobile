(ns legacy.status-im.data-store.communities
  (:require
    [clojure.set :as set]
    [clojure.walk :as walk]
    [status-im.constants :as constants]))

(defn rpc->channel-permissions
  [rpc-channels-permissions]
  (update-vals rpc-channels-permissions
               (fn [{:keys [viewAndPostPermissions viewOnlyPermissions]}]
                 {:view-only     (set/rename-keys viewOnlyPermissions {:satisfied :satisfied?})
                  :view-and-post (set/rename-keys viewAndPostPermissions {:satisfied :satisfied?})})))

(defn <-revealed-accounts-rpc
  [accounts]
  (mapv
   #(set/rename-keys % {:isAirdropAddress :airdrop-address?})
   (js->clj accounts :keywordize-keys true)))

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
  [chats]
  (reduce-kv (fn [acc k v]
               (assoc acc
                      (name k)
                      (-> v
                          (assoc :can-post? (:canPost v))
                          (dissoc :canPost)
                          (update :members walk/stringify-keys))))
             {}
             chats))

(defn <-categories-rpc
  [categ]
  (reduce-kv #(assoc %1 (name %2) %3) {} categ))

(defn <-rpc
  [c]
  (-> c
      (set/rename-keys {:canRequestAccess            :can-request-access?
                        :canManageUsers              :can-manage-users?
                        :canDeleteMessageForEveryone :can-delete-message-for-everyone?
                        :canJoin                     :can-join?
                        :requestedToJoinAt           :requested-to-join-at
                        :isMember                    :is-member?
                        :adminSettings               :admin-settings
                        :tokenPermissions            :token-permissions
                        :communityTokensMetadata     :tokens-metadata
                        :introMessage                :intro-message
                        :muteTill                    :muted-till})
      (update :admin-settings
              set/rename-keys
              {:pinMessageAllMembersEnabled :pin-message-all-members-enabled?})
      (update :members walk/stringify-keys)
      (update :chats <-chats-rpc)
      (update :token-permissions seq)
      (update :categories <-categories-rpc)
      (assoc :membership-permissions?
             (some #(= (:type %) constants/community-token-permission-become-member)
                   (vals (:tokenPermissions c))))
      (assoc :token-images
             (reduce (fn [acc {sym :symbol image :image}]
                       (assoc acc sym image))
                     {}
                     (:communityTokensMetadata c)))))
