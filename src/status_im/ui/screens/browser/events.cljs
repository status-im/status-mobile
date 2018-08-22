(ns status-im.ui.screens.browser.events
  (:require [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.data-store.browser :as browser-store]
            [status-im.models.browser :as model]
            [status-im.native-module.core :as status]
            [status-im.ui.components.list-selection :as list-selection]
            status-im.ui.screens.browser.navigation
            [status-im.utils.handlers :as handlers]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.utils.http :as http]
            [status-im.utils.platform :as platform]
            [status-im.utils.random :as random]
            [status-im.utils.types :as types]
            [status-im.utils.universal-links.core :as utils.universal-links]
            [taoensso.timbre :as log]))

(re-frame/reg-fx
 :browse
 (fn [link]
   (if (utils.universal-links/universal-link? link)
     (utils.universal-links/open! link)
     (list-selection/browse link))))

(re-frame/reg-fx
 :call-rpc
 (fn [[payload callback]]
   (status/call-rpc
    (types/clj->json payload)
    (fn [response]
      (if (= "" response)
        (do
          (log/warn :web3-response-error)
          (callback "web3-response-error" nil))
        (callback nil (.parse js/JSON response)))))))

(re-frame/reg-fx
 :send-to-bridge-fx
 (fn [[message webview]]
   (.sendToBridge webview (types/clj->json message))))

(handlers/register-handler-fx
 :browse-link-from-message
 (fn [_ [_ link]]
   {:browse link}))

(handlers/register-handler-fx
 :open-url-in-browser
 [re-frame/trim-v]
 (fn [cofx [url]]
   (let [normalized-url (http/normalize-and-decode-url url)]
     (model/update-browser-and-navigate cofx {:browser-id    (or (http/url-host normalized-url) (random/id))
                                              :history-index 0
                                              :history       [normalized-url]}))))

(handlers/register-handler-fx
 :send-to-bridge
 [re-frame/trim-v]
 (fn [cofx [message]]
   {:send-to-bridge-fx [message (get-in cofx [:db :webview-bridge])]}))

(handlers/register-handler-fx
 :open-browser
 [re-frame/trim-v]
 (fn [cofx [browser]]
   (model/update-browser-and-navigate cofx browser)))

(handlers/register-handler-fx
 :update-browser-on-nav-change
 [re-frame/trim-v]
 (fn [cofx [browser url loading]]
   (model/update-browser-history-fx cofx browser url loading)))

(handlers/register-handler-fx
 :update-browser-options
 [re-frame/trim-v]
 (fn [{:keys [db]} [options]]
   {:db (update db :browser/options merge options)}))

(handlers/register-handler-fx
 :remove-browser
 [re-frame/trim-v]
 (fn [{:keys [db]} [browser-id]]
   {:db            (update-in db [:browser/browsers] dissoc browser-id)
    :data-store/tx [(browser-store/remove-browser-tx browser-id)]}))

(defn nav-update-browser [cofx browser history-index]
  (model/update-browser-fx cofx (assoc browser :history-index history-index)))

(handlers/register-handler-fx
 :browser-nav-back
 [re-frame/trim-v]
 (fn [cofx [{:keys [history-index] :as browser}]]
   (when (pos? history-index)
     (nav-update-browser cofx browser (dec history-index)))))

(handlers/register-handler-fx
 :browser-nav-forward
 [re-frame/trim-v]
 (fn [cofx [{:keys [history-index] :as browser}]]
   (when (< history-index (dec (count (:history browser))))
     (nav-update-browser cofx browser (inc history-index)))))

(handlers/register-handler-fx
 :on-bridge-message
 [re-frame/trim-v]
 (fn [{:keys [db] :as cofx} [message]]
   (let [{:browser/keys [options browsers]} db
         {:keys [browser-id]} options
         browser (get browsers browser-id)
         data    (types/json->clj message)
         {{:keys [url]} :navState :keys [type host permissions payload messageId]} data]
     (cond

       (and (= type constants/history-state-changed) platform/ios? (not= "about:blank" url))
       (model/update-browser-history-fx cofx browser url false)

       (= type constants/web3-send-async)
       (model/web3-send-async payload messageId cofx)

       (= type constants/status-api-request)
       (let [{:keys [dapp? name]} browser
             dapp-name (if dapp? name host)]
         {:db       (update-in db [:browser/options :permissions-queue] conj {:dapp-name   dapp-name
                                                                              :permissions permissions})
          :dispatch [:check-permissions-queue]})))))

(handlers/register-handler-fx
 :check-permissions-queue
 [re-frame/trim-v]
 (fn [{:keys [db] :as cofx} _]
   (let [{:keys [show-permission permissions-queue]} (:browser/options db)]
     (when (and (nil? show-permission) (last permissions-queue))
       (let [{:keys [dapp-name permissions]} (last permissions-queue)
             {:account/keys [account]} db]
         (handlers-macro/merge-fx
          cofx
          {:db (update-in db [:browser/options :permissions-queue] drop-last)}
          (model/request-permission
           {:dapp-name             dapp-name
            :index                 0
            :user-permissions      (get-in db [:dapps/permissions dapp-name :permissions])
            :requested-permissions permissions
            :permissions-data      {constants/dapp-permission-contact-code (:public-key account)}})))))))

(handlers/register-handler-fx
 :next-dapp-permission
 [re-frame/trim-v]
 (fn [cofx [params permission permissions-data]]
   (model/next-permission {:params           params
                           :permission       permission
                           :permissions-data permissions-data}
                          cofx)))
