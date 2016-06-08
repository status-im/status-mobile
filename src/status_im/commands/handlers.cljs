(ns status-im.commands.handlers
  (:require [re-frame.core :refer [register-handler after dispatch subscribe
                                   trim-v debug]]
            [status-im.utils.handlers :as u]
            [status-im.components.react :as r]
            [status-im.utils.utils :refer [http-get toast]]
            [clojure.string :as s]))

;; commands loading flow
;           ┌────────────────────────────┐
;           │ user starts chat with dapp │
;           └────────────────────────────┘
;                          │
;                          ▼
;              ┌───────────────────────┐
;              │     fetch current     │
;              │  `commands.js` hash   │
;              └───────────────────────┘
;                          │
;                          ▼
;            ┌───────────────────────────┐
;            │try to fetch `commands.js` │
;            └───────────────────────────┘
;                          │
;                          ▼
;┌───┐        ┌─────────────────────────┐    ┌───┐
;│no ├────────│ there is `commands.js`  │────┤yes│
;└─┬─┘        └─────────────────────────┘    └─┬─┘
;  │                                           │
;  │                                           ▼
;  │                                 ┌───────────────────┐
;  │          ┌────┐                 │  Is file's hash   │
;  │          │nope├─────────────────│ equal to current? │
;  │          └─┬──┘                 └───────────────────┘
;  │            │                              │
;  │            │                            ┌─┴─┐
;  │            │                            │yes│
;  │            │                            └─┬─┘
;  │            │                              │
;  │            │                              ▼
;  │            │     ┌────┐  ┌──────────────────────────┐  ┌────┐
;  │            │     │fail├──│ ask `otto` to handle js  │──┤succ│
;  │            │     └──┬─┘  └──────────────────────────┘  └─┬──┘
;  │            │        │                                    │
;  │            │        │                                    ▼
;  │            │        │                     ┌────────────────────────────┐
;  │            │        ▼                     │       save commands        │
;  │            │  ┌────────────────────────┐  │         save js ?          │
;  │            │  │the dapp emit a message │  │  add some API object form  │
;  │            └─▶│  saying js is broken   │  │       otto to app-db       │
;  │               └────────────────────────┘  └────────────────────────────┘
;  │                            │                             │
;  │                            │                             │
;  │        ┌───────────────────┘                             ▼
;  │        │                                    ┌─────────────────────────┐
;  │        │                                    │ if it is necessary show │
;  │        ▼                                    │      as fullscreen      │
;  │  ┌───────────┐                              └─────────────────────────┘
;  │  │           │                                           │
;  └─▶│   Fin'    │◀──────────────────────────────────────────┘
;     │           │
;     └───────────┘

(defn reg-handler
  ([name handler] (reg-handler name nil handler))
  ([name middleware handler]
   (register-handler name [debug trim-v middleware] handler)))

(def commands-js "commands.js")

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

(defn get-hash-by-dentity
  [db identity]
  (get-in db [:chats identity :dapp-hash]))

(defn get-hash-by-file
  [file]
  ;; todo implement
  123)

(defn get-jail []
  (.-Jail (.-NativeModules r/react)))

(defn parse [file success-callback fail-callback]
  (.parse (get-jail) file success-callback fail-callback))

(defn json->clj [json]
  (js->clj (.parse js/JSON json) :keywordize-keys true))

(defn parse-commands! [_ [identity file]]
  (parse file
         (fn [result]
           (let [commands (json->clj result)]
             (dispatch [::add-commands identity commands])))
         #(dispatch [::loading-failed! identity ::error-in-jail %])))

(defn validate-hash
  [db [identity file]]
  (let [valid? (= (get-hash-by-dentity db identity)
                  (get-hash-by-file file))]
    (assoc db ::valid-hash valid?)))

(defn save-commands!
  [db]
  #_(realm/save (::new-commands db)))

(defn add-commands
  [db [id commands-obj]]
  (let [commands (:commands commands-obj)]
    (-> db
        (update-in [:chats id :commands] merge commands)
        (assoc db ::new-commands commands))))

(defn loading-failed
  [db [id reason details]]
  (let [url (get-in db [:chats id :dapp-url])]
    (toast (s/join "\n" ["commands.js loading failed"
                         url
                         id
                         (name reason)
                         details]))))

(reg-handler :load-commands! (u/side-effect! fetch-commands!))

(reg-handler ::validate-hash
             (after dispatch-loaded!)
             validate-hash)

(reg-handler ::parse-commands! (u/side-effect! parse-commands!))

(reg-handler ::add-commands
             (after save-commands!)
             add-commands)

(reg-handler ::loading-failed! (u/side-effect! loading-failed))

