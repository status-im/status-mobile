(ns status-im.commands.handlers.loading
  (:require [re-frame.core :refer [path after dispatch subscribe trim-v debug]]
            [status-im.utils.handlers :as u]
            [status-im.utils.utils :refer [http-get show-popup]]
            [clojure.string :as s]
            [status-im.data-store.commands :as commands]
            [status-im.components.status :as status]
            [status-im.utils.types :refer [json->clj]]
            [status-im.commands.utils :refer [reg-handler]]
            [status-im.constants :refer [console-chat-id wallet-chat-id]]
            [taoensso.timbre :as log]
            [status-im.i18n :refer [label]]
            [status-im.utils.homoglyph :as h]
            [status-im.utils.js-resources :as js-res]
            [status-im.utils.random :as random]))

(def commands-js "commands.js")

(defn load-commands!
  [{:keys [current-chat-id contacts]} [identity]]
  (let [identity (or identity current-chat-id)
        contact  (or (get contacts identity)
                     {:whisper-identity identity})]
    (when identity
      (dispatch [::fetch-commands! contact])))
  ;; todo uncomment
  #_(if-let [{:keys [file]} (commands/get-by-chat-id identity)]
      (dispatch [::parse-commands! identity file])
      (dispatch [::fetch-commands! identity])))

(defn fetch-commands!
  [_ [{:keys [whisper-identity dapp? dapp-url]}]]
  (cond
    (= console-chat-id whisper-identity)
    (dispatch [::validate-hash whisper-identity js-res/console-js])

    (= wallet-chat-id whisper-identity)
    (dispatch [::validate-hash whisper-identity js-res/wallet-js])

    (and dapp? dapp-url)
    (http-get (s/join "/" [dapp-url commands-js])
              (fn [response]
                (and
                  (string? (.text response))
                  (when-let [content-type (.. response -headers (get "Content-Type"))]
                    (s/includes? "application/javascript" content-type))))
              #(dispatch [::validate-hash whisper-identity %])
              #(dispatch [::validate-hash whisper-identity js-res/dapp-js]))

    :else
    (dispatch [::validate-hash whisper-identity js-res/commands-js])))

(defn dispatch-loaded!
  [db [identity file]]
  (if (::valid-hash db)
    (dispatch [::parse-commands! identity file])
    (dispatch [::loading-failed! identity ::wrong-hash])))

(defn get-hash-by-identity
  [db identity]
  (get-in db [:contacts identity :dapp-hash]))

(defn get-hash-by-file
  [file]
  ;; todo tbd hashing algorithm
  (hash file))

(defn parse-commands! [_ [identity file]]
  (status/parse-jail identity file
                     (fn [result]
                       (let [{:keys [error result]} (json->clj result)]
                         (log/debug "Error parsing commands: " error result)
                         (if error
                           (dispatch [::loading-failed! identity ::error-in-jail error])
                           (if identity
                             (dispatch [::add-commands identity file result])
                             (dispatch [::add-all-commands result])))))))

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
  (let [account    @(subscribe [:get-current-account])
        commands'  (filter-forbidden-names account id commands)
        responses' (filter-forbidden-names account id responses)]
    (-> db
        (assoc-in [id :commands] (mark-as :command commands'))
        (assoc-in [id :responses] (mark-as :response responses'))
        (assoc-in [id :commands-loaded] true)
        (assoc-in [id :autorun] autorun))))

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
      (log/debug m))))

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
   (after #(dispatch [:update-suggestions]))
   (after (fn [_ [id]]
            (dispatch [:invoke-commands-loading-callbacks id])
            (dispatch [:invoke-chat-loaded-callbacks id])))]
  add-commands)

(reg-handler ::add-all-commands
  (fn [db [{:keys [commands responses]}]]
    (assoc db :all-commands {:commands  (mark-as :command commands)
                             :responses (mark-as :response responses)})))

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
