(ns status-im.components.jail
  (:require-macros [status-im.utils.slurp :refer [slurp]])
  (:require [status-im.components.react :as r]
            [status-im.utils.types :as t]))

(def status-js (slurp "resources/status.js"))

(def jail
  (when (exists? (.-NativeModules r/react))
    (.-Jail (.-NativeModules r/react))))

(when jail
  (.init jail status-js))

(defn parse [chat-id file callback]
  (.parse jail chat-id file callback))

(defn cljs->json [data]
  (.stringify js/JSON (clj->js data)))

(defn call [chat-id path params callback]
  (println :call chat-id (cljs->json path) (cljs->json params))
  (let [cb (fn [r]
             (let [r' (t/json->clj r)]
               (println r')
               (callback r')))]
    (.call jail chat-id (cljs->json path) (cljs->json params) cb)))

(def adjust-resize 16)
(def adjust-pan 32)
