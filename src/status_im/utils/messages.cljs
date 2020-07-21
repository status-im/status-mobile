(ns status-im.utils.messages
  (:require [clojure.string :as string]
            [status-im.utils.types :as util-types]
            [clojure.set :as clj-set]
            [status-im.browser.webview-ref :as webview-ref]))

(defn replace-several [content & replacements]
  (let [replacement-list (partition 2 replacements)]
    (reduce #(apply string/replace %1 %2) content replacement-list)))

(defn sanitize-text-for-parse
  "These characters cause JSON payloads to fail being sent over the bridge without proper scrubbing"
  [text]
  (replace-several text
                   #"\"\{" "{"
                   #"\"\"\{" "{"
                   #"\"\}" "}"
                   #":\"\{" ":{"
                   #"\}\"" "}"
                   #"\\\"" "\""
                   #"\n" ""
                   #"\r" "\\r"
                   #"\t" "\\t"
                   #"\'" ""))

(defn format-message-client
  [{:keys [content] :as message}]
  (let [reduced-message (select-keys message [:timestamp :from :alias :message-id])
        js-formatted (clj-set/rename-keys reduced-message {:message-id :messageId})]
    (merge {:text (:text content)} js-formatted)))

(defn messages-format-js
  [messages]
  (let [chat-keys (keys messages)
        chat-content (mapcat #(get messages %) chat-keys)
        chat-id (first chat-keys)
        flat-messages (map (comp format-message-client second) chat-content)
        payload {:chat chat-id :messages flat-messages}]
    payload))

(defn dapp-post-message
  [message]
  (let [^js webview @webview-ref/webview-ref
        msg (str "window.postMessage("
                 (str (util-types/serialize message))
                 ");"
                 newline "true;")]
    (when (and message webview)
      (.injectJavaScript webview msg))))
