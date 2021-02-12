(ns status-im.transport.filters.core
  "This namespace is used to handle filters loading and unloading from statusgo"
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.contact.db :as contact.db]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.mailserver.core :as mailserver]
            [status-im.mailserver.topics :as mailserver.topics]
            [status-im.multiaccounts.model :as multiaccounts.model]
            [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]))

(defn is-public-key? [k]
  (and
   (string? k)
   (string/starts-with? k "0x")))

(defn load-filters-rpc [chats on-success on-failure]
  (json-rpc/call {:method (json-rpc/call-ext-method "loadFilters")
                  :params [chats]
                  :on-success                 on-success
                  :on-failure                 on-failure}))

(defn remove-filters-rpc [chats on-success on-failure]
  (json-rpc/call {:method (json-rpc/call-ext-method "removeFilters")
                  :params [chats]
                  :on-success                 on-success
                  :on-failure                 on-failure}))

;; fx functions

(defn load-filter-fx [filters]
  {:filters/load-filters [[filters]]})

(defn remove-filter-fx [filters]
  (when (seq filters)
    {:filters/remove-filters [filters]}))

;; dispatches

(defn filters-added! [filters]
  (re-frame/dispatch [:filters.callback/filters-added filters]))

(defn filters-removed! [filters]
  (re-frame/dispatch [:filters.callback/filters-removed filters]))

;; Mailserver topics

(fx/defn upsert-mailserver-topic
  "Update the topics with the newly created filter"
  [cofx {:keys [discovery?
                negotiated?
                filter-id
                topic
                chat-id
                filter]}]
  (mailserver.topics/upsert cofx {:topic    topic
                                  :negotiated? negotiated?
                                  :filter-ids #{filter-id}
                                  :discovery? discovery?
                                  :chat-ids #{chat-id}}))

;; Every time we load a filter, we want to update group chats where the user is a member, has it might be a negotiated filter, so should be included in the request topics
(fx/defn upsert-group-chat-topics
  "Update topics for each member of the group chat"
  [{:keys [db] :as cofx}]
  (let [group-chats (filter (fn [{:keys [chat-type]}]
                              (= chat-type constants/private-group-chat-type))
                            (vals (:chats db)))]
    (apply fx/merge
           cofx
           (map
            #(mailserver.topics/upsert-group-chat (:chat-id %) (:members-joined %))
            group-chats))))

;; Filter db


;; We use two structures for filters:
;; filter/filters -> {"filter-id" filter} which is just a map of filters indexed by filte-id
;; filter/chat-ids -> which is a set of loaded chat-ids, which is set everytime we load
;; a non negotiated filter for a chat.

(defn loaded?
  "Given a filter, check if we already loaded it"
  [db {:keys [filter-id]}]
  (get-in db [:filter/filters filter-id]))

(def not-loaded?
  (complement loaded?))

(defn chat-loaded?
  [db chat-id]
  (get-in db [:filter/chat-ids chat-id]))

(defn new-filters? [db filters]
  (some
   (partial not-loaded? db)
   filters))

(fx/defn set-raw-filter
  "Update filter ids cached and set filter in the db"
  [{:keys [db]} {:keys [chat-id negotiated? filter-id] :as filter}]
  {:db (cond-> (assoc-in db [:filter/filters filter-id] filter)
         ;; We only set non negotiated filters as negotiated filters are not to
         ;; be removed
         (not negotiated?)
         (update :filter/chat-ids (fnil conj #{}) chat-id))})

(fx/defn unset-raw-filter
  "Remove filter from db and from chat-id"
  [{:keys [db]} {:keys [chat-id filter-id]}]
  {:db (-> db
           (update :filter/chat-ids disj chat-id)
           (update :filter/filters dissoc filter-id))})

(fx/defn add-filter-to-db
  "Set the filter in the db and upsert a mailserver topic"
  [{:keys [db] :as cofx} filter]
  (when (and (not (loaded? db filter))
             (not (:ephemeral? filter))
             (:listen? filter))
    (fx/merge cofx
              (set-raw-filter filter)
              (upsert-mailserver-topic filter))))

(fx/defn remove-filter-from-db
  "Remve the filter from the db"
  [cofx filter]
  (fx/merge cofx
            (unset-raw-filter filter)))

(fx/defn add-filters-to-db [cofx filters]
  (apply fx/merge cofx (map add-filter-to-db filters)))

(fx/defn remove-filters-from-db [cofx filters]
  (apply fx/merge cofx (map remove-filter-from-db filters)))

(defn non-negotiated-filters-for-chat-id
  "Returns all the non-negotiated filters matching chat-id"
  [db chat-id]
  (filter
   (fn [{:keys [negotiated?] :as f}]
     (and (= chat-id
             (:chat-id f))
          (not negotiated?)))
   (vals (:filter/filters db))))

;; Filter requests

(defn- ->remove-filter-request
  [{:keys [id filter-id]}]
  {:chatId id
   :filterId filter-id})

(defn- ->filter-request
  "Transform input in a filter-request. For a group chat we need a filter-request
  for each member."
  [{:keys [chat-id
           group-chat
           members-joined
           timeline?
           public?]}]
  (cond
    ;;ignore timeline chats
    timeline?
    nil
    (not group-chat)
    ;; Some legacy one-to-one chats (bots), have not a public key for id, we exclude those
    (when (is-public-key? chat-id)
      [{:ChatID chat-id
        :OneToOne true
        :Identity (subs chat-id 2)}])
    public?
    [{:ChatID chat-id
      :OneToOne false}]
    :else
    (mapcat #(->filter-request {:chat-id %}) members-joined)))

(defn- chats->filter-requests
  "Convert a list of active chats to filter requests"
  [chats]
  (->> chats
       (filter :is-active)
       (mapcat ->filter-request)))

(defn- contacts->filter-requests
  "Convert added contacts to filter requests"
  [contacts]
  (->> contacts
       (filter contact.db/added?)
       (map #(hash-map :chat-id (:public-key %)))
       (mapcat ->filter-request)))

;; shh filters

(defn responses->filters [{:keys [negotiated
                                  discovery
                                  filterId
                                  chatId
                                  listen
                                  ephemeral
                                  topic
                                  identity]}]
  {:chat-id (if (not= identity "") (str "0x" identity) chatId)
   :id chatId
   :filter-id filterId
   :negotiated? negotiated
   :ephemeral? ephemeral
   :listen? listen
   :discovery? discovery
   :topic topic})

(defn messenger-started? [db]
  (:messenger/started? db))

(fx/defn handle-filters-added
  "Called every time we load a filter from statusgo, either from explicit call
  or through signals. It stores the filter in the db and upsert the relevant
  mailserver topics."
  {:events [:filters.callback/filters-added]}
  [{:keys [db] :as cofx} filters]
  (fx/merge cofx
            (mailserver/reset-request-to)
            (add-filters-to-db filters)
            (upsert-group-chat-topics)
            (when (new-filters? db filters)
              (mailserver/process-next-messages-request))))

(fx/defn handle-filters [cofx filters]
  (handle-filters-added cofx (map responses->filters filters)))

(fx/defn handle-loaded-filter [cofx filter]
  (when (and (not (:ephemeral? filter))
             (:listen? filter))
    (set-raw-filter cofx filter)))

(fx/defn handle-loaded-filters [cofx filters]
  (let [processed-filters (map responses->filters filters)]
    (apply fx/merge cofx (map handle-loaded-filter processed-filters))))

(fx/defn handle-filters-removed
  "Called when we remove a filter from status-go, it will update the mailserver
  topics"
  {:events [:filters.callback/filters-removed]}
  [cofx filters]
  (fx/merge cofx
            (remove-filters-from-db filters)
            (mailserver.topics/delete-many filters)
            (mailserver/process-next-messages-request)))

;; Public functions

(fx/defn handle-negotiated-filter
  "Check if it's a new filter, if so create an shh filter and process it"
  [{:keys [db] :as cofx} {:keys [filters]}]
  (let [processed-filters (map #(responses->filters (assoc % :negotiated true)) filters)
        new-filters     (filter
                         (partial not-loaded? db)
                         processed-filters)]
    (when (seq new-filters)
      {:filters/add-raw-filters
       {:filters new-filters}})))

;; Load functions: utility function to load filters

(fx/defn load-chat
  "Check if a filter already exists for that chat, otherw load the filter"
  [{:keys [db] :as cofx} chat-id]
  (when (and (messenger-started? db)
             (not (chat-loaded? db chat-id)))
    (let [chat (get-in db [:chats chat-id])]
      (load-filter-fx (->filter-request chat)))))

(fx/defn load-chats
  "Check if a filter already exists for that chat, otherw load the filter"
  [{:keys [db] :as cofx} chats]
  (let [chats (filter #(chat-loaded? db (:chat-id %)) chats)]
    (when (and (messenger-started? db) (seq chats))
      (load-filter-fx (chats->filter-requests chats)))))

(fx/defn load-contact
  "Check if we already have a filter for that contact, otherwise load the filter
  if the contact has been added"
  [{:keys [db] :as cofx} contact]
  (when-not (chat-loaded? db (:public-key contact))
    (load-filter-fx (contacts->filter-requests [contact]))))

(fx/defn load-member
  "Check if we already have a filter for that member, otherwise load the filter, regardless of whether is in our contacts"
  [{:keys [db] :as cofx} public-key]
  (when-not (chat-loaded? db public-key)
    (load-filter-fx (->filter-request {:chat-id public-key}))))

(fx/defn load-members
  "Load multiple members"
  [cofx members]
  (apply fx/merge cofx (map load-member members)))

(fx/defn stop-listening
  "We can stop listening to contact codes when we don't have any active chat
  with the user (one-to-one or group-chat), and it is not in our contacts"
  [{:keys [db] :as cofx} chat-id]
  (let [my-public-key (multiaccounts.model/current-public-key cofx)
        one-to-one?   (not (get-in db [:chats chat-id :group-chat]))
        public?       (get-in db [:chats chat-id :public?])
        active-group-chats (filter (fn [{:keys [is-active members members-joined]}]
                                     (and is-active
                                          (contains? members-joined my-public-key)
                                          (contains? members chat-id)))
                                   (vals (:chats db)))]
    (when (or public?
              (and one-to-one?
                   (not (contact.db/active? db chat-id))
                   (not= my-public-key chat-id)
                   (not (get-in db [:chats chat-id :is-active]))
                   (empty? active-group-chats)))
      (fx/merge cofx
                ;; we exclude the negotiated filters as those are not to be removed
                ;; otherwise we might miss messages
                (remove-filter-fx
                 (non-negotiated-filters-for-chat-id db chat-id))))))

(re-frame/reg-fx
 :filters/add-raw-filters
 (fn [{:keys [filters]}]
   (log/debug "PERF" :filters/add-raw-filters)
   (filters-added! filters)))

;; Here we stop first polling and then we hit status-go, otherwise it would throw
;; an error trying to poll from a delete filter. If we fail to remove the filter though
;; we should recreate it.
(re-frame/reg-fx
 :filters/remove-filters
 (fn [[filters]]
   (log/debug "removing filters" filters)
   (remove-filters-rpc
    (map ->remove-filter-request filters)
    #(filters-removed! filters)
    #(log/error "remove-filters: failed error" %))))

(re-frame/reg-fx
 :filters/load-filters
 (fn [raw-filters]
   (let [all-filters (mapcat first raw-filters)]
     (load-filters-rpc
      all-filters
      #(filters-added! (map responses->filters %))
      #(log/error "load-filters: failed error" %)))))
