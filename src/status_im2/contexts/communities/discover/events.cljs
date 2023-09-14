(ns status-im2.contexts.communities.discover.events
  (:require [taoensso.timbre :as log]
            [utils.re-frame :as rf]))

(def commmunity-keys-renamed
  {:requestedAccessAt           :requested-access-at
   :fileSize                    :file-size
   :communityTokensMetadata     :community-tokens-metadata
   :activeMembersCount          :active-members-count
   :unknownCommunities          :unknown-communities
   :canRequestAccess            :can-request-access?
   :adminSettings               :admin-settings
   :canManageUsers              :can-manage-users?
   :categoryID                  :category-id
   :canPost                     :can-post?
   :isControlNode               :is-control-node?
   :pinMessageAllMembersEnabled :pin-message-all-members-enabled
   :isMember                    :is-member?
   :canDeleteMessageForEveryone :can-delete-message-for-everyone?
   :tokenPermissions            :token-permissions
   :muteTill                    :mute-till
   :contractCommunities         :contract-communities
   :banList                     :ban-list
   :keyUid                      :key-uid
   :memberRole                  :member-role
   :introMessage                :intro-message
   :contractFeaturedCommunities :contract-featured-communities
   :canJoin                     :can-join?
   :outroMessage                :outro-message
   :resizeTarget                :resize-target})

(defn rename-contract-community-key
  [k]
  (let [s                  (name k)
        starts-with-digit? (re-matches #"^\d.*" s)
        existing-rename    (k commmunity-keys-renamed)]
    (cond starts-with-digit? s
          existing-rename    existing-rename
          :else              (keyword s))))

(defn rename-contract-community-keys
  [m]
  (reduce (fn [acc [k v]]
            (let [new-key (if (keyword? k) (rename-contract-community-key k) k)]
              (cond
                (map? v) (assoc acc new-key (rename-contract-community-keys v))
                :else    (assoc acc new-key v))))
          {}
          m))

(rf/defn handle-contract-communities
  {:events [:fetched-contract-communities]}
  [{:keys [db]} contract-communities]
  (let [cc       (rename-contract-community-keys contract-communities)
        featured (:contract-featured-communities cc)
        other    (remove (set featured) (:contract-communities cc))]
    {:db (assoc db
                :contract-communities
                {:featured (select-keys (:communities cc) featured)
                 :other    (select-keys (:communities cc) other)})}))

(rf/defn fetch-contract-communities
  {:events [:fetch-contract-communities]}
  [_]
  {:json-rpc/call [{:method     "wakuext_curatedCommunities"
                    :params     []
                    :on-success #(rf/dispatch [:fetched-contract-communities %])
                    :on-error   #(log/error "failed to fetch contract communities" %)}]})
