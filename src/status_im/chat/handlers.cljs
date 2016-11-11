(ns status-im.chat.handlers
  (:require-macros [cljs.core.async.macros :as am])
  (:require [re-frame.core :refer [enrich after debug dispatch]]
            [status-im.models.commands :as commands]
            [clojure.string :as str]
            [status-im.components.styles :refer [default-chat-color]]
            [status-im.chat.suggestions :as suggestions]
            [status-im.protocol.core :as protocol]
            [status-im.data-store.chats :as chats]
            [status-im.data-store.contacts :as contacts]
            [status-im.data-store.messages :as messages]
            [status-im.data-store.pending-messages :as pending-messages]
            [status-im.constants :refer [text-content-type
                                         content-type-command
                                         content-type-command-request
                                         default-number-of-messages
                                         wallet-chat-id]]
            [status-im.utils.random :as random]
            [status-im.chat.sign-up :as sign-up-service]
            [status-im.navigation.handlers :as nav]
            [status-im.utils.handlers :refer [register-handler] :as u]
            [status-im.handlers.server :as server]
            [status-im.utils.phone-number :refer [format-phone-number
                                                  valid-mobile-number?]]
            [status-im.components.status :as status]
            [status-im.utils.types :refer [json->clj]]
            status-im.chat.handlers.commands
            [status-im.commands.utils :refer [command-prefix]]
            [status-im.chat.utils :refer [console? not-console?]]
            [status-im.constants :refer [console-chat-id]]
            [status-im.utils.gfycat.core :refer [generate-gfy]]
            status-im.chat.handlers.animation
            status-im.chat.handlers.requests
            status-im.chat.handlers.unviewed-messages
            status-im.chat.handlers.send-message
            status-im.chat.handlers.receive-message
            [cljs.core.async :as a]
            status-im.chat.handlers.webview-bridge
            status-im.chat.handlers.wallet-chat
            status-im.chat.handlers.console
            [taoensso.timbre :as log]))

(register-handler :set-chat-ui-props
  (fn [db [_ ui-element value]]
    (assoc-in db [:chat-ui-props ui-element] value)))

(register-handler :set-show-info
  (fn [db [_ show-info]]
    (assoc db :show-info show-info)))

(register-handler :show-message-details
  (u/side-effect!
    (fn [_ [_ details]]
      (dispatch [:set-chat-ui-props :show-bottom-info? true])
      (dispatch [:set-chat-ui-props :bottom-info details]))))

(register-handler :load-more-messages
  (fn [{:keys [current-chat-id loading-allowed] :as db} _]
    (let [all-loaded? (get-in db [:chats current-chat-id :all-loaded?])]
      (if loading-allowed
        (do (am/go
              (<! (a/timeout 400))
              (dispatch [:set :loading-allowed true]))
            (if all-loaded?
              db
              (let [messages-path [:chats current-chat-id :messages]
                    messages      (get-in db messages-path)
                    chat-messages (filter #(= current-chat-id (:chat-id %)) messages)
                    new-messages  (messages/get-by-chat-id current-chat-id (count chat-messages))
                    all-loaded?   (> default-number-of-messages (count new-messages))]
                (-> db
                    (assoc :loading-allowed false)
                    (update-in messages-path concat new-messages)
                    (assoc-in [:chats current-chat-id :all-loaded?] all-loaded?)))))
        db))))

(defn safe-trim [s]
  (when (string? s)
    (str/trim s)))

(register-handler :cancel-command
  (fn [{:keys [current-chat-id] :as db} _]
    (-> db
        (assoc-in [:chats current-chat-id :command-input] {})
        (update-in [:chats current-chat-id :input-text] safe-trim))))

(register-handler :start-cancel-command
  (after #(dispatch [:set-soft-input-mode :resize]))
  (u/side-effect!
    (fn [db _]
      (dispatch [:animate-cancel-command]))))

(defn update-input-text
  [{:keys [current-chat-id] :as db} text]
  (assoc-in db [:chats current-chat-id :input-text] text))

(register-handler :set-message-input []
  (fn [db [_ input]]
    (assoc db :message-input input)))

(register-handler :blur-message-input
  (u/side-effect!
    (fn [db _]
      (when-let [message-input (:message-input db)]
        (.blur message-input)))))

(defn update-text [db [_ chat-id text]]
  (assoc-in db [:chats chat-id :input-text] text))

(defn update-command [db [_ text]]
  (if-not (commands/get-chat-command db)
    (let [{:keys [command]} (suggestions/check-suggestion db text)]
      (if command
        (commands/set-command-input db :commands command)
        db))
    db))

(defn check-suggestions
  [db [_ chat-id text]]
  (let [suggestions (suggestions/get-suggestions db text)]
    (assoc-in db [:command-suggestions chat-id] suggestions)))

(defn select-suggestion!
  [db [_ chat-id text]]
  (let [suggestions (get-in db [:command-suggestions chat-id])]
    (if (= 1 (count suggestions))
      (dispatch [:set-chat-command (ffirst suggestions)])
      (dispatch [::set-text chat-id text]))))

(register-handler :set-chat-input-text
  (u/side-effect!
    (fn [{:keys [current-chat-id]} [_ text]]
      (if (console? current-chat-id)
        (dispatch [::check-input-for-commands text])
        (dispatch [::check-suggestions current-chat-id text])))))

(def possible-commands
  {[:confirmation-code :responses] #(re-matches #"^[\d]{4}$" %)
   [:phone :commands]              valid-mobile-number?})

(defn check-text-for-commands [text]
  (ffirst (filter (fn [[_ f]] (f text)) possible-commands)))

(register-handler ::check-input-for-commands
  (u/side-effect!
    (fn [_ [_ text]]
      (if-let [[_ type :as command] (check-text-for-commands text)]
        (let [text' (if (= :commands type)
                      (str command-prefix text)
                      text)]
          (dispatch [::stage-command-with-content command text']))
        (dispatch [::check-suggestions console-chat-id text])))))

(register-handler ::stage-command-with-content
  (u/side-effect!
    (fn [_ [_ [command type] text]]
      (dispatch [:set-chat-command command type])
      (dispatch [:set-chat-command-content text]))))

(register-handler :set-staged-commands-scroll-view
  (fn [{:keys [current-chat-id] :as db} [_ view]]
    (assoc-in db [:chats current-chat-id :staged-scroll-view] view)))

(register-handler :set-staged-commands-scroll-height
  (fn [{:keys [current-chat-id] :as db} [_ height]]
    (assoc-in db [:chats current-chat-id :staged-scroll-height] height)))

(register-handler :staged-commands-scroll-to
  (u/side-effect!
    (fn [{:keys [current-chat-id chats]} [_ height]]
      (let [{:keys [staged-scroll-view staged-scroll-height]} (get chats current-chat-id)]
        (when staged-scroll-view
          (let [y (if (< 0 staged-scroll-height height)
                    (- height staged-scroll-height)
                    0)]
            (.scrollTo staged-scroll-view (clj->js {:x 0 :y y}))))))))

(register-handler :set-message-input-view-height
  (fn [{:keys [current-chat-id] :as db} [_ height]]
    (assoc-in db [:chats current-chat-id :message-input-height] height)))

(register-handler ::check-suggestions
  [(after select-suggestion!)
   (after #(dispatch [:animate-command-suggestions]))]
  check-suggestions)

(register-handler ::set-text update-text)

(defn set-message-shown
  [db chat-id message-id]
  (update-in db [:chats chat-id :messages] (fn [messages]
                                             (map (fn [message]
                                                    (if (= message-id (:message-id message))
                                                      (assoc message :new? false)
                                                      message))
                                                  messages))))

(register-handler :set-message-shown
  (fn [db [_ {:keys [chat-id message-id]}]]
    (set-message-shown db chat-id message-id)))

(defn init-console-chat
  ([existing-account?] (init-console-chat {} existing-account?))
  ([{:keys [chats] :as db} existing-account?]
   (let [new-chat sign-up-service/console-chat]
     (if (chats console-chat-id)
       db
       (do
         (dispatch [:add-contacts [sign-up-service/console-contact]])
         (chats/save new-chat)
         (contacts/save-all [sign-up-service/console-contact])
         (sign-up-service/intro)
         (when existing-account?
           (sign-up-service/start-signup))
         (-> db
             (assoc :new-chat new-chat)
             (update :chats assoc console-chat-id new-chat)
             (update :chats-ids conj console-chat-id)
             (assoc :current-chat-id console-chat-id)))))))

(register-handler :init-console-chat
  (fn [db _]
    (init-console-chat db false)))

(register-handler :account-generation-message
  (u/side-effect!
    (fn [{:keys [chats]}]
      (when (> 4 (count (get-in chats [console-chat-id :messages])))
        (sign-up-service/account-generation-message)))))

(register-handler :show-mnemonic
  (u/side-effect!
    (fn [{:keys [chats]} [_ mnemonic]]
      (let [messages-count (count (get-in chats [console-chat-id :messages]))]
        (sign-up-service/passpharse-messages mnemonic messages-count)))))

(register-handler :sign-up
  (after (fn [_ [_ phone-number]]
           (dispatch [:account-update {:phone phone-number}])))
  (fn [db [_ phone-number message-id]]
    (let [formatted (format-phone-number phone-number)]
      (-> db
          (assoc :user-phone-number formatted)
          sign-up-service/start-listening-confirmation-code-sms
          (server/sign-up formatted
                          message-id
                          sign-up-service/on-sign-up-response)))))

(register-handler :stop-listening-confirmation-code-sms
  (fn [db [_]]
    (if (:confirmation-code-sms-listener db)
      (sign-up-service/stop-listening-confirmation-code-sms db)
      db)))

(register-handler :sign-up-confirm
  (u/side-effect!
    (fn [_ [_ confirmation-code message-id]]
      (server/sign-up-confirm
        confirmation-code
        message-id
        sign-up-service/on-send-code-response))))

(register-handler :set-signed-up
  (u/side-effect!
    (fn [_ [_ signed-up]]
      (dispatch [:account-update {:signed-up? signed-up}]))))

(defn load-messages!
  ([db] (load-messages! db nil))
  ([{:keys [current-chat-id] :as db} _]
   (assoc db :messages (messages/get-by-chat-id current-chat-id))))

(defn init-chat
  ([db] (init-chat db nil))
  ([{:keys [messages current-chat-id] :as db} _]
   (assoc-in db [:chats current-chat-id :messages] messages)))

(defn load-commands!
  [{:keys [current-chat-id]}]
  (dispatch [:load-commands! current-chat-id]))

(register-handler :init-chat
  (after #(dispatch [:load-requests!]))
  (-> load-messages!
      ((enrich init-chat))
      ((after load-commands!))))

(defn initialize-chats
  [{:keys [loaded-chats account-creation? chats] :as db} _]
  (let [chats' (if account-creation?
                 chats
                 (->> loaded-chats
                      (map (fn [{:keys [chat-id] :as chat}]
                             (let [last-message (messages/get-last-message db chat-id)]
                               [chat-id (assoc chat :last-message last-message)])))
                      (into {})))
        ids    (set (keys chats'))]

    (-> db
        (assoc :chats chats')
        (assoc :chats-ids ids)
        (dissoc :loaded-chats)
        (init-console-chat true))))

(defn load-chats!
  [{:keys [account-creation?] :as db} _]
  (if account-creation?
    db
    (assoc db :loaded-chats (chats/get-all))))

;TODO: check if its new account / signup status / create console chat
(register-handler :initialize-chats
  [(after #(dispatch [:load-unviewed-messages!]))
   (after #(dispatch [:init-wallet-chat]))]
  ((enrich initialize-chats) load-chats!))

(defmethod nav/preload-data! :chat
  [{:keys [current-chat-id] :as db} [_ _ id]]
  (let [chat-id          (or id current-chat-id)
        messages         (get-in db [:chats chat-id :messages])
        db'              (assoc db :current-chat-id chat-id)
        commands-loaded? (if js/goog.DEBUG
                           false
                           (get-in db [:chats chat-id :commands-loaded]))]
    (when (= current-chat-id wallet-chat-id)
      (dispatch [:cancel-command]))
    (dispatch [:load-requests! chat-id])
    (if-not commands-loaded?
      (dispatch [:load-commands! chat-id])
      (dispatch [:invoke-chat-loaded-callbacks chat-id]))
    (if (and (seq messages)
             (not= (count messages) 1))
      db'
      (-> db'
          load-messages!
          init-chat))))

(register-handler :add-chat-loaded-callback
  (fn [db [_ chat-id callback]]
    (log/debug "Add chat loaded callback: " chat-id callback)
    (update-in db [::chat-loaded-callbacks chat-id] conj callback)))

(register-handler ::clear-chat-loaded-callbacks
  (fn [db [_ chat-id]]
    (log/debug "Clear chat loaded callback: " chat-id)
    (assoc-in db [::chat-loaded-callbacks chat-id] nil)))

(register-handler :invoke-chat-loaded-callbacks
  (u/side-effect!
    (fn [db [_ chat-id]]
      (log/debug "Invoking chat loaded callbacks: " chat-id)
      (let [callbacks (get-in db [::chat-loaded-callbacks chat-id])]
        (log/debug "Invoking chat loaded callbacks: " callbacks)
        (doseq [callback callbacks]
          (callback))
        (dispatch [::clear-chat-loaded-callbacks chat-id])))))

(defn prepare-chat [{:keys [contacts]} chat-id chat]
  (let [name (get-in contacts [chat-id :name])]
    (merge {:chat-id    chat-id
            :name       (or name (generate-gfy))
            :color      default-chat-color
            :group-chat false
            :is-active  true
            :timestamp  (.getTime (js/Date.))
            :contacts   [{:identity chat-id}]
            :dapp-url   nil
            :dapp-hash  nil}
           chat)))

(defn add-new-chat
  [db [_ chat-id chat]]
  (assoc db :new-chat (prepare-chat db chat-id chat)))

(defn add-chat [{:keys [new-chat] :as db} [_ chat-id]]
  (-> db
      (update :chats assoc chat-id new-chat)
      (update :chats-ids conj chat-id)))

(defn save-new-chat!
  [{:keys [new-chat]} _]
  (chats/save new-chat))

(defn open-chat!
  [_ [_ chat-id _ navigation-type]]
  (dispatch [(or navigation-type :navigate-to) :chat chat-id]))

(register-handler ::start-chat!
  (-> add-new-chat
      ((enrich add-chat))
      ((after save-new-chat!))
      ((after open-chat!))))

(register-handler :start-chat
  (u/side-effect!
    (fn [{:keys [chats]} [_ contact-id options navigation-type]]
      (if (chats contact-id)
        (dispatch [(or navigation-type :navigate-to) :chat contact-id])
        (dispatch [::start-chat! contact-id options navigation-type])))))

(register-handler :add-chat
  (u/side-effect!
    (fn [{:keys [chats]} [_ chat-id chat]]
      (when-not (get chats chat-id)
        (dispatch [::add-chat chat-id chat])))))

(register-handler ::add-chat
  (-> add-new-chat
      ((enrich add-chat))
      ((after save-new-chat!))))

(defn update-chat!
  [_ [_ {:keys [name] :as chat}]]
  (let [chat' (if name chat (dissoc chat :name))]
    (chats/save chat')))

(register-handler :update-chat!
  (-> (fn [db [_ {:keys [chat-id name] :as chat}]]
        (let [chat' (if name chat (dissoc chat :name))]
          (if (get-in db [:chats chat-id])
            (update-in db [:chats chat-id] merge chat')
            db)))
      ((after update-chat!))))

(register-handler :upsert-chat!
  (fn [db [_ {:keys [chat-id clock-value] :as opts}]]
    (let [chat (if (chats/exists? chat-id)
                 (let [{old-clock-value :clock-value :as chat} (chats/get-by-id chat-id)]
                   (assoc chat :clock-value (max old-clock-value clock-value)))
                 (prepare-chat db chat-id opts))]
      (chats/save chat)
      (update-in db [:chats chat-id] merge chat))))

(register-handler :switch-command-suggestions!
  (u/side-effect!
    (fn [db]
      (let [text (if (suggestions/typing-command? db) "" "!")]
        (dispatch [:set-chat-input-text text])))))

(defn remove-chat
  [db [_ chat-id]]
  (update db :chats dissoc chat-id))

; todo do we really need this message?
(defn leaving-message!
  [{:keys [current-chat-id]} _]
  (messages/save
    current-chat-id
    {:from         "system"
     :message-id   (random/id)
     :content      "You left this chat"
     :content-type text-content-type}))

(defn delete-messages!
  [{:keys [current-chat-id]} [_ chat-id]]
  (let [id (or chat-id current-chat-id)]
    (messages/delete-by-chat-id id)))

(defn delete-chat!
  [_ [_ chat-id]]
  (chats/delete chat-id))

(defn remove-pending-messages!
  [_ [_ chat-id]]
  (pending-messages/delete-all-by-chat-id chat-id))

(register-handler :leave-group-chat
  ;; todo oreder of operations tbd
  (after (fn [_ _] (dispatch [:navigation-replace :chat-list])))
  (u/side-effect!
    (fn [{:keys [web3 current-chat-id chats current-public-key]} _]
      (let [{:keys [public-key private-key]} (chats current-chat-id)]
        (protocol/stop-watching-group!
          {:web3     web3
           :group-id current-chat-id})
        (protocol/leave-group-chat!
          {:web3     web3
           :group-id current-chat-id
           :keypair  {:public  public-key
                      :private private-key}
           :message  {:from       current-public-key
                      :message-id (random/id)}}))
      (dispatch [::remove-chat current-chat-id]))))

(register-handler ::remove-chat
  (-> remove-chat
      ;((after leaving-message!))
      ((after delete-messages!))
      ((after remove-pending-messages!))
      ((after delete-chat!))))

(defn edit-mode-handler [mode]
  (fn [{:keys [current-chat-id] :as db} _]
    (assoc-in db [:edit-mode current-chat-id] mode)))

(register-handler :command-edit-mode
  (after #(dispatch [:clear-validation-errors]))
  (edit-mode-handler :command))

(register-handler :text-edit-mode
  (after #(dispatch [:set-chat-input-text ""]))
  (edit-mode-handler :text))

(register-handler :set-layout-height
  [(after
     (fn [{:keys [current-chat-id] :as db}]
       (let [suggestions (get-in db [:has-suggestions? current-chat-id])
             mode        (get-in db [:edit-mode current-chat-id])]
         (when (and (= :command mode) suggestions)
           (dispatch [:fix-response-height nil nil true])))))
   (after
     (fn [{:keys [current-chat-id] :as db}]
       (let [suggestions (get-in db [:command-suggestions current-chat-id])
             mode        (get-in db [:edit-mode current-chat-id])]
         (when (and (not= :command mode) (seq suggestions))
           (dispatch [:fix-commands-suggestions-height nil nil true])))))]
  (fn [db [_ h]]
    (assoc db :layout-height h)))

(defn send-seen!
  [{:keys [web3 current-public-key chats]}
   [_ {:keys [from chat-id message-id]}]]
  (when-not (console? chat-id)
    (let [{:keys [group-chat]} (chats chat-id)]
      (protocol/send-seen! {:web3    web3
                            :message {:from       current-public-key
                                      :to         from
                                      :group-id   (when group-chat chat-id)
                                      :message-id message-id}}))))
(register-handler :send-seen!
  [(after (fn [_ [_ {:keys [message-id]}]]
            (messages/update {:message-id     message-id
                              :message-status :seen})))
   (after (fn [_ [_ {:keys [chat-id]}]]
            (dispatch [:remove-unviewed-messages chat-id])))]
  (u/side-effect! send-seen!))

(register-handler :set-web-view-url
  (fn [{:keys [current-chat-id] :as db} [_ url]]
    (assoc-in db [:web-view-url current-chat-id] url)))

(register-handler :set-soft-input-mode
  (after
    (fn [{:keys [current-chat-id]} [_ mode chat-id]]
      (when (or (nil? chat-id) (= current-chat-id chat-id))
        (status/set-soft-input-mode (if (= :pan mode)
                                      status/adjust-pan
                                      status/adjust-resize)))))
  (fn [db [_ chat-id mode]]
    (assoc-in db [:kb-mode chat-id] mode)))

(register-handler :check-autorun
  (u/side-effect!
    (fn [{:keys [current-chat-id] :as db}]
      (let [autorun (get-in db [:chats current-chat-id :autorun])]
        (when autorun
          (am/go
            ;;todo: find another way to make it work...
            (a/<! (a/timeout 100))
            (dispatch [:set-chat-command (keyword autorun)])
            (dispatch [:animate-command-suggestions])))))))

(register-handler :inc-clock
  (u/side-effect!
    (fn [_ [_ chat-id]]
      (let [chat (-> (chats/get-by-id chat-id)
                     (update :clock-value inc))]
        (dispatch [:update-chat! chat])))))
