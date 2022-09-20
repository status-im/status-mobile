(ns status-im.subs.communities
  (:require [re-frame.core :as re-frame]
            [clojure.string :as string]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ui.screens.profile.visibility-status.utils :as visibility-status-utils]
            [status-im.constants :as constants]))

(re-frame/reg-sub
 :communities
 :<- [:raw-communities]
 :<- [:communities/enabled?]
 (fn [[raw-communities communities-enabled?]]
   (if communities-enabled?
     raw-communities
     [])))

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
 :communities/sorted-community-members
 (fn [[_ community-id]]
   (let [contacts (re-frame/subscribe [:contacts/contacts])
         multiaccount (re-frame/subscribe [:multiaccount])
         members (re-frame/subscribe [:communities/community-members community-id])]
     [contacts multiaccount members]))
 (fn [[contacts multiaccount members] _]
   (let [names (reduce (fn [acc identity]
                         (let [me? (= (:public-key multiaccount) identity)
                               contact (when-not me?
                                         (multiaccounts/contact-by-identity contacts identity))
                               name (first (multiaccounts/contact-two-names-by-identity contact multiaccount identity))]
                           (assoc acc identity name)))
                       {}
                       (keys members))]
     (->> members
          (sort-by #(get names (get % 0)))
          (sort-by #(visibility-status-utils/visibility-status-order (get % 0)))))))

(re-frame/reg-sub
 :communities/featured-communities
 :<- [:communities/enabled?]
 :<- [:search/home-filter]
 :<- [:communities]
 (fn [[communities-enabled? search-filter communities]]
   (filterv
    (fn [{:keys [name featured id]}]
      (and (or featured (= name "Status"))                  ;; TO DO: remove once featured communities exist
           (or communities-enabled?
               (= id constants/status-community-id))
           (or (empty? search-filter)
               (string/includes? (string/lower-case (str name)) search-filter))))
    (vals communities))))

(re-frame/reg-sub
 :communities/communities
 :<- [:communities/enabled?]
 :<- [:search/home-filter]
 :<- [:communities]
 (fn [[communities-enabled? search-filter communities]]
   (filterv
    (fn [{:keys [name joined id]}]
      (and joined
           (or communities-enabled?
               (= id constants/status-community-id))
           (or (empty? search-filter)
               (string/includes? (string/lower-case (str name)) search-filter))))
    (vals communities))))

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

(re-frame/reg-sub
 :communities/unviewed-counts
 (fn [[_ community-id]]
   [(re-frame/subscribe [:chats/by-community-id community-id])])
 (fn [[chats]]
   (reduce (fn [acc {:keys [unviewed-mentions-count unviewed-messages-count]}]
             {:unviewed-messages-count (+ (:unviewed-messages-count acc) (or unviewed-messages-count 0))
              :unviewed-mentions-count (+ (:unviewed-mentions-count acc) (or unviewed-mentions-count 0))})
           {:unviewed-messages-count 0
            :unviewed-mentions-count 0}
           chats)))

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