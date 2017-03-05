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
            [status-im.utils.homoglyph :as h]
            [status-im.utils.js-resources :as js-res]
            [status-im.chat.sign-up :as sign-up]))

(defn load-commands!
  [{:keys [current-chat-id contacts]} [identity callback]]
  (let [identity (or identity current-chat-id)
        contact  (or (get contacts identity)
                     sign-up/console-contact)]
    (when identity
      (dispatch [::fetch-commands! {:contact  contact
                                    :callback callback}])))
  ;; todo uncomment
  #_(if-let [{:keys [file]} (commands/get-by-chat-id identity)]
      (dispatch [::parse-commands! identity file])
      (dispatch [::fetch-commands! identity])))

(defn fetch-commands!
  [_ [{{:keys [dapp? dapp-url bot-url whisper-identity]} :contact
       :as                                               params}]]
  (cond
    (js-res/local-resource? bot-url)
    (dispatch [::validate-hash params (js-res/get-resource bot-url)])

    (and dapp? dapp-url)
    (http-get (s/join "/" [dapp-url "commands.js"])
              #(dispatch [::validate-hash whisper-identity %])
              #(dispatch [::validate-hash whisper-identity js-res/dapp-js]))

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
       (remove (fn [[_ {:keys [registered-only]}]]
                 (and (not (:address account))
                      registered-only)))
       (remove (fn [[n]]
                 (and
                   (not= console-chat-id id)
                   (h/matches (name n) "password"))))
       (into {})))

(defn add-commands
  [db [id _ {:keys [commands responses autorun]}]]
  (let [account        @(subscribe [:get-current-account])
        commands'      (filter-forbidden-names account id commands)
        global-command (:global commands')
        commands''     (apply dissoc commands' [:init :global])
        responses'     (filter-forbidden-names account id responses)]
    (cond-> db

            (get-in db [:chats id])
            (update-in [:chats id] assoc
                       :commands (mark-as :command commands'')
                       :responses (mark-as :response responses')
                       :commands-loaded true
                       :autorun autorun
                       :global-command global-command)

            global-command
            (update :global-commands assoc (keyword id)
                    (assoc global-command :bot id
                                          :type :command)))))

(defn save-commands-js!
  [_ [id file]]
  #_(commands/save {:chat-id id :file file}))

(defn save-global-command!
  [{:keys [global-commands]} [id]]
  (let [command (get global-commands (keyword id))]
    (when command
      (contacts/save {:whisper-identity id
                      :global-command   command}))))

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
    (fn [{:keys [chats]} [identity callback]]
      (if (get-in chats [identity :commands-loaded])
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
   (after save-global-command!)
   (after #(dispatch [:check-autorun]))
   (after (fn [_ [id]]
            (dispatch [:invoke-commands-loading-callbacks id])
            (dispatch [:invoke-chat-loaded-callbacks id])))]
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
