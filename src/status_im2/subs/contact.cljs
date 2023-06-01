(ns status-im2.subs.contact
  (:require [clojure.string :as string]
            [utils.i18n :as i18n]
            [re-frame.core :as re-frame]
            [status-im.contact.db :as contact.db]
            [status-im.ethereum.core :as ethereum]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ui.screens.profile.visibility-status.utils :as visibility-status-utils]
            [status-im.utils.gfycat.core :as gfycat]
            [utils.image-server :as image-server]
            [utils.collection]
            [status-im2.constants :as constants]
            [quo2.theme :as theme]))

(re-frame/reg-sub
 ::query-current-chat-contacts
 :<- [:chats/current-chat]
 :<- [:contacts/contacts]
 (fn [[chat contacts] [_ query-fn]]
   (contact.db/query-chat-contacts chat contacts query-fn)))

(re-frame/reg-sub
 :multiaccount/profile-pictures-show-to
 :<- [:multiaccount]
 (fn [multiaccount]
   (get multiaccount :profile-pictures-show-to)))

(re-frame/reg-sub
 ::profile-pictures-visibility
 :<- [:multiaccount]
 (fn [multiaccount]
   (get multiaccount :profile-pictures-visibility)))

(defn- replace-contact-image-uri
  [contact port identity]
  (let [theme          (theme/get-theme)
        contact-images (:images contact)
        contact-images (reduce (fn [acc image]
                                 (let [image-name (:type image)
                                       ; We pass the clock so that we reload the image if the image is
                                       ; updated
                                       clock      (:clock image)
                                       uri        (image-server/get-contact-image-uri port
                                                                                      identity
                                                                                      image-name
                                                                                      clock
                                                                                      theme)]
                                   (assoc-in acc [(keyword image-name) :uri] uri)))
                               contact-images
                               (vals contact-images))]
    (assoc contact :images contact-images)))

(defn- reduce-contacts-image-uri
  [contacts port]
  (reduce-kv (fn [acc public-key contact]
               (let [contact (replace-contact-image-uri contact port public-key)]
                 (assoc acc public-key contact)))
             {}
             contacts))

(re-frame/reg-sub
 :contacts/contacts
 :<- [:contacts/contacts-raw]
 :<- [::profile-pictures-visibility]
 :<- [:multiaccount/public-key]
 :<- [:mediaserver/port]
 (fn [[contacts profile-pictures-visibility public-key port]]
   (let [contacts (contact.db/enrich-contacts contacts profile-pictures-visibility public-key)]
     (reduce-contacts-image-uri contacts port))))

(re-frame/reg-sub
 :contacts/active
 :<- [:contacts/contacts]
 (fn [contacts]
   (->> contacts
        (filter (fn [[_ contact]] (:active? contact)))
        contact.db/sort-contacts)))

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
          (let [first-char (first (:alias contact))]
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
        (sort-by :alias)
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
        contact.db/sort-contacts)))

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

(defn filter-recipient-contacts
  [search-filter {:keys [primary-name secondary-name]}]
  (or
   (when primary-name
     (string/includes? (string/lower-case (str primary-name)) search-filter))
   (when secondary-name
     (string/includes? (string/lower-case (str secondary-name)) search-filter))))

(re-frame/reg-sub
 :contacts/active-with-ens-names
 :<- [:contacts/active]
 :<- [:search/recipient-filter]
 (fn [[contacts search-filter]]
   (let [contacts (filter :ens-verified contacts)]
     (if (string/blank? search-filter)
       contacts
       (filter (partial filter-recipient-contacts
                        (string/lower-case search-filter))
               contacts)))))

(defn- enrich-contact
  [_ identity ens-name port]
  (let [contact (contact.db/enrich-contact
                 (contact.db/public-key-and-ens-name->new-contact identity ens-name))]
    (replace-contact-image-uri contact port identity)))

(re-frame/reg-sub
 :contacts/current-contact
 :<- [:contacts/contacts]
 :<- [:contacts/current-contact-identity]
 :<- [:contacts/current-contact-ens-name]
 :<- [:mediaserver/port]
 (fn [[contacts identity ens-name port]]
   (let [contact (get contacts identity)]
     (cond-> contact
       (nil? contact)
       (enrich-contact identity ens-name port)))))

(re-frame/reg-sub
 :contacts/contact-by-identity
 :<- [:contacts/contacts]
 (fn [contacts [_ identity]]
   (multiaccounts/contact-by-identity contacts identity)))

(re-frame/reg-sub
 :contacts/contact-added?
 (fn [[_ identity] _]
   [(re-frame/subscribe [:contacts/contact-by-identity identity])])
 (fn [[contact] _]
   (:added? contact)))

(re-frame/reg-sub
 :contacts/contact-blocked?
 (fn [[_ identity] _]
   [(re-frame/subscribe [:contacts/contact-by-identity identity])])
 (fn [[contact] _]
   (:blocked contact)))

(re-frame/reg-sub
 :contacts/contact-two-names-by-identity
 (fn [[_ identity] _]
   [(re-frame/subscribe [:contacts/contact-by-identity identity])
    (re-frame/subscribe [:multiaccount])])
 (fn [[contact current-multiaccount] [_ identity]]
   (multiaccounts/contact-two-names-by-identity contact
                                                current-multiaccount
                                                identity)))

(re-frame/reg-sub
 :contacts/contact-name-by-identity
 (fn [[_ identity] _]
   [(re-frame/subscribe [:contacts/contact-two-names-by-identity identity])])
 (fn [[names] _]
   (first names)))

(re-frame/reg-sub
 :messages/quote-info
 :<- [:chats/messages]
 :<- [:contacts/contacts]
 :<- [:multiaccount]
 (fn [[messages contacts current-multiaccount] [_ message-id]]
   (when-let [message (get messages message-id)]
     (let [identity (:from message)
           me?      (= (:public-key current-multiaccount) identity)]
       (if me?
         {:quote    {:from identity
                     :text (get-in message [:content :text])}
          :ens-name (:preferred-name current-multiaccount)
          :alias    (gfycat/generate-gfy identity)}
         (let [contact (or (contacts identity)
                           (contact.db/public-key->new-contact identity))]
           {:quote    {:from identity
                       :text (get-in message [:content :text])}
            :ens-name (when (:ens-verified contact)
                        (:name contact))
            :alias    (or (:alias contact)
                          (gfycat/generate-gfy identity))}))))))

(re-frame/reg-sub
 :contacts/all-contacts-not-in-current-chat
 :<- [::query-current-chat-contacts remove]
 (fn [contacts]
   (filter :added? contacts)))

(re-frame/reg-sub
 :contacts/current-chat-contacts
 :<- [:chats/current-chat]
 :<- [:contacts/contacts]
 :<- [:multiaccount]
 (fn [[{:keys [contacts admins]} all-contacts current-multiaccount]]
   (contact.db/get-all-contacts-in-group-chat contacts admins all-contacts current-multiaccount)))

(re-frame/reg-sub
 :contacts/contacts-by-chat
 (fn [[_ _ chat-id] _]
   [(re-frame/subscribe [:chats/chat chat-id])
    (re-frame/subscribe [:contacts/contacts])])
 (fn [[chat all-contacts] [_ query-fn]]
   (contact.db/query-chat-contacts chat all-contacts query-fn)))

(re-frame/reg-sub
 :contacts/contact-by-address
 :<- [:contacts/contacts]
 :<- [:multiaccount/contact]
 (fn [[contacts multiaccount] [_ address]]
   (if (ethereum/address= address (:public-key multiaccount))
     multiaccount
     (contact.db/find-contact-by-address contacts address))))

(re-frame/reg-sub
 :contacts/contacts-by-address
 :<- [:contacts/contacts]
 (fn [contacts]
   (reduce (fn [acc [_ {:keys [address] :as contact}]]
             (if address
               (assoc acc address contact)
               acc))
           {}
           contacts)))

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
 :<- [:contacts/current-chat-contacts]
 (fn [members]
   (let [admins  (filter :admin? members)
         online  (filter #(and (not (:admin? %)) (:online? %)) members)
         offline (filter #(and (not (:admin? %)) (not (:online? %))) members)]
     (vals (cond-> {}
             (seq admins)  (assoc :owner {:title (i18n/label :t/owner) :data admins})
             (seq online)  (assoc :online {:title (i18n/label :t/online) :data online})
             (seq offline) (assoc :offline {:title (i18n/label :t/offline) :data offline}))))))


