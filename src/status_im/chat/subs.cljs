(ns status-im.chat.subs
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame :refer [reg-sub subscribe]]
            [status-im.utils.config :as utils.config]
            [status-im.chat.constants :as chat.constants]
            [status-im.chat.commands.core :as commands]
            [status-im.chat.commands.input :as commands.input]
            [status-im.utils.datetime :as time]
            [status-im.utils.platform :as platform]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.i18n :as i18n]
            [status-im.models.transactions :as transactions]
            [clojure.set :as clojure.set]
            [status-im.utils.identicon :as identicon]))

(reg-sub :get-chats :chats)

(reg-sub :get-current-chat-id :current-chat-id)

(reg-sub :chat-ui-props :chat-ui-props)

(reg-sub :get-id->command :id->command)

(reg-sub :get-access-scope->command-id :access-scope->command-id)

(reg-sub
 :get-current-chat-ui-props
 :<- [:chat-ui-props]
 :<- [:get-current-chat-id]
 (fn [[chat-ui-props id]]
   (get chat-ui-props id)))

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

(reg-sub
 :get-current-chat
 :<- [:get-active-chats]
 :<- [:get-current-chat-id]
 :<- [:get-current-chat-contact]
 (fn [[chats current-chat-id contact]]
   (when-let [current-chat (get chats current-chat-id)]
     (assoc current-chat
            :chat-name (chat-name current-chat contact)
            :contact contact))))

(reg-sub
 :get-current-chat-name
 :<- [:get-current-chat-contact]
 :<- [:get-current-chat]
 (fn [[contact chat]]
   (chat-name chat contact)))

(reg-sub
 :get-chat-name
 :<- [:get-contacts]
 :<- [:get-chats]
 (fn [[contacts chats] [_ chat-id]]
   (chat-name (get chats chat-id) (get contacts chat-id))))

(reg-sub
 :get-current-chat-ui-prop
 :<- [:get-current-chat-ui-props]
 (fn [ui-props [_ prop]]
   (get ui-props prop)))

(reg-sub
 :validation-messages
 :<- [:get-current-chat-ui-props]
 (fn [ui-props]
   (some-> ui-props :validation-messages)))

(reg-sub
 :chat-input-margin
 :<- [:get :keyboard-height]
 (fn [kb-height]
   (cond
     (and platform/iphone-x? (> kb-height 0)) (- kb-height 34)
     platform/ios? kb-height
     :default 0)))

(defn active-chats [[contacts chats {:keys [dev-mode?]}]]
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

(reg-sub
 :get-active-chats
 :<- [:get-contacts]
 :<- [:get-chats]
 :<- [:account/account]
 active-chats)

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

(defn get-last-message [messages message-groups]
  (->> (sort-message-groups message-groups messages)
       first
       second
       last
       :message-id
       (get messages)))

(reg-sub
 :chats/active-chats
 :<- [:get-contacts]
 :<- [:get-chats]
 :<- [:contacts/blocked]
 :<- [:account/account]
 (fn [[contacts chats blocked-contacts {:keys [dev-mode?]}]]
   (keep (fn [{:keys [chat-id group-chat public? is-active messages
                      message-groups unviewed-messages]
               :as chat}]
           (let [{:keys [public-key tags photo-path] :as contact}
                 (get contacts chat-id)]
             (when (and is-active
                        (not (blocked-contacts public-key))
                        ;; not a group chat
                        (or (not (and group-chat (not public?)))
                            ;; if it's a group chat
                            (utils.config/group-chats-enabled? dev-mode?)))
               (let [unviewed-messages-count (count unviewed-messages)
                     large-unviewed-messages-label? (< 9 unviewed-messages-count)
                     last-message (get-last-message messages message-groups)]
                 (cond-> chat
                   tags
                   (update :tags clojure.set/union tags)

                   public-key
                   (assoc :random-name (gfycat/generate-gfy public-key))

                   (pos? unviewed-messages-count)
                   (assoc :unviewed-messages-label (if large-unviewed-messages-label?
                                                     "9+"
                                                     unviewed-messages-count))
                   large-unviewed-messages-label? (assoc :large-unviewed-messages-label? large-unviewed-messages-label?)
                   last-message (assoc :last-message last-message)
                   :always (assoc :name (chat-name chat contact)
                                  :photo-path (or photo-path
                                                  photo-path
                                                  (identicon/identicon chat-id))))))))
         (vals chats))))

(reg-sub
 :get-chat
 :<- [:get-active-chats]
 (fn [chats [_ chat-id]]
   (get chats chat-id)))

(reg-sub
 :get-current-chat-message
 :<- [:get-current-chat]
 (fn [{:keys [messages]} [_ message-id]]
   (get messages message-id)))

(defn filter-messages-from-blocked-contacts
  [messages blocked-contacts]
  (reduce (fn [acc [message-id {:keys [from] :as message}]]
            (if (blocked-contacts from)
              acc
              (assoc acc message-id message)))
          {}
          messages))

(reg-sub
 :get-current-chat-messages
 :<- [:get-current-chat]
 :<- [:contacts/blocked]
 (fn [[{:keys [messages]} blocked-contacts]]
   (filter-messages-from-blocked-contacts messages blocked-contacts)))

(reg-sub
 :get-current-chat-message-groups
 :<- [:get-current-chat]
 (fn [{:keys [message-groups]}]
   (or message-groups {})))

(reg-sub
 :get-current-chat-message-statuses
 :<- [:get-current-chat]
 (fn [{:keys [message-statuses]}]
   (or message-statuses {})))

(reg-sub
 :get-current-chat-referenced-messages
 :<- [:get-current-chat]
 (fn [{:keys [referenced-messages]}]
   (or referenced-messages {})))

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

(defn get-photo-path [contacts account id]
  (let [photo-path (or (:photo-path (contacts id))
                       (when (= id (:public-key account))
                         (:photo-path account)))]
    (if (string/blank? photo-path)
      (identicon/identicon id)
      photo-path)))

(defn get-contact-name [contacts account id]
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

(defn messages-with-metadata [messages contacts account]
  (mapv #(add-metadata % contacts account) messages))

(reg-sub
 :get-current-chat-messages-stream
 :<- [:get-current-chat-messages]
 :<- [:get-current-chat-message-groups]
 :<- [:get-current-chat-message-statuses]
 :<- [:get-current-chat-referenced-messages]
 :<- [:get-contacts]
 :<- [:account/account]
 (fn [[messages message-groups message-statuses referenced-messages contacts account]]
   (-> (sort-message-groups message-groups messages)
       (messages-with-datemarks-and-statuses messages message-statuses referenced-messages)
       messages-stream
       (messages-with-metadata contacts account))))

(reg-sub
 :chat/current
 :<- [:get-current-chat]
 :<- [:get-current-chat-messages-stream]
 (fn [[current-chat messages]]
   (assoc current-chat :messages messages)))

(reg-sub
 :get-commands-for-chat
 :<- [:get-id->command]
 :<- [:get-access-scope->command-id]
 :<- [:get-current-chat]
 (fn [[id->command access-scope->command-id chat]]
   (commands/chat-commands id->command access-scope->command-id chat)))

(def ^:private map->sorted-seq (comp (partial map second) (partial sort-by first)))

(defn- available-commands [[commands {:keys [input-text]}]]
  (->> commands
       map->sorted-seq
       (filter (fn [{:keys [type]}]
                 (when (commands.input/starts-as-command? input-text)
                   (string/includes? (commands/command-name type) input-text))))))

(reg-sub
 :get-available-commands
 :<- [:get-commands-for-chat]
 :<- [:get-current-chat]
 available-commands)

(reg-sub
 :get-all-available-commands
 :<- [:get-commands-for-chat]
 (fn [commands]
   (map->sorted-seq commands)))

(reg-sub
 :selected-chat-command
 :<- [:get-current-chat]
 :<- [:get-current-chat-ui-prop :selection]
 :<- [:get-commands-for-chat]
 (fn [[{:keys [input-text]} selection commands]]
   (commands.input/selected-chat-command input-text selection commands)))

(reg-sub
 :chat-input-placeholder
 :<- [:get-current-chat]
 :<- [:selected-chat-command]
 (fn [[{:keys [input-text]} {:keys [params current-param-position]}]]
   (when (string/ends-with? (or input-text "") chat.constants/spacing-char)
     (get-in params [current-param-position :placeholder]))))

(reg-sub
 :chat-parameter-box
 :<- [:get-current-chat]
 :<- [:selected-chat-command]
 (fn [[_ {:keys [current-param-position params]}]]
   (when (and params current-param-position)
     (get-in params [current-param-position :suggestions]))))

(reg-sub
 :show-parameter-box?
 :<- [:chat-parameter-box]
 :<- [:show-suggestions?]
 :<- [:validation-messages]
 :<- [:selected-chat-command]
 (fn [[chat-parameter-box show-suggestions? validation-messages {:keys [command-completion]}]]
   (and chat-parameter-box
        (not validation-messages)
        (not show-suggestions?)
        (not (= :complete command-completion)))))

(reg-sub
 :show-suggestions-view?
 :<- [:get-current-chat-ui-prop :show-suggestions?]
 :<- [:get-current-chat]
 :<- [:get-all-available-commands]
 (fn [[show-suggestions? {:keys [input-text]} commands]]
   (and (or show-suggestions?
            (commands.input/starts-as-command? (string/trim (or input-text ""))))
        (seq commands))))

(reg-sub
 :show-suggestions?
 :<- [:show-suggestions-view?]
 :<- [:selected-chat-command]
 (fn [[show-suggestions-box? selected-command]]
   (and show-suggestions-box? (not selected-command))))

(reg-sub
 :unviewed-messages-count
 (fn [[_ chat-id]]
   (subscribe [:get-chat chat-id]))
 (fn [{:keys [unviewed-messages]}]
   (count unviewed-messages)))

(reg-sub
 :get-photo-path
 :<- [:get-contacts]
 :<- [:account/account]
 (fn [[contacts account] [_ id]]
   (get-photo-path contacts account id)))

(reg-sub
 :get-last-message
 (fn [[_ chat-id]]
   (subscribe [:get-chat chat-id]))
 (fn [{:keys [messages message-groups]}]
   (->> (sort-message-groups message-groups messages)
        first
        second
        last
        :message-id
        (get messages))))

(reg-sub
 :chat-animations
 (fn [db [_ key type]]
   (let [chat-id (subscribe [:get-current-chat-id])]
     (get-in db [:animations :chats @chat-id key type]))))

(reg-sub
 :get-chats-unread-messages-number
 :<- [:get-active-chats]
 (fn [chats _]
   (apply + (map (comp count :unviewed-messages) (vals chats)))))

(reg-sub
 :transaction-confirmed?
 (fn [db [_ tx-hash]]
   (-> (get-in db [:wallet :transactions tx-hash :confirmations] "0")
       (js/parseInt)
       (>= transactions/confirmations-count-threshold))))

(reg-sub
 :wallet-transaction-exists?
 (fn [db [_ tx-hash]]
   (not (nil? (get-in db [:wallet :transactions tx-hash])))))

(reg-sub
 :chat/cooldown-enabled?
 (fn [db]
   (:chat/cooldown-enabled? db)))

(reg-sub
 :chat-cooldown-enabled?
 :<- [:get-current-chat]
 :<- [:chat/cooldown-enabled?]
 (fn [[{:keys [public?]} cooldown-enabled?]]
   (and public?
        cooldown-enabled?)))

(reg-sub
 :get-reply-message
 :<- [:get-current-chat]
 :<- [:get-contacts]
 :<- [:account/account]
 (fn [[{:keys [metadata messages]} contacts account]]
   (when-let [message (get messages (:responding-to-message metadata))]
     (add-response-metadata message contacts account))))
