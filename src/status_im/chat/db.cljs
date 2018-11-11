(ns status-im.chat.db
  (:require [clojure.set :as set]
            [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.utils.config :as utils.config]
            [status-im.utils.core :as utils]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.utils.identicon :as identicon]))

(defn filter-messages-from-blocked-contacts
  [messages blocked-contacts]
  (reduce (fn [acc [message-id {:keys [from] :as message}]]
            (if (blocked-contacts from)
              acc
              (assoc acc message-id message)))
          {}
          messages))

(defn quoted-message-data
  "Selects certain data from quoted message which must be available in the view"
  [message-id messages referenced-messages]
  (when-let [{:keys [from content]} (get messages message-id
                                         (get referenced-messages message-id))]
    {:from from
     :text (:text content)}))

(defn messages-with-datemarks-and-statuses
  "Converts message groups into sequence of messages interspersed with datemarks,
  with correct user statuses associated into message"
  [message-groups messages message-statuses referenced-messages]
  (mapcat (fn [[datemark message-references]]
            (into (list {:value datemark
                         :type  :datemark})
                  (map (fn [{:keys [message-id timestamp-str]}]
                         (let [{:keys [content] :as message} (get messages message-id)
                               quote (some-> (:response-to content)
                                             (quoted-message-data messages referenced-messages))]
                           (cond-> (-> message
                                       (update :content dissoc :response-to)
                                       (assoc :datemark      datemark
                                              :timestamp-str timestamp-str
                                              :user-statuses (get message-statuses message-id)))
                             quote ;; quoted message reference
                             (assoc-in [:content :response-to] quote)))))
                  message-references))
          message-groups))

(defn- set-previous-message-info
  [stream]
  (let [{:keys [display-photo? message-type] :as previous-message} (peek stream)]
    (conj (pop stream) (assoc previous-message
                              :display-username? (and display-photo?
                                                      (not= :system-message message-type))
                              :first-in-group?   true))))

(defn display-photo?
  [{:keys [outgoing message-type]}]
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
                           ;; update previuous message if necessary
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
  [response contacts account]
  (when response
    (let [{:keys [from]}  response
          me? (= (:public-key account) from)]
      (assoc response
             :photo-path (get-photo-path contacts account from)
             :user-name (if me?
                          (i18n/label :t/You)
                          (:name (contacts from)))
             :generated-name (when-not me?
                               (gfycat/generate-gfy from))))))

(defn add-metadata
  [{:keys [from response-to message-id chat-id outgoing message-status user-statuses] :as message} contacts account]
  (let [current-public-key (:public-key account)]
    (-> message
        (assoc :can-reply? (not= (get-in user-statuses [current-public-key :status])
                                 :not-sent)
               :on-seen-message-fn #(when (and message-id
                                               chat-id
                                               (not outgoing)
                                               (not= :seen message-status)
                                               (not= :seen (keyword (get-in user-statuses [current-public-key :status]))))
                                      (re-frame/dispatch [:send-seen! {:chat-id    chat-id
                                                                       :from       from
                                                                       :message-id message-id}]))
               :on-press-photo-fn #(when-not (= current-public-key from)
                                     (re-frame/dispatch [:show-profile-desktop from]))
               :photo-path (get-photo-path contacts account from)
               :user-name (get-contact-name contacts account from)
               :generated-name (gfycat/generate-gfy from)
               ;;TODO: remove this once message-delivery-status is fixed
               :current-public-key current-public-key)
        (update-in [:content :response-to] #(add-response-metadata % contacts account)))))

(defn messages-with-metadata
  [messages contacts account]
  (mapv #(add-metadata % contacts account) messages))

(defn sort-message-groups
  "Sorts message groups according to timestamp of first message in group"
  [message-groups messages]
  (let [message-ids (set (keys messages))]
    (sort-by
     (comp unchecked-negate :timestamp (partial get messages) :message-id first second)
     (reduce (fn [acc [datemark group-messages]]
               (let [filtered-messages (filter #(message-ids (:message-id %))
                                               group-messages)]
                 (if (zero? (count filtered-messages))
                   acc
                   (assoc acc datemark filtered-messages))))
             {}
             message-groups))))

(defn get-current-chat-messages-stream
  [messages message-groups message-statuses referenced-messages contacts account]
  (-> (sort-message-groups message-groups messages)
      (messages-with-datemarks-and-statuses messages message-statuses referenced-messages)
      messages-stream
      (messages-with-metadata contacts account)))

(defn- get-last-message
  [messages]
  (when messages
    (first (sort-by :timestamp > (vals messages)))))

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
  [{:keys [chat-id messages unviewed-messages] :as chat}
   {:keys [public-key tags photo-path] :as contact}
   blocked-contacts]
  (let [filtered-messages (filter-messages-from-blocked-contacts messages
                                                                 blocked-contacts)
        unviewed-messages-count (count (set/intersection unviewed-messages
                                                         (set (keys filtered-messages))))
        large-unviewed-messages-label? (< 9 unviewed-messages-count)
        last-message (get-last-message filtered-messages)
        name (chat-name chat contact)]
    (cond-> chat
      tags
      (update :tags clojure.set/union tags)

      public-key
      (assoc :random-name (gfycat/generate-gfy public-key))

      (pos? unviewed-messages-count)
      (assoc :unviewed-messages-label (if large-unviewed-messages-label?
                                        (i18n/label :t/counter-9-plus)
                                        unviewed-messages-count))
      large-unviewed-messages-label? (assoc :large-unviewed-messages-label? large-unviewed-messages-label?)
      last-message (assoc :last-message last-message)
      :always (assoc :name name
                     :unviewed-messages-count unviewed-messages-count
                     :truncated-name (utils/truncate-str name 30)
                     :messages filtered-messages
                     :photo-path (or photo-path
                                     (identicon/identicon chat-id))))))

(defn- active-chat?
  [{:keys [group-chat public? is-active] :as chat}
   {:keys [blocked?] :as contact}
   dev-mode?]
  (and is-active
       (not blocked?)
       ;; not a group chat
       (or (not (and group-chat (not public?)))
           ;; if it's a group chat
           (utils.config/group-chats-enabled? dev-mode?))))

(defn active-chats
  [chats contacts blocked-contacts dev-mode?]
  (reduce (fn [acc [chat-id chat]]
            (let [contact (get contacts chat-id)]
              (if (active-chat? chat contact dev-mode?)
                (assoc acc chat-id (enrich-active-chat chat contact blocked-contacts))
                acc)))
          {}
          chats))
