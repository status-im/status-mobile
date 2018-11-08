(ns status-im.chat.db
  (:require [clojure.set :as set]
            [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.contact.db :as contact.db]
            [status-im.i18n :as i18n]
            [status-im.utils.config :as utils.config]
            [status-im.utils.core :as utils]
            [status-im.utils.datetime :as time]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.utils.identicon :as identicon]))

;; any message that comes after this amount of ms will be grouped separately
(def ^:private group-ms 60000)

(defn display-photo?
  [{:keys [outgoing message-type]}]
  (or (= :system-message message-type)
      (and (not outgoing)
           (not (= :user-message message-type)))))

(defn- intersperse-datemark
  "Reduce step which expects the input list of messages to be sorted by clock value.
  It makes best effort to group them by day.
  We cannot sort them by :timestamp, as that represents the clock of the sender
  and we have no guarantees on the order.
  We naively and arbitrarly group them assuming that out-of-order timestamps
  fall in the previous bucket.
  A sends M1 to B with timestamp 2000-01-01T00:00:00
  B replies M2 with timestamp    1999-12-31-23:59:59
  M1 needs to be displayed before M2
  so we bucket both in 1999-12-31"
  [{:keys [acc last-timestamp last-datemark]} {:keys [timestamp datemark] :as msg}]
  (let [initial-element? (empty? acc)
        same-day? (= last-datemark datemark)
        out-of-order? (> timestamp last-timestamp)]
    (cond
      initial-element?
      {:last-timestamp timestamp
       :last-datemark  datemark
       :acc            (conj acc msg)}

      (and (not same-day?)
           (not out-of-order?))
      {:last-timestamp timestamp
       :last-datemark  datemark
       ;; intersperse datemark message
       :acc            (conj acc {:value last-datemark
                                  :type  :datemark}
                             msg)}
      :else
      ;; use last datemark
      {:last-timestamp (max timestamp last-timestamp)
       :last-datemark  last-datemark
       :acc            (conj acc (assoc msg :datemark last-datemark))})))

(defn- add-datemark [{:keys [timestamp] :as msg}]
  (assoc msg :datemark (time/day-relative time/date-fmt timestamp)))

(defn- add-timestamp [{:keys [timestamp] :as msg}]
  (assoc msg :timestamp-str (time/timestamp->time timestamp)))

(defn get-photo-path
  [contacts account id]
  (let [photo-path (or (:photo-path (contacts id))
                       (when (= id (:public-key account))
                         (:photo-path account)))]
    (if (string/blank? photo-path)
      (identicon/identicon id)
      photo-path)))

(defn get-contact-name
  [contacts account id]
  (let [me? (= (:public-key account) id)]
    (if me?
      (:name account)
      (:name (contacts id)))))

(defn add-response-metadata
  "Selects certain data from quoted message which must be available in the view"
  [message-id messages referenced-messages contacts account]
  (when message-id
    (when-let [message (get messages
                            message-id
                            (get referenced-messages message-id))]
      (let [{:keys [from content]} message
            me? (= (:public-key account) from)]
        {:from from
         :text (:text content)
         :photo-path (get-photo-path contacts account from)
         :user-name (if me?
                      (i18n/label :t/You)
                      (:name (contacts from)))
         :generated-name (when-not me?
                           (gfycat/generate-gfy from))}))))

(defn get-group-message-delivery-status
  [{:keys [user-statuses]} participants current-public-key]
  (let [delivery-statuses (dissoc user-statuses current-public-key)
        delivery-count    (count delivery-statuses)
        seen-by-everyone? (and (= delivery-count (count participants))
                               (every? (comp (partial = :seen) :status second) delivery-statuses))]
    (cond
      seen-by-everyone? :seen-by-everyone
      (zero? delivery-count) :sent
      :default :not-seen-by-everyone)))

(defn get-delivery-status
  [{:keys [message-type outgoing content chat-id]
    :as message}
   last-outgoing?
   chat-contacts
   message-statuses
   current-public-key
   current-network]
  (let [outgoing-status (get-in message-statuses [current-public-key :status])
        delivery-status (get-in message-statuses [chat-id :status])
        status          (or delivery-status outgoing-status :not-sent)
        incoming-command? (and (not outgoing)
                               (:command content))
        command-network-mismatch? (and incoming-command?
                                       (not= (get-in content
                                                     [:command :params :network])
                                             current-network))
        system-message? (= :system-message message-type)
        group-user-message? (= message-type :group-user-message)]
    (when (not system-message?)
      (cond
        (#{:sending :not-sent} status)
        status

        command-network-mismatch?
        :network-mismatch

        last-outgoing?
        (if group-user-message?
          (get-group-message-delivery-status message
                                             chat-contacts
                                             current-public-key)
          :sent)))))

(defn get-delivery-details
  [user-statuses chat-contacts current-public-key]
  (let [delivery-statuses (set (dissoc user-statuses current-public-key))
        delivery-count    (count delivery-statuses)
        delivery-recipients (select-keys chat-contacts delivery-statuses)]
    {:delivery-recipients  delivery-recipients
     :delivery-count-label (when (> delivery-count 3)
                             (str "+ " (- delivery-count 3)))}))

(defn intersperse-datemarks
  "Add a datemark in between an ordered seq of messages when two datemarks are not
  the same. Ignore messages with out-of-order timestamps"
  [messages]
  (when (seq messages)
    (let [messages-with-datemarks (transduce (comp
                                              (map add-datemark)
                                              (map add-timestamp))
                                             (completing intersperse-datemark :acc)
                                             {:acc []}
                                             messages)]
      ;; Append last datemark
      (conj messages-with-datemarks
            {:value (:datemark (peek messages-with-datemarks))
             :type  :datemark}))))

(defn- add-message-to-stream
  "update previous message if necessary"
  [stream message previous-first-in-group?]
  (if (and stream previous-first-in-group?)
    (conj (pop stream)
          (assoc (peek stream) :first-in-group? true)
          message)
    (conj (or stream []) message)))

;;TODO separate positional metadata handling for regular metadata
(defn add-metadata
  "Reduce step which adds positional metadata to a message and conditionally
  update the previous message with :first-in-group?."
  [{:keys [chat-id messages message-statuses referenced-messages] :as chat}
   contacts
   account
   network
   {:keys [stream last-outgoing-seen]}
   {:keys [message-id type from datemark outgoing timestamp group-chat] :as message}]
  (let [chat-contacts (:contacts chat)
        previous-message         (peek stream)
        ;; Was the previous message from a different author or this message
        ;; comes after x ms
        last-in-group?           (or (not= from (:from previous-message))
                                     (> (- (:timestamp previous-message)
                                           timestamp)
                                        group-ms))
        same-direction?          (= outgoing (:outgoing previous-message))
        ;; Have we seen an outgoing message already?
        last-outgoing?           (if stream
                                   (and (not last-outgoing-seen)
                                        outgoing)
                                   outgoing)
        datemark?                (= :datemark (:type message))
        ;; If this is a datemark or this is the last-message of a group,
        ;; then the previous message was the first
        previous-first-in-group? (or datemark?
                                     last-in-group?)
        message-statuses (get message-statuses message-id)
        current-public-key (:public-key account)
        status (get-delivery-status message
                                    last-outgoing?
                                    chat-contacts
                                    message-statuses
                                    current-public-key
                                    network)
        delivery-details (when (= status :not-seen-by-everyone)
                           (get-delivery-details message-statuses
                                                 chat-contacts
                                                 current-public-key))
        unseen? (and message-id
                     chat-id
                     (not outgoing)
                     (not= :seen status)
                     (not= :seen (keyword (get-in message-statuses
                                                  [current-public-key :status]))))
        new-message (cond->
                     (-> message
                         (assoc :can-reply? (not= status :not-sent)
                                :photo-path (get-photo-path contacts account from)
                                :user-name (get-contact-name contacts account from)
                                :generated-name (gfycat/generate-gfy from)
                                :group-chat group-chat
                                :on-seen-message-fn
                                #(when unseen?
                                   (re-frame/dispatch [:send-seen! {:chat-id    chat-id
                                                                    :from       from
                                                                    :message-id message-id}])))
                         (update-in [:content :response-to]
                                    #(add-response-metadata %
                                                            messages
                                                            referenced-messages
                                                            contacts
                                                            account)))
                      (not stream)
                      (assoc :last-in-group? true
                             :last? true
                             :display-photo? (display-photo? message)
                             :last-outgoing? outgoing)

                      stream
                      (assoc :same-direction? same-direction?
                             :last-in-group? last-in-group?
                             :last-outgoing? last-outgoing?)

                      status
                      (assoc :status status)

                      delivery-details
                      (assoc :delivery-details delivery-details)

                      unseen?
                      (assoc :unseen? unseen?)

                      (not (= current-public-key from))
                      (assoc :on-press-photo-fn
                             #(re-frame/dispatch [:show-profile-desktop from])))]
    {:stream (add-message-to-stream stream new-message previous-first-in-group?)
     ;; mark the last message sent by the user
     :last-outgoing-seen (if stream
                           (or last-outgoing-seen last-outgoing?)
                           (:last-outgoing? new-message))}))

;;TODO: transduce everything?
(defn messages-stream
  "Enhances the messages in message sequence interspersed with datemarks
  with derived stream context information, like:
  `:first-in-group?`, `last-in-group?`, `:same-direction?`,
  `:last?` and `:last-outgoing?` flags."
  [{:keys [messages] :as chat}
   contacts
   account
   network]
  (let [messages-with-datemarks (-> messages
                                    vals
                                    intersperse-datemarks)]
    (when (seq messages-with-datemarks)
      (:stream (reduce (partial add-metadata
                                chat
                                contacts
                                account
                                network)
                       {:stream nil
                        :last-outgoing-seen nil}
                       messages-with-datemarks)))))

(defn chat-name [{:keys [group-chat
                         chat-id
                         public?
                         name]}
                 {contact-name :name}]
  (cond
    public?    (str "#" name)
    group-chat name
    :else      (or contact-name
                   (gfycat/generate-gfy chat-id))))

(defn- enrich-active-chat
  [{:keys [chat-id messages unviewed-messages group-chat
           public? message-statuses referenced-messages] :as chat}
   contacts
   account
   network]
  (let [{:keys [public-key tags photo-path] :as contact} (get contacts chat-id)
        messages (messages-stream chat
                                  contacts
                                  account
                                  network)
        unviewed-messages-count (count (filter #(:unseen? %) messages))
        large-unviewed-messages-label? (< 9 unviewed-messages-count)
        last-message (first messages)
        name (chat-name chat contact)
        chat-contacts (:contacts chat)]
    (cond-> chat
      tags
      (update :tags set/union tags)

      public-key
      (assoc :random-name (gfycat/generate-gfy public-key))

      (and group-chat (not public?))
      (update :contacts
              #(reduce (fn [acc public-key]
                         (assoc acc public-key
                                (contact.db/public-key->contact contacts
                                                                public-key)))
                       {} %))

      (not group-chat)
      (assoc :contact (contact.db/public-key->contact contacts public-key))

      (pos? unviewed-messages-count)
      (assoc :unviewed-messages-label (if large-unviewed-messages-label?
                                        (i18n/label :t/counter-9-plus)
                                        unviewed-messages-count))
      large-unviewed-messages-label?
      (assoc :large-unviewed-messages-label? large-unviewed-messages-label?)

      last-message (assoc :last-message last-message)
      :always (assoc :name name
                     :unviewed-messages-count unviewed-messages-count
                     :truncated-name (utils/truncate-str name 30)
                     :messages messages
                     :photo-path (or photo-path
                                     (identicon/identicon chat-id))))))

(defn- active-chat?
  [{:keys [group-chat public? is-active] :as chat}
   dev-mode?]
  (and is-active
       ;; not a group chat
       (or (not (and group-chat (not public?)))
           ;; if it's a group chat
           (utils.config/group-chats-enabled? dev-mode?))))

(defn active-chats
  [chats
   contacts
   {:keys [dev-mode?] :as account}
   network]
  (reduce (fn [acc [chat-id chat]]
            (if (active-chat? chat dev-mode?)
              (assoc acc chat-id (enrich-active-chat chat
                                                     contacts
                                                     account
                                                     network))
              acc))
          {}
          chats))
