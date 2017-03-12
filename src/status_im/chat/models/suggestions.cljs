(ns status-im.chat.models.suggestions
  (:require [status-im.chat.constants :as chat-consts]
            [clojure.string :as str]))

(defn suggestion? [text]
  (= (get text 0) chat-consts/command-char))

(defn can-be-suggested? [text]
  (fn [{:keys [name]}]
    (let [text' (cond
                  (.startsWith text chat-consts/command-char)
                  text

                  (str/blank? text)
                  chat-consts/command-char

                  :default
                  nil)]
      (.startsWith (str chat-consts/command-char name) text'))))

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