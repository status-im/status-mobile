(ns status-im.utils.logging.core
  (:require [re-frame.core :as re-frame]
            [status-im.native-module.core :as status]
            [status-im.utils.fx :as fx]
            [status-im.utils.types :as types]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.email :as mail]
            [taoensso.timbre :as log]
            [status-im.i18n :as i18n]
            [status-im.utils.platform :as platform]
            [status-im.utils.build :as build]
            [status-im.transport.utils :as transport.utils]
            [status-im.utils.datetime :as datetime]
            [clojure.string :as string]
            [status-im.utils.config :as config]))

(def report-email "error-reports@status.im")
(def max-log-entries 1000)
(def logs-queue (atom #queue[]))
(defn add-log-entry [entry]
  (swap! logs-queue conj entry)
  (when (>= (count @logs-queue) max-log-entries)
    (swap! logs-queue pop)))

(defn init-logs [level]
  (when-not (string/blank? level)
    (log/set-level! (-> level
                        string/lower-case
                        keyword))
    (log/merge-config!
     {:output-fn (fn [& data]
                   (let [res (apply log/default-output-fn data)]
                     (add-log-entry res)
                     res))})))

(defn get-js-logs []
  (string/join "\n" @logs-queue))

(re-frame/reg-fx
 :logs/archive-logs
 (fn [[db-json callback-handler]]
   (status/send-logs
    db-json
    (get-js-logs)
    #(re-frame/dispatch [callback-handler %]))))

(re-frame/reg-fx
 :logs/set-level
 (fn [level]
   (init-logs level)))

(fx/defn set-log-level
  [{:keys [db]} log-level]
  (let [log-level (or log-level config/log-level)]
    {:db             (assoc-in db [:multiaccount :log-level] log-level)
     :logs/set-level log-level}))

(fx/defn send-logs
  [{:keys [db]}]
  ;; TODO: Add message explaining db export
  (let [db-json (types/clj->json (select-keys db [:app-state
                                                  :current-chat-id
                                                  :initial-props
                                                  :keyboard-height
                                                  :keyboard-max-height
                                                  :network
                                                  :network-status
                                                  :peers-count
                                                  :peers-summary
                                                  :sync-state
                                                  :view-id
                                                  :chat/cooldown-enabled?
                                                  :chat/cooldowns
                                                  :chat/last-outgoing-message-sent-at
                                                  :chat/spam-messages-frequency
                                                  :chats/loading?
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

(handlers/register-handler-fx
 :show-client-error
 (fn [_ _]
   {:utils/show-popup {:title   (i18n/label :t/cant-report-bug)
                       :content (i18n/label :t/mail-should-be-configured)}}))

(fx/defn dialog-closed
  [{:keys [db]}]
  {:db (dissoc db :logging/dialog-shown?)})

(defn email-body
  "logs attached"
  [{:keys [:web3-node-version :mailserver/current-id
           :node-info :peers-summary]}]
  (let [build-number  build/build-no
        build-version (str build/version " (" build-number ")")
        separator (string/join (take 40 (repeat "-")))
        [enode-id ip-address port]
        (transport.utils/extract-url-components (:enode node-info))]
    (string/join
     "\n"
     (concat [(i18n/label :t/report-bug-email-template)]
             [separator
              (str "App version: " build-version)
              (str "OS: " platform/os)
              (str "Node version: " web3-node-version)
              (when current-id
                (str "Mailserver: " (name current-id)))
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
     (fn [event]
       (when (= event "not_available")
         (re-frame/dispatch [:show-client-error])))))))

(handlers/register-handler-fx
 :logging/dialog-canceled
 (fn [cofx]
   (dialog-closed cofx)))
