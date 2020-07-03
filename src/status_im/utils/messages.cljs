(ns status-im.utils.messages
  (:require [clojure.string :as string]))

(defn replace-several [content & replacements]
  (let [replacement-list (partition 2 replacements)]
    (reduce #(apply string/replace %1 %2) content replacement-list)))

(defn sanitize-text-for-parse
  "These characters cause JSON payloads to fail being sent over the bridge without proper scrubbing"
  [text]
  (replace-several text
                   #"\n" "\\n"
                   #"\r" "\\r"
                   #"\t" "\\t"
                   #"\'" ""))

(defn format-message-client
  [[message-id, {:keys [content] :as message}]]
  (let [reduced-message (select-keys message [:timestamp :from :alias :message-id])]
    (merge {:text (sanitize-text-for-parse (:text content))} reduced-message)))

(defn messages-format-js
  [messages]
  (let [chat-keys (keys messages)
        chat-content (mapcat #(get messages %) chat-keys)
        reduced-content (map format-message-client chat-content)]
    reduced-content))
