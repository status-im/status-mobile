(ns status-im.subs.communities
  (:require
    [clojure.string :as string]
    [legacy.status-im.ui.screens.profile.visibility-status.utils :as visibility-status-utils]
    [re-frame.core :as re-frame] ;; [re-frame.db :as rf-db]
    [status-im.common.resources :as resources]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.common.utils :as wallet.utils]
    [utils.i18n :as i18n]))

(re-frame/reg-sub
 :communities/fetching-community
 :<- [:communities/fetching-community]
 (fn [info [_ id]]
   (get info id)))

(re-frame/reg-sub
 :communities/section-list
 :<- [:communities]
 (fn [communities]
   (->> (vals communities)
        (group-by (comp (fnil string/upper-case "") first :name))
        (sort-by (fn [[title]] title))
        (map (fn [[title data]]
               {:title title
                :data  data})))))

(re-frame/reg-sub
 :communities/community
 :<- [:communities]
 (fn [communities [_ id]]
   (get communities id)))

(re-frame/reg-sub
 :communities/community-chats
 :<- [:communities]
 (fn [communities [_ id]]
   (get-in communities [id :chats])))

(re-frame/reg-sub
 :communities/community-members
 :<- [:communities]
 (fn [communities [_ id]]
   (get-in communities [id :members])))

(re-frame/reg-sub
 :communities/current-community-members
 :<- [:chats/current-chat]
 :<- [:communities]
 (fn [[{:keys [community-id]} communities]]
   (get-in communities [community-id :members])))

(re-frame/reg-sub
 :communities/sorted-community-members
 (fn [[_ community-id]]
   (let [profile (re-frame/subscribe [:profile/profile])
         members (re-frame/subscribe [:communities/community-members community-id])]
     [profile members]))
 (fn [[profile members] _]
   (let [names (reduce (fn [acc contact-identity]
                         (assoc acc
                                contact-identity
                                (when (= (:public-key profile) contact-identity)
                                  (:primary-name profile)
                                  contact-identity)))
                       {}
                       (keys members))]
     (->> members
          (sort-by #(get names (get % 0)))
          (sort-by #(visibility-status-utils/visibility-status-order (get % 0)))))))

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

(re-frame/reg-sub
 :communities/community-ids
 :<- [:communities]
 (fn [communities]
   (map :id (vals communities))))

(def memo-communities-stack-items (atom nil))

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
     (let [grouped-communities (reduce (fn [acc community]
                                         (let [joined?      (:joined community)
                                               community-id (:id community)
                                               pending?     (boolean (get requests community-id))]
                                           (cond
                                             joined?  (update acc :joined conj community)
                                             pending? (update acc :pending conj community)
                                             :else    (update acc :opened conj community))))
                                       {:joined [] :pending [] :opened []}
                                       (vals communities))]
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
 :communities/edited-community
 :<- [:communities]
 :<- [:communities/community-id-input]
 (fn [[communities community-id]]
   (get communities community-id)))

(re-frame/reg-sub
 :communities/current-community
 :<- [:communities]
 :<- [:chats/current-raw-chat]
 (fn [[communities {:keys [community-id]}]]
   (get communities community-id)))

(re-frame/reg-sub
 :communities/unviewed-count
 (fn [[_ community-id]]
   [(re-frame/subscribe [:chats/by-community-id community-id])])
 (fn [[chats]]
   (reduce (fn [acc {:keys [unviewed-messages-count]}]
             (+ acc (or unviewed-messages-count 0)))
           0
           chats)))

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

(re-frame/reg-sub
 :community/categories
 (fn [[_ community-id]]
   [(re-frame/subscribe [:communities/community community-id])])
 (fn [[{:keys [categories]}] _]
   categories))

(re-frame/reg-sub
 :communities/sorted-categories
 :<- [:communities]
 (fn [communities [_ id]]
   (->> (get-in communities [id :categories])
        (map #(assoc (get % 1) :community-id id))
        (sort-by :position)
        (into []))))

(defn- get-chat-lock-state
  "Returns the chat lock state.

  - Nil:   no lock  (there are no permissions for the chat)
  - True:  locked   (there are permissions and can-post? is false)
  - False: unlocked (there are permissions and can-post? is true)"
  [community-id channels-permissions {chat-id :id}]
  (let [composite-key                            (keyword (str community-id chat-id))
        permissions                              (get channels-permissions composite-key)
        {view-only-satisfied?  :satisfied?
         view-only-permissions :permissions}     (:view-only permissions)
        {view-and-post-satisfied?  :satisfied?
         view-and-post-permissions :permissions} (:view-and-post permissions)
        can-access?                              (or (and (seq view-only-permissions)
                                                          view-only-satisfied?)
                                                     (and (seq view-and-post-permissions)
                                                          view-and-post-satisfied?))]
    (if (and (empty? view-only-permissions)
             (empty? view-and-post-permissions))
      nil
      (not can-access?))))

(re-frame/reg-sub
 :communities/community-channels-permissions
 :<- [:communities/channels-permissions]
 (fn [channel-permissions [_ community-id]]
   (get channel-permissions community-id)))

(defn- reduce-over-categories
  [community-id
   categories
   collapsed-categories
   full-chats-data
   channels-permissions]
  (fn [acc
       [_ {:keys [name categoryID position id emoji] :as chat}]]
    (let [category-id       (if (seq categoryID) categoryID constants/empty-category-id)
          {:keys [unviewed-messages-count
                  unviewed-mentions-count
                  muted]}   (get full-chats-data
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
          categorized-chat  {:name             name
                             :emoji            emoji
                             :muted?           muted
                             :unread-messages? (pos? unviewed-messages-count)
                             :position         position
                             :mentions-count   (or unviewed-mentions-count 0)
                             :locked?          (get-chat-lock-state community-id
                                                                    channels-permissions
                                                                    chat)
                             :id               id}]
      (update-in acc-with-category [category-id :chats] conj categorized-chat))))

(re-frame/reg-sub
 :communities/categorized-channels
 (fn [[_ community-id]]
   [(re-frame/subscribe [:communities/community community-id])
    (re-frame/subscribe [:chats/chats])
    (re-frame/subscribe [:communities/collapsed-categories-for-community community-id])
    (re-frame/subscribe [:communities/community-channels-permissions community-id])])
 (fn [[{:keys [categories chats]} full-chats-data collapsed-categories
       channels-permissions]
      [_ community-id]]
   (let [reduce-fn (reduce-over-categories
                    community-id
                    categories
                    collapsed-categories
                    full-chats-data
                    channels-permissions)
         categories-and-chats
         (->> chats
              (reduce reduce-fn {})
              (sort-by (comp :position second))
              (map (fn [[k v]]
                     [k (update v :chats #(sort-by :position %))])))]
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
  (let [sym    (:symbol criteria)
        amount (:amount criteria)]
    {:symbol      sym
     :sufficient? satisfied
     :loading?    checking-permissions?
     :amount      (wallet.utils/remove-trailing-zeroes amount)
     :img-src     (get token-images sym)}))

(re-frame/reg-sub
 :communities/checking-permissions-by-id
 :<- [:communities/permissions-check]
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

(re-frame/reg-sub :communities/permissioned-balances-by-address
 :<- [:communities/permissioned-balances]
 (fn [balances [_ community-id account-address]]
   (get-in balances [community-id (keyword account-address)])))

(re-frame/reg-sub
 :communities/selected-permission-addresses
 (fn [[_ community-id]]
   [(re-frame/subscribe [:communities/community community-id])])
 (fn [[{:keys [selected-permission-addresses]}] _]
   selected-permission-addresses))

(re-frame/reg-sub
 :communities/share-all-addresses?
 (fn [[_ community-id]]
   [(re-frame/subscribe [:communities/community community-id])])
 (fn [[{:keys [share-all-addresses?]}] _]
   share-all-addresses?))

(re-frame/reg-sub
 :communities/unsaved-address-changes?
 (fn [[_ community-id]]
   [(re-frame/subscribe [:communities/community community-id])])
 (fn [[{:keys [share-all-addresses? previous-share-all-addresses?
               selected-permission-addresses previous-permission-addresses]}] _]
   (or (not= share-all-addresses? previous-share-all-addresses?)
       (not= selected-permission-addresses previous-permission-addresses))))

(re-frame/reg-sub
 :communities/selected-permission-accounts
 (fn [[_ community-id]]
   [(re-frame/subscribe [:wallet/accounts-without-watched-accounts])
    (re-frame/subscribe [:communities/selected-permission-addresses community-id])])
 (fn [[accounts selected-permission-addresses]]
   (filter #(contains? selected-permission-addresses (:address %)) accounts)))

(re-frame/reg-sub
 :communities/airdrop-address
 (fn [[_ community-id]]
   [(re-frame/subscribe [:communities/community community-id])])
 (fn [[{:keys [airdrop-address]}] _]
   airdrop-address))

(re-frame/reg-sub
 :communities/airdrop-account
 (fn [[_ community-id]]
   [(re-frame/subscribe [:wallet/accounts-with-customization-color])
    (re-frame/subscribe [:communities/airdrop-address community-id])])
 (fn [[accounts airdrop-address]]
   (first (filter #(= (:address %) airdrop-address) accounts))))

(re-frame/reg-sub
 :communities/checking-all-channels-permissions?
 (fn [db [_ community-id]]
   (get-in db [:communities community-id])))

;; (re-frame/reg-sub
;;  :community/token-permissions
;;  (fn [[_ community-id]]
;;    [(re-frame/subscribe [:communities/community community-id])
;;     (re-frame/subscribe [:communities/checking-permissions-by-id community-id])])

;;  (fn [[community permissions-check] _]
;;    (let [token-permissions (:token-permissions community)
;;          token-images      (:token-images community)
;;          grouped-by-type   (group-by (comp :type second) token-permissions)
;;          mock-images       (when (and (contains? token-permissions 5)
;;                                       (contains? token-permissions 6))
;;                              (resources/mock-images :collectible))
;;          permissions       (:check permissions-check)
;;          sufficient        (mapcat (fn [[_ permission]]
;;                                      (mapcat (fn [token-req]
;;                                                [(get token-req :satisfied)])
;;                                       (:tokenRequirement permission)))
;;                             (:permissions permissions))]

;;      (tap> ["sufficient" sufficient])
;;      (tap> ["token images" token-images])
;;      (js/console.log ["sufficient" (clj->js sufficient)])
;;      (tap> ["database" @rf-db/app-db])
;;      (into {}
;;            (map (fn [[type tokens]]
;;                   [type
;;                    (map (fn [token]
;;                           (map-indexed (fn [i criteria]
;;                                          (let [sym (:symbol criteria)]
;;                                            (-> criteria
;;                                                (assoc :amount
;;                                                       (wallet.utils/remove-trailing-zeroes (:amount
;;                                                                                             criteria))
;;                                                       :sufficient? (nth sufficient i true)
;;                                                       :img-src
;;                                                       (if (= type 2)
;;                                                         (or mock-images
;;                                                             (get token-images sym))
;;                                                         (get token-images sym))))))
;;                                        (:token_criteria (second token))))
;;                         tokens)])
;;                 grouped-by-type)))))

(re-frame/reg-sub :community/token-permissions
 (fn [[_ community-id]]
   (println "community" community-id)
   [(re-frame/subscribe [:communities/community community-id])
    (re-frame/subscribe [:communities/checking-permissions-by-id community-id])])
 (fn [[{:keys [token-permissions token-images]} permissions-check] _]
   (let [mock-images (when (and (contains? token-permissions 5)
                                (contains? token-permissions 6))
                       (resources/mock-images :collectible))]
     (tap> {:permissions-check permissions-check})
     (->> token-permissions
          (map second)
          (map
           (fn [token-permission]
             (let [satisfied-criteria (into []
                                            (get-in permissions-check
                                                    [:check :permissions (:id token-permission)
                                                     :tokenRequirement]))]
               (map-indexed (fn [idx criterion]
                              (let [sym  (:symbol criterion)
                                    type (:type token-permission)]
                                {:symbol          sym
                                 :permission-type type
                                 :amount          (wallet.utils/remove-trailing-zeroes (:amount
                                                                                        criterion))
                                 :sufficient?     (get-in satisfied-criteria [idx :satisfied] false)
                                 :img-src         (if (= type 2)
                                                    (or (get mock-images sym)
                                                        (get token-images sym))
                                                    (get token-images sym))}))
                            (:token_criteria token-permission)))))
          (group-by (comp :permission-type first))))))
