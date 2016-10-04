(ns status-im.commands.handlers.loading
  (:require-macros [status-im.utils.slurp :refer [slurp]])
  (:require [re-frame.core :refer [path after dispatch subscribe trim-v debug]]
            [status-im.utils.handlers :as u]
            [status-im.utils.utils :refer [http-get show-popup]]
            [clojure.string :as s]
            [status-im.data-store.commands :as commands]
            [status-im.components.status :as status]
            [status-im.utils.types :refer [json->clj]]
            [status-im.commands.utils :refer [reg-handler]]
            [status-im.constants :refer [console-chat-id wallet-chat-id]]))

(def commands-js "commands.js")

(defn load-commands!
  [_ [identity]]
  (dispatch [::fetch-commands! identity])
  ;; todo uncomment
  #_(if-let [{:keys [file]} (commands/get-by-chat-id identity)]
      (dispatch [::parse-commands! identity file])
      (dispatch [::fetch-commands! identity])))

(defn fetch-commands!
  [db [identity]]
  (when true
    ;-let [url (get-in db [:chats identity :dapp-url])]
    (cond
      (= console-chat-id identity)
      (dispatch [::validate-hash identity (slurp "resources/console.js")])

      (= wallet-chat-id identity)
      (dispatch [::validate-hash identity (slurp "resources/wallet.js")])

      :else
      (dispatch [::validate-hash identity (slurp "resources/commands.js")])
      #_(http-get (s/join "/" [url commands-js])

                #(dispatch [::validate-hash identity %])
                #(dispatch [::loading-failed! identity ::file-was-not-found])))))

(defn dispatch-loaded!
  [db [identity file]]
  (if (::valid-hash db)
    (dispatch [::parse-commands! identity file])
    (dispatch [::loading-failed! identity ::wrong-hash])))

(defn get-hash-by-identity
  [db identity]
  (get-in db [:chats identity :dapp-hash]))

(defn get-hash-by-file
  [file]
  ;; todo tbd hashing algorithm
  (hash file))

(defn parse-commands! [_ [identity file]]
  (status/parse-jail identity file
                     (fn [result]
                       (let [{:keys [error result]} (json->clj result)]
                         (if error
                           (dispatch [::loading-failed! identity ::error-in-jail error])
                           (dispatch [::add-commands identity file result]))))))

(defn validate-hash
  [db [identity file]]
  (let [valid? true
        ;; todo check
        #_(= (get-hash-by-identity db identity)
             (get-hash-by-file file))]
    (assoc db ::valid-hash valid?)))

(defn mark-as [as coll]
  (->> coll
       (map (fn [[k v]] [k (assoc v :type as)]))
       (into {})))

(defn add-commands
  [db [id _ {:keys [commands responses autorun] :as data}]]
  (-> db
      (update-in [id :commands] merge (mark-as :command commands))
      (update-in [id :responses] merge (mark-as :response responses))
      (assoc-in [id :commands-loaded] true)
      (assoc-in [id :autorun] autorun)))

(defn save-commands-js!
  [_ [id file]]
    (commands/save {:chat-id id :file file}))

(defn loading-failed!
  [db [id reason details]]
  (let [url (get-in db [:chats id :dapp-url])]
    (let [m (s/join "\n" ["commands.js loading failed"
                          url
                          id
                          (name reason)
                          details])]
      (show-popup "Error" m)
      (println m))))

(reg-handler :load-commands! (u/side-effect! load-commands!))
(reg-handler ::fetch-commands! (u/side-effect! fetch-commands!))

(reg-handler ::validate-hash
  (after dispatch-loaded!)
  validate-hash)

(reg-handler ::parse-commands! (u/side-effect! parse-commands!))

(reg-handler ::add-commands
  [(path :chats)
   (after save-commands-js!)
   (after #(dispatch [:check-autorun]))
   (after (fn [_ [id]]
            (dispatch [:invoke-commands-loading-callbacks id])))]
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
