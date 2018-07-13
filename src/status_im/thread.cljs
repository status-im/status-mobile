(ns status-im.thread
  (:require [cognitect.transit :as transit]
            [re-frame.db]
            [status-im.tron :as tron]
            [taoensso.timbre :as log]
            [status-im.utils.config :as config]
            [re-frame.core :as re-frame]
            [status-im.ui.components.permissions :as permissions]
            [status-im.utils.utils :as utils]
            [status-im.i18n :as i18n]))

(def rn-threads (.-Thread (js/require "react-native-threads")))

(goog-define platform "android")

(def thread (atom nil))
(def writer (transit/writer :json))
(def reader (transit/reader :json))

(def initialized? (atom false))
(def calls (atom []))

(defn post [data-str]
  (tron/log (str "Post message: " data-str))
  (.postMessage @thread data-str))

(defn make-stored-calls []
  (doseq [call @calls]
    (post call)))

(defn dispatch [args]
  (if (config/handlers-thread?)
    (do
      (log/debug "Th " config/thread args)
      (re-frame/dispatch args))
    (let [args' (transit/write writer args)]
      (if @initialized?
        (post args')
        (swap! calls conj args')))))

(defn- update-chats [db {:keys [changed-chats removed-chats]}]
  (reduce (fn [db [chat-id {:keys [changed-data removed-keys]}]]
            (update-in db [:chats chat-id]
                       (fn [chat]
                         (cond-> (merge chat changed-data)

                           removed-keys
                           (as-> db
                                 (apply dissoc db removed-keys))))))
          (if removed-chats
            (update db :chats #(apply dissoc % removed-chats))
            db)
          changed-chats))

(defn- update-db [{:keys [changed-data removed-keys chats]}]
  (swap! re-frame.db/app-db
         (fn [db]
           (let [db' (merge db changed-data)]
             (cond-> db'

               removed-keys
               (as-> db' (apply dissoc db' removed-keys))

               chats
               (update-chats chats))))))

(defn- request-permissions
  [{:keys                   [permissions on-allowed]
    {:keys [title content]} :on-denied}]
  (permissions/request-permissions
   {:permissions permissions
    :on-allowed  #(status-im.thread/dispatch on-allowed)
    :on-denied   (fn []
                   (utils/set-timeout
                    #(utils/show-popup title content)
                    50))}))

(defn- response-handler [data]
  (let [[event event-data] (transit/read reader data)]
    (log/debug "Response from thread " event)

    (case event
      :initialized (do (reset! initialized? true)
                       (make-stored-calls))
      :db (update-db event-data)

      :request-permissions (request-permissions event-data))))

(defn start []
  (let [new-thread (rn-threads. (str "worker." platform ".js"))]
    (set! (.-onmessage new-thread) response-handler)
    (reset! thread new-thread)))

