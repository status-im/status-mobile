(ns status-im.events
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.data-store.messages :as data-store.messages]
            [status-im.data-store.chats :as data-store.chats]
            [status-im.multiaccounts.create.core :as multiaccounts.create]
            [status-im.multiaccounts.login.core :as multiaccounts.login]
            [status-im.multiaccounts.logout.core :as multiaccounts.logout]
            [status-im.multiaccounts.recover.core :as multiaccounts.recover]
            [status-im.multiaccounts.update.core :as multiaccounts.update]
            status-im.multiaccounts.biometric.core
            [status-im.bootnodes.core :as bootnodes]
            [status-im.browser.core :as browser]
            [status-im.browser.permissions :as browser.permissions]
            [status-im.chat.db :as chat.db]
            [status-im.chat.models :as chat]
            [status-im.chat.models.input :as chat.input]
            [status-im.chat.models.loading :as chat.loading]
            [status-im.chat.models.message :as chat.message]
            [status-im.contact.block :as contact.block]
            [status-im.contact.core :as contact]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.ens :as ethereum.ens]
            [status-im.ethereum.subscriptions :as ethereum.subscriptions]
            [status-im.ethereum.transactions.core :as ethereum.transactions]
            [status-im.fleet.core :as fleet]
            [status-im.group-chats.core :as group-chats]
            [status-im.signing.keycard :as signing.keycard]
            [status-im.i18n :as i18n]
            [status-im.init.core :as init]
            [status-im.log-level.core :as log-level]
            status-im.waku.core
            [status-im.mailserver.core :as mailserver]
            [status-im.mailserver.constants :as mailserver.constants]
            [status-im.mailserver.topics :as mailserver.topics]
            [status-im.node.core :as node]
            [status-im.pairing.core :as pairing]
            [status-im.privacy-policy.core :as privacy-policy]
            [status-im.protocol.core :as protocol]
            [status-im.qr-scanner.core :as qr-scanner]
            [status-im.search.core :as search]
            [status-im.signals.core :as signals]
            [status-im.stickers.core :as stickers]
            [status-im.transport.core :as transport]
            [status-im.transport.message.core :as transport.message]
            status-im.wallet.choose-recipient.core
            status-im.wallet.collectibles.core
            status-im.wallet.accounts.core
            [status-im.ui.components.bottom-sheet.core :as bottom-sheet]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.add-new.new-chat.db :as new-chat.db]
            [status-im.ui.screens.currency-settings.models
             :as
             currency-settings.models]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.build :as build]
            [status-im.utils.config :as config]
            [status-im.utils.fx :as fx]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.logging.core :as logging]
            [status-im.utils.utils :as utils]
            [status-im.wallet.core :as wallet]
            [status-im.wallet.custom-tokens.core :as custom-tokens]
            [status-im.wallet.db :as wallet.db]
            [taoensso.timbre :as log]
            [status-im.utils.money :as money]
            status-im.hardwallet.core
            status-im.popover.core))

;; init module
(handlers/register-handler-fx
 :init/app-started
 (fn [cofx _]
   (init/start-app cofx)))

;; multiaccounts module
(handlers/register-handler-fx
 :multiaccounts.update.callback/published
 (fn [{:keys [now] :as cofx} _]
   (multiaccounts.update/multiaccount-update cofx :last-updated now {})))

(handlers/register-handler-fx
 :multiaccounts.update.callback/failed-to-publish
 (fn [{:keys [now] :as cofx} [_ message]]
   (log/warn "failed to publish multiaccount update" message)
   (multiaccounts.update/multiaccount-update cofx :last-updated now {})))

(handlers/register-handler-fx
 :multiaccounts.ui/dev-mode-switched
 (fn [cofx [_ dev-mode?]]
   (multiaccounts/switch-dev-mode cofx dev-mode?)))

(def CUD-url "https://chaos-unicorn-day.org")

(defn open-chaos-unicorn-day-link []
  (.openURL react/linking CUD-url))

(handlers/register-handler-fx
 :multiaccounts.ui/chaos-mode-switched
 (fn [{:keys [db] :as cofx} [_ chaos-mode?]]
   (let [old-chaos-mode? (get-in db [:multiaccount :chaos-mode?])]
     (fx/merge
      cofx
      (when (and chaos-mode?
                 (not= old-chaos-mode? chaos-mode?))
        {:ui/show-confirmation
         {:title               (i18n/label :t/chaos-unicorn-day)
          :content             (i18n/label :t/chaos-unicorn-day-details)
          :confirm-button-text (i18n/label :t/see-details)
          :cancel-button-text  (i18n/label :t/cancel)
          :on-accept           open-chaos-unicorn-day-link}})
      (multiaccounts/switch-chaos-mode chaos-mode?)))))

(handlers/register-handler-fx
 :multiaccounts.ui/notifications-enabled
 (fn [cofx [_ desktop-notifications?]]
   (multiaccounts/enable-notifications cofx desktop-notifications?)))

(handlers/register-handler-fx
 :multiaccounts.ui/preview-privacy-mode-switched
 (fn [cofx [_ private?]]
   (multiaccounts/switch-preview-privacy-mode cofx private?)))

(handlers/register-handler-fx
 :multiaccounts.ui/wallet-set-up-confirmed
 (fn [cofx _]
   (multiaccounts/confirm-wallet-set-up cofx)))

(handlers/register-handler-fx
 :multiaccounts.ui/hide-home-tooltip
 (fn [cofx _]
   (multiaccounts/confirm-home-tooltip cofx)))

;; multiaccounts login module
(handlers/register-handler-fx
 :multiaccounts.login.ui/multiaccount-selected
 (fn [{:keys [db] :as cofx} [_ key-uid]]
   (let [{:keys [photo-path name public-key]}
         (get-in db [:multiaccounts/multiaccounts key-uid])]
     (fx/merge
      cofx
      {:db (-> db
               (dissoc :intro-wizard)
               (update :hardwallet dissoc :application-info))}
      (multiaccounts.login/open-login key-uid photo-path name public-key)))))

;; multiaccounts update module

(handlers/register-handler-fx
 :multiaccounts.update.callback/save-settings-success
 (fn [cofx _]
   (multiaccounts.logout/logout cofx)))

;; mailserver module

(handlers/register-handler-fx
 :mailserver.ui/user-defined-mailserver-selected
 (fn [cofx [_ mailserver-id]]
   (mailserver/edit cofx mailserver-id)))

(handlers/register-handler-fx
 :mailserver.ui/default-mailserver-selected
 (fn [cofx [_ mailserver-id]]
   (mailserver/show-connection-confirmation cofx mailserver-id)))

(handlers/register-handler-fx
 :mailserver.ui/add-pressed
 (fn [cofx _]
   (navigation/navigate-to-cofx cofx :edit-mailserver nil)))

(handlers/register-handler-fx
 :mailserver.ui/save-pressed
 [(re-frame/inject-cofx :random-id-generator)]
 (fn [cofx _]
   (mailserver/upsert cofx)))

(handlers/register-handler-fx
 :mailserver.ui/input-changed
 (fn [cofx [_ input-key value]]
   (mailserver/set-input cofx input-key value)))

(handlers/register-handler-fx
 :mailserver.ui/delete-confirmed
 (fn [cofx [_ mailserver-id]]
   (mailserver/delete cofx mailserver-id)))

(handlers/register-handler-fx
 :mailserver.ui/delete-pressed
 (fn [cofx [_ mailserver-id]]
   (mailserver/show-delete-confirmation cofx mailserver-id)))

(handlers/register-handler-fx
 :mailserver.callback/qr-code-scanned
 (fn [cofx [_ url _]]
   (mailserver/set-url-from-qr cofx url)))

(handlers/register-handler-fx
 :mailserver.callback/resend-request
 (fn [cofx [_ request]]
   (mailserver/resend-request cofx request)))

(handlers/register-handler-fx
 :mailserver.ui/connect-pressed
 (fn [cofx [_ mailserver-id]]
   (mailserver/show-connection-confirmation cofx mailserver-id)))

(handlers/register-handler-fx
 :mailserver.ui/connect-confirmed
 (fn [cofx [_ current-fleet mailserver-id]]
   (mailserver/save-settings cofx current-fleet mailserver-id)))

(handlers/register-handler-fx
 :mailserver.ui/unpin-pressed
 (fn [cofx _]
   (mailserver/unpin cofx)))

(handlers/register-handler-fx
 :mailserver.ui/pin-pressed
 (fn [cofx _]
   (mailserver/pin cofx)))

(handlers/register-handler-fx
 :mailserver.ui/request-error-pressed
 (fn [cofx _]
   (mailserver/show-request-error-popup cofx)))

(handlers/register-handler-fx
 :mailserver.ui/retry-request-pressed
 (fn [cofx [_ args]]
   (mailserver/retry-next-messages-request cofx)))

(handlers/register-handler-fx
 :mailserver/check-connection-timeout
 (fn [cofx _]
   (mailserver/check-connection cofx)))

(handlers/register-handler-fx
 :mailserver/fetch-history
 (fn [cofx [_ chat-id from-timestamp]]
   (mailserver/fetch-history cofx chat-id {:from from-timestamp})))

(handlers/register-handler-fx
 :mailserver.callback/generate-mailserver-symkey-success
 (fn [cofx [_ mailserver sym-key-id]]
   (mailserver/add-mailserver-sym-key cofx mailserver sym-key-id)))

(handlers/register-handler-fx
 :mailserver.callback/mark-trusted-peer-success
 (fn [cofx _]
   (mailserver/add-mailserver-trusted cofx)))

(handlers/register-handler-fx
 :mailserver.callback/mark-trusted-peer-error
 (fn [cofx [_ error]]
   (log/error "Error on mark-trusted-peer: " error)
   (mailserver/check-connection cofx)))

(handlers/register-handler-fx
 :mailserver.callback/request-error
 (fn [cofx [_ error]]
   (mailserver/handle-request-error cofx error)))

(handlers/register-handler-fx
 :mailserver.callback/request-success
 (fn [cofx [_ request-id]]
   (mailserver/handle-request-success cofx request-id)))

;; fleet module

(handlers/register-handler-fx
 :fleet.ui/save-fleet-confirmed
 (fn [cofx [_ fleet]]
   (fleet/save cofx fleet)))

(handlers/register-handler-fx
 :fleet.ui/fleet-selected
 (fn [cofx [_ fleet]]
   (fleet/show-save-confirmation cofx fleet)))

;; bootnodes module

(handlers/register-handler-fx
 :bootnodes.ui/add-bootnode-pressed
 (fn [cofx [_ bootnode-id]]
   (bootnodes/edit cofx bootnode-id)))

(handlers/register-handler-fx
 :bootnodes.callback/qr-code-scanned
 (fn [cofx [_ url _]]
   (bootnodes/set-bootnodes-from-qr cofx url)))

(handlers/register-handler-fx
 :bootnodes.ui/input-changed
 (fn [cofx [_ input-key value]]
   (bootnodes/set-input cofx input-key value)))

(handlers/register-handler-fx
 :bootnodes.ui/save-pressed
 [(re-frame/inject-cofx :random-id-generator)]
 (fn [cofx _]
   (bootnodes/upsert cofx)))

(handlers/register-handler-fx
 :bootnodes.ui/delete-pressed
 (fn [cofx [_ id]]
   (bootnodes/show-delete-bootnode-confirmation cofx id)))

(handlers/register-handler-fx
 :bootnodes.ui/delete-confirmed
 (fn [cofx [_ bootnode-id]]
   (bootnodes/delete-bootnode cofx bootnode-id)))

;; logging module

(handlers/register-handler-fx
 :logging.ui/send-logs-pressed
 (fn [cofx _]
   (logging/send-logs cofx)))

;; log-level module

(handlers/register-handler-fx
 :log-level.ui/change-log-level-confirmed
 (fn [cofx [_ log-level]]
   (log-level/save-log-level cofx log-level)))

(handlers/register-handler-fx
 :log-level.ui/log-level-selected
 (fn [cofx [_ log-level]]
   (log-level/show-change-log-level-confirmation cofx log-level)))

(handlers/register-handler-fx
 :log-level.ui/logging-enabled
 (fn [cofx [_ enabled]]
   (log/debug "### :log-level.ui/logging-enabled" enabled)

   (log-level/show-logging-enabled-confirmation cofx enabled)))

(handlers/register-handler-fx
 :log-level.ui/logging-enabled-confirmed
 (fn [cofx [_ enabled]]
   ;;FIXME desktop only
   #_(log-level/save-logging-enabled cofx enabled)))

;; Browser bridge module

(handlers/register-handler-fx
 :browser.bridge.callback/qr-code-scanned
 (fn [cofx [_ data qr-code-data]]
   (browser/handle-scanned-qr-code cofx data (:data qr-code-data))))

(handlers/register-handler-fx
 :browser.bridge.callback/qr-code-canceled
 (fn [cofx [_ qr-code-data _]]
   (browser/handle-canceled-qr-code cofx (:data qr-code-data))))

;; qr-scanner module

(handlers/register-handler-fx
 :qr-scanner.ui/scan-qr-code-pressed
 (fn [cofx [_ opts]]
   (qr-scanner/scan-qr-code cofx opts)))

(handlers/register-handler-fx
 :qr-scanner.callback/scan-qr-code-success
 (fn [cofx [_ opts data]]
   (qr-scanner/set-qr-code cofx opts data)))

(handlers/register-handler-fx
 :qr-scanner.callback/scan-qr-code-cancel
 (fn [cofx [_ opts]]
   (fx/merge cofx
             (qr-scanner/set-qr-code-cancel opts)
             (navigation/navigate-back))))

;; privacy-policy module

(handlers/register-handler-fx
 :privacy-policy/privacy-policy-button-pressed
 (fn [cofx _]
   (privacy-policy/open-privacy-policy-link cofx)))

;; wallet modules

(handlers/register-handler-fx
 :wallet.settings.ui/currency-selected
 (fn [cofx [_ currency]]
   (currency-settings.models/set-currency cofx currency)))

;; chat module

(handlers/register-handler-fx
 :chat.ui/clear-history-pressed
 (fn [_ [_ chat-id]]
   {:ui/show-confirmation {:title               (i18n/label :t/clear-history-title)
                           :content             (i18n/label :t/clear-history-confirmation-content)
                           :confirm-button-text (i18n/label :t/clear-history-action)
                           :on-accept           #(re-frame/dispatch [:chat.ui/clear-history chat-id])}}))

(handlers/register-handler-fx
 :chat.ui/fetch-history-pressed
 (fn [{:keys [now] :as cofx} [_ chat-id]]
   (mailserver/fetch-history cofx chat-id
                             {:from (- (quot now 1000) mailserver.constants/one-day)})))

(handlers/register-handler-fx
 :chat.ui/fetch-history-pressed48-60
 (fn [{:keys [now] :as cofx} [_ chat-id]]
   (let [now (quot now 1000)]
     (mailserver/fetch-history cofx chat-id
                               {:from (- now (* 2.5 mailserver.constants/one-day))
                                :to   (- now (* 2 mailserver.constants/one-day))}))))

(handlers/register-handler-fx
 :chat.ui/fetch-history-pressed84-96
 (fn [{:keys [now] :as cofx} [_ chat-id]]
   (let [now (quot now 1000)]
     (mailserver/fetch-history cofx chat-id
                               {:from (- now (* 4 mailserver.constants/one-day))
                                :to   (- now (* 3.5 mailserver.constants/one-day))}))))

(handlers/register-handler-fx
 :chat.ui/fill-gaps
 (fn [{:keys [db] :as cofx} [_ gap-ids]]
   (let [chat-id           (:current-chat-id db)
         topics            (mailserver.topics/topics-for-current-chat db)
         gaps              (keep
                            (fn [id]
                              (get-in db [:mailserver/gaps chat-id id]))
                            gap-ids)]
     (mailserver/fill-the-gap
      cofx
      {:gaps    gaps
       :topics  topics
       :chat-id chat-id}))))

(handlers/register-handler-fx
 :chat.ui/fetch-more
 (fn [{:keys [db] :as cofx}]
   (let [chat-id           (:current-chat-id db)

         {:keys [lowest-request-from]}
         (get-in db [:mailserver/ranges chat-id])

         topics            (mailserver.topics/topics-for-current-chat db)
         gaps              [{:id   :first-gap
                             :to   lowest-request-from
                             :from (- lowest-request-from mailserver.constants/one-day)}]]
     (mailserver/fill-the-gap
      cofx
      {:gaps    gaps
       :topics   topics
       :chat-id chat-id}))))

(handlers/register-handler-fx
 :chat.ui/remove-chat-pressed
 (fn [_ [_ chat-id]]
   {:ui/show-confirmation {:title               (i18n/label :t/delete-confirmation)
                           :content             (i18n/label :t/delete-chat-confirmation)
                           :confirm-button-text (i18n/label :t/delete)
                           :on-accept           #(re-frame/dispatch [:chat.ui/remove-chat chat-id])}}))

(handlers/register-handler-fx
 :chat.ui/set-chat-ui-props
 (fn [{:keys [db]} [_ kvs]]
   {:db (chat/set-chat-ui-props db kvs)}))

(handlers/register-handler-fx
 :chat.ui/join-time-messages-checked
 (fn [cofx [_ chat-id]]
   (chat/join-time-messages-checked cofx chat-id)))

(handlers/register-handler-fx
 :chat.ui/show-message-options
 (fn [{:keys [db]} [_ options]]
   {:db (chat/set-chat-ui-props db {:show-message-options? true
                                    :message-options       options})}))

(handlers/register-handler-fx
 :chat.ui/navigate-to-chat
 (fn [cofx [_ chat-id _]]
   (chat/navigate-to-chat cofx chat-id)))

(handlers/register-handler-fx
 :chat.ui/load-more-messages
 (fn [cofx _]
   (chat.loading/load-more-messages cofx)))

(handlers/register-handler-fx
 :chat.ui/start-chat
 (fn [cofx [_ contact-id opts]]
   (chat/start-chat cofx contact-id opts)))

(handlers/register-handler-fx
 :chat.ui/start-public-chat
 (fn [cofx [_ topic opts]]
   (chat/start-public-chat cofx topic opts)))

(handlers/register-handler-fx
 :chat.ui/remove-chat
 (fn [cofx [_ chat-id]]
   (chat/remove-chat cofx chat-id)))

(handlers/register-handler-fx
 :chat.ui/clear-history
 (fn [cofx  [_ chat-id]]
   (chat/clear-history cofx chat-id)))

(handlers/register-handler-fx
 :chat.ui/resend-message
 (fn [{:keys [db] :as cofx} [_ chat-id message-id]]
   (let [message (get-in db [:chats chat-id :messages message-id])]
     (fx/merge
      cofx
      (transport.message/set-message-envelope-hash chat-id message-id (:message-type message) 1)
      (chat.message/resend-message chat-id message-id)))))

(handlers/register-handler-fx
 :chat.ui/delete-message
 (fn [cofx [_ chat-id message-id]]
   (chat.message/delete-message cofx chat-id message-id)))

(handlers/register-handler-fx
 :chat.ui/message-expand-toggled
 (fn [cofx [_ chat-id message-id]]
   (chat.message/toggle-expand-message cofx chat-id message-id)))

(handlers/register-handler-fx
 :chat.ui/set-chat-input-text
 (fn [cofx [_ text]]
   (chat.input/set-chat-input-text cofx text)))

(handlers/register-handler-fx
 :chat.ui/cancel-message-reply
 (fn [cofx _]
   (chat.input/cancel-message-reply cofx)))

(handlers/register-handler-fx
 :chat.ui/reply-to-message
 (fn [cofx [_ message-id]]
   (chat.input/reply-to-message cofx message-id)))

(handlers/register-handler-fx
 :chat.ui/send-current-message
 (fn [cofx _]
   (chat.input/send-current-message cofx)))

(defn- mark-messages-seen
  [{:keys [db] :as cofx}]
  (let [{:keys [current-chat-id]} db]
    (chat/mark-messages-seen cofx current-chat-id)))

(handlers/register-handler-fx
 :chat.ui/mark-messages-seen
 (fn [{:keys [db] :as cofx} [_ view-id]]
   (fx/merge cofx
             {:db (assoc db :view-id view-id)}
             #(mark-messages-seen %))))

(handlers/register-handler-fx
 :chat/send-sticker
 (fn [{{:keys [current-chat-id multiaccount]} :db :as cofx} [_ {:keys [hash] :as sticker}]]
   (fx/merge
    cofx
    (multiaccounts.update/multiaccount-update
     :stickers/recent-stickers
     (conj (remove #(= hash %) (:stickers/recent-stickers multiaccount)) hash)
     {})
    (chat.input/send-sticker-fx sticker current-chat-id))))

(handlers/register-handler-fx
 :chat/disable-cooldown
 (fn [cofx _]
   (chat/disable-chat-cooldown cofx)))

(handlers/register-handler-fx
 :message/update-message-status
 (fn [cofx [_ chat-id message-id status]]
   (chat.message/update-message-status cofx chat-id message-id status)))

;; signal module

(handlers/register-handler-fx
 :signals/signal-received
 (fn [cofx [_ event-str]]
   (log/debug :event-str event-str)
   (signals/process cofx event-str)))

;; hardwallet module

(handlers/register-handler-fx
 :hardwallet.ui/go-to-settings-button-pressed
 (fn [_ _]
   {:hardwallet/open-nfc-settings nil}))

(handlers/register-handler-fx
 :hardwallet.ui/pair-card-button-pressed
 (fn [{:keys [db]} _]
   {:db (assoc-in db [:hardwallet :setup-step] :enter-pair-code)}))

(handlers/register-handler-fx
 :hardwallet.ui/pair-code-input-changed
 (fn [{:keys [db]} [_ pair-code]]
   {:db (assoc-in db [:hardwallet :secrets :password] pair-code)}))

(handlers/register-handler-fx
 :hardwallet.ui/recovery-phrase-confirm-word-back-button-pressed
 (fn [{:keys [db]} _]
   {:db (assoc-in db [:hardwallet :setup-step] :recovery-phrase)}))

(handlers/register-handler-fx
 :hardwallet.ui/recovery-phrase-confirm-word-input-changed
 (fn [{:keys [db]} [_ input]]
   {:db (assoc-in db [:hardwallet :recovery-phrase :input-word] input)}))

(handlers/register-handler-fx
 :hardwallet.ui/recovery-phrase-cancel-pressed
 (fn [{:keys [db]} _]
   {:db (assoc-in db [:hardwallet :setup-step] :recovery-phrase)}))

(handlers/register-handler-fx
 :hardwallet.ui/pin-numpad-delete-button-pressed
 (fn [{:keys [db]} [_ step]]
   (when-not (empty? (get-in db [:hardwallet :pin step]))
     {:db (update-in db [:hardwallet :pin step] pop)})))

(handlers/register-handler-fx
 :hardwallet.ui/create-pin-button-pressed
 (fn [{:keys [db]} _]
   {:db (-> db
            (assoc-in [:hardwallet :setup-step] :pin)
            (assoc-in [:hardwallet :pin :enter-step] :original))}))

;; browser module

(handlers/register-handler-fx
 :browser.ui/browser-item-selected
 (fn [cofx [_ browser-id]]
   (browser/open-existing-browser cofx browser-id)))

(handlers/register-handler-fx
 :browser.ui/url-input-pressed
 (fn [cofx _]
   (browser/update-browser-option cofx :url-editing? true)))

(handlers/register-handler-fx
 :browser.ui/url-input-blured
 (fn [cofx _]
   (browser/update-browser-option cofx :url-editing? false)))

(handlers/register-handler-fx
 :browser.ui/url-submitted
 (fn [cofx [_ url]]
   (browser/open-url-in-current-browser cofx url)))

(handlers/register-handler-fx
 :browser.ui/message-link-pressed
 (fn [cofx [_ link]]
   (browser/handle-message-link cofx link)))

(handlers/register-handler-fx
 :browser.ui/remove-browser-pressed
 (fn [cofx [_ browser-id]]
   (browser/remove-browser cofx browser-id)))

(handlers/register-handler-fx
 :browser.ui/lock-pressed
 (fn [cofx [_ secure?]]
   (browser/update-browser-option cofx :show-tooltip (if secure? :secure :not-secure))))

(handlers/register-handler-fx
 :browser.ui/close-tooltip-pressed
 (fn [cofx _]
   (browser/update-browser-option cofx :show-tooltip nil)))

(handlers/register-handler-fx
 :browser.ui/previous-page-button-pressed
 (fn [cofx _]
   (browser/navigate-to-previous-page cofx)))

(handlers/register-handler-fx
 :browser.ui/next-page-button-pressed
 (fn [cofx _]
   (browser/navigate-to-next-page cofx)))

(handlers/register-handler-fx
 :browser/navigation-state-changed
 (fn [cofx [_ event error?]]
   (browser/navigation-state-changed cofx event error?)))

(handlers/register-handler-fx
 :browser/bridge-message-received
 (fn [cofx [_ message]]
   (browser/process-bridge-message cofx message)))

(handlers/register-handler-fx
 :browser/error-occured
 (fn [cofx _]
   (browser/handle-browser-error cofx)))

(handlers/register-handler-fx
 :browser/loading-started
 (fn [cofx _]
   (browser/update-browser-options cofx {:error? false :loading? true})))

(handlers/register-handler-fx
 :browser.callback/resolve-ens-multihash-success
 (fn [cofx [_ m]]
   (browser/resolve-ens-multihash-success cofx m)))

(handlers/register-handler-fx
 :browser.callback/resolve-ens-multihash-error
 (fn [cofx _]
   (browser/resolve-ens-multihash-error cofx)))

(handlers/register-handler-fx
 :browser.callback/resolve-ens-contenthash
 (fn [cofx _]
   (browser/resolve-ens-contenthash cofx)))

(handlers/register-handler-fx
 :browser.callback/call-rpc
 (fn [cofx [_ message]]
   (browser/send-to-bridge cofx message)))

(handlers/register-handler-fx
 :browser.permissions.ui/dapp-permission-allowed
 (fn [cofx _]
   (browser.permissions/allow-permission cofx)))

(handlers/register-handler-fx
 :browser.permissions.ui/dapp-permission-denied
 (fn [cofx _]
   (browser.permissions/deny-permission cofx)))

(handlers/register-handler-fx
 :browser.permissions.ui/permission-animation-finished
 (fn [cofx [_ dapp-name]]
   (browser.permissions/process-next-permission cofx dapp-name)))

(handlers/register-handler-fx
 :browser.ui/open-url
 (fn [cofx [_ url]]
   (browser/open-url cofx url)))

(handlers/register-handler-fx
 :browser.ui/open-modal-chat-button-pressed
 (fn [cofx [_ host]]
   (browser/open-chat-from-browser cofx host)))

(handlers/register-handler-fx
 :dapps/revoke-access
 (fn [cofx [_ dapp]]
   (browser.permissions/revoke-dapp-permissions cofx dapp)))

;; group-chats module

(handlers/register-handler-fx
 :group-chats.ui/create-pressed
 [(re-frame/inject-cofx :random-guid-generator)]
 (fn [cofx [_ chat-name]]
   (group-chats/create cofx chat-name)))

(handlers/register-handler-fx
 :group-chats.ui/name-changed
 (fn [cofx [_ chat-name]]
   (group-chats/handle-name-changed cofx chat-name)))

(handlers/register-handler-fx
 :group-chats.ui/save-pressed
 (fn [cofx _]
   (group-chats/save cofx)))

(handlers/register-handler-fx
 :group-chats.ui/add-members-pressed
 (fn [cofx _]
   (group-chats/add-members cofx)))

(handlers/register-handler-fx
 :group-chats.ui/remove-member-pressed
 (fn [cofx [_ chat-id public-key]]
   (group-chats/remove-member cofx chat-id public-key)))

(handlers/register-handler-fx
 :group-chats.ui/make-admin-pressed
 (fn [cofx [_ chat-id public-key]]
   (group-chats/make-admin cofx chat-id public-key)))

(handlers/register-handler-fx
 :group-chats.ui/remove-chat-pressed
 (fn [_ [_ chat-id group?]]
   {:ui/show-confirmation {:title               (i18n/label :t/delete-confirmation)
                           :content             (i18n/label :t/delete-chat-confirmation)
                           :confirm-button-text (i18n/label :t/delete)
                           :on-accept           #(re-frame/dispatch [:group-chats.ui/remove-chat-confirmed chat-id])}}))

(handlers/register-handler-fx
 :group-chats.ui/remove-chat-confirmed
 (fn [cofx [_ chat-id]]
   (group-chats/remove cofx chat-id)))

(handlers/register-handler-fx
 :group-chats.ui/join-pressed
 (fn [cofx [_ chat-id]]
   (group-chats/join-chat cofx chat-id)))

;; transport module

(handlers/register-handler-fx
 :transport/send-status-message-error
 (fn [{:keys [db] :as cofx} [_ err]]
   (log/error :send-status-message-error err)))

(fx/defn handle-update [cofx {:keys [chats messages] :as response}]
  (let [chats (map data-store.chats/<-rpc chats)
        messages (map data-store.messages/<-rpc messages)
        message-fxs (map chat.message/receive-one messages)
        chat-fxs (map #(chat/ensure-chat (dissoc % :unviewed-messages-count)) chats)]
    (apply fx/merge cofx (concat chat-fxs message-fxs))))

(handlers/register-handler-fx
 :transport/message-sent
 (fn [cofx [_ response messages-count]]
   (let [{:keys [localChatId id messageType]} (-> response :messages first)]
     (fx/merge cofx
               (handle-update response)
               (transport.message/set-message-envelope-hash localChatId id messageType messages-count)))))

(handlers/register-handler-fx
 :transport/contact-message-sent
 (fn [cofx [_ chat-id envelope-hash]]
   (transport.message/set-contact-message-envelope-hash cofx chat-id envelope-hash)))

(handlers/register-handler-fx
 :transport.callback/node-info-fetched
 (fn [cofx [_ node-info]]
   (transport/set-node-info cofx node-info)))

;; contact module

(handlers/register-handler-fx
 :contact.ui/add-to-contact-pressed
 [(re-frame/inject-cofx :random-id-generator)]
 (fn [cofx [_ public-key]]
   (contact/add-contact cofx public-key)))

(handlers/register-handler-fx
 :contact.ui/block-contact-confirmed
 (fn [cofx [_ public-key]]
   (contact.block/block-contact cofx public-key)))

(handlers/register-handler-fx
 :contact.ui/unblock-contact-pressed
 (fn [cofx [_ public-key]]
   (contact.block/unblock-contact cofx public-key)))

(defn get-validation-label [value]
  (case value
    :invalid
    (i18n/label :t/use-valid-contact-code)
    :yourself
    (i18n/label :t/can-not-add-yourself)))

(handlers/register-handler-fx
 :contact/qr-code-scanned
 [(re-frame/inject-cofx :random-id-generator)]
 (fn [{:keys [db] :as cofx} [_ contact-identity _]]
   (let [public-key?       (and (string? contact-identity)
                                (string/starts-with? contact-identity "0x"))
         validation-result (new-chat.db/validate-pub-key db contact-identity)]
     (cond
       (and public-key? (not (some? validation-result)))
       (chat/start-chat cofx contact-identity {:navigation-reset? true})

       (and (not public-key?) (string? contact-identity))
       (let [chain (ethereum/chain-keyword db)]
         {:resolve-public-key {:chain            chain
                               :contact-identity contact-identity
                               :cb               #(re-frame/dispatch [:contact/qr-code-scanned %])}})

       :else
       {:utils/show-popup {:title      (i18n/label :t/unable-to-read-this-code)
                           :content    (get-validation-label validation-result)
                           :on-dismiss #(re-frame/dispatch [:navigate-to-clean :home])}}))))

(handlers/register-handler-fx
 :contact.ui/start-group-chat-pressed
 (fn [{:keys [db] :as cofx} _]
   (contact/open-contact-toggle-list cofx)))

(handlers/register-handler-fx
 :contact.ui/send-message-pressed
 [(re-frame/inject-cofx :random-id-generator)]
 (fn [cofx [_ {:keys [public-key]}]]
   (chat/start-chat cofx public-key {:navigation-reset? true})))

(handlers/register-handler-fx
 :contact.ui/contact-code-submitted
 [(re-frame/inject-cofx :random-id-generator)]
 (fn [{{:contacts/keys [new-identity]} :db :as cofx} _]
   (let [{:keys [public-key ens-name]} new-identity]
     (fx/merge cofx
               (chat/start-chat public-key {:navigation-reset? true})
               #(when ens-name
                  (contact/name-verified % public-key ens-name))))))

;; pairing module

(handlers/register-handler-fx
 :pairing.ui/pair-devices-pressed
 (fn [cofx _]
   (log/info "Sending pair installation")
   (pairing/send-pair-installation cofx)))

(handlers/register-handler-fx
 :pairing.ui/set-name-pressed
 (fn [cofx [_ installation-name]]
   (pairing/set-name cofx installation-name)))

(handlers/register-handler-fx
 :pairing.ui/synchronize-installation-pressed
 (fn [cofx _]
   (pairing/send-installation-messages cofx)))

(handlers/register-handler-fx
 :pairing.callback/get-our-installations-success
 (fn [cofx [_ installations]]
   (pairing/load-installations cofx installations)))

(handlers/register-handler-fx
 :pairing.callback/set-installation-metadata-success
 (fn [cofx [_ installation-id metadata]]
   (pairing/update-installation cofx installation-id metadata)))

(handlers/register-handler-fx
 :set-initial-props
 (fn [cofx [_ initial-props]]
   {:db (assoc (:db cofx) :initial-props initial-props)}))

(handlers/register-handler-fx
 :pairing.ui/enable-installation-pressed
 (fn [cofx [_ installation-id]]
   (pairing/enable-fx cofx installation-id)))

(handlers/register-handler-fx
 :pairing.ui/disable-installation-pressed
 (fn [cofx [_ installation-id]]
   (pairing/disable-fx cofx installation-id)))

(handlers/register-handler-fx
 :pairing.ui/prompt-dismissed
 (fn [cofx _]
   (pairing/prompt-dismissed cofx)))

(handlers/register-handler-fx
 :pairing.ui/prompt-accepted
 (fn [cofx _]
   (pairing/prompt-accepted cofx)))

(handlers/register-handler-fx
 :pairing.callback/enable-installation-success
 (fn [cofx [_ installation-id]]
   (fx/merge cofx
             (pairing/enable installation-id)
             (multiaccounts.update/send-multiaccount-update))))

(handlers/register-handler-fx
 :pairing.callback/disable-installation-success
 (fn [cofx [_ installation-id]]
   (fx/merge cofx
             (pairing/disable installation-id)
             (multiaccounts.update/send-multiaccount-update))))

(handlers/register-handler-fx
 :stickers/load-sticker-pack-success
 (fn [cofx [_ edn-string id price]]
   (stickers/load-sticker-pack-success cofx edn-string id price)))

(handlers/register-handler-fx
 :stickers/install-pack
 (fn [cofx [_ id]]
   (stickers/install-stickers-pack cofx id)))

(handlers/register-handler-fx
 :stickers/load-packs
 (fn [cofx _]
   (stickers/load-packs cofx)))

(handlers/register-handler-fx
 :stickers/load-pack
 (fn [cofx [_ url id price]]
   (stickers/load-pack cofx url id price)))

(handlers/register-handler-fx
 :stickers/select-pack
 (fn [{:keys [db]} [_ id]]
   {:db (assoc db :stickers/selected-pack id)}))

(handlers/register-handler-fx
 :stickers/open-sticker-pack
 (fn [cofx [_ id]]
   (stickers/open-sticker-pack cofx id)))

(handlers/register-handler-fx
 :stickers/buy-pack
 (fn [cofx [_ id price]]
   (stickers/approve-pack cofx id price)))

(handlers/register-handler-fx
 :stickers/pack-owned
 (fn [cofx [_ id]]
   (stickers/pack-owned cofx id)))

(handlers/register-handler-fx
 :stickers/pending-pack
 (fn [cofx [_ id]]
   (stickers/pending-pack cofx id)))

(handlers/register-handler-fx
 :stickers/pending-timeout
 (fn [cofx _]
   (stickers/pending-timeout cofx)))

;; Tribute to Talk


;; bottom-sheet events
(handlers/register-handler-fx
 :bottom-sheet/show-sheet
 (fn [cofx [_ view options]]
   (bottom-sheet/show-bottom-sheet
    cofx
    {:view view
     :options options})))

(handlers/register-handler-fx
 :bottom-sheet/hide-sheet
 (fn [cofx _]
   (bottom-sheet/hide-bottom-sheet cofx)))

;;custom tokens

(handlers/register-handler-fx
 :wallet.custom-token/decimals-result
 (fn [cofx [_ result]]
   (custom-tokens/decimals-result cofx result)))

(handlers/register-handler-fx
 :wallet.custom-token/symbol-result
 (fn [cofx [_ contract result]]
   (custom-tokens/symbol-result cofx contract result)))

(handlers/register-handler-fx
 :wallet.custom-token/name-result
 (fn [cofx [_ contract result]]
   (custom-tokens/name-result cofx contract result)))

(handlers/register-handler-fx
 :wallet.custom-token/balance-result
 (fn [cofx [_ contract result]]
   (custom-tokens/balance-result cofx contract result)))

(handlers/register-handler-fx
 :wallet.custom-token/total-supply-result
 (fn [cofx [_ contract result]]
   (custom-tokens/total-supply-result cofx contract result)))

(handlers/register-handler-fx
 :wallet.custom-token/contract-address-is-pasted
 (fn [cofx [_ contract]]
   (custom-tokens/contract-address-is-changed cofx contract)))

(handlers/register-handler-fx
 :wallet.custom-token.ui/contract-address-paste
 (fn [_ _]
   {:wallet.custom-token/contract-address-paste nil}))

(handlers/register-handler-fx
 :wallet.custom-token.ui/field-is-edited
 (fn [cofx [_ field-key value]]
   (custom-tokens/field-is-edited cofx field-key value)))

;; ethereum subscriptions events

(handlers/register-handler-fx
 :ethereum.callback/subscription-success
 (fn [cofx [_ id handler]]
   (ethereum.subscriptions/register-subscription cofx id handler)))

;; wallet events

(handlers/register-handler-fx
 :wallet.ui/pull-to-refresh
 (fn [cofx _]
   (wallet/update-prices cofx)))

(handlers/register-handler-fx
 :wallet.transactions/add-filter
 (fn [{:keys [db]} [_ id]]
   {:db (update-in db [:wallet :filters] conj id)}))

(handlers/register-handler-fx
 :wallet.transactions/remove-filter
 (fn [{:keys [db]} [_ id]]
   {:db (update-in db [:wallet :filters] disj id)}))

(handlers/register-handler-fx
 :wallet.transactions/add-all-filters
 (fn [{:keys [db]} _]
   {:db (assoc-in db [:wallet :filters]
                  wallet.db/default-wallet-filters)}))

(handlers/register-handler-fx
 :wallet.settings/toggle-visible-token
 (fn [cofx [_ symbol checked?]]
   (wallet/toggle-visible-token cofx symbol checked?)))

(handlers/register-handler-fx
 :wallet.settings.ui/navigate-back-pressed
 (fn [cofx [_ on-close]]
   (fx/merge cofx
             (when on-close
               {:dispatch on-close})
             (navigation/navigate-back))))

(handlers/register-handler-fx
 :wallet.ui/show-transaction-details
 (fn [cofx [_ hash address]]
   (wallet/open-transaction-details cofx hash address)))

(handlers/register-handler-fx
 :wallet.setup.ui/navigate-back-pressed
 (fn [{:keys [db] :as cofx}]
   (fx/merge cofx
             {:db (assoc-in db [:wallet :send-transaction] {})}
             (navigation/navigate-back))))

(handlers/register-handler-fx
 :shake-event
 (fn [cofx _]
   (logging/show-logs-dialog cofx)))

(re-frame/reg-fx
 :dismiss-keyboard
 (fn []
   (react/dismiss-keyboard!)))

(handlers/register-handler-fx
 :dismiss-keyboard
 (fn [_]
   {:dismiss-keyboard nil}))

(handlers/register-handler-fx
 :wallet-send-request
 (fn [{:keys [db] :as cofx} [_ public-key amount symbol decimals]]
   (assert public-key)
   (let [request-command (get-in db [:id->command ["request" #{:personal-chats}]])]
     (fx/merge cofx
               (navigation/navigate-back)
               (chat/start-chat public-key nil)
               ;; TODO send
               #_(commands.sending/send public-key
                                        request-command
                                        {:asset  (name symbol)
                                         :amount (str (money/internal->formatted amount symbol decimals))})))))

(handlers/register-handler-fx
 :identicon-generated
 (fn [{:keys [db]} [_ path identicon]]
   {:db (assoc-in db path identicon)}))

(handlers/register-handler-fx
 :gfycat-generated
 (fn [{:keys [db]} [_ path gfycat]]
   {:db (assoc-in db path gfycat)}))

(handlers/register-handler-fx
 :system-theme-mode-changed
 (fn [{:keys [db]} [_ theme]]
   (let [cur-theme (get-in db [:multiaccount :appearance])]
     (when (or (nil? cur-theme) (zero?  cur-theme))
       {::multiaccounts/switch-theme (if (= :dark theme) 2 1)}))))