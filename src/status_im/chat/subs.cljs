(ns status-im.chat.subs
  (:require [clojure.string :as string]
            [re-frame.core :refer [reg-sub subscribe]]
            [status-im.constants :as constants]
            [status-im.chat.constants :as chat-constants]
            [status-im.chat.models.input :as input-model]
            [status-im.chat.models.commands :as commands-model]
            [status-im.chat.views.input.utils :as input-utils]
            [status-im.commands.utils :as commands-utils]
            [status-im.utils.datetime :as time]
            [status-im.utils.platform :as platform]
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

(defn active-chats [dev-mode?]
  (fn [[_ chat]]
    (and (:is-active chat)
         (or dev-mode?
             (not= const/console-chat-id (:chat-id chat))))))

(reg-sub
  :get-active-chats
  :<- [:get-chats]
  :<- [:get-current-account]
  (fn [[chats {:keys [dev-mode?]}]]
    (into {} (filter (active-chats dev-mode?) chats))))

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
  (cond (empty? acc)                                     ; initial element
        {:last-timestamp timestamp
         :last-datemark  datemark
         :acc            (conj acc msg)}

        (and (not= last-datemark datemark)               ; not the same day
             (< timestamp last-timestamp))               ; not out-of-order
        {:last-timestamp timestamp
         :last-datemark  datemark
         :acc            (conj acc {:value last-datemark ; intersperse datemark message
                                    :type  :datemark}
                               msg)}
        :else
        {:last-timestamp (max timestamp last-timestamp)  ; use last datemark
         :last-datemark  last-datemark
         :acc            (conj acc (assoc msg :datemark last-datemark))}))

(defn sort-messages
  "Remove hidden messages and sort by clock-value desc, breaking ties by message id"
  [id->messages]
  (->> id->messages
       vals
      (filter :show?)
      (sort-by (juxt (comp unchecked-negate :clock-value) :message-id))))

(defn- add-datemark [{:keys [timestamp] :as msg}]
   (assoc msg :datemark (time/day-relative timestamp)))

(defn- add-timestamp [{:keys [timestamp] :as msg}]
  (assoc msg :timestamp-str (time/timestamp->time timestamp)))

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
      ; Append last datemark
      (conj messages-with-datemarks {:value (:datemark (peek messages-with-datemarks))
                                     :type  :datemark}))))

(defn- set-previous-message-first-in-group [stream]
  (conj (pop stream) (assoc (peek stream) :first-in-group? true)))

; any message that comes after this amount of ms will be grouped separately
(def ^:private group-ms 60000)

(defn add-positional-metadata
  "Reduce step which adds positional metadata to a message and conditionally
  update the previous message with :first-in-group?."
  [{:keys [stream last-outgoing-seen]}
   {:keys [type from datemark outgoing timestamp] :as message}]
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
                                        :same-direction? same-direction?
                                        :last-in-group? last-in-group?
                                        :last-outgoing? last-outgoing?)]
    {:stream             (cond-> stream
                           previous-first-in-group?
                           ; update previuous message if necessary
                           set-previous-message-first-in-group

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
                                       :last-outgoing? (:outgoing initial-message))]
      (->> (rest ordered-messages)
           (reduce add-positional-metadata
                   {:stream             [message-with-metadata]
                    :last-outgoing-seen (:last-outgoing? message-with-metadata)})
           :stream))))

(reg-sub
  :get-ordered-chat-messages
  (fn [[_ chat-id]]
    (subscribe [:get-chat chat-id]))
  (fn [{:keys [messages]}]
    (sort-messages messages)))

(reg-sub
  :get-current-chat-messages
  :<- [:get-current-chat]
  (fn [{:keys [messages]}]
    (-> messages sort-messages intersperse-datemarks messages-stream)))

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
  :web-view-extra-js
  :<- [:get-current-chat]
  (fn [current-chat]
    (:web-view-extra-js current-chat)))

(reg-sub
  :get-photo-path
  :<- [:get-contacts]
  (fn [contacts [_ id]]
    (:photo-path (contacts id))))

(reg-sub
  :get-last-message
  (fn [[_ chat-id]]
    (subscribe [:get-ordered-chat-messages chat-id]))
  first)

(reg-sub
  :chat-animations
  (fn [db [_ key type]]
    (let [chat-id (subscribe [:get-current-chat-id])]
      (get-in db [:chat-animations @chat-id key type]))))

(reg-sub
  :get-chats-unread-messages-number
  :<- [:get-active-chats]
  (fn [chats _]
    (apply + (map #(count (:unviewed-messages %)) (vals chats)))))
