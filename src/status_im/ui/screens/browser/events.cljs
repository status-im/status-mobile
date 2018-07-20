(ns status-im.ui.screens.browser.events
  (:require status-im.ui.screens.browser.navigation
            [status-im.utils.handlers :as handlers]
            [re-frame.core :as re-frame]
            [status-im.utils.random :as random]
            [status-im.i18n :as i18n]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.utils.universal-links.core :as utils.universal-links]
            [status-im.data-store.browser :as browser-store]
            [status-im.utils.http :as http]
            [status-im.models.browser :as model]
            [status-im.utils.platform :as platform]
            [status-im.utils.utils :as utils]
            [status-im.constants :as constants]))

(re-frame/reg-fx
 :browse
 (fn [link]
   (if (utils.universal-links/universal-link? link)
     (utils.universal-links/open! link)
     (list-selection/browse link))))

(re-frame/reg-fx
 :send-to-bridge-fx
 (fn [[permissions-allowed webview]]
   (.sendToBridge @webview (.stringify js/JSON (clj->js {:type constants/status-api-success
                                                         :data permissions-allowed
                                                         :keys (keys permissions-allowed)})))))

(re-frame/reg-fx
 :show-dapp-permission-confirmation-fx
 (fn [[permission {:keys [dapp-name permissions-data] :as params}]]
   (utils/show-confirmation
    {:ios-confirm-style "default"}
    (str "\"" dapp-name "\" " (i18n/label :t/would-like-to-access) " " (:label (get model/permissions permission)))
    (i18n/label :t/make-sure-you-trust-dapp)
    nil
    #(re-frame/dispatch [:next-dapp-permission params permission permissions-data])
    #(re-frame/dispatch [:next-dapp-permission params])
    (i18n/label :t/dont-allow))))

(handlers/register-handler-fx
 :initialize-browsers
 [(re-frame/inject-cofx :data-store/all-browsers)]
 (fn [{:keys [db all-stored-browsers]} _]
   (let [browsers (into {} (map #(vector (:browser-id %) %) all-stored-browsers))]
     {:db (assoc db :browser/browsers browsers)})))

(handlers/register-handler-fx
 :initialize-dapp-permissions
 [(re-frame/inject-cofx :data-store/all-dapp-permissions)]
 (fn [{:keys [db all-dapp-permissions]} _]
   (let [dapp-permissions (into {} (map #(vector (:dapp %) %) all-dapp-permissions))]
     {:db (assoc db :dapps/permissions dapp-permissions)})))

(handlers/register-handler-fx
 :browse-link-from-message
 (fn [_ [_ link]]
   {:browse link}))

(handlers/register-handler-fx
 :open-dapp-in-browser
 [re-frame/trim-v]
 (fn [cofx [{:keys [name dapp-url]}]]
   (let [browser {:browser-id    name
                  :name          name
                  :dapp?         true
                  :history-index 0
                  :history       [(http/normalize-and-decode-url dapp-url)]}]
     (model/update-browser-and-navigate cofx browser))))

(handlers/register-handler-fx
 :open-url-in-browser
 [re-frame/trim-v]
 (fn [cofx [url]]
   (let [browser {:browser-id    (random/id)
                  :name          (i18n/label :t/browser)
                  :history-index 0
                  :history       [(http/normalize-and-decode-url url)]}]
     (model/update-browser-and-navigate cofx browser))))

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
 (fn [{:keys [db] :as cofx} [{{:keys [url]} :navState :keys [type host permissions]} browser webview]]
   (cond

     (and (= type constants/history-state-changed) platform/ios? (not= "about:blank" url))
     (model/update-browser-history-fx cofx browser url false)

     (= type constants/status-api-request)
     (let [{:account/keys [account]} db
           {:keys [dapp? name]} browser
           dapp-name (if dapp? name host)]
       (model/request-permission
        cofx
        {:dapp-name             dapp-name
         :webview               webview
         :index                 0
         :user-permissions      (get-in db [:dapps/permissions dapp-name :permissions])
         :requested-permissions permissions
         :permissions-data      {constants/dapp-permission-contact-code (:public-key account)}})))))

(handlers/register-handler-fx
 :next-dapp-permission
 [re-frame/trim-v]
 (fn [cofx [params permission permissions-data]]
   (model/next-permission cofx params permission permissions-data)))