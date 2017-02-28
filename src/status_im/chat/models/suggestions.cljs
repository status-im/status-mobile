(ns status-im.chat.models.suggestions
  (:require [status-im.chat.constants :as chat-consts]
            [clojure.string :as str]))

(defn can-be-suggested?
  ([text] (can-be-suggested? chat-consts/command-char :name text))
  ([first-char name-key text]
   (fn [command]
     (let [name (get command name-key)]
       (let [text' (cond
                     (.startsWith text first-char)
                     text

                     (str/blank? text)
                     first-char

                     :default
                     nil)]
         (.startsWith (str first-char name) text'))))))

(defn get-request-suggestions
  [{:keys [current-chat-id] :as db} text]
  (let [requests (get-in db [:chats current-chat-id :requests])]
    (->> requests
         (map (fn [{:keys [type] :as v}]
                (assoc v :name (get-in db [:chats current-chat-id :responses type :name]))))
         (filter (fn [v] ((can-be-suggested? text) v))))))

(defn get-command-suggestions
  [{:keys [current-chat-id] :as db} text]
  (let [commands (get-in db [:chats current-chat-id :commands])]
    (filter (fn [[_ v]] ((can-be-suggested? text) v)) commands)))

(defn get-global-command-suggestions
  [{:keys [global-commands] :as db} text]
  (filter (fn [[_ v]] ((can-be-suggested? chat-consts/bot-char :bot text) v))
          global-commands))
