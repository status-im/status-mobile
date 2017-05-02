(ns status-im.commands.handlers.loading
  (:require [re-frame.core :refer [path after dispatch subscribe trim-v debug]]
            [status-im.utils.handlers :as u]
            [status-im.utils.utils :refer [http-get show-popup]]
            [clojure.string :as s]
            [status-im.data-store.commands :as commands]
            [status-im.data-store.contacts :as contacts]
            [status-im.components.status :as status]
            [status-im.utils.types :refer [json->clj]]
            [status-im.commands.utils :refer [reg-handler]]
            [status-im.constants :refer [console-chat-id wallet-chat-id]]
            [taoensso.timbre :as log]
            [status-im.i18n :refer [label]]
            [status-im.utils.homoglyph :as h]
            [status-im.utils.js-resources :as js-res]
            [status-im.utils.random :as random]
            [status-im.chat.sign-up :as sign-up]
            [status-im.bots.constants :as bots-constants]))

(def commands-js "commands.js")

(defn load-commands!
  [{:keys [current-chat-id contacts]} [contact callback]]
  (let [whole-contact? (map? contact)
        {:keys [whisper-identity]} contact
        identity'      (or whisper-identity contact current-chat-id)
        contact'       (if whole-contact?
                         contact
                         (or (get contacts identity')
                             sign-up/console-contact))]
    (when identity'
      (dispatch [::fetch-commands! {:contact  contact'
                                    :callback callback}])))
  ;; todo uncomment
  #_(if-let [{:keys [file]} (commands/get-by-chat-id contact)]
      (dispatch [::parse-commands! contact file])
      (dispatch [::fetch-commands! contact])))


(defn http-get-commands [params url]
  (http-get url
            (fn [response]
              (when-let [content-type (.. response -headers (get "Content-Type"))]
                (s/includes? content-type "application/javascript")))
            #(dispatch [::validate-hash params %])
            #(log/debug (str "command.js wasn't found at " url))))


(defn fetch-commands!
  [_ [{{:keys [dapp? dapp-url bot-url whisper-identity]} :contact
       :as                                               params}]]
  (cond
    bot-url
    (if-let [url (js-res/get-resource bot-url)]
      (dispatch [::validate-hash params url])
      (http-get-commands params bot-url))

    dapp-url
    (let [url (s/join "/" [dapp-url "commands.js"])]
      (http-get-commands params url))

    :else
    (dispatch [::validate-hash params js-res/commands-js])))

(defn dispatch-loaded!
  [db [{{:keys [whisper-identity]} :contact
        :as                        params} file]]
  (if (::valid-hash db)
    (dispatch [::parse-commands! params file])
    (dispatch [::loading-failed! whisper-identity ::wrong-hash])))

(defn get-hash-by-identity
  [db identity]
  (get-in db [:contacts identity :dapp-hash]))

(defn get-hash-by-file
  [file]
  ;; todo tbd hashing algorithm
  (hash file))

(defn parse-commands!
  [_ [{{:keys [whisper-identity]} :contact
       :keys                      [callback]}
      file]]
  (status/parse-jail
    whisper-identity file
    (fn [result]
      (let [{:keys [error result]} (json->clj result)]
        (log/debug "Parsing commands results: " error result)
        (if error
          (dispatch [::loading-failed! whisper-identity ::error-in-jail error])
          (do
            (dispatch [::add-commands whisper-identity file result])
            (when callback (callback))))))))

(defn validate-hash
  [db [_ file]]
  (let [valid? true
        ;; todo check
        #_(= (get-hash-by-identity db identity)
             (get-hash-by-file file))]
    (assoc db ::valid-hash valid?)))

(defn mark-as [as coll]
  (->> coll
       (map (fn [[k v]] [k (assoc v :type as)]))
       (into {})))

(defn filter-forbidden-names [account id commands]
  (->> commands
       (remove (fn [[_ {:keys [registered-only name]}]]
                 (and (not (:address account))
                      (not= name "global")
                      registered-only)))
       (remove (fn [[n]]
                 (and
                   (not= console-chat-id id)
                   (h/matches (name n) "password"))))
       (into {})))

(defn get-mailmans-commands [db]
  (->> (get-in db [:contacts bots-constants/mailman-bot :commands])
       (map
         (fn [[k v :as com]]
           [k (-> v
                  (update :params (fn [p]
                                    (if (map? p)
                                      ((comp vec vals) p)
                                      p)))
                  (assoc :bot bots-constants/mailman-bot
                         :type :command))]))
       (into {})))

(defn add-commands
  [db [id _ {:keys [commands responses subscriptions]}]]
  (let [account          @(subscribe [:get-current-account])
        commands'        (filter-forbidden-names account id commands)
        global-command   (:global commands')
        commands''       (apply dissoc commands' [:init :global])
        responses'       (filter-forbidden-names account id responses)
        mailman-commands (get-mailmans-commands db)]
    (cond-> db

            true
            (update-in [:contacts id] assoc
                       :commands-loaded true
                       :commands (mark-as :command (merge mailman-commands commands''))
                       :responses (mark-as :response responses')
                       :subscriptions subscriptions)

            global-command
            (update :global-commands assoc (keyword id)
                    (assoc global-command :bot id
                                          :type :command))

            (= id bots-constants/mailman-bot)
            (update db :contacts (fn [contacts]
                                   (reduce (fn [contacts [k _]]
                                             (update-in contacts [k :commands]
                                                        (fn [c]
                                                          (merge mailman-commands c))))
                                           contacts
                                           contacts))))))

(defn save-commands-js!
  [_ [id file]]
  #_(commands/save {:chat-id id :file file}))

(defn save-commands!
  [{:keys [global-commands contacts]} [id]]
  (let [command   (get global-commands (keyword id))
        commands  (get-in contacts [id :commands])
        responses (get-in contacts [id :commands])]
    (contacts/save {:whisper-identity id
                    :global-command   command
                    :commands         (vals commands)
                    :responses        (vals responses)})))

(defn loading-failed!
  [db [id reason details]]
  (let [url (get-in db [:chats id :dapp-url])]
    (let [m (s/join "\n" ["commands.js loading failed"
                          url
                          id
                          (name reason)
                          details])]
      (show-popup "Error" m)
      (log/debug m))))

(reg-handler :check-and-load-commands!
  (u/side-effect!
    (fn [{:keys [contacts]} [identity callback]]
      (if (get-in contacts [identity :commands-loaded])
        (callback)
        (dispatch [:load-commands! identity callback])))))

(reg-handler :load-commands! (u/side-effect! load-commands!))
(reg-handler ::fetch-commands! (u/side-effect! fetch-commands!))

(reg-handler ::validate-hash
  (after dispatch-loaded!)
  validate-hash)

(reg-handler ::parse-commands! (u/side-effect! parse-commands!))

(reg-handler ::add-commands
  [(after save-commands-js!)
   (after save-commands!)
   (after #(dispatch [:check-and-open-dapp!]))
   (after #(dispatch [:update-suggestions]))
   (after (fn [_ [id]]
            (dispatch [:invoke-commands-loading-callbacks id])
            (dispatch [:invoke-chat-loaded-callbacks id])))
   (after (fn [{:keys [contacts]} [id]]
            (let [subscriptions (get-in contacts [id :subscriptions])]
              (doseq [[name opts] subscriptions]
                (dispatch [:register-bot-subscription
                           (assoc opts :bot id
                                       :name name)])))))]

  add-commands)

(reg-handler ::loading-failed! (u/side-effect! loading-failed!))

(reg-handler :add-commands-loading-callback
  (fn [db [chat-id callback]]
    (update-in db [::commands-callbacks chat-id] conj callback)))

(reg-handler :invoke-commands-loading-callbacks
  (u/side-effect!
    (fn [db [chat-id]]
      (let [callbacks (get-in db [::commands-callbacks chat-id])]
        (doseq [callback callbacks]
          (callback))
        (dispatch [::clear-commands-callbacks chat-id])))))

(reg-handler ::clear-commands-callbacks
  (fn [db [chat-id]]
    (assoc-in db [::commands-callbacks chat-id] nil)))
