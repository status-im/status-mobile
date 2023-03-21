(ns status-im.qr-scanner.core
  (:require [re-frame.core :as re-frame]
            [status-im.add-new.db :as new-chat.db]
            [status-im.chat.models :as chat]
            [status-im.ethereum.core :as ethereum]
            [status-im.group-chats.core :as group-chats]
            [utils.i18n :as i18n]
            [status-im.router.core :as router]
            [utils.re-frame :as rf]
            [status-im.utils.utils :as utils]
            [status-im2.navigation.events :as navigation]
            [taoensso.timbre :as log]))

(rf/defn scan-qr-code
  {:events [::scan-code]}
  [_ opts]
  {:request-permissions-fx
   {:permissions [:camera]
    :on-allowed  #(re-frame/dispatch [:open-modal :qr-scanner opts])
    :on-denied   (fn []
                   (utils/set-timeout
                    #(utils/show-popup (i18n/label :t/error)
                                       (i18n/label :t/camera-access-error))
                    50))}})

(rf/defn set-qr-code
  {:events [:qr-scanner.callback/scan-qr-code-success]}
  [{:keys [db]} opts data]
  (when-let [handler (:handler opts)]
    {:dispatch [handler data opts]}))

(rf/defn set-qr-code-cancel
  {:events [:qr-scanner.callback/scan-qr-code-cancel]}
  [cofx opts]
  (rf/merge cofx
            (navigation/navigate-back)
            (when-let [handler (:cancel-handler opts)]
              (fn [] {:dispatch [handler opts]}))))

(rf/defn handle-browse
  [cofx {:keys [url]}]
  (rf/merge cofx
            {:browser/show-browser-selection url}
            (navigation/navigate-back)))

(rf/defn handle-private-chat
  [{:keys [db] :as cofx} {:keys [chat-id]}]
  (if-not (new-chat.db/own-public-key? db chat-id)
    {:dispatch [:chat.ui/start-chat chat-id]}
    {:utils/show-popup {:title   (i18n/label :t/unable-to-read-this-code)
                        :content (i18n/label :t/can-not-add-yourself)}}))

(rf/defn handle-public-chat
  [cofx {:keys [topic]}]
  (when (seq topic)
    (chat/start-public-chat cofx topic)))

(rf/defn handle-group-chat
  [cofx params]
  (group-chats/create-from-link cofx params))

(rf/defn handle-view-profile
  [{:keys [db] :as cofx} {:keys [public-key ens-name]}]
  (let [own (new-chat.db/own-public-key? db public-key)]
    (cond
      (and public-key own)
      (rf/merge cofx
                {:pop-to-root-fx :shell-stack}
                (navigation/navigate-to :my-profile nil))

      (and public-key (not own))
      (rf/merge cofx
                {:dispatch [:chat.ui/show-profile public-key ens-name]}
                (navigation/navigate-back))

      :else
      {:utils/show-popup {:title      (i18n/label :t/unable-to-read-this-code)
                          :content    (i18n/label :t/ens-name-not-found)
                          :on-dismiss #(re-frame/dispatch [:pop-to-root :shell-stack])}})))

(rf/defn handle-eip681
  [cofx data]
  (rf/merge cofx
            {:dispatch [:wallet/parse-eip681-uri-and-resolve-ens data]}
            (navigation/change-tab :wallet-stack)
            (navigation/pop-to-root :shell-stack)))

(rf/defn handle-wallet-connect
  {:events [::handle-wallet-connect-uri]}
  [cofx data]
  {:dispatch [:wallet-connect/pair data]})

(rf/defn handle-local-pairing
  {:events [::handle-local-pairing-uri]}
  [_ data]
  {:dispatch [:syncing/input-connection-string-for-bootstrapping data]})

(rf/defn match-scan
  {:events [::match-scanned-value]}
  [cofx {:keys [type] :as data}]
  (case type
    :public-chat  (handle-public-chat cofx data)
    :group-chat   (handle-group-chat cofx data)
    :private-chat (handle-private-chat cofx data)
    :contact      (handle-view-profile cofx data)
    :browser      (handle-browse cofx data)
    :eip681       (handle-eip681 cofx data)
    ;; Re-enable with https://github.com/status-im/status-mobile/issues/13429
    ;;:wallet-connect (handle-wallet-connect cofx data)
    :localpairing (handle-local-pairing cofx data)
    (do
      (log/info "Unable to find matcher for scanned value"
                {:type  type
                 :event ::match-scanned-value})
      {:dispatch         [:navigate-back]
       :utils/show-popup {:title      (i18n/label :t/unable-to-read-this-code)
                          :on-dismiss #(re-frame/dispatch [:pop-to-root :shell-stack])}})))

(rf/defn on-scan
  {:events [::on-scan-success]}
  [{:keys [db]} uri]
  {::router/handle-uri {:chain (ethereum/chain-keyword db)
                        :chats (get db :chats)
                        :uri   uri
                        :cb    #(re-frame/dispatch [::match-scanned-value %])}})
