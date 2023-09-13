(ns status-im2.contexts.contacts.events
  (:require
    [oops.core :as oops]
    [status-im.utils.types :as types]
    [status-im2.constants :as constants]
    [taoensso.timbre :as log]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn <-rpc-js
  [^js js-contact]
  {:public-key            (oops/oget js-contact "id")
   :compressed-key        (oops/oget js-contact "compressedKey")
   :primary-name          (oops/oget js-contact "primaryName")
   :secondary-name        (.-secondaryName js-contact)
   :ens-name              (.-name js-contact)
   :nickname              (.-localNickname js-contact)
   :images                (types/js->clj (oops/oget js-contact "images"))
   :ens-verified          (oops/oget js-contact "ensVerified")
   :contact-request-state (oops/oget js-contact "contactRequestState")
   :last-updated          (oops/oget js-contact "lastUpdated")
   :active?               (oops/oget js-contact "active")
   :blocked?              (oops/oget js-contact "blocked")
   :added?                (oops/oget js-contact "added")
   :has-added-us?         (oops/oget js-contact "hasAddedUs")
   :mutual?               (oops/oget js-contact "mutual")})

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

(rf/defn initialize-contacts
  [_]
  {:json-rpc/call [{:method      "wakuext_contacts"
                    :params      []
                    :js-response true
                    :on-success  #(rf/dispatch [:contacts/contacts-loaded (map <-rpc-js %)])
                    :on-error    #(log/error "failed to fetch contacts" %)}]})

(rf/defn contacts-loaded
  {:events [:contacts/contacts-loaded]}
  [{:keys [db]} contacts]
  {:db (assoc db :contacts/contacts (into {} (map #(vector (:public-key %) %) contacts)))})

(rf/defn send-contact-request
  {:events [:contact.ui/send-contact-request]}
  [{:keys [db]} id]
  (when (not= id (get-in db [:profile/profile :public-key]))
    {:json-rpc/call
     [{:method      "wakuext_sendContactRequest"
       :js-response true
       :params      [{:id id :message (i18n/label :t/add-me-to-your-contacts)}]
       :on-error    (fn [error]
                      (log/error "Failed to send contact request"
                                 {:error error
                                  :event :contact.ui/send-contact-request
                                  :id    id}))
       :on-success  #(rf/dispatch [:transport/message-sent %])}]}))

(rf/defn remove-contact
  "Remove a contact from current account's contact list"
  {:events [:contact.ui/remove-contact-pressed]}
  [{:keys [db]} {:keys [public-key]}]
  {:db            (-> db
                      (assoc-in [:contacts/contacts public-key :added?] false)
                      (assoc-in [:contacts/contacts public-key :active?] false)
                      (assoc-in [:contacts/contacts public-key :contact-request-state]
                                constants/contact-request-state-none))
   :json-rpc/call [{:method      "wakuext_retractContactRequest"
                    :params      [{:id public-key}]
                    :js-response true
                    :on-success  #(rf/dispatch [:sanitize-messages-and-process-response %])
                    :on-error    #(log/error "failed to remove contact" public-key %)}]})

(rf/defn update-nickname
  {:events [:contacts/update-nickname]}
  [_ public-key nickname]
  {:json-rpc/call [{:method      "wakuext_setContactLocalNickname"
                    :params      [{:id public-key :nickname nickname}]
                    :js-response true
                    :on-success  #(rf/dispatch [:sanitize-messages-and-process-response %])
                    :on-error    #(log/error "failed to set contact nickname " public-key nickname %)}]})

(rf/defn block
  [_ contact-id on-success]
  {:json-rpc/call [{:method      "wakuext_blockContact"
                    :params      [contact-id]
                    :js-response true
                    :on-success  on-success
                    :on-error    #(log/error "failed to block contact" % contact-id)}]})

(rf/defn unblock
  [_ contact-id on-success]
  {:json-rpc/call [{:method      "wakuext_unblockContact"
                    :params      [contact-id]
                    :on-success  on-success
                    :js-response true
                    :on-error    #(log/error "failed to unblock contact" % contact-id)}]})
