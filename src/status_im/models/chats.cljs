(ns status-im.models.chats
  (:require [clojure.set :refer [difference]]
            [re-frame.core :refer [dispatch]]
            [status-im.persistence.realm.core :as r]
            [status-im.utils.random :as random :refer [timestamp]]
            [clojure.string :refer [join blank?]]
            [status-im.constants :refer [content-type-status]]
            [status-im.models.messages :refer [save-message]]
            [status-im.persistence.realm-queries :refer [include-query]]))

(defn chat-name-from-contacts [identities]
  (let [chat-name (->> identities
                       (map (fn [identity]
                              (-> (r/get-by-field :account :contact :whisper-identity identity)
                                  (r/single-cljs)
                                  :name)))
                       (filter identity)
                       (join ","))]
    (when-not (blank? chat-name)
      chat-name)))

(defn get-chat-name [chat-id identities]
  (or (chat-name-from-contacts identities)
      chat-id))

(defn chat-exists? [chat-id]
  (r/exists? :account :chat {:chat-id chat-id}))

(defn add-status-message [chat-id]
  ;; TODO Get real status
  (save-message chat-id
                {:from         "Status"
                 :to           nil
                 :message-id   (random/id)
                 :content      (str "The brash businessman’s braggadocio "
                                    "and public exchange with candidates "
                                    "in the US presidential election")
                 :content-type content-type-status
                 :outgoing     false}))

(defn chat-contacts [chat-id]
  (-> (r/get-by-field :account :chat :chat-id chat-id)
      (r/single)
      (aget "contacts")))

(defn re-join-group-chat [db group-id identities group-name]
  (r/write :account
           (fn []
             (let [new-identities (set identities)
                   only-old-contacts (->> (chat-contacts group-id)
                                          (r/cljs-list)
                                          (remove (fn [{:keys [identity]}]
                                                    (new-identities identity))))
                   contacts (->> new-identities
                                 (mapv (fn [ident]
                                         {:identity ident}))
                                 (concat only-old-contacts))]
               (r/create :account :chat
                         {:chat-id   group-id
                          :is-active true
                          :name      group-name
                          :contacts  contacts} true))))
  db)

(defn normalize-contacts
  [chats]
  (map #(update % :contacts vals) chats))

(defn chats-list []
  (-> (r/get-by-field :account :chat :is-active true)
      (r/sorted :timestamp :desc)
      r/realm-collection->list
      normalize-contacts))

(defn chat-by-id [chat-id]
  (-> (r/get-by-field :account :chat :chat-id chat-id)
      (r/single-cljs)
      (r/list-to-array :contacts)))

(defn update-chat [{:keys [last-message-id chat-id] :as chat}]
  (let [{old-chat-id :chat-id
         :as         old-chat} (chat-by-id chat-id)]
    (when old-chat-id
      (let [chat (-> (merge old-chat chat)
                     (assoc chat :last-message-id (or last-message-id "")))]
        (r/write :account #(r/create :account :chat chat true))))))

(defn create-chat
  ([{:keys [last-message-id] :as chat}]
   (let [chat (assoc chat :last-message-id (or last-message-id ""))]
     (r/write :account #(r/create :account :chat chat true)))))

(defn chat-add-participants [chat-id identities]
  (r/write :account
           (fn []
             (let [contacts (chat-contacts chat-id)
                   added-at (timestamp)]
               (doseq [contact-identity identities]
                 (if-let [contact (.find contacts (fn [object index collection]
                                                    (= contact-identity (aget object "identity"))))]
                   (doto contact
                     (aset "is-in-chat" true)
                     (aset "added-at" added-at))
                   (.push contacts (clj->js {:identity contact-identity
                                             :added-at added-at})))))))
  ;; TODO temp. Update chat in db atom
  (dispatch [:initialize-chats]))

(defn chat-remove-participants [chat-id identities]
  (r/write :account
           (fn []
             (let [query (include-query :identity identities)
                   chat (r/single (r/get-by-field :account :chat :chat-id chat-id))]
               (-> (aget chat "contacts")
                   (r/filtered query)
                   (.forEach (fn [object _ _]
                               (r/delete :account object))))))))

(defn- groups [active?]
  (r/filtered (r/get-all :account :chat)
              (str "group-chat = true && is-active = "
                   (if active? "true" "false"))))

(defn active-group-chats []
  (map
    (fn [{:keys [chat-id public-key private-key]}]
      {:chat-id chat-id
       :keypair {:private private-key
                 :public  public-key}})
    (r/realm-collection->list (groups true))))

(defn set-chat-active [chat-id active?]
  (r/write :account
           (fn []
             (-> (r/get-by-field :account :chat :chat-id chat-id)
                 (r/single)
                 (aset "is-active" active?)))))

(defn get-property [chat-id property]
  (when-let [chat (r/single (r/get-by-field :account :chat :chat-id chat-id))]
    (aget chat (name property))))

(defn is-active? [chat-id]
  (get-property chat-id :is-active))

(defn removed-at [chat-id]
  (get-property chat-id :removed-at))

(defn contact [chat-id id]
  (let [contacts (r/cljs-list (chat-contacts chat-id))]
    (some (fn [{:keys [identity]}]
            (= id identity))
          contacts)))
