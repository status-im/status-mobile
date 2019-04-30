(ns status-im.utils.logging.core
  (:require [re-frame.core :as re-frame]
            [status-im.native-module.core :as status]
            [status-im.utils.fx :as fx]
            [status-im.utils.types :as types]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.email :as mail]))

(def report-email "error-reports@status.im")

(re-frame/reg-fx
 :logs/archive-logs
 (fn [[db-json callback-handler]]
   (status/send-logs
    db-json
    #(re-frame/dispatch [callback-handler %]))))

(fx/defn send-logs
  [{:keys [db]}]
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
    {:logs/archive-logs [db-json ::send-email]}))

(handlers/register-handler-fx
 ::send-email
 (fn [cofx [_ archive-path]]
   (mail/send-email cofx
                    {:subject    "Error report"
                     :recipients [report-email]
                     :body       "logs attached"
                     :attachment {:path archive-path
                                  :type "zip"
                                  :name "status_logs.zip"}}
                    (fn []))))
