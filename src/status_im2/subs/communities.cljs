(ns status-im2.subs.communities
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ui.screens.profile.visibility-status.utils :as visibility-status-utils]
            [status-im2.constants :as constants]
            [utils.i18n :as i18n]))

(re-frame/reg-sub
 :communities/fetching-community
 :<- [:communities/resolve-community-info]
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
   (let [contacts     (re-frame/subscribe [:contacts/contacts])
         multiaccount (re-frame/subscribe [:multiaccount])
         members      (re-frame/subscribe [:communities/community-members community-id])]
     [contacts multiaccount members]))
 (fn [[contacts multiaccount members] _]
   (let [names (reduce (fn [acc identity]
                         (let [me?     (= (:public-key multiaccount) identity)
                               contact (when-not me?
                                         (multiaccounts/contact-by-identity contacts identity))
                               name    (first (multiaccounts/contact-two-names-by-identity contact
                                                                                           multiaccount
                                                                                           identity))]
                           (assoc acc identity name)))
                       {}
                       (keys members))]
     (->> members
          (sort-by #(get names (get % 0)))
          (sort-by #(visibility-status-utils/visibility-status-order (get % 0)))))))

(re-frame/reg-sub
 :communities/featured-communities
 :<- [:communities]
 (fn [communities]
   (vals communities)))

(re-frame/reg-sub
 :communities/sorted-communities
 :<- [:communities]
 (fn [communities]
   (sort-by :name (vals communities))))

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
 ;; Return communities splitted by level of user participation. Some communities user
 ;; already joined, to some of them join request sent and others were opened one day
 ;; and their data remained in app-db.
 ;; Result map has form: {:joined [id1, id2] :pending [id3, id5] :opened [id4]}"
 (fn [[view-id communities requests]]
   (if (= view-id :communities-stack)
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
 (fn [[communities counts] [_ identity]]
   (community->home-item
    (get communities identity)
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
  (reduce (fn [acc {:keys [unviewed-mentions-count unviewed-messages-count]}]
            {:unviewed-messages-count (+ (:unviewed-messages-count acc) (or unviewed-messages-count 0))
             :unviewed-mentions-count (+ (:unviewed-mentions-count acc) (or unviewed-mentions-count 0))})
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

(defn reduce-over-categories
  [community-id
   joined
   categories
   collapsed-categories
   full-chats-data]
  (fn [acc
       [_ {:keys [name categoryID position id emoji can-post?]}]]
    (let [category-id                       (if (seq categoryID) categoryID constants/empty-category-id)
          {:keys [unviewed-messages-count
                  unviewed-mentions-count]} (get full-chats-data
                                                 (str community-id id))
          acc-with-category                 (if (get acc category-id)
                                              acc
                                              (assoc acc
                                                     category-id
                                                     (assoc
                                                      (or (get categories category-id)
                                                          {:name (i18n/label :t/none)})
                                                      :collapsed? (get collapsed-categories
                                                                       category-id)
                                                      :chats      [])))
          chat                              {:name             name
                                             :emoji            emoji
                                             :unread-messages? (pos? unviewed-messages-count)
                                             :position         position
                                             :mentions-count   (or unviewed-mentions-count 0)
                                             :locked?          (or (not joined)
                                                                   (not can-post?))
                                             :id               id}]
      (update-in acc-with-category [category-id :chats] conj chat))))

(re-frame/reg-sub
 :communities/categorized-channels
 (fn [[_ community-id]]
   [(re-frame/subscribe [:communities/community community-id])
    (re-frame/subscribe [:chats/chats])
    (re-frame/subscribe [:communities/collapsed-categories-for-community community-id])])
 (fn [[{:keys [joined categories chats]} full-chats-data collapsed-categories] [_ community-id]]
   (let [reduce-fn (reduce-over-categories
                    community-id
                    joined
                    categories
                    collapsed-categories
                    full-chats-data)
         categories-and-chats
         (->>
           chats
           (reduce
            reduce-fn
            {})
           (sort-by (comp :position second))
           (map (fn [[k v]]
                  [k (update v :chats #(sort-by :position %))])))]

     categories-and-chats)))

(re-frame/reg-sub
 :communities/users
 :<- [:communities]
 (fn [_ [_ _]]
   [{:full-name "Alicia K"}
    {:full-name "Marcus C"}
    {:full-name "MNO PQR"}
    {:full-name "STU VWX"}]))

(re-frame/reg-sub
 :communities/collapsed-categories-for-community
 :<- [:communities/collapsed-categories]
 (fn [collapsed-categories [_ community-id]]
   (get collapsed-categories community-id)))
