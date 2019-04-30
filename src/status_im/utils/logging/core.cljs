(ns status-im.utils.logging.core
  (:require [re-frame.core :as re-frame]
            [status-im.native-module.core :as status]
            [status-im.utils.fx :as fx]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]
            [status-im.utils.config :as config]))

(def max-log-entries 1000)
(def logs-queue (atom #queue[]))
(defn add-log-entry [entry]
  (swap! logs-queue conj entry)
  (when (>= (count @logs-queue) max-log-entries)
    (swap! logs-queue pop)))

(defn init-logs []
  (log/set-level! config/log-level)
  (log/debug)
  (log/merge-config!
   {:output-fn (fn [& data]
                 (let [res (apply log/default-output-fn data)]
                   (add-log-entry res)
                   res))}))

(defn print-js-logs []
  (clojure.string/join "\n" @logs-queue))

(fx/defn send-logs
  [{:keys [db] :as cofx}]
  ;; TODO: Add message explaining db export
  (let [db-json (types/clj->json (select-keys db [:app-state
                                                  :current-chat-id
                                                  :initial-props
                                                  :keyboard-height
                                                  :keyboard-max-height
                                                  :navigation-stack
                                                  :network
                                                  :network-status
                                                  :peers-count
                                                  :peers-summary
                                                  :sync-state
                                                  :tab-bar-visible?
                                                  :view-id
                                                  :chat/cooldown-enabled?
                                                  :chat/cooldowns
                                                  :chat/last-outgoing-message-sent-at
                                                  :chat/spam-messages-frequency
                                                  :chats/loading?
                                                  :desktop/desktop
                                                  :dimensions/window
                                                  :my-profile/editing?
                                                  :node/status]))]
    (status/send-logs db-json (print-js-logs))))
