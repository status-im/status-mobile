(ns status-im.chat.db
  (:require [clojure.set :as clojure.set]
            [clojure.string :as string]
            [status-im.chat.commands.core :as commands]
            [status-im.chat.commands.input :as commands.input]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.contact.db :as contact.db]
            [status-im.group-chats.db :as group-chats.db]
            [status-im.mailserver.constants :as mailserver.constants]
            [status-im.utils.gfycat.core :as gfycat]))

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
                 :chat-name (multiaccounts/displayed-name contact)
                 :name contact-name
                 :random-name random-name)
          (update :tags clojure.set/union (:tags contact))))))

(defn active-chats
  [contacts chats {:keys [public-key]}]
  (reduce-kv (fn [acc chat-id {:keys [is-active] :as chat}]
               (if is-active
                 (assoc acc
                        chat-id
                        (enrich-active-chat contacts chat public-key))
                 acc))
             {}
             chats))

(defn datemark? [{:keys [type]}]
  (= type :datemark))

(defn intersperse-datemark
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
  [{:keys [acc last-timestamp last-datemark]} {:keys [whisper-timestamp datemark] :as msg}]
  (cond (empty? acc)                                     ; initial element
        {:last-timestamp whisper-timestamp
         :last-datemark  datemark
         :acc            (conj acc msg)}

        (and (not= last-datemark datemark)               ; not the same day
             (< whisper-timestamp last-timestamp))               ; not out-of-order
        {:last-timestamp whisper-timestamp
         :last-datemark  datemark
         :acc            (conj acc {:value last-datemark ; intersperse datemark message
                                    :type  :datemark}
                               msg)}
        :else
        {:last-timestamp (min whisper-timestamp last-timestamp)  ; use last datemark
         :last-datemark  last-datemark
         :acc            (conj acc (assoc msg :datemark last-datemark))}))

(defn add-datemarks
  "Add a datemark in between an ordered seq of messages when two datemarks are not
  the same. Ignore messages with out-of-order timestamps"
  [messages]
  (when (seq messages)
    (let [messages-with-datemarks (:acc (reduce intersperse-datemark {:acc []} messages))]
      ; Append last datemark
      (conj messages-with-datemarks {:value (:datemark (peek messages-with-datemarks))
                                     :type  :datemark}))))

(defn gap? [{:keys [type]}]
  (= type :gap))

(defn check-gap
  [gaps previous-message next-message]
  (let [previous-timestamp     (:whisper-timestamp previous-message)
        next-whisper-timestamp (:whisper-timestamp next-message)
        next-timestamp         (:timestamp next-message)
        ignore-next-message?   (> (js/Math.abs
                                   (- next-whisper-timestamp next-timestamp))
                                  120000)]
    (reduce
     (fn [acc {:keys [from to id]}]
       (if (and next-message
                (not ignore-next-message?)
                (or
                 (and (nil? previous-timestamp)
                      (< from next-whisper-timestamp))
                 (and
                  (< previous-timestamp from)
                  (< to next-whisper-timestamp))
                 (and
                  (< from previous-timestamp)
                  (< to next-whisper-timestamp))))
         (-> acc
             (update :gaps-number inc)
             (update-in [:gap :ids] conj id))
         (reduced acc)))
     {:gaps-number 0
      :gap         nil}
     gaps)))

(defn add-gap [messages gaps]
  (conj messages
        {:type  :gap
         :value (clojure.string/join (:ids gaps))
         :gaps gaps}))

(defn add-gaps
  "Converts message groups into sequence of messages interspersed with datemarks,
  with correct user statuses associated into message"
  [message-list messages-gaps
   {:keys [highest-request-to lowest-request-from]} all-loaded? public?]
  (transduce
   (map identity)
   (fn
     ([]
      (let [acc {:messages         (list)
                 :previous-message nil
                 :gaps             messages-gaps}]
        (if (and
             public?
             all-loaded?
             (not (nil? highest-request-to))
             (not (nil? lowest-request-from))
             (< (- highest-request-to lowest-request-from)
                mailserver.constants/max-gaps-range))
          (update acc :messages conj {:type       :gap
                                      :value      (str :first-gap)
                                      :first-gap? true})
          acc)))
     ([{:keys [messages previous-message gaps]} message]
      (let [{:keys [gaps-number gap]}
            (check-gap gaps previous-message message)
            add-gap?      (pos? gaps-number)]
        {:messages           (cond-> messages

                               add-gap?
                               (add-gap gap)

                               :always
                               (conj message))
         :previous-message   message
         :gaps               (if add-gap?
                               (drop gaps-number gaps)
                               gaps)}))
     ([{:keys [messages gaps]}]
      (cond-> messages
        (seq gaps)
        (add-gap {:ids (map :id gaps)}))))
   (reverse message-list)))

(def map->sorted-seq
  (comp (partial map second) (partial sort-by first)))

(defn available-commands
  [commands {:keys [input-text]}]
  (->> commands
       map->sorted-seq
       (filter (fn [{:keys [type]}]
                 (when (commands.input/starts-as-command? input-text)
                   (string/includes? (commands/command-name type) input-text))))))
