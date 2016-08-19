(ns status-im.commands.handlers.loading
  (:require-macros [status-im.utils.slurp :refer [slurp]])
  (:require [re-frame.core :refer [path after dispatch subscribe trim-v debug]]
            [status-im.utils.handlers :as u]
            [status-im.utils.utils :refer [http-get toast]]
            [clojure.string :as s]
            [status-im.persistence.realm.core :as realm]
            [status-im.components.jail :as j]
            [status-im.utils.types :refer [json->clj]]
            [status-im.commands.utils :refer [reg-handler]]))

(def commands-js "commands.js")

(defn load-commands!
  [_ [identity]]
  (dispatch [::fetch-commands! identity])
  ;; todo uncomment
  #_(if-let [{:keys [file]} (realm/get-one-by-field :account :command
                                                    :chat-id identity)]
      (dispatch [::parse-commands! identity file])
      (dispatch [::fetch-commands! identity])))

(defn fetch-commands!
  [db [identity]]
  (when true
    ;;when-let [url (:dapp-url (get-in db [:chats identity]))]
    ;; todo fix this after demo
    (if true
      ;(= "console" identity)
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
  (j/parse identity file
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
  [db [id _ {:keys [commands responses]}]]
  (-> db
      (update-in [id :commands] merge (mark-as :command commands))
      (update-in [id :responses] merge (mark-as :response responses))
      (assoc-in [id :commands-loaded] true)))

(defn save-commands-js!
  [_ [id file]]
    (realm/create-object :account :command {:chat-id id :file file}))

(defn loading-failed!
  [db [id reason details]]
  (let [url (get-in db [:chats id :dapp-url])]
    (let [m (s/join "\n" ["commands.js loading failed"
                          url
                          id
                          (name reason)
                          details])]
      (toast m)
      (println m))))

(reg-handler :load-commands! (u/side-effect! load-commands!))
(reg-handler ::fetch-commands! (u/side-effect! fetch-commands!))

(reg-handler ::validate-hash
  (after dispatch-loaded!)
  validate-hash)

(reg-handler ::parse-commands! (u/side-effect! parse-commands!))

(reg-handler ::add-commands
  [(path :chats)
   (after save-commands-js!)]
  add-commands)

(reg-handler ::loading-failed! (u/side-effect! loading-failed!))
