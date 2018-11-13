(ns status-im.chat.db
  (:require [clojure.set :as clojure.set]
            [clojure.string :as string]
            [status-im.chat.commands.core :as commands]
            [status-im.chat.commands.input :as commands.input]
            [status-im.utils.config :as utils.config]
            [status-im.utils.gfycat.core :as gfycat]))

(defn chat-name
  [{:keys [group-chat
           chat-id
           public?
           name]}
   {contact-name :name}]
  (cond
    public?    (str "#" name)
    group-chat name
    :else      (or contact-name
                   (gfycat/generate-gfy chat-id))))

(defn active-chats
  [contacts chats {:keys [dev-mode?]}]
  (reduce (fn [acc [chat-id {:keys [group-chat public? is-active] :as chat}]]
            (if (and is-active
                     ;; not a group chat
                     (or (not (and group-chat (not public?)))
                         ;; if it's a group chat
                         (utils.config/group-chats-enabled? dev-mode?)))
              (assoc acc chat-id (if-let [contact (get contacts chat-id)]
                                   (-> chat
                                       (assoc :name (:name contact))
                                       (assoc :random-name (gfycat/generate-gfy (:public-key contact)))
                                       (update :tags clojure.set/union (:tags contact)))
                                   chat))
              acc))
          {}
          chats))

(defn sort-message-groups
  "Sorts message groups according to timestamp of first message in group"
  [message-groups messages]
  (sort-by
   (comp unchecked-negate :timestamp (partial get messages) :message-id first second)
   message-groups))

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

                                        ; any message that comes after this amount of ms will be grouped separately
(def ^:private group-ms 60000)

(defn add-positional-metadata
  "Reduce step which adds positional metadata to a message and conditionally
  update the previous message with :first-in-group?."
  [{:keys [stream last-outgoing-seen]}
   {:keys [type message-type from datemark outgoing timestamp] :as message}]
  (let [previous-message         (peek stream)
                                        ; Was the previous message from a different author or this message
                                        ; comes after x ms
        last-in-group?           (or (= :system-message message-type)
                                     (not= from (:from previous-message))
                                     (> (- (:timestamp previous-message) timestamp) group-ms))
        same-direction?          (= outgoing (:outgoing previous-message))
                                        ; Have we seen an outgoing message already?
        last-outgoing?           (and (not last-outgoing-seen)
                                      outgoing)
        datemark?                (= :datemark (:type message))
                                        ; If this is a datemark or this is the last-message of a group,
                                        ; then the previous message was the first
        previous-first-in-group? (or datemark?
                                     last-in-group?)
        new-message              (assoc message
                                        :display-photo?  (display-photo? message)
                                        :same-direction? same-direction?
                                        :last-in-group?  last-in-group?
                                        :last-outgoing?  last-outgoing?)]
    {:stream             (cond-> stream
                           previous-first-in-group?
                                        ; update previuous message if necessary
                           set-previous-message-info

                           :always
                           (conj new-message))
                                        ; mark the last message sent by the user
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
