(ns legacy.status-im.utils.logging.core
  (:require
    [clojure.string :as string]
    [goog.string :as gstring]
    [legacy.status-im.ui.components.react :as react]
    [legacy.status-im.utils.build :as build]
    [legacy.status-im.utils.deprecated-types :as types]
    [legacy.status-im.utils.logging.view :as view]
    [native-module.core :as native-module]
    [re-frame.core :as re-frame]
    [react-native.mmkv :as mmkv]
    [react-native.platform :as platform]
    [status-im.common.json-rpc.events :as json-rpc]
    [status-im.common.log :as common-log]
    [status-im.config :as config]
    [status-im.constants :as constants]
    [status-im.navigation.events :as navigation]
    [taoensso.timbre :as log]
    [utils.datetime :as datetime]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(re-frame/reg-fx
 :logs/archive-logs
 (fn [[db-json callback-handler]]
   (native-module/send-logs
    db-json
    (string/join "\n" (common-log/get-logs-queue))
    #(re-frame/dispatch [callback-handler %]))))

(rf/defn store-web3-client-version
  {:events [:logging/store-web3-client-version]}
  [{:keys [db]} node-version]
  {:db (assoc db :web3-node-version node-version)})

(re-frame/reg-fx :logging/initialize-web3-client-version
 (fn []
   (json-rpc/call {:method     "web3_clientVersion"
                   :on-success [:logging/store-web3-client-version]})))

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

(rf/defn send-email
  [_ opts callback]
  {:email/send [opts callback]})

(rf/defn send-email-event
  {:events [::send-email]}
  [{:keys [db] :as cofx} archive-uri]
  (rf/merge
   cofx
   {:db (dissoc db :bug-report/details)}
   (send-email
    (cond-> {:subject    "Error report"
             :recipients [constants/report-email]
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
                    (config/log-level))]
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
                                                 :network/status
                                                 :peers-summary
                                                 :sync-state
                                                 :view-id
                                                 :chat/cooldown-enabled?
                                                 :chat/cooldowns
                                                 :chat/last-outgoing-message-sent-at
                                                 :chat/spam-messages-frequency
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
  [_]
  {:dispatch [:show-bottom-sheet {:content view/logs-management-drawer}]})

(re-frame/reg-fx
 :email/send
 (fn [[opts callback]]
   (native-module/mail (clj->js opts) callback)))

(re-frame/reg-fx
 ::share-archive
 (fn [{:keys [url] :as opts}]
   (if platform/android?
     (native-module/share-logs url (fn [error] (log/error "Error sharing logs" error)))
     (.share ^js react/sharing (clj->js opts)))))

(rf/defn share-archive
  [_ opts]
  {::share-archive opts})

(rf/defn share-logs-file
  {:events [::share-logs-file]}
  [cofx archive-uri]
  (rf/merge
   cofx
   (share-archive
    {:title "Archived logs"
     :url   archive-uri})))

(rf/defn details
  {:events [:logging/report-details]}
  [{:keys [db]} log-key value]
  {:db (-> db
           (assoc-in [:bug-report/details log-key] value)
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
  [{:keys [db] :as cofx}]
  (if-let [error (validate-description db)]
    {:db (assoc db :bug-report/description-error error)}
    (rf/merge
     cofx
     (navigation/hide-bottom-sheet)
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
  [{:keys [db] :as cofx}]
  (rf/merge
   cofx
   {:db (dissoc db :bug-report/details)}
   (navigation/hide-bottom-sheet)
   (submit-issue)))

(rf/defn change-pre-login-log-level
  {:events [:log-level.ui/change-pre-login-log-level]}
  [{:keys [db]} log-level]
  (let [old-log-level (get-in db [:log-level/pre-login-log-level])]
    (when (not= old-log-level log-level)
      (let [need-set-pre-login-log-enabled? (or (empty? old-log-level) (empty? log-level))
            pre-login-log-enabled?          (boolean (seq log-level))]
        {:fx [[:log-level/set-pre-login-log-level log-level]
              (when need-set-pre-login-log-enabled?
                [:log-level/set-pre-login-log-enabled pre-login-log-enabled?])
              ;; update log level in taoensso.timbre
              [:logs/set-level log-level]
              [:dispatch [:hide-bottom-sheet]]]
         :db (assoc db :log-level/pre-login-log-level log-level)}))))

(rf/reg-fx
 :log-level/set-pre-login-log-level
 (fn [log-level]
   (mmkv/set constants/pre-login-log-level-key log-level)
   (when (seq log-level)
     (native-module/set-pre-login-log-level log-level))))

(rf/reg-fx
 :log-level/set-pre-login-log-enabled
 (fn [enabled?]
   (native-module/set-pre-login-log-enabled enabled?)))
