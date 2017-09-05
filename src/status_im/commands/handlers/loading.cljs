(ns status-im.commands.handlers.loading
  (:require [re-frame.core :refer [path after dispatch subscribe trim-v debug]]
            [status-im.utils.handlers :as u]
            [status-im.utils.utils :refer [http-get show-popup]]
            [clojure.string :as s]
            [status-im.data-store.commands :as commands]
            [status-im.data-store.contacts :as contacts]
            [status-im.native-module.core :as status]
            [status-im.utils.types :refer [json->clj]]
            [status-im.commands.utils :refer [reg-handler]]
            [status-im.constants :refer [console-chat-id wallet-chat-id]]
            [taoensso.timbre :as log]
            [status-im.i18n :refer [label]]
            [status-im.utils.homoglyph :as h]
            [status-im.utils.js-resources :as js-res]
            [status-im.utils.random :as random] 
            [status-im.bots.constants :as bots-constants]
            [status-im.utils.datetime :as time]
            [status-im.data-store.local-storage :as local-storage]
            [clojure.string :as str]))


(defn load-commands!
  [{:keys [current-chat-id chats]
    :contacts/keys [contacts]} [jail-id callback]]
  (let [identity    (or jail-id current-chat-id)
        contact-ids (if (get contacts identity)
                      [identity]
                      (->> (get-in chats [identity :contacts])
                           (map :identity)
                           (into [])))]
    (when (seq contacts)
      (doseq [contact-id contact-ids]
        (when-let [contact (get contacts contact-id)]
          (dispatch [::fetch-commands! {:contact  contact
                                        :callback callback}]))))))

(defn http-get-commands [params url]
  (http-get url
            (fn [response]
              (when-let [content-type (.. response -headers (get "Content-Type"))]
                (s/includes? content-type "application/javascript")))
            #(dispatch [::validate-hash params %])
            #(log/debug (str "command.js wasn't found at " url))))


(defn fetch-commands!
  [_ [{{:keys [whisper-identity
               dapp-url
               bot-url
               dapp?]} :contact
       :as             params}]]
  (if bot-url
    (if-let [resource (js-res/get-resource bot-url)]
      (dispatch [::validate-hash params resource])
      (http-get-commands params bot-url))
    (when-not dapp?
      ;; TODO: this part should be removed in the future
      (dispatch [::validate-hash params js-res/wallet-js]))))

(defn dispatch-loaded!
  [db [{{:keys [whisper-identity]} :contact
        :as                        params} file]]
  (if (:command-hash-valid? db)
    (dispatch [::parse-commands! params file])
    (dispatch [::loading-failed! whisper-identity ::wrong-hash])))

(defn get-hash-by-identity
  [db identity]
  (get-in db [:contacts/contacts identity :dapp-hash]))

(defn get-hash-by-file
  [file]
  ;; todo tbd hashing algorithm
  (hash file))

(defn parse-commands!
  [_ [{{:keys [whisper-identity]} :contact
       :keys                      [callback]}
      file]]
  (let [data             (local-storage/get-data whisper-identity)
        local-storage-js (js-res/local-storage-data data)]
    (status/parse-jail
      whisper-identity (str local-storage-js file)
      (fn [result]
        (let [{:keys [error result]} (json->clj result)]
          (log/debug "Parsing commands results: " error result)
          (if error
            (dispatch [::loading-failed! whisper-identity ::error-in-jail error])
            (do
              (dispatch [::add-commands whisper-identity file result])
              (when callback (callback)))))))))

(defn validate-hash
  [db [_ file]]
  (let [valid? true
        ;; todo check
        #_(= (get-hash-by-identity db identity)
             (get-hash-by-file file))]
    (assoc db :command-hash-valid? valid?)))

(defn each-merge [coll with]
  (->> coll
       (map (fn [[k v]] [k (merge v with)]))
       (into {})))

(defn filter-commands [account {:keys [contacts chat-id] :as chat} commands]
  (->> commands
       (remove (fn [[_ {:keys [registered-only name]}]]
                 (and (not (:address account))
                      (not= name "global")
                      registered-only)))
       ;; TODO: this part should be removed because it's much better to provide the ability to do this in the API
       (map (fn [[k {:keys [name] :as v}]]
              [k (assoc v :hidden? (and (some #{name} ["send" "request"])
                                        (= chat-id wallet-chat-id)))]))
       (remove (fn [[k _]]
                 (and (= (count contacts) 1)
                      (not= console-chat-id (get (first contacts) :identity))
                      (h/matches (name k) "password"))))
       (into {})))

(defn get-mailmans-commands [db]
  (->> (get-in db [:contacts/contacts bots-constants/mailman-bot :commands])
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
  [{:keys [chats] :as db} [id _ {:keys [commands responses subscriptions]}]]
  (let [account          @(subscribe [:get-current-account])
        chat             (get chats id)
        commands'        (filter-commands account chat commands)
        responses'       (filter-commands account chat responses)
        global-command   (:global commands')
        commands''       (each-merge (apply dissoc commands' [:init :global])
                                     {:type     :command
                                      :owner-id id})
        mailman-commands (get-mailmans-commands db)]
    (cond-> db

            true
            (update-in [:contacts/contacts id] assoc
                       :commands-loaded? true
                       :commands (merge mailman-commands commands'')
                       :responses (each-merge responses' {:type     :response
                                                          :owner-id id})
                       :subscriptions subscriptions)

            global-command
            (update :global-commands assoc (keyword id)
                    (assoc global-command :bot id
                                          :type :command))

            (= id bots-constants/mailman-bot)
            (update :contacts/contacts (fn [contacts]
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
  [{:keys [global-commands] :contacts/keys [contacts]} [id]]
  (let [command   (get global-commands (keyword id))
        commands  (get-in contacts [id :commands])
        responses (get-in contacts [id :responses])]
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
    (fn [{:contacts/keys [contacts]} [identity callback]]
      (if (get-in contacts [identity :commands-loaded?])
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
   (after #(dispatch [:update-suggestions]))
   (after (fn [_ [id]]
            (dispatch [:invoke-commands-loading-callbacks id])
            (dispatch [:invoke-chat-loaded-callbacks id])))
   (after (fn [{:contacts/keys [contacts]} [id]]
            (let [subscriptions (get-in contacts [id :subscriptions])]
              (doseq [[name opts] subscriptions]
                (dispatch [:register-bot-subscription
                           (assoc opts :bot id
                                       :name name)])))))]

  add-commands)

(reg-handler ::loading-failed! (u/side-effect! loading-failed!))

(reg-handler :add-commands-loading-callback
  (fn [db [chat-id callback]]
    (update-in db [:commands-callbacks chat-id] conj callback)))

(reg-handler :invoke-commands-loading-callbacks
  (u/side-effect!
    (fn [db [chat-id]]
      (let [callbacks (get-in db [:commands-callbacks chat-id])]
        (doseq [callback callbacks]
          (callback))
        (dispatch [::clear-commands-callbacks chat-id])))))

(reg-handler ::clear-commands-callbacks
  (fn [db [chat-id]]
    (assoc-in db [:commands-callbacks chat-id] nil)))
