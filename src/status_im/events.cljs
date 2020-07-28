(ns status-im.events
  (:require [re-frame.core :as re-frame]
            [status-im.bootnodes.core :as bootnodes]
            [status-im.browser.core :as browser]
            [status-im.browser.permissions :as browser.permissions]
            [status-im.chat.models :as chat]
            [status-im.chat.models.input :as chat.input]
            [status-im.chat.models.loading :as chat.loading]
            [status-im.chat.models.message :as chat.message]
            [status-im.chat.models.message-seen :as message-seen]
            [status-im.contact.block :as contact.block]
            [status-im.contact.core :as contact]
            [status-im.data-store.chats :as data-store.chats]
            [status-im.data-store.messages :as data-store.messages]
            [status-im.ethereum.subscriptions :as ethereum.subscriptions]
            [status-im.fleet.core :as fleet]
            [status-im.group-chats.core :as group-chats]
            [status-im.i18n :as i18n]
            [status-im.init.core :as init]
            [status-im.log-level.core :as log-level]
            [status-im.mailserver.constants :as mailserver.constants]
            [status-im.mailserver.core :as mailserver]
            [status-im.mailserver.topics :as mailserver.topics]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.multiaccounts.login.core :as multiaccounts.login]
            [status-im.multiaccounts.logout.core :as multiaccounts.logout]
            [status-im.multiaccounts.update.core :as multiaccounts.update]
            [status-im.pairing.core :as pairing]
            [status-im.privacy-policy.core :as privacy-policy]
            [status-im.qr-scanner.core :as qr-scanner]
            [status-im.signals.core :as signals]
            [status-im.stickers.core :as stickers]
            [status-im.transport.core :as transport]
            [status-im.transport.message.core :as transport.message]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.currency-settings.models
             :as
             currency-settings.models]
            [status-im.navigation :as navigation]
            [status-im.utils.fx :as fx]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.logging.core :as logging]
            [status-im.utils.universal-links.core :as universal-links]
            [status-im.wallet.core :as wallet]
            [status-im.wallet.custom-tokens.core :as custom-tokens]
            [status-im.wallet.db :as wallet.db]
            [taoensso.timbre :as log]
            status-im.waku.core
            status-im.wallet.choose-recipient.core
            status-im.wallet.collectibles.core
            status-im.wallet.accounts.core
            status-im.popover.core
            [status-im.keycard.core :as keycard]
            [status-im.utils.dimensions :as dimensions]
            [status-im.multiaccounts.biometric.core :as biometric]
            [status-im.constants :as constants]
            [status-im.native-module.core :as status]
            [status-im.ui.components.permissions :as permissions]
            [status-im.utils.http :as http]
            [status-im.utils.utils :as utils]
            status-im.ui.components.bottom-sheet.core
            status-im.ui.screens.add-new.new-chat.events
            status-im.ui.screens.group.chat-settings.events
            status-im.ui.screens.group.events
            status-im.utils.universal-links.events
            status-im.search.core
            status-im.ui.screens.profile.events
            status-im.chat.models.images
            status-im.ui.screens.privacy-and-security-settings.events))

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
  (.openURL ^js react/linking CUD-url))

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
               (update :keycard dissoc :application-info))}
      (multiaccounts.login/open-login key-uid photo-path name public-key)))))

(handlers/register-handler-fx
 :login/filters-initialized
 (fn [cofx]
   (universal-links/process-stored-event cofx)))

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
 (fn [cofx [_ _]]
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
                           :on-accept           #(do
                                                   (re-frame/dispatch [:bottom-sheet/hide])
                                                   (re-frame/dispatch [:chat.ui/clear-history chat-id]))}}))

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
   (let [chat-id (:current-chat-id db)
         topics  (mailserver.topics/topics-for-current-chat db)
         gaps    (keep
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
   (let [chat-id (:current-chat-id db)

         {:keys [lowest-request-from]}
         (get-in db [:mailserver/ranges chat-id])

         topics  (mailserver.topics/topics-for-current-chat db)
         gaps    [{:id   :first-gap
                   :to   lowest-request-from
                   :from (- lowest-request-from mailserver.constants/one-day)}]]
     (mailserver/fill-the-gap
      cofx
      {:gaps    gaps
       :topics  topics
       :chat-id chat-id}))))

(handlers/register-handler-fx
 :chat.ui/remove-chat-pressed
 (fn [_ [_ chat-id]]
   {:ui/show-confirmation {:title               (i18n/label :t/delete-confirmation)
                           :content             (i18n/label :t/delete-chat-confirmation)
                           :confirm-button-text (i18n/label :t/delete)
                           :on-accept           #(do
                                                   (re-frame/dispatch [:bottom-sheet/hide])
                                                   (re-frame/dispatch [:chat.ui/remove-chat chat-id]))}}))

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
 (fn [cofx [_ chat-id]]
   (chat/clear-history cofx chat-id)))

(handlers/register-handler-fx
 :chat.ui/resend-message
 (fn [{:keys [db] :as cofx} [_ chat-id message-id]]
   (let [message (get-in db [:messages chat-id message-id])]
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
 (fn [cofx [_ message]]
   (chat.input/reply-to-message cofx message)))

(handlers/register-handler-fx
 :chat.ui/send-current-message
 (fn [cofx _]
   (chat.input/send-current-message cofx)))

(defn- mark-messages-seen
  [{:keys [db] :as cofx}]
  (let [{:keys [current-chat-id]} db]
    (message-seen/mark-messages-seen cofx current-chat-id)))

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
    (chat.input/send-sticker-message sticker current-chat-id))))

(handlers/register-handler-fx
 :chat/send-audio
 (fn [{{:keys [current-chat-id]} :db :as cofx} [_ audio-path duration]]
   (chat.input/send-audio-message cofx audio-path duration current-chat-id)))

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

;; keycard module

(handlers/register-handler-fx
 :keycard.ui/go-to-settings-button-pressed
 (fn [_ _]
   {:keycard/open-nfc-settings nil}))

(handlers/register-handler-fx
 :keycard.ui/pair-card-button-pressed
 (fn [{:keys [db]} _]
   {:db (assoc-in db [:keycard :setup-step] :enter-pair-code)}))

(handlers/register-handler-fx
 :keycard.ui/pair-code-input-changed
 (fn [{:keys [db]} [_ pair-code]]
   {:db (assoc-in db [:keycard :secrets :password] pair-code)}))

(handlers/register-handler-fx
 :keycard.ui/recovery-phrase-confirm-word-back-button-pressed
 (fn [{:keys [db]} _]
   {:db (assoc-in db [:keycard :setup-step] :recovery-phrase)}))

(handlers/register-handler-fx
 :keycard.ui/recovery-phrase-confirm-word-input-changed
 (fn [{:keys [db]} [_ input]]
   {:db (assoc-in db [:keycard :recovery-phrase :input-word] input)}))

(handlers/register-handler-fx
 :keycard.ui/recovery-phrase-cancel-pressed
 (fn [{:keys [db]} _]
   {:db (assoc-in db [:keycard :setup-step] :recovery-phrase)}))

(handlers/register-handler-fx
 :keycard.ui/pin-numpad-delete-button-pressed
 (fn [{:keys [db]} [_ step]]
   (when-not (empty? (get-in db [:keycard :pin step]))
     {:db (update-in db [:keycard :pin step] pop)})))

(handlers/register-handler-fx
 :keycard.ui/create-pin-button-pressed
 (fn [{:keys [db]} _]
   {:db (-> db
            (assoc-in [:keycard :setup-step] :pin)
            (assoc-in [:keycard :pin :enter-step] :original))}))

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
 :group-chats.ui/leave-chat-pressed
 (fn [_ [_ chat-id _]]
   {:ui/show-confirmation {:title               (i18n/label :t/leave-confirmation)
                           :content             (i18n/label :t/leave-chat-confirmation)
                           :confirm-button-text (i18n/label :t/leave)
                           :on-accept           #(do
                                                   (re-frame/dispatch [:bottom-sheet/hide])
                                                   (re-frame/dispatch [:group-chats.ui/leave-chat-confirmed chat-id]))}}))
(handlers/register-handler-fx
 :group-chats.ui/join-pressed
 (fn [cofx [_ chat-id]]
   (group-chats/join-chat cofx chat-id)))

;; transport module

(handlers/register-handler-fx
 :transport/send-status-message-error
 (fn [_ [_ err]]
   (log/error :send-status-message-error err)))

(fx/defn handle-update [cofx {:keys [chats messages] :as response}]
  (let [chats       (map data-store.chats/<-rpc chats)
        messages    (map data-store.messages/<-rpc messages)
        message-fxs (map chat.message/receive-one messages)
        chat-fxs    (map #(chat/ensure-chat (dissoc % :unviewed-messages-count)) chats)]
    (apply fx/merge cofx (concat chat-fxs message-fxs))))

(handlers/register-handler-fx
 :transport/message-sent
 (fn [cofx [_ response messages-count]]
   (let [set-hash-fxs (map (fn [{:keys [localChatId id messageType]}]
                             (transport.message/set-message-envelope-hash localChatId id messageType messages-count))
                           (:messages response))]
     (apply fx/merge cofx
            (conj set-hash-fxs
                  (handle-update response))))))

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

(handlers/register-handler-fx
 :contact.ui/start-group-chat-pressed
 (fn [cofx _]
   (contact/open-contact-toggle-list cofx)))

(handlers/register-handler-fx
 :contact.ui/send-message-pressed
 [(re-frame/inject-cofx :random-id-generator)]
 (fn [cofx [_ {:keys [public-key]}]]
   (chat/start-chat cofx public-key {:navigation-reset? true})))

(handlers/register-handler-fx
 :contact.ui/contact-code-submitted
 [(re-frame/inject-cofx :random-id-generator)]
 (fn [{{:contacts/keys [new-identity]} :db :as cofx} [_ new-contact?]]
   (let [{:keys [public-key ens-name]} new-identity]
     (fx/merge cofx
               #(if new-contact?
                  (contact/add-contact % public-key)
                  (chat/start-chat % public-key {:navigation-reset? true}))
               #(when new-contact?
                  (navigation/navigate-back %))
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
 (fn [cofx [_ public-key _ _ _]]
   (assert public-key)
   (fx/merge cofx
             (navigation/navigate-back)
             (chat/start-chat public-key nil)
             ;; TODO send
             #_(commands.sending/send public-key
                                      request-command
                                      {:asset  (name symbol)
                                       :amount (str (money/internal->formatted amount symbol decimals))}))))

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
     (when (or (nil? cur-theme) (zero? cur-theme))
       {::multiaccounts/switch-theme (if (= :dark theme) 2 1)}))))

(defn- http-get [{:keys [url response-validator success-event-creator failure-event-creator timeout-ms]}]
  (let [on-success #(re-frame/dispatch (success-event-creator %))
        on-error   (when failure-event-creator #(re-frame/dispatch (failure-event-creator %)))
        opts       {:valid-response? response-validator
                    :timeout-ms      timeout-ms}]
    (http/get url on-success on-error opts)))

(re-frame/reg-fx
 :http-get
 http-get)

(defn- http-raw-get [{:keys [url success-event-creator failure-event-creator timeout-ms]}]
  (let [on-success #(when-let [event (success-event-creator %)] (re-frame/dispatch event))
        on-error   (when failure-event-creator #(re-frame/dispatch (failure-event-creator %)))
        opts       {:timeout-ms timeout-ms}]
    (http/raw-get url on-success on-error opts)))

(re-frame/reg-fx
 :http-raw-get
 http-raw-get)

(re-frame/reg-fx
 :http-get-n
 (fn [calls]
   (doseq [call calls]
     (http-get call))))

(defn- http-post [{:keys [url data response-validator success-event-creator failure-event-creator timeout-ms opts]}]
  (let [on-success #(re-frame/dispatch (success-event-creator %))
        on-error   (when failure-event-creator #(re-frame/dispatch (failure-event-creator %)))
        all-opts   (assoc opts
                          :valid-response? response-validator
                          :timeout-ms timeout-ms)]
    (http/post url data on-success on-error all-opts)))

(re-frame/reg-fx
 :http-post
 http-post)

(defn- http-raw-post [{:keys [url body response-validator on-success on-error timeout-ms opts]}]
  (let [all-opts   (assoc opts
                          :valid-response? response-validator
                          :timeout-ms timeout-ms)]
    (http/raw-post url body on-success on-error all-opts)))

(re-frame/reg-fx
 :http-raw-post
 http-raw-post)

(re-frame/reg-fx
 :request-permissions-fx
 (fn [options]
   (permissions/request-permissions options)))

(re-frame/reg-fx
 :ui/listen-to-window-dimensions-change
 (fn []
   (dimensions/add-event-listener)))

(re-frame/reg-fx
 :ui/show-error
 (fn [content]
   (utils/show-popup "Error" content)))

(re-frame/reg-fx
 :ui/show-confirmation
 (fn [options]
   (utils/show-confirmation options)))

(re-frame/reg-fx
 :ui/close-application
 (fn [_]
   (status/close-application)))

(re-frame/reg-fx
 ::app-state-change-fx
 (fn [state]
   (status/app-state-change state)))

(handlers/register-handler-fx
 :set
 (fn [{:keys [db]} [_ k v]]
   {:db (assoc db k v)}))

(handlers/register-handler-fx
 :set-once
 (fn [{:keys [db]} [_ k v]]
   (when-not (get db k)
     {:db (assoc db k v)})))

(handlers/register-handler-fx
 :set-in
 (fn [{:keys [db]} [_ path v]]
   {:db (assoc-in db path v)}))

(def authentication-options
  {:reason (i18n/label :t/biometric-auth-reason-login)})

(defn- on-biometric-auth-result [{:keys [bioauth-success bioauth-code bioauth-message]}]
  (when-not bioauth-success
    (if (= bioauth-code "USER_FALLBACK")
      (re-frame/dispatch [:multiaccounts.logout.ui/logout-confirmed])
      (utils/show-confirmation {:title               (i18n/label :t/biometric-auth-confirm-title)
                                :content             (or bioauth-message (i18n/label :t/biometric-auth-confirm-message))
                                :confirm-button-text (i18n/label :t/biometric-auth-confirm-try-again)
                                :cancel-button-text  (i18n/label :t/biometric-auth-confirm-logout)
                                :on-accept           #(biometric/authenticate nil on-biometric-auth-result authentication-options)
                                :on-cancel           #(re-frame/dispatch [:multiaccounts.logout.ui/logout-confirmed])}))))

(fx/defn on-return-from-background [{:keys [db now] :as cofx}]
  (let [app-in-background-since (get db :app-in-background-since)
        signed-up?              (get-in db [:multiaccount :signed-up?])
        biometric-auth?         (= (:auth-method db) "biometric")
        requires-bio-auth       (and
                                 signed-up?
                                 biometric-auth?
                                 (some? app-in-background-since)
                                 (>= (- now app-in-background-since)
                                     constants/ms-in-bg-for-require-bioauth))]
    (fx/merge cofx
              {:db (-> db
                       (dissoc :app-in-background-since)
                       (assoc :app-active-since now))}
              (mailserver/process-next-messages-request)
              #(when requires-bio-auth
                 (biometric/authenticate % on-biometric-auth-result authentication-options)))))

(fx/defn on-going-in-background [{:keys [db now]}]
  {:db (-> db
           (dissoc :app-active-since)
           (assoc :app-in-background-since now))
   :dispatch-n [[:audio-recorder/on-background] [:audio-message/on-background]]})

(defn app-state-change [state {:keys [db] :as cofx}]
  (let [app-coming-from-background? (= state "active")
        app-going-in-background?    (= state "background")]
    (fx/merge cofx
              {::app-state-change-fx state
               :db                   (assoc db :app-state state)}
              #(when app-coming-from-background?
                 (on-return-from-background %))
              #(when app-going-in-background?
                 (on-going-in-background %)))))

(handlers/register-handler-fx
 :app-state-change
 (fn [cofx [_ state]]
   (app-state-change state cofx)))

(handlers/register-handler-fx
 :request-permissions
 (fn [_ [_ options]]
   {:request-permissions-fx options}))

(handlers/register-handler-fx
 :set-swipe-position
 (fn [{:keys [db]} [_ type item-id value]]
   {:db (assoc-in db [:animations type item-id :delete-swiped] value)}))

(handlers/register-handler-fx
 :update-window-dimensions
 (fn [{:keys [db]} [_ dimensions]]
   {:db (assoc db :dimensions/window (dimensions/window dimensions))}))

;; NOTE: Will be removed with the keycard PR
(handlers/register-handler-fx
 :screens/on-will-focus
 (fn [cofx [_ view-id]]
   (fx/merge cofx
             #(case view-id
                :keycard-settings (keycard/settings-screen-did-load %)
                :reset-card (keycard/reset-card-screen-did-load %)
                :enter-pin-settings (keycard/enter-pin-screen-did-load %)
                :keycard-login-pin (keycard/enter-pin-screen-did-load %)
                :add-new-account-pin (keycard/enter-pin-screen-did-load %)
                :keycard-authentication-method (keycard/authentication-method-screen-did-load %)
                ;; We need this as if you click on universal-links you transition
                ;; from chat to chat, and therefore we won't be loading new
                ;; messages
                :chat (chat.loading/load-messages %)
                :multiaccounts (keycard/multiaccounts-screen-did-load %)
                nil))))
