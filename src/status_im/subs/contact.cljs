(ns status-im.subs.contact
  (:require
    [clojure.set :as set]
    [clojure.string :as string]
    [legacy.status-im.ui.screens.profile.visibility-status.utils :as visibility-status-utils]
    [quo.theme]
    [re-frame.core :as re-frame]
    [status-im.common.pixel-ratio :as pixel-ratio]
    [status-im.constants :as constants]
    [status-im.contexts.profile.utils :as profile.utils]
    [status-im.subs.chat.utils :as chat.utils]
    [utils.address :as address]
    [utils.collection]
    [utils.i18n :as i18n]))

(defn query-chat-contacts
  [{:keys [contacts]} all-contacts query-fn]
  (let [participant-set (into #{} (filter identity) contacts)]
    (query-fn (comp participant-set :public-key) (vals all-contacts))))

(re-frame/reg-sub
 ::query-current-chat-contacts
 :<- [:chats/current-chat]
 :<- [:contacts/contacts]
 (fn [[chat contacts] [_ query-fn]]
   (query-chat-contacts chat contacts query-fn)))

(re-frame/reg-sub
 :multiaccount/profile-pictures-show-to
 :<- [:profile/profile]
 (fn [multiaccount]
   (get multiaccount :profile-pictures-show-to)))

(re-frame/reg-sub
 ::profile-pictures-visibility
 :<- [:profile/profile]
 (fn [multiaccount]
   (get multiaccount :profile-pictures-visibility)))

(defn- replace-contact-image-uri
  [contact port public-key font-file theme]
  (let [{:keys [images ens-name customization-color]} contact
        images
        (reduce (fn [acc image]
                  (let [image-name (:type image)
                        clock      (:clock image)
                        options    {:port port
                                    :ratio pixel-ratio/ratio
                                    :public-key
                                    public-key
                                    :image-name
                                    image-name
                                    ; We pass the clock so that we reload the
                                    ; image if the image is updated
                                    :clock
                                    clock
                                    :theme
                                    theme
                                    :override-ring?
                                    (when ens-name false)}]
                    (assoc-in acc
                     [(keyword image-name) :config]
                     {:type    :contact
                      :options options})))
                images
                (vals images))

        images (if (seq images)
                 images
                 {:thumbnail
                  {:config {:type    :initials
                            :options {:port                port
                                      :ratio               pixel-ratio/ratio
                                      :public-key          public-key
                                      :override-ring?      (when ens-name false)
                                      :uppercase-ratio     (:uppercase-ratio
                                                            constants/initials-avatar-font-conf)
                                      :customization-color customization-color
                                      :theme               theme
                                      :font-file           font-file}}}})]

    (assoc contact :images images)))

(defn- enrich-contact
  ([contact] (enrich-contact contact nil nil))
  ([{:keys [public-key] :as contact} setting own-public-key]
   (cond-> contact
     (and setting
          (not= public-key own-public-key)
          (or (= setting constants/profile-pictures-visibility-none)
              (and (= setting constants/profile-pictures-visibility-contacts-only)
                   (not (:added? contact)))))
     (dissoc :images))))

(defn- enrich-contacts
  [contacts profile-pictures-visibility own-public-key]
  (reduce-kv
   (fn [acc public-key contact]
     (assoc acc public-key (enrich-contact contact profile-pictures-visibility own-public-key)))
   {}
   contacts))

(defn- reduce-contacts-image-uri
  [contacts port font-file theme]
  (reduce-kv (fn [acc public-key contact]
               (let [contact (replace-contact-image-uri contact port public-key font-file theme)]
                 (assoc acc public-key contact)))
             {}
             contacts))

(re-frame/reg-sub
 :contacts/contacts
 :<- [:contacts/contacts-raw]
 :<- [::profile-pictures-visibility]
 :<- [:multiaccount/public-key]
 :<- [:mediaserver/port]
 :<- [:initials-avatar-font-file]
 :<- [:theme]
 (fn [[contacts profile-pictures-visibility public-key port font-file theme]]
   (let [contacts (enrich-contacts contacts profile-pictures-visibility public-key)]
     (reduce-contacts-image-uri contacts port font-file theme))))

(defn sort-contacts
  [contacts]
  (sort (fn [c1 c2]
          (let [name1 (:primary-name c1)
                name2 (:primary-name c2)]
            (when (and name1 name2)
              (compare (string/lower-case name1)
                       (string/lower-case name2)))))
        (vals contacts)))

(re-frame/reg-sub
 :contacts/active
 :<- [:contacts/contacts]
 (fn [contacts]
   (->> contacts
        (filter (fn [[_ contact]] (:active? contact)))
        sort-contacts)))

(re-frame/reg-sub
 :contacts/active-sections
 :<- [:contacts/active]
 (fn [contacts]
   (->> contacts
        (group-by #(string/upper-case (first (:primary-name %))))
        sort
        (mapv (fn [[title items]] {:title title :data items})))))

(re-frame/reg-sub
 :contacts/grouped-by-first-letter
 :<- [:contacts/current-chat-contacts]
 :<- [:contacts/active]
 (fn [[members contacts]]
   (-> (reduce
        (fn [acc contact]
          (let [first-char (first (:primary-name contact))]
            (if (get acc first-char)
              (update-in acc [first-char :data] #(conj % contact))
              (assoc acc first-char {:title first-char :data [contact]}))))
        {}
        (utils.collection/distinct-by :public-key (concat members contacts)))
       sort
       vals)))

(re-frame/reg-sub
 :contacts/sorted-contacts
 :<- [:contacts/active]
 (fn [active-contacts]
   (->> active-contacts
        (sort-by :primary-name)
        (sort-by
         #(visibility-status-utils/visibility-status-order (:public-key %))))))

(re-frame/reg-sub
 :contacts/sorted-and-grouped-by-first-letter
 :<- [:contacts/active]
 :<- [:selected-contacts-count]
 (fn [[contacts selected-contacts-count]]
   (->> contacts
        (filter :mutual?)
        (map #(assoc %
                     :allow-new-users?
                     (< selected-contacts-count
                        (dec constants/max-group-chat-participants))))
        (group-by (comp (fnil string/upper-case "") first :primary-name))
        (sort-by first)
        (map (fn [[title data]]
               {:title title
                :data  data})))))

(re-frame/reg-sub
 :contacts/active-count
 :<- [:contacts/active]
 (fn [active-contacts]
   (count active-contacts)))

(re-frame/reg-sub
 :contacts/blocked
 :<- [:contacts/contacts]
 (fn [contacts]
   (->> contacts
        (filter (fn [[_ contact]]
                  (:blocked? contact)))
        sort-contacts)))

(re-frame/reg-sub
 :contacts/blocked-set
 :<- [:contacts/blocked]
 (fn [contacts]
   (into #{} (map :public-key contacts))))

(re-frame/reg-sub
 :contacts/blocked-count
 :<- [:contacts/blocked]
 (fn [blocked-contacts]
   (count blocked-contacts)))

(defn public-key-and-ens-name->new-contact
  [public-key ens-name]
  (let [contact {:public-key public-key}]
    (if ens-name
      (-> contact
          (assoc :ens-name ens-name)
          (assoc :ens-verified true)
          (assoc :name ens-name))
      contact)))

(defn- prepare-contact
  [_ contact-identity ens-name port font-file theme]
  (let [contact (enrich-contact
                 (public-key-and-ens-name->new-contact contact-identity ens-name))]
    (replace-contact-image-uri contact port contact-identity font-file theme)))

(re-frame/reg-sub
 :contacts/current-contact
 :<- [:contacts/contacts]
 :<- [:contacts/current-contact-identity]
 :<- [:contacts/current-contact-ens-name]
 :<- [:mediaserver/port]
 :<- [:initials-avatar-font-file]
 :<- [:theme]
 (fn [[contacts contact-identity ens-name port font-file theme]]
   (let [contact (get contacts contact-identity)]
     (cond-> contact
       (nil? contact)
       (prepare-contact contact-identity ens-name port font-file theme)))))

(re-frame/reg-sub
 :contacts/contact-by-identity
 :<- [:contacts/contacts]
 (fn [contacts [_ contact-identity]]
   (get contacts contact-identity {:public-key contact-identity})))

(re-frame/reg-sub
 :contacts/contact-two-names-by-identity
 (fn [[_ contact-identity] _]
   [(re-frame/subscribe [:contacts/contact-by-identity contact-identity])
    (re-frame/subscribe [:profile/profile])])
 (fn [[{:keys [primary-name] :as contact}
       {:keys [public-key preferred-name display-name]}]
      [_ contact-identity]]
   [(if (= public-key contact-identity)
      (cond
        (not (string/blank? preferred-name)) preferred-name
        (not (string/blank? display-name))   display-name
        (not (string/blank? primary-name))   primary-name
        :else                                public-key)
      (profile.utils/displayed-name contact))
    (:secondary-name contact)]))

(re-frame/reg-sub
 :contacts/all-contacts-not-in-current-chat
 :<- [::query-current-chat-contacts remove]
 (fn [contacts]
   (filter :added? contacts)))

(defn get-all-contacts-in-group-chat
  [members admins contacts {:keys [public-key preferred-name name display-name] :as current-account}]
  (let [current-contact (some->
                          current-account
                          (select-keys [:name :preferred-name :public-key :images :compressed-key])
                          (set/rename-keys {:name :alias :preferred-name :name})
                          (assoc :primary-name (or display-name preferred-name name)))
        all-contacts    (cond-> contacts
                          current-contact
                          (assoc public-key current-contact))]
    (->> members
         (map #(or (get all-contacts %)
                   {:public-key %}))
         (sort-by (comp string/lower-case
                        (fn [{:keys [primary-name name alias public-key]}]
                          (or primary-name
                              name
                              alias
                              public-key))))
         (map #(if (get admins (:public-key %))
                 (assoc % :admin? true)
                 %)))))

(re-frame/reg-sub
 :contacts/current-chat-contacts
 :<- [:chats/current-chat]
 :<- [:contacts/contacts]
 :<- [:profile/profile]
 (fn [[{:keys [contacts admins]} all-contacts current-multiaccount]]
   (get-all-contacts-in-group-chat contacts admins all-contacts current-multiaccount)))

(re-frame/reg-sub
 :contacts/contacts-by-chat
 (fn [[_ chat-id]]
   [(re-frame/subscribe [:chats/chat chat-id])
    (re-frame/subscribe [:contacts/contacts])
    (re-frame/subscribe [:profile/profile])])
 (fn [[{:keys [contacts admins]} all-contacts current-multiaccount]]
   (get-all-contacts-in-group-chat contacts admins all-contacts current-multiaccount)))

(defn- contact-by-address
  [[addr contact] address]
  (when (address/address= addr address)
    contact))

(defn find-contact-by-address
  [contacts address]
  (some #(contact-by-address % address) contacts))

(re-frame/reg-sub
 :contacts/contact-by-address
 :<- [:contacts/contacts]
 :<- [:multiaccount/contact]
 (fn [[contacts multiaccount] [_ address]]
   (if (address/address= address (:public-key multiaccount))
     multiaccount
     (find-contact-by-address contacts address))))

(re-frame/reg-sub
 :contacts/contact-customization-color-by-address
 (fn [[_ address]]
   [(re-frame/subscribe [:contacts/contact-by-address address])])
 (fn [[contact]]
   (:customization-color contact)))

(re-frame/reg-sub
 :contacts/filtered-active-sections
 :<- [:contacts/active-sections]
 :<- [:contacts/search-query]
 (fn [[contacts query]]
   (if (empty? query)
     contacts
     (->> contacts
          (map (fn [item]
                 (update item
                         :data
                         (fn [data]
                           (filter #(string/includes?
                                     (string/lower-case (:alias %))
                                     (string/lower-case query))
                                   data)))))
          (remove #(empty? (:data %)))))))

(re-frame/reg-sub
 :contacts/group-members-sections
 (fn [[_ chat-id]]
   [(re-frame/subscribe [:contacts/contacts-by-chat chat-id])
    (re-frame/subscribe [:visibility-status-updates])
    (re-frame/subscribe [:multiaccount/public-key])
    (re-frame/subscribe [:multiaccount/current-user-visibility-status])])
 (fn [[members status-updates current-user-public-key current-user-visibility-status]]
   (let [members (map (fn [{:keys [public-key] :as member}]
                        (assoc member
                               :online?
                               (chat.utils/online?
                                (if (= public-key current-user-public-key)
                                  (:status-type current-user-visibility-status)
                                  (get-in status-updates [public-key :status-type])))))
                      members)
         admins  (filter :admin? members)
         online  (filter #(and (not (:admin? %)) (:online? %)) members)
         offline (filter #(and (not (:admin? %)) (not (:online? %))) members)]
     (vals (cond-> {}
             (seq admins)  (assoc :owner {:title (i18n/label :t/owner) :data admins})
             (seq online)  (assoc :online {:title (i18n/label :t/online) :data online})
             (seq offline) (assoc :offline {:title (i18n/label :t/offline) :data offline}))))))
