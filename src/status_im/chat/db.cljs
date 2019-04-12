(ns status-im.chat.db
  (:require [clojure.set :as clojure.set]
            [clojure.string :as string]
            [status-im.contact.db :as contact.db]
            [status-im.chat.commands.core :as commands]
            [status-im.chat.commands.input :as commands.input]
            [status-im.group-chats.db :as group-chats.db]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.transport.partitioned-topic :as topic]))

(defn group-chat-name
  [{:keys [public? name]}]
  (str (when public? "#") name))

(defn enrich-active-chat
  [contacts {:keys [chat-id public? group-chat name] :as chat} current-public-key]
  (if group-chat
    (let [pending-invite-inviter-name
          (group-chats.db/get-pending-invite-inviter-name contacts
                                                          chat
                                                          current-public-key)
          inviter-name
          (group-chats.db/get-inviter-name contacts
                                           chat
                                           current-public-key)]
      (cond-> chat
        pending-invite-inviter-name
        (assoc :pending-invite-inviter-name pending-invite-inviter-name)
        inviter-name
        (assoc :inviter-name inviter-name)
        :always
        (assoc :chat-name (group-chat-name chat))))
    (let [{contact-name :name :as contact}
          (get contacts chat-id
               (contact.db/public-key->new-contact chat-id))
          random-name (gfycat/generate-gfy chat-id)]
      (-> chat
          (assoc :contact contact
                 :chat-name (or contact-name random-name)
                 :name contact-name
                 :random-name random-name)
          (update :tags clojure.set/union (:tags contact))))))

(defn active-chats
  [contacts chats {:keys [public-key]}]
  (reduce (fn [acc [chat-id {:keys [is-active] :as chat}]]
            (if is-active
              (assoc acc chat-id (enrich-active-chat contacts chat public-key))
              acc))
          {}
          chats))

(defn topic-by-current-chat
  [{:keys [current-chat-id chats] :as db}]
  (let [{:keys [public?]} (get chats current-chat-id)
        public-key (get-in db [:account/account :public-key])]
    (if public?
      (get-in db [:transport/chats current-chat-id :topic])
      (topic/public-key->discovery-topic-hash public-key))))

(defn messages-gap
  [mailserver-topics topic]
  (let [{:keys [gap-from gap-to]}
        (get mailserver-topics topic)]
    {:from    gap-from
     :to      gap-to
     :exists? (and gap-from gap-to
                   (> gap-to gap-from))}))

(defn sort-message-groups
  "Sorts message groups according to timestamp of first message in group"
  [message-groups messages]
  (sort-by
   (comp :timestamp (partial get messages) :message-id first second)
   message-groups))

(defn quoted-message-data
  "Selects certain data from quoted message which must be available in the view"
  [message-id messages referenced-messages]
  (when-let [{:keys [from content]} (get messages message-id
                                         (get referenced-messages message-id))]
    {:from from
     :text (:text content)}))

(defn add-datemark
  [[datemark
    message-references]]
  (let [{:keys [whisper-timestamp timestamp]} (first message-references)]
    (conj message-references
          {:value             datemark
           :type              :datemark
           :whisper-timestamp whisper-timestamp
           :timestamp         timestamp})))

(defn datemark? [{:keys [type]}]
  (= type :datemark))

(defn transform-message
  [messages message-statuses referenced-messages]
  (fn [{:keys [message-id timestamp-str] :as reference}]
    (if (datemark? reference)
      reference
      (let [{:keys [content] :as message} (get messages message-id)
            {:keys [response-to response-to-v2]} content
            quote (some-> (or response-to-v2 response-to)
                          (quoted-message-data messages referenced-messages))]
        (cond-> (-> message
                    (update :content dissoc :response-to :response-to-v2)
                    (assoc :timestamp-str timestamp-str
                           :user-statuses (get message-statuses message-id)))
          ;; quoted message reference
          quote
          (assoc-in [:content :response-to] quote))))))

(defn check-gap
  [{:keys [exists? from]} previous-message next-message gap-added?]
  (let [previous-timestamp (:whisper-timestamp previous-message)
        next-whisper-timestamp     (:whisper-timestamp next-message)
        next-timestamp (quot (:timestamp next-message) 1000)
        ignore-next-message? (> (js/Math.abs
                                 (- next-whisper-timestamp next-timestamp))
                                120)]
    (and (not gap-added?)
         (or (and exists? previous-timestamp next-whisper-timestamp
                  (not ignore-next-message?)
                  (< previous-timestamp from next-whisper-timestamp))
             (and exists? (nil? next-message))))))

(defn add-gap [messages gap]
  (conj messages
        {:type  :gap
         :value (str (:from gap))}))

(defn messages-with-datemarks-and-statuses
  "Converts message groups into sequence of messages interspersed with datemarks,
  with correct user statuses associated into message"
  [message-groups messages message-statuses referenced-messages messages-gap]
  (transduce
   (comp
    (mapcat add-datemark)
    (map (transform-message messages message-statuses referenced-messages)))
   (completing
    (fn [{:keys [messages datemark-reference previous-message gap-added?]}
         message]
      (let [new-datemark? (datemark? message)
            add-gap?      (check-gap messages-gap previous-message message gap-added?)]
        {:messages           (cond-> messages

                               add-gap?
                               (add-gap messages-gap)

                               :always
                               (conj
                                (cond-> message
                                  (not new-datemark?)
                                  (assoc
                                   :datemark
                                   (:value datemark-reference)))))
         :previous-message   message
         :datemark-reference (if new-datemark?
                               message
                               datemark-reference)
         :gap-added?         (or gap-added? add-gap?)}))
    (fn [{:keys [messages previous-message gap-added?]}]
      (let [add-gap? (check-gap messages-gap previous-message nil gap-added?)]
        (cond-> messages
          add-gap?
          (add-gap messages-gap)))))
   {:messages         (list)
    :previous-message nil}
   message-groups))

(defn- set-previous-message-info [stream]
  (let [{:keys [display-photo? message-type] :as previous-message} (peek stream)]
    (conj (pop stream) (assoc previous-message
                              :display-username? (and display-photo?
                                                      (not= :system-message message-type))
                              :first-in-group?   true))))

(defn display-photo? [{:keys [outgoing message-type]}]
  (or (= :system-message message-type)
      (and (not outgoing)
           (not (= :user-message message-type)))))

;; any message that comes after this amount of ms will be grouped separately
(def ^:private group-ms 60000)

(defn add-positional-metadata
  "Reduce step which adds positional metadata to a message and conditionally
  update the previous message with :first-in-group?."
  [{:keys [stream last-outgoing-seen]}
   {:keys [type message-type from datemark outgoing timestamp] :as message}]
  (let [previous-message         (peek stream)
        ;; Was the previous message from a different author or this message
        ;; comes after x ms
        last-in-group?           (or (= :system-message message-type)
                                     (not= from (:from previous-message))
                                     (> (- (:timestamp previous-message) timestamp) group-ms))
        same-direction?          (= outgoing (:outgoing previous-message))
        ;; Have we seen an outgoing message already?
        last-outgoing?           (and (not last-outgoing-seen)
                                      outgoing)
        datemark?                (= :datemark (:type message))
        ;; If this is a datemark or this is the last-message of a group,
        ;; then the previous message was the first
        previous-first-in-group? (or datemark?
                                     last-in-group?)
        new-message              (assoc message
                                        :display-photo?  (display-photo? message)
                                        :same-direction? same-direction?
                                        :last-in-group?  last-in-group?
                                        :last-outgoing?  last-outgoing?)]
    {:stream             (cond-> stream
                           previous-first-in-group?
                           ;; update previous message if necessary
                           set-previous-message-info

                           :always
                           (conj new-message))
     ;; mark the last message sent by the user
     :last-outgoing-seen (or last-outgoing-seen last-outgoing?)}))

(defn messages-stream
  "Enhances the messages in message sequence interspersed with datemarks
  with derived stream context information, like:
  `:first-in-group?`, `last-in-group?`, `:same-direction?`, `:last?` and `:last-outgoing?` flags."
  [ordered-messages]
  (when (seq ordered-messages)
    (let [initial-message (first ordered-messages)
          message-with-metadata (assoc initial-message
                                       :last-in-group? true
                                       :last? true
                                       :display-photo? (display-photo? initial-message)
                                       :last-outgoing? (:outgoing initial-message))]
      (->> (rest ordered-messages)
           (reduce add-positional-metadata
                   {:stream             [message-with-metadata]
                    :last-outgoing-seen (:last-outgoing? message-with-metadata)})
           :stream))))

(def map->sorted-seq
  (comp (partial map second) (partial sort-by first)))

(defn available-commands
  [commands {:keys [input-text]}]
  (->> commands
       map->sorted-seq
       (filter (fn [{:keys [type]}]
                 (when (commands.input/starts-as-command? input-text)
                   (string/includes? (commands/command-name type) input-text))))))
