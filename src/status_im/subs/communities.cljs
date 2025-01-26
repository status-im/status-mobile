(ns status-im.subs.communities
  (:require
    [clojure.string :as string]
    [legacy.status-im.data-store.chats :as data-store.chats]
    [legacy.status-im.ui.screens.profile.visibility-status.utils :as visibility-status-utils]
    [re-frame.core :as re-frame]
    [status-im.constants :as constants]
    [status-im.contexts.communities.utils :as utils]
    [status-im.contexts.profile.utils :as profile.utils]
    [status-im.subs.chat.utils :as subs.utils]
    [status-im.subs.contact.utils :as contact.utils]
    [utils.i18n :as i18n]
    [utils.money :as money]))

(re-frame/reg-sub
 :communities/fetching-community
 :<- [:communities/fetching-communities]
 (fn [info [_ id]]
   (get info id)))

;; Do not use this subscription directly in views. There is a significant risk
;; of re-rendering views too frequently because an active community can change
;; for numerous reasons.
(re-frame/reg-sub
 :communities/community
 :<- [:communities]
 (fn [communities [_ id]]
   (get communities id)))

(re-frame/reg-sub :communities/logo
 (fn [[_ community-id]]
   [(re-frame/subscribe [:communities/community community-id])])
 (fn [[community]]
   (get-in community [:images :thumbnail :uri])))

(re-frame/reg-sub :communities/name
 (fn [[_ community-id]]
   [(re-frame/subscribe [:communities/community community-id])])
 (fn [[{:keys [name]}]]
   name))

(re-frame/reg-sub :communities/permissions
 (fn [[_ community-id]]
   [(re-frame/subscribe [:communities/community community-id])])
 (fn [[{:keys [permissions]}]]
   permissions))

(re-frame/reg-sub
 :communities/community-color
 (fn [[_ community-id]]
   [(re-frame/subscribe [:communities/community community-id])])
 (fn [[{:keys [color]}] _]
   color))

(re-frame/reg-sub
 :communities/community-outro-message
 (fn [[_ community-id]]
   [(re-frame/subscribe [:communities/community community-id])])
 (fn [[{:keys [outro-message]}] _]
   outro-message))

(re-frame/reg-sub
 :communities/community-joined
 (fn [[_ community-id]]
   [(re-frame/subscribe [:communities/community community-id])])
 (fn [[{:keys [joined]}] _]
   joined))

(re-frame/reg-sub
 :communities/community-members
 (fn [[_ community-id]]
   [(re-frame/subscribe [:communities/community community-id])])
 (fn [[{:keys [members]}] _]
   (js-keys members)))

(re-frame/reg-sub
 :communities/community-chat-members
 (fn [[_ community-id]]
   [(re-frame/subscribe [:communities/community community-id])])
 (fn [[{:keys [members chats]}] [_ _ chat-id]]
   (let [channel-id (data-store.chats/community-chat-id->channel-id chat-id)]
     (if (get-in chats [channel-id :token-gated?])
       (get-in chats [channel-id :members])
       members))))

(defn- keys->names
  [public-keys profile]
  (reduce (fn [acc contact-identity]
            (assoc acc
                   contact-identity
                   (when (= (:public-key profile) contact-identity)
                     (:primary-name profile)
                     contact-identity)))
          {}
          public-keys))

(defn- sort-members-by-name-old
  [names descending? members]
  (if descending?
    (sort-by #(get names %) #(compare %2 %1) members)
    (sort-by #(get names %) members)))

(defn- sort-members-by-name
  [names members-keys]
  (let [forced-last-key "zzzzzz"
        sort-keyfn      (fn [k]
                          (if-let [[primary-name secondary-name] (get names k)]
                            (or (some-> primary-name
                                        string/lower-case)
                                (some-> secondary-name
                                        string/lower-case))
                            ;; Sort unknown keys at the end.
                            forced-last-key))]
    (sort-by sort-keyfn members-keys)))

;; This implementation is wrong, but since it's only used in a legacy view, we
;; can ignore it for now.
(re-frame/reg-sub :communities/sorted-community-members
 (fn [[_ community-id]]
   [(re-frame/subscribe [:profile/profile])
    (re-frame/subscribe [:communities/community-members community-id])])
 (fn [[profile members] _]
   (let [names (keys->names members profile)]
     (->> members
          (sort-members-by-name-old names false)
          (sort-by #(visibility-status-utils/visibility-status-order (get % 0)))))))

(re-frame/reg-sub :communities/chat-members
 (fn [[_ community-id chat-id]]
   [(re-frame/subscribe [:profile/public-key])
    (re-frame/subscribe [:communities/community-chat-members community-id chat-id])
    (re-frame/subscribe [:visibility-status-updates])
    (re-frame/subscribe [:multiaccount/current-user-visibility-status])])
 (fn [[profile-pub-key members-js visibility-status-updates my-status-update] [_ _ _ visibility-status]]
   (let [members-keys (js-keys members-js)
         online?      (fn [public-key]
                        (let [{visibility-status-type :status-type}
                              (if (or (string/blank? profile-pub-key)
                                      (= profile-pub-key public-key))
                                my-status-update
                                (get visibility-status-updates public-key))]
                          (subs.utils/online? visibility-status-type)))]
     (filter (if (= :online visibility-status)
               online?
               (complement online?))
             members-keys))))

(defn- names-by-key
  [contacts profile public-keys]
  (let [names (reduce (fn [acc k]
                        (if-let [contact (get contacts k)]
                          (assoc acc k (contact.utils/contact-two-names contact profile))
                          acc))
                      {}
                      public-keys)]
    (assoc names
           (:public-key profile)
           [(profile.utils/displayed-name profile) nil])))

;; This is a potentially expensive subscription because we don't control how
;; many members and contacts exist in the app-db. Future improvements include
;; removing members from the payload and paginating them from status-go.
(re-frame/reg-sub :communities/chat-members-sorted
 (fn [[_ community-id chat-id visibility-status]]
   [(re-frame/subscribe [:profile/profile])
    (re-frame/subscribe [:contacts/contacts-raw])
    (re-frame/subscribe [:communities/chat-members community-id chat-id visibility-status])])
 (fn [[profile contacts ^js members-keys]]
   (sort-members-by-name (names-by-key contacts profile members-keys)
                         members-keys)))

(re-frame/reg-sub
 :communities/featured-contract-communities
 :<- [:contract-communities]
 (fn [contract-communities]
   (sort-by :name (vals (:featured contract-communities)))))

(re-frame/reg-sub
 :communities/other-contract-communities
 :<- [:contract-communities]
 (fn [contract-communities]
   (sort-by :name (vals (:other contract-communities)))))

(def memo-communities-stack-items (atom nil))

(defn- group-communities-by-status
  [requests
   {:keys [id]
    :as   community}]
  (cond
    (:joined community)         :joined
    (boolean (get requests id)) :pending
    (:spectated community)      :opened
    :else                       :other))

(re-frame/reg-sub
 :communities/grouped-by-status
 :<- [:view-id]
 :<- [:communities]
 :<- [:communities/my-pending-requests-to-join]
 ;; Return communities splitted by level of user participation. Some communities user already
 ;; joined, to some of them join request sent and others were opened one day and their data remained
 ;; in app-db. Result map has form: {:joined [id1, id2] :pending [id3, id5] :opened [id4]}"
 (fn [[view-id communities requests]]
   (if (or (empty? @memo-communities-stack-items) (= view-id :communities-stack))
     (let [grouped-communities (->> communities
                                    vals
                                    ;; Remove data that can grow fast or is
                                    ;; reliably not needed to list communities.
                                    ;; We could use an allowlist of keys for
                                    ;; optimal performance of this sub, but
                                    ;; that's harder to maintain in case we miss
                                    ;; any key.
                                    (map #(dissoc % :members :chats :token-permissions :tokens-metadata))
                                    (group-by #(group-communities-by-status requests %))
                                    (map (fn [[k v]]
                                           {k (sort-by (fn [{:keys [requested-to-join-at last-opened-at
                                                                    joined-at]}]
                                                         (condp = k
                                                           :joined  joined-at
                                                           :pending requested-to-join-at
                                                           :opened  last-opened-at
                                                           last-opened-at))
                                                       #(compare %2 %1)
                                                       v)}))
                                    (into {}))]
       (reset! memo-communities-stack-items grouped-communities)
       grouped-communities)
     @memo-communities-stack-items)))

(defn community->home-item
  [community counts]
  {:name                  (:name community)
   :muted?                (:muted community)
   :unread-messages?      (pos? (:unviewed-messages-count counts))
   :unread-mentions-count (:unviewed-mentions-count counts)
   :community-icon        (:images community)})

(re-frame/reg-sub
 :communities/home-item
 (fn [[_ community-id]]
   [(re-frame/subscribe [:communities])
    (re-frame/subscribe [:communities/unviewed-counts community-id])])
 (fn [[communities counts] [_ community-identity]]
   (community->home-item
    (get communities community-identity)
    counts)))

(re-frame/reg-sub
 :communities/my-pending-request-to-join
 :<- [:communities/my-pending-requests-to-join]
 (fn [requests [_ community-id]]
   (:id (get requests community-id))))

(re-frame/reg-sub
 :communities/current-community
 :<- [:communities]
 :<- [:chats/current-raw-chat]
 (fn [[communities {:keys [community-id]}]]
   (get communities community-id)))

(defn calculate-unviewed-counts
  [chats]
  (reduce (fn [acc {:keys [unviewed-mentions-count unviewed-messages-count muted]}]
            (if-not muted
              (-> acc
                  (update :unviewed-messages-count + unviewed-messages-count)
                  (update :unviewed-mentions-count + unviewed-mentions-count))
              acc))
          {:unviewed-messages-count 0
           :unviewed-mentions-count 0}
          chats))

(re-frame/reg-sub
 :communities/unviewed-counts
 (fn [[_ community-id]]
   [(re-frame/subscribe [:chats/by-community-id community-id])])
 (fn [[chats]]
   (calculate-unviewed-counts chats)))

(re-frame/reg-sub
 :communities/requests-to-join-for-community
 :<- [:communities/requests-to-join]
 (fn [requests [_ community-id]]
   (->>
     (get requests community-id {})
     vals
     (filter (fn [{:keys [state]}]
               (= state constants/request-to-join-pending-state))))))

(defn- reduce-over-categories
  [community-id
   categories
   collapsed-categories
   full-chats-data]
  (fn [acc
       [_
        {:keys [name
                categoryID
                position
                id
                emoji
                can-view?
                can-post?
                token-gated?
                hide-if-permissions-not-met?]}]]
    (let [category-id       (if (seq categoryID) categoryID constants/empty-category-id)
          {:keys [unviewed-messages-count
                  unviewed-mentions-count
                  muted
                  color]}   (get full-chats-data
                                 (str community-id id))
          acc-with-category (if (get acc category-id)
                              acc
                              (assoc acc
                                     category-id
                                     (assoc
                                      (or (get categories category-id)
                                          {:name (i18n/label :t/none)})
                                      :collapsed? (get collapsed-categories
                                                       category-id)
                                      :chats      [])))
          locked?           (when token-gated?
                              (and (not can-view?)
                                   (not can-post?)))
          categorized-chat  {:name                         name
                             :emoji                        emoji
                             :muted?                       muted
                             :unread-messages?             (pos? unviewed-messages-count)
                             :position                     position
                             :mentions-count               (or unviewed-mentions-count 0)
                             :can-post?                    can-post?
                             ;; NOTE: this is a troolean nil->no permissions, true->no access, false
                             ;; -> has access
                             :locked?                      locked?
                             :hide-if-permissions-not-met? (and hide-if-permissions-not-met? locked?)
                             :id                           id
                             :color                        color}]
      (update-in acc-with-category [category-id :chats] conj categorized-chat))))

(re-frame/reg-sub
 :communities/categorized-channels
 (fn [[_ community-id]]
   [(re-frame/subscribe [:communities/community community-id])
    (re-frame/subscribe [:chats/chats])
    (re-frame/subscribe [:communities/collapsed-categories-for-community community-id])])
 (fn [[{:keys [categories chats]} full-chats-data collapsed-categories]
      [_ community-id]]
   (let [reduce-fn (reduce-over-categories
                    community-id
                    categories
                    collapsed-categories
                    full-chats-data)
         categories-and-chats
         (->> chats
              (reduce reduce-fn {})
              (sort-by (comp :position second))
              (map (fn [[k v]]
                     [k
                      (-> v
                          (update :chats #(sort-by :position %))
                          (update :chats
                                  #(filter (comp not :hide-if-permissions-not-met?)
                                           %)))])))]
     categories-and-chats)))

(re-frame/reg-sub
 :communities/collapsed-categories-for-community
 :<- [:communities/collapsed-categories]
 (fn [collapsed-categories [_ community-id]]
   (get collapsed-categories community-id)))


(defn token-requirement->token
  [checking-permissions?
   token-images
   {:keys [satisfied criteria]}]
  (let [sym           (:symbol criteria)
        amount-in-wei (:amountInWei criteria)
        decimals      (:decimals criteria)]
    {:symbol       sym
     :sufficient?  satisfied
     :collectible? (= (:type criteria) constants/community-token-type-erc721)
     :loading?     checking-permissions?
     :amount       (money/to-fixed (money/token->unit amount-in-wei decimals))
     :img-src      (get token-images sym)}))

(re-frame/reg-sub
 :communities/checking-permissions-by-id
 :<- [:communities/permissions-check]
 (fn [permissions [_ id]]
   (get permissions id)))


(re-frame/reg-sub
 :communities/checking-permissions-all-by-id
 :<- [:communities/permissions-check-all]
 (fn [permissions [_ id]]
   (get permissions id)))

(re-frame/reg-sub
 :community/token-gated-overview
 (fn [[_ community-id]]
   [(re-frame/subscribe [:communities/community community-id])
    (re-frame/subscribe [:communities/checking-permissions-by-id community-id])])
 (fn [[{:keys [token-images]}
       {:keys [checking? check]}] _]
   (let [highest-role            (:highestRole check)
         networks-not-supported? (:networksNotSupported check)
         lowest-role             (last (:roles check))
         highest-permission-role (:type highest-role)
         can-request-access?     (and (boolean highest-permission-role) (not networks-not-supported?))]
     {:can-request-access?     can-request-access?
      :checking?               checking?
      :highest-permission-role highest-permission-role
      :networks-not-supported? networks-not-supported?
      :no-member-permission?   (and highest-permission-role
                                    (not (-> check :highestRole :criteria)))
      :tokens                  (map (fn [{:keys [tokenRequirement]}]
                                      (map
                                       (partial token-requirement->token
                                                checking?
                                                token-images)
                                       tokenRequirement))
                                    (or (:criteria highest-role)
                                        (:criteria lowest-role)))})))

(re-frame/reg-sub
 :community/images
 :<- [:communities]
 (fn [communities [_ id]]
   (get-in communities [id :images])))

(re-frame/reg-sub
 :communities/rules
 :<- [:communities]
 (fn [communities [_ community-id]]
   (get-in communities [community-id :intro-message])))

(re-frame/reg-sub
 :communities/token-images-by-symbol
 (fn [[_ community-id]]
   [(re-frame/subscribe [:communities/community community-id])])
 (fn [[{:keys [tokens-metadata]}] _]
   (->> tokens-metadata
        (map (fn [{sym :symbol image :image}]
               {sym image}))
        (into {}))))

(re-frame/reg-sub
 :community/token-permissions
 (fn [[_ community-id]]
   [(re-frame/subscribe [:communities/community community-id])
    (re-frame/subscribe [:communities/checking-permissions-all-by-id community-id])])
 (fn [[{:keys [token-images]}
       {:keys [checking? check]}] _]
   (let [roles                      (:roles check)
         member-and-satisifed-roles (filter #(or (= (:type %)
                                                    constants/community-token-permission-become-member)
                                                 (:satisfied %))
                                            roles)]
     (mapv (fn [role]
             {:role       (:type role)
              :role-text  (i18n/label (utils/role->translation-key (:type role)))
              :satisfied? (:satisfied role)
              :tokens     (map (fn [{:keys [tokenRequirement]}]
                                 (map
                                  (partial token-requirement->token
                                           checking?
                                           token-images)
                                  tokenRequirement))
                               (:criteria role))})
           member-and-satisifed-roles))))

(re-frame/reg-sub
 :communities/has-permissions?
 (fn [[_ community-id]]
   [(re-frame/subscribe [:community/token-permissions community-id])])
 (fn [[permissions] _]
   (let [all-tokens (apply concat (map :tokens permissions))]
     (boolean (some seq all-tokens)))))
