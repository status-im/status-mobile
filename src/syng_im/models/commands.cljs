(ns syng-im.models.commands
  (:require [clojure.string :refer [join split]]
            [clojure.walk :refer [stringify-keys keywordize-keys]]
            [cljs.core.async :as async :refer [chan put! <! >!]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.db :as db]
            [syng-im.models.chat :refer [current-chat-id]]
            [syng-im.utils.utils :refer [log toast]]
            [syng-im.persistence.realm :as realm]))

(def commands [{:command :money
                :text "!money"
                :description "Send money"
                :color "#48ba30"
                :suggestion true}
               {:command :location
                :text "!location"
                :description "Send location"
                :color "#9a5dcf"
                :suggestion true}
               {:command :phone
                :text "!phone"
                :description "Send phone number"
                :color "#48ba30"
                :suggestion true}
               {:command :confirmation-code
                :text "!confirmationCode"
                :description "Send confirmation code"
                :color "#019af0"
                :suggestion true}
               {:command :send
                :text "!send"
                :description "Send location"
                :color "#9a5dcf"
                :suggestion true}
               {:command :request
                :text "!request"
                :description "Send request"
                :color "#48ba30"
                :suggestion true}
               {:command :keypair-password
                :text "!keypairPassword"
                :description ""
                :color "#019af0"
                :suggestion false}
               {:command :help
                :text "!help"
                :description "Help"
                :color "#9a5dcf"
                :suggestion true}])

(def suggestions (filterv :suggestion commands))

(defn get-command [command-key]
  (first (filter #(= command-key (:command %)) commands)))

(defn get-chat-command-content [db]
  (get-in db (db/chat-command-content-path (current-chat-id db))))

(defn set-chat-command-content [db content]
  (assoc-in db (db/chat-command-content-path (get-in db db/current-chat-id-path))
            content))

(defn get-chat-command [db]
  (get-in db (db/chat-command-path (current-chat-id db))))

(defn set-chat-command [db command-key]
  (-> db
      (set-chat-command-content nil)
      (assoc-in (db/chat-command-path (get-in db db/current-chat-id-path))
                (get-command command-key))))

(defn get-chat-command-request [db]
  (get-in db (db/chat-command-request-path (current-chat-id db))))

(defn set-chat-command-request [db handler]
  (assoc-in db (db/chat-command-request-path (current-chat-id db)) handler))


(defn- map-to-str
  [m]
  (join ";" (map #(join "=" %) (stringify-keys m))))

(defn- str-to-map
  [s]
  (keywordize-keys (apply hash-map (split s #"[;=]"))))

;; TODO store command key in separate field
(defn format-command-msg-content [command content]
  (map-to-str {:command (name command) :content content}))

(defn parse-command-msg-content [content]
  (update (str-to-map content) :command #(get-command (keyword %))))

(defn format-command-request-msg-content [command content]
  (map-to-str {:command (name command) :content content}))

(defn parse-command-request-msg-content [content]
  (update (str-to-map content) :command #(get-command (keyword %))))
