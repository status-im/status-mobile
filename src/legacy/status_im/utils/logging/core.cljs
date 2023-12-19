(ns legacy.status-im.utils.logging.core
  (:require
    [clojure.string :as string]
    [goog.string :as gstring]
    [legacy.status-im.bottom-sheet.events :as bottom-sheet]
    [legacy.status-im.ui.components.react :as react]
    [legacy.status-im.utils.build :as build]
    [legacy.status-im.utils.deprecated-types :as types]
    [native-module.core :as native-module]
    [re-frame.core :as re-frame]
    [react-native.mail :as react-native-mail]
    [react-native.platform :as platform]
    [status-im2.common.log :as log]
    [status-im2.config :as config]
    [utils.datetime :as datetime]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(def report-email "error-reports@status.im")

(re-frame/reg-fx
 :logs/archive-logs
 (fn [[db-json callback-handler]]
   (native-module/send-logs
    db-json
    (string/join "\n" (log/get-logs-queue))
    #(re-frame/dispatch [callback-handler %]))))

(rf/defn store-web3-client-version
  {:events [:logging/store-web3-client-version]}
  [{:keys [db]} node-version]
  {:db (assoc db :web3-node-version node-version)})

(rf/defn initialize-web3-client-version
  {:events [:logging/initialize-web3-client-version]}
  [_]
  {:json-rpc/call [{:method     "web3_clientVersion"
                    :on-success #(re-frame/dispatch [:logging/store-web3-client-version %])}]})

(defn extract-url-components
  [address]
  (when address
    (rest (re-matches #"enode://(.*?)@(.*):(.*)" address))))

(defn email-body
  "logs attached"
  [{:keys [:web3-node-version :mailserver/current-id
           :node-info :peers-summary :bug-report/details]}]
  (let [build-number build/build-no
        build-version (str build/version " (" build-number ")")
        separator (string/join (take 40 (repeat "-")))
        [enode-id ip-address port]
        (extract-url-components (:enode node-info))]
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
                      (extract-url-components enode)]
                  [(str "id: " enode-id)
                   (str "ip: " ip-address)
                   (str "port: " port)
                   "\n"]))
              peers-summary)
             [separator
              (datetime/timestamp->long-date
               (datetime/now))]))))

(rf/defn dialog-closed
  {:events [:logging/dialog-left]}
  [{:keys [db]}]
  {:db (dissoc db :logging/dialog-shown?)})

(rf/defn send-email
  [_ opts callback]
  {:email/send [opts callback]})

(rf/defn send-email-event
  {:events [::send-email]}
  [{:keys [db] :as cofx} archive-uri]
  (rf/merge
   cofx
   {:db (dissoc db :bug-report/details)}
   (dialog-closed)
   (send-email
    (cond-> {:subject    "Error report"
             :recipients [report-email]
             :body       (email-body db)}

      (not (nil? archive-uri))
      (assoc :attachments
             [{:uri  archive-uri
               :type "zip"
               :name "status_logs.zip"}]))
    (fn [event]
      (when (= event "not_available")
        (re-frame/dispatch [:show-client-error]))))))

(defn logs-enabled?
  [{:profile/keys [profile]}]
  (let [log-level (if profile ;; already login
                    (get profile :log-level)
                    config/log-level)]
    (not (string/blank? log-level))))

(rf/defn send-logs
  {:events [:logging.ui/send-logs-pressed]}
  [{:keys [db] :as cofx} transport]
  (if (logs-enabled? db)
    ;; TODO: Add message explaining db export
    (let [db-json (types/clj->json (select-keys db
                                                [:app-state
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
      {:logs/archive-logs [db-json
                           (if (= transport :email)
                             ::send-email
                             ::share-logs-file)]})
    (send-email-event cofx nil)))

(rf/defn send-logs-on-error
  {:events [:logging/send-logs-on-error]}
  [{:keys [db]} error-message]
  (rf/merge
   {:db (assoc-in db [:bug-report/details :description] error-message)}
   (send-logs :email)))

(rf/defn show-client-error
  {:events [:show-client-error]}
  [_]
  {:effects.utils/show-popup {:title   (i18n/label :t/cant-report-bug)
                              :content (i18n/label :t/mail-should-be-configured)}})

(rf/defn show-logs-dialog
  {:events [:shake-event]}
  [{:keys [db]}]
  (when-not (:logging/dialog-shown? db)
    {:db (assoc db :logging/dialog-shown? true)
     :effects.utils/show-confirmation
     (cond-> {:title               (i18n/label :t/send-logs)
              :content             (i18n/label :t/send-logs-to
                                               {:email report-email})
              :confirm-button-text (i18n/label :t/send-logs)
              :on-accept           #(do (re-frame/dispatch [:open-modal :bug-report])
                                        (re-frame/dispatch [:logging/dialog-left]))
              :on-cancel           #(re-frame/dispatch [:logging/dialog-left])}

       platform/ios?
       (assoc :extra-options
              [{:text    (i18n/label :t/share-logs)
                :onPress #(re-frame/dispatch
                           [:logging.ui/send-logs-pressed :sharing])
                :style   "default"}]))}))

(re-frame/reg-fx
 :email/send
 (fn [[opts callback]]
   (react-native-mail/mail (clj->js opts) callback)))

(re-frame/reg-fx
 ::share-archive
 (fn [opts]
   (.share ^js react/sharing (clj->js opts))))

(rf/defn share-archive
  [_ opts]
  {::share-archive opts})

(rf/defn share-logs-file
  {:events [::share-logs-file]}
  [{:keys [db] :as cofx} archive-uri]
  (rf/merge
   cofx
   (dialog-closed)
   (share-archive
    {:title "Archived logs"
     :url   archive-uri})))

(rf/defn details
  {:events [:logging/report-details]}
  [{:keys [db]} key value]
  {:db (-> db
           (assoc-in [:bug-report/details key] value)
           (dissoc :bug-report/description-error))})

(def min-description-lenght 6)

(defn validate-description
  [db]
  (let [description (get-in db [:bug-report/details :description] "")]
    (when (> min-description-lenght
             (count (string/trim description)))
      (i18n/label :t/bug-report-too-short-description))))

(rf/defn submit-report
  {:events [:logging/submit-report]}
  [{:keys [db] :as cofx} details steps]
  (if-let [error (validate-description db)]
    {:db (assoc db :bug-report/description-error error)}
    (rf/merge
     cofx
     (bottom-sheet/hide-bottom-sheet-old)
     (send-logs :email))))

(re-frame/reg-fx
 ::open-url
 (fn [url]
   (.openURL ^js react/linking url)))

(def gh-issue-url "https://github.com/status-im/status-mobile/issues/new?labels=bug&title=%s&body=%s")

(rf/defn submit-issue
  [{:keys [db]}]
  (let [{:keys [steps description]}
        (get db :bug-report/details)

        title (or description (i18n/label :t/bug-report-description-placeholder))
        body (str title
                  "\n\n"
                  (or steps (i18n/label :t/bug-report-steps-placeholder)))
        url (gstring/format gh-issue-url (js/escape title) (js/escape body))]
    {::open-url url}))

(rf/defn submit-gh-issue
  {:events [:logging/submit-gh-issue]}
  [{:keys [db] :as cofx} details steps]
  (rf/merge
   cofx
   {:db (dissoc db :bug-report/details)}
   (bottom-sheet/hide-bottom-sheet-old)
   (submit-issue)))
