(ns status-im.components.jail
  (:require [status-im.components.react :as r]))

(def jail (.-Jail (.-NativeModules r/react)))

(defn parse [chat-id file success-callback fail-callback]
  (.parse jail chat-id file success-callback fail-callback))

(defn call
  [chat-id path params callback]
  (.call jail chat-id (clj->js path) (clj->js params) callback))

(defn add-listener
  [chat-id callback]
  (.addListener jail chat-id callback))
