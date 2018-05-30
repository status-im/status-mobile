(ns status-im.chat.subs
  (:require [clojure.string :as string]
            [re-frame.core :refer [reg-sub subscribe]]
            [status-im.chat.constants :as chat-constants]
            [status-im.chat.models.input :as input-model]
            [status-im.chat.models.commands :as commands-model]
            [status-im.chat.views.input.utils :as input-utils]
            [status-im.commands.utils :as commands-utils]
            [status-im.utils.datetime :as time]
            [status-im.utils.platform :as platform]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.i18n :as i18n]
            [status-im.constants :as const]))

(reg-sub :get-chats :chats)

(reg-sub :get-current-chat-id :current-chat-id)

(reg-sub :chat-ui-props :chat-ui-props)

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
    :else      (i18n/get-contact-translated
                chat-id
                :name
                (or contact-name
                    (gfycat/generate-gfy chat-id)))))
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
   (some-> ui-props :validation-messages commands-utils/generate-hiccup)))

(reg-sub
 :result-box-markup
 :<- [:get-current-chat-ui-props]
 (fn [ui-props]
   (some-> ui-props :result-box :markup commands-utils/generate-hiccup)))

(reg-sub
 :chat-input-margin
 :<- [:get :keyboard-height]
 (fn [kb-height]
   (cond
     (and platform/iphone-x? (> kb-height 0)) (- kb-height 34)
     platform/ios? kb-height
     :default 0)))

(defn- active-chat? [dev-mode? [_ chat]]
  (and (:is-active chat)
       (or dev-mode?
           (not= const/console-chat-id (:chat-id chat)))))

(defn active-chats [[chats {:keys [dev-mode?]}]]
  (into {} (filter (partial active-chat? dev-mode?) chats)))

(reg-sub
 :get-active-chats
 :<- [:get-chats]
 :<- [:get-current-account]
 active-chats)

(reg-sub
 :get-chat
 :<- [:get-active-chats]
 (fn [chats [_ chat-id]]
   (get chats chat-id)))

(reg-sub
 :get-current-chat
 :<- [:get-active-chats]
 :<- [:get-current-chat-id]
 (fn [[chats current-chat-id]]
   (get chats current-chat-id)))

(reg-sub
 :get-current-chat-message
 :<- [:get-current-chat]
 (fn [{:keys [messages]} [_ message-id]]
   (get messages message-id)))

(reg-sub
 :get-current-chat-messages
 :<- [:get-current-chat]
 (fn [{:keys [messages]}]
   (or messages {})))

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

(defn sort-message-groups
  "Sorts message groups according to timestamp of first message in group "
  [message-groups messages]
  (sort-by
   (comp unchecked-negate :timestamp (partial get messages) :message-id first second)
   message-groups))

(defn messages-with-datemarks-and-statuses
  "Converts message groups into sequence of messages interspersed with datemarks,
  with correct user statuses associated into message"
  [message-groups messages message-statuses]
  (mapcat (fn [[datemark message-references]]
            (into (list {:value datemark
                         :type  :datemark})
                  (map (fn [{:keys [message-id timestamp-str]}]
                         (assoc (get messages message-id)
                                :datemark      datemark
                                :timestamp-str timestamp-str
                                :user-statuses (get message-statuses message-id))))
                  message-references))
          message-groups))

(defn- set-previous-message-info [stream]
  (let [{:keys [display-photo?] :as previous-message} (peek stream)]
    (conj (pop stream) (assoc previous-message
                              :display-username? display-photo?
                              :first-in-group?   true))))

(defn display-photo? [{:keys [outgoing message-type]}]
  (and (not outgoing)
       (not= message-type :user-message)))

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
        last-in-group?           (or (not= from (:from previous-message))
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

(reg-sub
 :get-current-chat-messages-stream
 :<- [:get-current-chat-messages]
 :<- [:get-current-chat-message-groups]
 :<- [:get-current-chat-message-statuses]
 (fn [[messages message-groups message-statuses]]
   (-> (sort-message-groups message-groups messages)
       (messages-with-datemarks-and-statuses messages message-statuses)
       messages-stream)))

(reg-sub
 :get-commands-for-chat
 :<- [:get-commands-responses-by-access-scope]
 :<- [:get-current-account]
 :<- [:get-current-chat]
 :<- [:get-contacts]
 (fn [[commands-responses account chat contacts]]
   (commands-model/commands-responses :command commands-responses account chat contacts)))

(reg-sub
 :get-responses-for-chat
 :<- [:get-commands-responses-by-access-scope]
 :<- [:get-current-account]
 :<- [:get-current-chat]
 :<- [:get-contacts]
 (fn [[commands-responses account {:keys [requests] :as chat} contacts]]
   (commands-model/requested-responses commands-responses account chat contacts (vals requests))))

(def ^:private map->sorted-seq (comp (partial map second) (partial sort-by first)))

(defn- available-commands-responses [[commands-responses {:keys [input-text]}]]
  (->> commands-responses
       map->sorted-seq
       (filter (fn [item]
                 (when (input-model/starts-as-command? input-text)
                   (string/includes? (commands-model/command-name item) input-text))))))

(reg-sub
 :get-available-commands
 :<- [:get-commands-for-chat]
 :<- [:get-current-chat]
 available-commands-responses)

(reg-sub
 :get-available-responses
 :<- [:get-responses-for-chat]
 :<- [:get-current-chat]
 available-commands-responses)

(reg-sub
 :get-available-commands-responses
 :<- [:get-commands-for-chat]
 :<- [:get-responses-for-chat]
 (fn [[commands responses]]
   (map->sorted-seq (merge commands responses))))

(reg-sub
 :selected-chat-command
 :<- [:get-current-chat]
 :<- [:get-commands-for-chat]
 :<- [:get-responses-for-chat]
 (fn [[chat commands responses]]
   (input-model/selected-chat-command chat commands responses)))

(reg-sub
 :chat-input-placeholder
 :<- [:get-current-chat]
 :<- [:selected-chat-command]
 (fn [[{:keys [input-text]} command]]
   (when (and (string/ends-with? (or input-text "") chat-constants/spacing-char)
              (not (get-in command [:command :sequential-params])))
     (let [input     (string/trim (or input-text ""))
           real-args (remove string/blank? (:args command))]
       (cond
         (and command (empty? real-args))
         (get-in command [:command :params 0 :placeholder])

         (and command
              (= (count real-args) 1)
              (input-model/text-ends-with-space? input))
         (get-in command [:command :params 1 :placeholder]))))))

(reg-sub
 :current-chat-argument-position
 :<- [:selected-chat-command]
 :<- [:get-current-chat]
 :<- [:get-current-chat-ui-prop :selection]
 (fn [[command {:keys [input-text seq-arguments]} selection]]
   (input-model/current-chat-argument-position command input-text selection seq-arguments)))

(reg-sub
 :chat-parameter-box
 :<- [:get-current-chat]
 :<- [:selected-chat-command]
 :<- [:current-chat-argument-position]
 (fn [[current-chat selected-chat-command argument-position]]
   (cond
     (and selected-chat-command
          (not= argument-position input-model/*no-argument-error*))
     (get-in current-chat [:parameter-boxes
                           (get-in selected-chat-command [:command :name])
                           argument-position])

     (not selected-chat-command)
     (get-in current-chat [:parameter-boxes :message])

     :default
     nil)))

(reg-sub
 :show-parameter-box?
 :<- [:chat-parameter-box]
 :<- [:show-suggestions?]
 :<- [:get-current-chat]
 :<- [:validation-messages]
 (fn [[chat-parameter-box show-suggestions? {:keys [input-text]} validation-messages]]
   (and (get chat-parameter-box :markup)
        (not validation-messages)
        (not show-suggestions?))))

(reg-sub
 :command-completion
 :<- [:selected-chat-command]
 input-model/command-completion)

(reg-sub
 :show-suggestions-view?
 :<- [:get-current-chat-ui-prop :show-suggestions?]
 :<- [:get-current-chat]
 :<- [:selected-chat-command]
 :<- [:get-available-commands-responses]
 (fn [[show-suggestions? {:keys [input-text]} selected-command commands-responses]]
   (and (or show-suggestions? (input-model/starts-as-command? (string/trim (or input-text ""))))
        (seq commands-responses))))

(reg-sub
 :show-suggestions?
 :<- [:show-suggestions-view?]
 :<- [:selected-chat-command]
 (fn [[show-suggestions-box? selected-command]]
   (and show-suggestions-box? (not (:command selected-command)))))

(reg-sub
 :is-request-answered?
 :<- [:get-current-chat]
 (fn [{:keys [requests]} [_ message-id]]
   (not= "open" (get-in requests [message-id :status]))))

(reg-sub
 :unviewed-messages-count
 (fn [[_ chat-id]]
   (subscribe [:get-chat chat-id]))
 (fn [{:keys [unviewed-messages]}]
   (count unviewed-messages)))

(reg-sub
 :get-photo-path
 :<- [:get-contacts]
 (fn [contacts [_ id]]
   (:photo-path (contacts id))))

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
     (get-in db [:chat-animations @chat-id key type]))))

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
       (pos?))))

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
