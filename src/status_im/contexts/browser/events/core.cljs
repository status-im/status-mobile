(ns status-im.contexts.browser.events.core
  (:require [cljs.pprint :as pprint]
            [re-frame.core :as rf]
            [status-im.contexts.browser.db :as browser.db]
            status-im.contexts.browser.events.effects
            status-im.contexts.browser.events.rpc
            status-im.contexts.browser.events.screenshots
            [status-im.contexts.browser.messages :as messages]
            [status-im.contexts.browser.native-dapps :as native-dapps]))

(rf/reg-event-fx :browser/set-tab-ref
 (fn [{:keys [db]} [tab-id ref]]
   {:db (assoc-in db [:browser/tabs-by-id tab-id :ref] ref)}))

(rf/reg-event-fx :browser/add-tab
 (fn [{:keys [db]} [url tab-type]]
   (let [tab-id (random-uuid)]
     {:db (-> db
              (assoc-in [:browser/tabs-by-id tab-id]
                        {:type    tab-type
                         :url     url
                         :dapp-id url
                         :id      tab-id})
              (update :browser/tab-ids conj tab-id))})))

(rf/reg-event-fx :browser/focus-tab-by-idx
 (fn [{:keys [db]} [tab-idx]]
   (let [tab-id (browser.db/get-tab-id-by-index db tab-idx)]
     {:fx [[:dispatch [:browser/focus-tab tab-id]]]})))

(rf/reg-event-fx :browser/focus-tab
 (fn [{:keys [db]} [tab-id]]
   (let [tab-ids       (get db :browser/tab-ids)
         valid-tab-id? (-> tab-ids set (contains? tab-id))]
     (when valid-tab-id?
       {:db (assoc db :browser/focused-tab-id tab-id)}))))

(rf/reg-event-fx :browser/save-native-dapp
 (fn [_ [dapp-id]]
   {:fx [[:dispatch [:browser/save-dapp dapp-id (get native-dapps/metadata dapp-id)]]]}))

(rf/reg-event-fx :browser/init-native-dapps
 (fn [_]
   {:fx [[:dispatch [:browser/save-native-dapp "wallet.status"]]
         [:dispatch [:browser/save-native-dapp "messages.status"]]
         [:dispatch [:browser/save-native-dapp "communities.status"]]
         [:dispatch [:browser/add-tab "wallet.status" :tab/native]]
         [:dispatch [:browser/add-tab "messages.status" :tab/native]]
         [:dispatch [:browser/add-tab "communities.status" :tab/native]]]}))

(rf/reg-event-fx :browser/init
 (fn [{:keys [db]}]
   {:db (assoc db :browser/mode :browser-mode/browser)
    :fx [[:dispatch [:browser/init-native-dapps]]
         [:dispatch [:browser/init-tabs]]
         [:dispatch [:browser.rpc/get-permissions]]]}))

(rf/reg-event-fx :browser/init-tabs
 (fn [{:keys [db]}]
   {:db (-> db
            (assoc :browser/tabs-by-id {}
                   :browser/tab-ids    []))
    :fx [[:dispatch [:browser/add-tab "https://pancakeswap.finance/swap" :tab/web]]
         [:dispatch [:browser/add-tab "https://app.uniswap.org" :tab/web]]]}))

(rf/reg-event-fx :browser/on-message
 (fn [{:keys [_]} [tab-id js-event]]
   (let [event       (messages/parse-native-event js-event)
         event-topic (messages/event-topic event)]
     {:fx [(condp = event-topic
             "website-metadata" [:dispatch [:browser/on-website-metadata tab-id event]]
             "rpc"              [:dispatch [:browser.rpc/on-event tab-id event]]
             (do (println :unhandled-event-topic event-topic)
                 (pprint/pprint event)))]})))

(rf/reg-event-fx :browser/on-website-metadata
 (fn [{:keys [db]} [tab-id event]]
   (let [{:keys [origin-url logo-url page-title]} (messages/get-website-metadata event)
         dapp-metadata                            {:logo-url   logo-url
                                                   :origin-url origin-url
                                                   :title      page-title}]
     {:db (update-in db
                     [:browser/tabs-by-id tab-id]
                     assoc
                     :dapp-id
                     origin-url)
      :fx [[:dispatch [:browser/save-dapp origin-url dapp-metadata]]]})))

(rf/reg-event-fx :browser/save-dapp
 (fn [{:keys [db]} [dapp-id dapp-metadata]]
   (when-not (contains? (:browser/dapps db) dapp-id)
     ;;TODO: persist dapps
     {:db (assoc-in db [:browser/dapps dapp-id] dapp-metadata)})))

(rf/reg-event-fx :browser/show-tabs
 (fn [{:keys [db]}]
   {:db (assoc db :browser/mode :browser-mode/tabs)}))

(rf/reg-event-fx :browser/show-browser
 (fn [{:keys [db]}]
   {:db (assoc db :browser/mode :browser-mode/browser)}))
