(ns status-im.contexts.chat.contacts.events
  (:require
    [oops.core :as oops]
    [re-frame.core :as re-frame]
    [status-im.common.json-rpc.events :as json-rpc]
    [status-im.constants :as constants]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.transforms :as transforms]))

(defn <-rpc-js
  [^js js-contact]
  {:public-key            (oops/oget js-contact "id")
   :compressed-key        (oops/oget js-contact "compressedKey")
   :primary-name          (oops/oget js-contact "primaryName")
   :secondary-name        (.-secondaryName js-contact)
   :ens-name              (.-name js-contact)
   :nickname              (.-localNickname js-contact)
   :images                (transforms/js->clj (oops/oget js-contact "images"))
   :ens-verified          (oops/oget js-contact "ensVerified")
   :contact-request-state (oops/oget js-contact "contactRequestState")
   :last-updated          (oops/oget js-contact "lastUpdated")
   :active?               (oops/oget js-contact "active")
   :blocked?              (oops/oget js-contact "blocked")
   :added?                (oops/oget js-contact "added")
   :has-added-us?         (oops/oget js-contact "hasAddedUs")
   :mutual?               (oops/oget js-contact "mutual")
   :emoji-hash            (oops/oget js-contact "emojiHash")
   :bio                   (oops/oget js-contact "bio")
   :customization-color   (-> js-contact
                              (oops/oget "customizationColor")
                              keyword
                              ;; newly created accounts
                              (or :blue))})

(defn prepare-events-for-contact
  [db chats-js]
  (fn [events {:keys [public-key has-added-us? blocked? contact-request-state] :as contact}]
    (let [was-blocked? (get-in db [:contacts/contacts public-key :blocked?])]
      (cond-> events
        (and (not has-added-us?) (= constants/contact-request-state-none contact-request-state))
        (conj [:activity-center/remove-pending-contact-request public-key])

        (and blocked? (not was-blocked?))
        (conj [:contacts/blocked contact chats-js])))))

(defn update-contacts
  [contacts-cljs]
  (fn [contacts]
    (reduce (fn [contacts {:keys [public-key] :as contact}]
              (update contacts public-key merge contact))
            contacts
            contacts-cljs)))

(rf/defn process-js-contacts
  [{:keys [db]} response-js]
  (let [contacts-js   (oops/oget response-js "contacts")
        contacts-cljs (map <-rpc-js contacts-js)
        chats-js      (.-chatsForContacts response-js)
        events        (reduce
                       (prepare-events-for-contact db chats-js)
                       [[:activity-center.notifications/fetch-unread-count]
                        [:activity-center.notifications/fetch-pending-contact-requests]]
                       contacts-cljs)]
    (js-delete response-js "contacts")
    (js-delete response-js "chatsForContacts")
    (merge
     {:db                   (update db :contacts/contacts (update-contacts contacts-cljs))
      :utils/dispatch-later [{:ms 20 :dispatch [:process-response response-js]}]}
     (when (> (count events) 1)
       {:dispatch-n events}))))

(re-frame/reg-event-fx :contacts/contacts-loaded
 (fn [{:keys [db]} [loaded-contacts]]
   (let [contacts (->> loaded-contacts
                       (map <-rpc-js)
                       (mapv (fn [{:keys [public-key] :as contact}] [public-key contact]))
                       (into {}))]
     {:db (assoc db :contacts/contacts contacts)})))

(re-frame/reg-fx :contacts/initialize-contacts
 (fn []
   (json-rpc/call {:method      "wakuext_contacts"
                   :params      []
                   :js-response true
                   :on-success  [:contacts/contacts-loaded]
                   :on-error    [:log/error "failed to fetch contacts"]})))

(defn send-contact-request
  [{:keys [db]} [id message]]
  (when (not= id (get-in db [:profile/profile :public-key]))
    {:fx [[:json-rpc/call
           [{:method      "wakuext_sendContactRequest"
             :js-response true
             :params      [{:id id :message (or message (i18n/label :t/add-me-to-your-contacts))}]
             :on-error    [:contacts/send-contact-request-error id]
             :on-success  [:transport/message-sent]}]]]}))

(rf/reg-event-fx :contact.ui/send-contact-request send-contact-request)

(defn send-contact-request-error
  [_ [id error]]
  {:fx [[:effects.log/error
         ["Failed to send contact request"
          {:id    id
           :error error
           :event :contact.ui/send-contact-request}]]]})

(rf/reg-event-fx :contact.ui/send-contact-request-error send-contact-request-error)

(defn remove-contact
  "Remove a contact from current account's contact list"
  [{:keys [db]} [{:keys [public-key]}]]
  {:db (-> db
           (assoc-in [:contacts/contacts public-key :added?] false)
           (assoc-in [:contacts/contacts public-key :active?] false)
           (assoc-in [:contacts/contacts public-key :contact-request-state]
                     constants/contact-request-state-none))
   :fx [[:json-rpc/call
         [{:method      "wakuext_retractContactRequest"
           :params      [{:id public-key}]
           :js-response true
           :on-success  [:sanitize-messages-and-process-response]
           :on-error    [:contacts/remove-contact-error public-key]}]]]})

(rf/reg-event-fx :contact.ui/remove-contact-pressed remove-contact)

(defn remove-contact-error
  [_ [public-key error]]
  {:fx [[:effects.log/error ["failed to remove contact" public-key error]]]})

(rf/reg-event-fx :contacts/remove-contact-error remove-contact-error)

(defn update-nickname
  [_ [public-key nickname]]
  {:fx [[:json-rpc/call
         [{:method      "wakuext_setContactLocalNickname"
           :params      [{:id public-key :nickname nickname}]
           :js-response true
           :on-success  [:sanitize-messages-and-process-response]
           :on-error    [:contacts/update-nickname-error public-key nickname]}]]]})

(rf/reg-event-fx :contacts/update-nickname update-nickname)

(defn update-nickname-error
  [_ [public-key nickname error]]
  {:fx [[:effects.log/error
         ["failed to set contact nickname"
          {:public-key public-key
           :nickname   nickname}
          error]]]})

(rf/reg-event-fx :contacts/update-nickname-error update-nickname-error)
