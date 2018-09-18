(ns status-im.ui.screens.browser.events
  (:require [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.data-store.browser :as browser-store]
            [status-im.qr-scanner.core :as qr-scanner]
            [status-im.i18n :as i18n]
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
            [taoensso.timbre :as log]
            [status-im.utils.ethereum.resolver :as resolver]
            [status-im.utils.ethereum.core :as ethereum]))

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

(re-frame/reg-fx
 :resolve-ens-multihash
 (fn [{:keys [web3 registry ens-name cb]}]
   (resolver/content web3 registry ens-name cb)))

(handlers/register-handler-fx
 :browse-link-from-message
 (fn [_ [_ link]]
   {:browse link}))

(handlers/register-handler-fx
 :ens-multihash-resolved
 (fn [{:keys [db] :as cofx} [_ hash]]
   (let [options (:browser/options db)
         browsers (:browser/browsers db)
         browser (get browsers (:browser-id options))
         history-index (:history-index browser)]
     (handlers-macro/merge-fx
      cofx
      {:db (assoc-in db [:browser/options :resolving?] false)}
      (model/update-browser-fx
       (assoc-in browser [:history history-index] (str "https://ipfs.infura.io/ipfs/" hash)))))))

(handlers/register-handler-fx
 :open-url-in-browser
 (fn [cofx [_ url]]
   (let [normalized-url (http/normalize-and-decode-url url)
         host (http/url-host normalized-url)]
     (model/update-new-browser-and-navigate
      host
      {:browser-id    (or host (random/id))
       :history-index 0
       :history       [normalized-url]}
      cofx))))

(handlers/register-handler-fx
 :send-to-bridge
 (fn [cofx [_ message]]
   {:send-to-bridge-fx [message (get-in cofx [:db :webview-bridge])]}))

(handlers/register-handler-fx
 :open-browser
 (fn [cofx [_ browser]]
   (model/update-browser-and-navigate browser cofx)))

(handlers/register-handler-fx
 :update-browser-on-nav-change
 (fn [cofx [_ browser url loading error?]]
   (let [host (http/url-host url)]
     (handlers-macro/merge-fx
      cofx
      (model/resolve-multihash-fx host loading error?)
      (model/update-browser-history-fx browser url loading)))))

(handlers/register-handler-fx
 :update-browser-options
 (fn [{:keys [db]} [_ options]]
   {:db (update db :browser/options merge options)}))

(handlers/register-handler-fx
 :remove-browser
 (fn [{:keys [db]} [_ browser-id]]
   {:db            (update-in db [:browser/browsers] dissoc browser-id)
    :data-store/tx [(browser-store/remove-browser-tx browser-id)]}))

(defn nav-update-browser [cofx browser history-index]
  (model/update-browser-fx (assoc browser :history-index history-index) cofx))

(handlers/register-handler-fx
 :browser-nav-back
 (fn [cofx [_ {:keys [history-index] :as browser}]]
   (when (pos? history-index)
     (nav-update-browser cofx browser (dec history-index)))))

(handlers/register-handler-fx
 :browser-nav-forward
 (fn [cofx [_ {:keys [history-index] :as browser}]]
   (when (< history-index (dec (count (:history browser))))
     (nav-update-browser cofx browser (inc history-index)))))

(handlers/register-handler-fx
 :browser.bridge.callback/scan-qr-code
 (fn [cofx [_ _ data message]]
   {:send-to-bridge-fx [(assoc message :result data) (get-in cofx [:db :webview-bridge])]
    :dispatch          [:navigate-back]}))

(handlers/register-handler-fx
 :on-bridge-message
 (fn [{:keys [db] :as cofx} [_ message]]
   (let [{:browser/keys [options browsers]} db
         {:keys [browser-id]} options
         browser (get browsers browser-id)
         data    (types/json->clj message)
         {{:keys [url]} :navState :keys [type host permissions payload messageId]} data
         {:keys [dapp? name]} browser
         dapp-name (if dapp? name host)]
     (cond

       (and (= type constants/history-state-changed) platform/ios? (not= "about:blank" url))
       (model/update-browser-history-fx browser url false cofx)

       (= type constants/scan-qr-code)
       (qr-scanner/scan-qr-code {:modal? false}
                                (merge {:handler :browser.bridge.callback/scan-qr-code}
                                       {:type constants/scan-qr-code-callback
                                        :data data})
                                cofx)

       (= type constants/web3-send-async)
       (model/web3-send-async payload messageId cofx)

       (= type constants/web3-send-async-read-only)
       (model/web3-send-async-read-only dapp-name payload messageId cofx)

       (= type constants/status-api-request)
       {:db       (update-in db [:browser/options :permissions-queue] conj {:dapp-name   dapp-name
                                                                            :permissions permissions})
        :dispatch [:check-permissions-queue]}))))

(handlers/register-handler-fx
 :check-permissions-queue
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
            :permissions-data      {constants/dapp-permission-contact-code (:public-key account)
                                    constants/dapp-permission-web3         (ethereum/normalized-address
                                                                            (:address account))}})))))))

(handlers/register-handler-fx
 :next-dapp-permission
 (fn [cofx [_ params permission permissions-data]]
   (model/next-permission {:params           params
                           :permission       permission
                           :permissions-data permissions-data}
                          cofx)))
