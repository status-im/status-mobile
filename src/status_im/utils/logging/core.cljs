(ns status-im.utils.logging.core
  (:require [re-frame.core :as re-frame]
            [status-im.native-module.core :as status]
            [status-im.utils.fx :as fx]
            [status-im.utils.types :as types]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.email :as mail]
            [taoensso.timbre :as log]
            [status-im.utils.config :as config]
            [status-im.i18n :as i18n]
            [status-im.utils.platform :as platform]
            [status-im.utils.build :as build]
            [status-im.transport.utils :as transport.utils]
            [status-im.utils.datetime :as datetime]))

(def report-email "error-reports@status.im")
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

(defn get-js-logs []
  (clojure.string/join "\n" @logs-queue))

(re-frame/reg-fx
 :logs/archive-logs
 (fn [[db-json callback-handler]]
   (status/send-logs
    db-json
    (get-js-logs)
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
                                                  :my-profile/editing?]))]
    {:logs/archive-logs [db-json ::send-email]}))

(fx/defn show-logs-dialog
  [{:keys [db]}]
  (when-not (:logging/dialog-shown? db)
    {:db
     (assoc db :logging/dialog-shown? true)
     :utils/show-confirmation
     {:title               (i18n/label :t/send-logs)
      :content             (i18n/label :t/send-logs-to
                                       {:email report-email})
      :confirm-button-text (i18n/label :t/send-logs)
      :on-accept           #(re-frame/dispatch
                             [:logging.ui/send-logs-pressed])
      :on-cancel           #(re-frame/dispatch
                             [:logging/dialog-canceled])}}))

(fx/defn dialog-closed
  [{:keys [db]}]
  {:db (dissoc db :logging/dialog-shown?)})

(defn email-body
  [{:keys [:web3-node-version :mailserver/current-id
           :node-info :peers-summary]
    :as db}]
  "logs attached"
  (let [build-number  (if platform/desktop? build/version build/build-no)
        build-version (str build/version " (" build-number ")")
        separator (clojure.string/join (take 40 (repeat "-")))
        [enode-id ip-address port]
        (transport.utils/extract-url-components (:enode node-info))]
    (clojure.string/join
     "\n"
     (concat [(i18n/label :t/report-bug-email-template)]
             [separator
              (str "App version: " build-version)
              (str "OS: " platform/os)
              (str "Node version: " web3-node-version)
              (str "Mailserver: " (name current-id))
              separator
              "Node Info"
              (str "id: " enode-id)
              (str "ip: " ip-address)
              (str "port: " port)
              separator
              "Peers"]
             (mapcat
              (fn [{:keys [enode]}]
                (let [[enode-id ip-address port]
                      (transport.utils/extract-url-components enode)]
                  [(str "id: " enode-id)
                   (str "ip: " ip-address)
                   (str "port: " port)
                   "\n"]))
              peers-summary)
             [separator
              (datetime/timestamp->long-date
               (datetime/now))]))))

(handlers/register-handler-fx
 ::send-email
 (fn [{:keys [db] :as cofx} [_ archive-path]]
   (fx/merge
    cofx
    (dialog-closed)
    (mail/send-email
     {:subject    "Error report"
      :recipients [report-email]
      :body       (email-body db)
      :attachment {:path archive-path
                   :type "zip"
                   :name "status_logs.zip"}}
     (fn [])))))

(handlers/register-handler-fx
 :logging/dialog-canceled
 (fn [cofx]
   (dialog-closed cofx)))
