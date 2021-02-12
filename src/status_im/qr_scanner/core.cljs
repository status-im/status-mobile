(ns status-im.qr-scanner.core
  (:require [re-frame.core :as re-frame]
            [status-im.i18n.i18n :as i18n]
            [status-im.chat.models :as chat]
            [status-im.router.core :as router]
            [status-im.navigation :as navigation]
            [status-im.utils.utils :as utils]
            [status-im.ethereum.core :as ethereum]
            [status-im.add-new.db :as new-chat.db]
            [status-im.utils.fx :as fx]
            [status-im.group-chats.core :as group-chats]))

(fx/defn scan-qr-code
  {:events [::scan-code]}
  [_ opts]
  {:request-permissions-fx
   {:permissions [:camera]
    :on-allowed  #(re-frame/dispatch [:navigate-to :qr-scanner opts])
    :on-denied   (fn []
                   (utils/set-timeout
                    #(utils/show-popup (i18n/label :t/error)
                                       (i18n/label :t/camera-access-error))
                    50))}})

(fx/defn set-qr-code
  {:events [:qr-scanner.callback/scan-qr-code-success]}
  [{:keys [db]} opts data]
  (when-let [handler (:handler opts)]
    {:dispatch [handler data opts]}))

(fx/defn set-qr-code-cancel
  {:events [:qr-scanner.callback/scan-qr-code-cancel]}
  [cofx opts]
  (fx/merge cofx
            (navigation/navigate-back)
            (when-let [handler (:cancel-handler opts)]
              (fn [] {:dispatch [handler opts]}))))

(fx/defn handle-browse [cofx {:keys [url]}]
  (fx/merge cofx
            {:browser/show-browser-selection url}
            (navigation/navigate-back)))

(fx/defn handle-private-chat [{:keys [db] :as cofx} {:keys [chat-id]}]
  (if-not (new-chat.db/own-public-key? db chat-id)
    (chat/start-chat cofx chat-id)
    {:utils/show-popup {:title   (i18n/label :t/unable-to-read-this-code)
                        :content (i18n/label :t/can-not-add-yourself)}}))

(fx/defn handle-public-chat [cofx {:keys [topic]}]
  (when (seq topic)
    (chat/start-public-chat cofx topic {})))

(fx/defn handle-group-chat [cofx params]
  (group-chats/create-from-link cofx params))

(fx/defn handle-view-profile
  [{:keys [db] :as cofx} {:keys [public-key]}]
  (let [own (new-chat.db/own-public-key? db public-key)]
    (cond
      (and public-key own)
      (navigation/navigate-to-cofx cofx :tabs {:screen :profile-stack
                                               :params {:screen :my-profile}})

      (and public-key (not own))
      (fx/merge cofx
                {:db (assoc db :contacts/identity public-key)
                 :dispatch [:navigate-to :profile]}
                (navigation/navigate-back))

      :else
      {:utils/show-popup {:title      (i18n/label :t/unable-to-read-this-code)
                          :content    (i18n/label :t/ens-name-not-found)
                          :on-dismiss #(re-frame/dispatch [:navigate-to :home])}})))

(fx/defn handle-eip681 [cofx data]
  (fx/merge cofx
            {:dispatch [:wallet/parse-eip681-uri-and-resolve-ens data]}
            (navigation/navigate-to-cofx :tabs {:screen :wallet})))

(fx/defn match-scan
  {:events [::match-scanned-value]}
  [cofx {:keys [type] :as data}]
  (case type
    :public-chat  (handle-public-chat cofx data)
    :group-chat   (handle-group-chat cofx data)
    :private-chat (handle-private-chat cofx data)
    :contact      (handle-view-profile cofx data)
    :browser      (handle-browse cofx data)
    :eip681       (handle-eip681 cofx data)
    {:utils/show-popup {:title      (i18n/label :t/unable-to-read-this-code)
                        :on-dismiss #(re-frame/dispatch [:navigate-to :home])}}))

(fx/defn on-scan
  {:events [::on-scan-success]}
  [{:keys [db]} uri]
  {::router/handle-uri {:chain (ethereum/chain-keyword db)
                        :chats (get db :chats)
                        :uri   uri
                        :cb    #(re-frame/dispatch [::match-scanned-value %])}})
