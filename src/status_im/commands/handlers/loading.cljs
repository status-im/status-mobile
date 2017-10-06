(ns status-im.commands.handlers.loading
  (:require [re-frame.core :refer [path after dispatch subscribe trim-v debug]]
            [status-im.utils.handlers :as u]
            [status-im.utils.utils :refer [http-get show-popup]]
            [clojure.string :as s]
            [status-im.data-store.contacts :as contacts]
            [status-im.data-store.local-storage :as local-storage]
            [status-im.commands.utils :refer [reg-handler]]
            [status-im.constants :refer [console-chat-id]]
            [status-im.chat.models.commands :as commands-model]
            [status-im.native-module.core :as status]
            [status-im.utils.homoglyph :as h]
            [status-im.utils.js-resources :as js-res]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]))


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
       callback        :callback
       :as             params}]]
  (if bot-url
    (if-let [resource (js-res/get-resource bot-url)]
      (dispatch [::validate-hash params resource])
      (http-get-commands params bot-url))
    (when callback (callback))))

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
  [{:networks/keys [networks]
    :keys          [network]
    :as            db}
   [{{:keys [whisper-identity]} :contact
     :keys                      [callback]}
    file]]
  (let [data             (local-storage/get-data whisper-identity)
        local-storage-js (js-res/local-storage-data data)
        network-id       (get-in networks [network :raw-config :NetworkId])
        ethereum-id-js   (js-res/network-id network-id)]
    (status/parse-jail
     whisper-identity (str local-storage-js ethereum-id-js file)
     (fn [result]
       (let [{:keys [error result]} (types/json->clj result)]
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

(defn each-merge [with coll]
  (->> coll
       (map (fn [[k v]] [k (merge v with)]))
       (into {})))

(defn extract-commands [{:keys [contacts]} commands]
  (->> commands
       (remove (fn [[_ {:keys [name]}]]
                 (and (= (count contacts) 1)
                      (not= console-chat-id (get (first contacts) :identity))
                      (h/matches name "password"))))
       (map (fn [[k {:keys [name scope] :as v}]]
              [[name scope] v]))
       (into {})))

(defn extract-global-commands [commands chat-id]
  (->> commands
       (filter (fn [[_ {:keys [scope]}]]
                 (:global? scope)))
       (map (fn [[k {:keys [name scope] :as v}]]
              [[name scope] (assoc v :bot chat-id :type :command)]))
       (into {})))

(defn transform-commands [commands]
  (reduce (fn [m [_ {:keys [name scope] :as v}]]
            (update m (keyword name) conj v))
          {}
          commands))

(defn add-commands
  [{:keys [current-account-id accounts chats] :as db}
   [id _ {:keys [commands responses subscriptions]}]]
  (let [account          (get accounts current-account-id)
        chat             (get chats id)
        global-commands  (extract-global-commands commands id)
        mixable-commands (when-not (get-in db [:contacts/contacts id :mixable?])
                           (commands-model/get-mixable-commands db))
        commands'        (->> (apply dissoc commands (keys global-commands))
                              (extract-commands chat)
                              (each-merge {:type     :command
                                           :owner-id id})
                              (transform-commands)
                              (merge mixable-commands))
        responses'       (->> (extract-commands chat responses)
                              (each-merge {:type     :response
                                           :owner-id id})
                              (transform-commands))
        new-db           (-> db
                             (update-in [:contacts/contacts id] assoc
                                        :commands-loaded? true
                                        :commands commands'
                                        :responses responses'
                                        :subscriptions subscriptions)
                             (update :global-commands merge (transform-commands global-commands)))]
    new-db))

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
  (after (fn [_ [id]]
           (dispatch [:invoke-commands-loading-callbacks id])
           (dispatch [:update-suggestions])))
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
