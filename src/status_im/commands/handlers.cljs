(ns status-im.commands.handlers
  (:require [re-frame.core :refer [register-handler after dispatch subscribe
                                   trim-v debug]]
            [status-im.utils.handlers :as u]
            [status-im.components.react :as r]
            [status-im.utils.utils :refer [http-get toast]]
            [clojure.string :as s]
            [status-im.persistence.realm :as realm]
            [status-im.components.jail :as j]
            [clojure.walk :as w]
            [status-im.components.react :refer [text scroll-view view
                                                image touchable-highlight]]
            [clojure.set :as set]))

(defn reg-handler
  ([name handler] (reg-handler name nil handler))
  ([name middleware handler]
   (register-handler name [debug trim-v middleware] handler)))

(def commands-js "commands.js")

(defn load-commands!
  [_ [identity]]
  (dispatch [::fetch-commands! identity])
  ;; todo uncomment
  #_(if-let [{:keys [file]} (realm/get-one-by-field :commands :chat-id
                                                    identity)]
      (dispatch [::parse-commands! identity file])
      (dispatch [::fetch-commands! identity])))

(defn fetch-commands!
  [db [identity]]
  (when-let [url (:dapp-url (get-in db [:chats identity]))]
    (http-get (s/join "/" [url commands-js])
              #(dispatch [::validate-hash identity %])
              #(dispatch [::loading-failed! identity ::file-was-not-found]))))

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

(defn json->cljs [json]
  (if (= json "undefined")
    nil
    (js->clj (.parse js/JSON json) :keywordize-keys true)))

(defn parse-commands! [_ [identity file]]
  (j/parse identity file
           (fn [result]
             (let [commands (json->cljs result)]
               ;; todo use commands from jail
               (dispatch [::add-commands identity file commands])))
           #_(dispatch [::loading-failed! identity ::error-in-jail %])))

(defn validate-hash
  [db [identity file]]
  (let [valid? true
        ;; todo check
        #_(= (get-hash-by-identity db identity)
             (get-hash-by-file file))]
    (println :hash
             (get-hash-by-identity db identity)
             (get-hash-by-file file))
    (assoc db ::valid-hash valid?)))

(defn add-commands
  [db [id _ {:keys [commands responses]}]]
  (-> db
      (update-in [:chats id :commands] merge commands)
      (update-in [:chats id :responses] merge responses)))

(defn save-commands-js!
  [_ [id file]]
  (realm/create-object :commands {:chat-id id :file file}))

(defn loading-failed!
  [db [id reason details]]
  (let [url (get-in db [:chats id :dapp-url])]
    (toast (s/join "\n" ["commands.js loading failed"
                         url
                         id
                         (name reason)
                         details]))))

(defn init-render-command!
  [_ [chat-id command message-id data]]
  (j/call chat-id [command :render] data
          (fn [res]
            (dispatch [::render-command chat-id message-id (json->cljs res)]))))

(def elements
  {:text        text
   :view        view
   :scroll-view scroll-view
   :image       image
   :touchable   touchable-highlight})

(defn get-element [n]
  (elements (keyword (.toLowerCase n))))

(def events #{:onPress})

(defn wrap-event [event]
  #(dispatch [:suggestions-event! event]))

(defn check-events [m]
  (let [ks  (set (keys m))
        evs (set/intersection ks events)]
    (reduce #(update %1 %2 wrap-event) m evs)))

(defn generate-hiccup [markup]
  ;; todo implement validation
  (w/prewalk
    (fn [el]
      (if (and (vector? el) (string? (first el)))
        (-> el
            (update 0 get-element)
            (update 1 check-events))
        el))
    markup))

(defn render-command
  [db [chat-id message-id markup]]
  (let [hiccup (generate-hiccup markup)]
    (assoc-in db [:rendered-commands chat-id message-id] hiccup)))

(def console-events
  {:save-password   #(dispatch [:save-password %])
   :sign-up         #(dispatch [:sign-up %])
   :confirm-sign-up #(dispatch [:sign-up-confirm %])})

(def regular-events {})

(defn command-nadler!
  [_ [{:keys [to]} response]]
  (let [{:keys [event params]} (json->cljs response)
        events (if (= "console" to)
                 (merge regular-events console-events)
                 regular-events)]
    (when-let [handler (events (keyword event))]
      (apply handler params))))

(defn suggestions-handler
  [db [_ response-json]]
  (let [response (json->cljs response-json)]
    (println response)
    (assoc db :current-suggestion (generate-hiccup response))))

(defn suggestions-events-handler!
  [db [[n data]]]
  (case (keyword n)
    :set-value (dispatch [:set-chat-command-content data])
    db))

(reg-handler :load-commands! (u/side-effect! load-commands!))
(reg-handler ::fetch-commands! (u/side-effect! fetch-commands!))

(reg-handler ::validate-hash
             (after dispatch-loaded!)
             validate-hash)

(reg-handler ::parse-commands! (u/side-effect! parse-commands!))

(reg-handler ::add-commands
             (after save-commands-js!)
             add-commands)

(reg-handler ::loading-failed! (u/side-effect! loading-failed!))

(reg-handler :init-render-command! init-render-command!)
(reg-handler ::render-command render-command)

(reg-handler :command-handler! (u/side-effect! command-nadler!))
(reg-handler :suggestions-handler suggestions-handler)
(reg-handler :suggestions-event! (u/side-effect! suggestions-events-handler!))
