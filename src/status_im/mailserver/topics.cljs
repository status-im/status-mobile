(ns ^{:doc "Mailserver events and API"}
 status-im.mailserver.topics
  (:require
   [clojure.string :as string]
   [taoensso.timbre :as log]
   [status-im.data-store.mailservers :as data-store.mailservers]
   [status-im.mailserver.constants :as constants]
   [status-im.utils.fx :as fx]))

(defn calculate-last-request [{:keys [discovery?]}
                              {:keys [previous-last-request
                                      now-s]}]
  ;; New topic, if discovery we don't fetch history
  (if (and (nil? previous-last-request)
           discovery?)
    (- now-s 10)
    (max previous-last-request
         (- now-s constants/max-request-range))))

(fx/defn store [{:keys [db]} {:keys [topic
                                     filter-ids
                                     chat-ids] :as mailserver-topic}]
  (if (or (empty? chat-ids)
          (empty? filter-ids))
    {:db (update db :mailserver/topics dissoc topic)}
    {:db (assoc-in db [:mailserver/topics topic] mailserver-topic)}))

(fx/defn persist [_ {:keys [chat-ids topic filter-ids]
                     :as mailserver-topic}]
  (let [op (if (or (empty? chat-ids)
                   (empty? filter-ids))
             (data-store.mailservers/delete-mailserver-topic-tx topic)
             (data-store.mailservers/save-mailserver-topic-tx
              {:topic            topic
               :mailserver-topic mailserver-topic}))]
    {:data-store/tx [op]}))

(defn new-chat-ids? [previous-mailserver-topic new-mailserver-topic]
  (seq (clojure.set/difference (:chat-ids new-mailserver-topic)
                               (:chat-ids previous-mailserver-topic))))

(defn new-filter-ids? [previous-mailserver-topic new-mailserver-topic]
  (seq (clojure.set/difference (:chat-ids new-mailserver-topic)
                               (:chat-ids previous-mailserver-topic))))

(defn merge-topic
  "Calculate last-request and merge chat-ids keeping the old ones and new ones"
  [old-mailserver-topic
   new-mailserver-topic
   {:keys [now-s]}]
  (let [last-request (calculate-last-request
                      new-mailserver-topic
                      {:previous-last-request (:last-request old-mailserver-topic)
                       :now-s now-s})]
    (-> old-mailserver-topic
        (assoc
         :topic        (:topic new-mailserver-topic)
         :discovery?   (boolean (:discovery? new-mailserver-topic))
         :negotiated?  (boolean (:negotiated? new-mailserver-topic))
         :last-request last-request)
        (update :filter-ids
                clojure.set/union
                (set (:filter-ids new-mailserver-topic)))
        (update :chat-ids
                clojure.set/union
                (set (:chat-ids new-mailserver-topic))))))

(fx/defn update-topic [cofx persist? topic]
  (fx/merge cofx
            (store topic)
            (when persist? (persist topic))))

(fx/defn upsert
  "if the topic didn't exist
  create the topic
  else if chat-id is not in the topic
  add the chat-id to the topic and reset last-request
  there was no filter for the chat and messages for that
  so the whole history for that topic needs to be re-fetched"
  [{:keys [db now] :as cofx} new-mailserver-topic]
  (let [old-mailserver-topic (get-in db [:mailserver/topics (:topic new-mailserver-topic)]
                                     {:topic (:topic new-mailserver-topic)
                                      :filter-ids #{}
                                      :chat-ids #{}})]
    (let [updated-topic (merge-topic old-mailserver-topic
                                     new-mailserver-topic
                                     {:now-s (quot now 1000)})]
      (update-topic cofx
                    (new-chat-ids? old-mailserver-topic
                                   new-mailserver-topic)
                    updated-topic))))

(fx/defn upsert-many [cofx mailserver-topics]
  (apply fx/merge cofx (map upsert mailserver-topics)))

(fx/defn update-many [cofx mailserver-topics]
  (apply fx/merge cofx (map (partial update-topic true) mailserver-topics)))

(fx/defn delete [{:keys [db] :as cofx} {:keys [chat-id filter-id]}]
  (when-let [matching-topics (filter (fn [{:keys [filter-ids] :as topic}]
                                       (if (not filter-ids)
                                         (do (log/warn "topic not initialized, removing" topic)
                                             true)
                                         (filter-ids filter-id)))
                                     (vals (:mailserver/topics db)))]
    (update-many  cofx (map #(update % :filter-ids disj filter-id) matching-topics))))

(fx/defn delete-many
  "Remove filter-ids from any topics and save"
  [cofx filters]
  (apply fx/merge cofx (map delete filters)))

(defn extract-topics
  "return all the topics for this chat, including discovery topics if specified"
  [topics chat-id include-discovery?]
  (reduce-kv
   (fn [acc topic {:keys [discovery? chat-ids]}]
     (if (or (and discovery?
                  include-discovery?)
             (chat-ids chat-id))
       (conj acc topic)
       acc))
   #{}
   topics))

(defn changed-for-group-chat
  "Returns all the discovery topics, or those topics that have at least one of the members.
  Returns those topic that had chat-id but the member is not there anymore"
  [topics chat-id members]
  (reduce
   (fn [acc {:keys [discovery? chat-ids] :as topic}]
     (cond (some chat-ids members)
           (update acc :modified conj
                   (assoc topic
                          :chat-ids #{chat-id}))
           (and
            (chat-ids chat-id)
            (not-any? chat-ids members))
           (update acc :removed conj
                   (-> topic
                       (assoc :topic (:topic topic))
                       (update :chat-ids disj chat-id)))
           :else
           acc))
   {:modified [] :removed []}
   topics))

(fx/defn upsert-group-chat
  "Based on the members it will upsert a mailserver topic for any discovery topic
  and any personal topic that is in members. It will also remove the chat-id from any existing topic if not with a member"
  [{:keys [db] :as cofx} chat-id members]
  (let [topics (reduce-kv
                (fn [acc topic-id topic]
                  (conj acc (assoc topic :topic topic-id)))
                []
                (:mailserver/topics db))
        {:keys [modified
                removed]} (changed-for-group-chat
                           topics
                           chat-id
                           members)]
    (fx/merge cofx
              (upsert-many modified)
              (update-many removed))))

(defn topics-for-chat [db chat-id]
  (extract-topics (:mailserver/topics db)
                  chat-id
                  (not (get-in (:chats db) [chat-id :public?]))))

(defn topics-for-current-chat
  "return a list of topics used by the current-chat, include discovery if
  private group chat or one-to-one"
  [{:keys [current-chat-id] :as db}]
  (topics-for-chat db current-chat-id))
