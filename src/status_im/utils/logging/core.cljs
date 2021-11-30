(ns status-im.utils.logging.core
  (:require ["react-native-mail" :default react-native-mail]
            [clojure.string :as string]
            [goog.string :as gstring]
            [re-frame.core :as re-frame]
            [status-im.bottom-sheet.core :as bottom-sheet]
            [status-im.i18n.i18n :as i18n]
            [status-im.native-module.core :as status]
            [status-im.transport.utils :as transport.utils]
            [status-im.ui.components.react :as react]
            [status-im.utils.build :as build]
            [status-im.utils.config :as config]
            [status-im.utils.datetime :as datetime]
            [status-im.utils.fx :as fx]
            [status-im.utils.platform :as platform]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]))

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

(defn email-body
  "logs attached"
  [{:keys [:web3-node-version :mailserver/current-id
           :node-info :peers-summary :bug-report/details]}]
  (let [build-number  build/build-no
        build-version (str build/version " (" build-number ")")
        separator (string/join (take 40 (repeat "-")))
        [enode-id ip-address port]
        (transport.utils/extract-url-components (:enode node-info))]
    (string/join
     "\n"
     (concat [(i18n/label :t/report-bug-email-template
                          {:description (:description details)
                           :steps       (:steps details)})]
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

(fx/defn dialog-closed
  {:events [:logging/dialog-left]}
  [{:keys [db]}]
  {:db (dissoc db :logging/dialog-shown?)})

(fx/defn send-email
  [_ opts callback]
  {:email/send [opts callback]})

(fx/defn send-email-event
  {:events [::send-email]}
  [{:keys [db] :as cofx} archive-uri]
  (fx/merge
   cofx
   {:db (dissoc db :bug-report/details)}
   (dialog-closed)
   (send-email
    (cond-> {:subject     "Error report"
             :recipients  [report-email]
             :body        (email-body db)}

      (not (nil? archive-uri))
      (assoc :attachments [{:uri archive-uri
                            :type "zip"
                            :name "status_logs.zip"}]))
    (fn [event]
      (when (= event "not_available")
        (re-frame/dispatch [:show-client-error]))))))

(defn logs-enabled? [db]
  (not (string/blank? (get-in db [:multiaccount :log-level]))))

(fx/defn send-logs
  {:events [:logging.ui/send-logs-pressed]}
  [{:keys [db] :as cofx} transport]
  (if (logs-enabled? db)
    ;; TODO: Add message explaining db export
    (let [db-json (types/clj->json (select-keys db [:app-state
                                                    :current-chat-id
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
                                                    :dimensions/window]))]
      {:logs/archive-logs [db-json (if (= transport :email)
                                     ::send-email
                                     ::share-logs-file)]})
    (send-email-event cofx nil)))

(fx/defn show-client-error
  {:events [:show-client-error]}
  [_]
  {:utils/show-popup {:title   (i18n/label :t/cant-report-bug)
                      :content (i18n/label :t/mail-should-be-configured)}})

(fx/defn show-logs-dialog
  {:events [:shake-event]}
  [{:keys [db]}]
  (when-not (:logging/dialog-shown? db)
    {:db (assoc db :logging/dialog-shown? true)
     :utils/show-confirmation
     (cond-> {:title               (i18n/label :t/send-logs)
              :content             (i18n/label :t/send-logs-to
                                               {:email report-email})
              :confirm-button-text (i18n/label :t/send-logs)
              :on-accept           #(do (re-frame/dispatch [:open-modal :bug-report])
                                        (re-frame/dispatch [:logging/dialog-left]))
              :on-cancel           #(re-frame/dispatch [:logging/dialog-left])}

       platform/ios?
       (assoc :extra-options       [{:text    (i18n/label :t/share-logs)
                                     :onPress #(re-frame/dispatch
                                                [:logging.ui/send-logs-pressed :sharing])
                                     :style   "default"}]))}))

(re-frame/reg-fx
 :email/send
 ;; https://github.com/chirag04/react-native-mail#example
 (fn [[opts callback]]
   (.mail react-native-mail (clj->js opts) callback)))

(re-frame/reg-fx
 ::share-archive
 (fn [opts]
   (.share ^js react/sharing (clj->js opts))))

(fx/defn share-archive
  [_ opts]
  {::share-archive opts})

(fx/defn share-logs-file
  {:events [::share-logs-file]}
  [{:keys [db] :as cofx} archive-uri]
  (fx/merge
   cofx
   (dialog-closed)
   (share-archive
    {:title   "Archived logs"
     :url     archive-uri})))

(fx/defn details
  {:events [:logging/report-details]}
  [{:keys [db]} key value]
  {:db (-> db
           (assoc-in [:bug-report/details key] value)
           (dissoc :bug-report/description-error))})

(def min-description-lenght 6)

(defn validate-description [db]
  (let [description (get-in db [:bug-report/details :description] "")]
    (when (> min-description-lenght
             (count (string/trim description)))
      (i18n/label :t/bug-report-too-short-description))))

(fx/defn submit-report
  {:events [:logging/submit-report]}
  [{:keys [db] :as cofx} details steps]
  (if-let [error (validate-description db)]
    {:db (assoc db :bug-report/description-error error)}
    (fx/merge
     cofx
     (bottom-sheet/hide-bottom-sheet)
     (send-logs :email))))

(re-frame/reg-fx
 ::open-url
 (fn [url]
   (.openURL ^js react/linking url)))

(def gh-issue-url "https://github.com/status-im/status-react/issues/new?labels=bug&title=%s&body=%s")

(fx/defn submit-issue
  [{:keys [db]}]
  (let [{:keys [steps description]}
        (get db :bug-report/details)

        title (or description (i18n/label :t/bug-report-description-placeholder))
        body  (str title
                   "\n\n"
                   (or steps (i18n/label :t/bug-report-steps-placeholder)))
        url   (gstring/format gh-issue-url (js/escape title) (js/escape body))]
    {::open-url url}))

(fx/defn submit-gh-issue
  {:events [:logging/submit-gh-issue]}
  [{:keys [db] :as cofx} details steps]
  (fx/merge
   cofx
   {:db (dissoc db :bug-report/details)}
   (bottom-sheet/hide-bottom-sheet)
   (submit-issue)))
