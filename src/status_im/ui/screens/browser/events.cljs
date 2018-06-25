(ns status-im.ui.screens.browser.events
  (:require status-im.ui.screens.browser.navigation
            [status-im.utils.handlers :as handlers]
            [re-frame.core :as re-frame]
            [status-im.utils.random :as random]
            [status-im.i18n :as i18n]
            [status-im.data-store.browser :as browser-store]
            [status-im.utils.http :as http]))

(handlers/register-handler-fx
 :initialize-browsers
 [(re-frame/inject-cofx :data-store/all-browsers)]
 (fn [{:keys [db all-stored-browsers]} _]
   (let [browsers (into {} (map #(vector (:browser-id %) %) all-stored-browsers))]
     {:db (assoc db :browser/browsers browsers)})))

(defn get-new-browser [browser now]
  (cond-> browser
    true
    (assoc :timestamp now)
    (not (:browser-id browser))
    (assoc :browser-id (random/id))
    (not (:name browser))
    (assoc :name (i18n/label :t/browser))
    (:url browser)
    (update :url (comp js/decodeURI http/normalize-url))))

(defn add-browser-fx [{:keys [db now]} browser]
  (let [new-browser (get-new-browser browser now)]
    {:db            (update-in db [:browser/browsers (:browser-id new-browser)]
                               merge new-browser)
     :data-store/tx [(browser-store/save-browser-tx new-browser)]}))

(handlers/register-handler-fx
 :open-dapp-in-browser
 [re-frame/trim-v]
 (fn [cofx [{:keys [name dapp-url]}]]
   (let [browser {:browser-id name
                  :name       name
                  :dapp?      true
                  :url        dapp-url}]
     (merge (add-browser-fx cofx browser)
            {:dispatch [:navigate-to :browser {:browser/browser-id (:browser-id browser)}]}))))

(handlers/register-handler-fx
 :open-browser
 [re-frame/trim-v]
 (fn [{:keys [now] :as cofx} [browser]]
   (let [new-browser (get-new-browser browser now)]
     (merge (add-browser-fx cofx new-browser)
            {:dispatch [:navigate-to :browser {:browser/browser-id (:browser-id new-browser)}]}))))

(handlers/register-handler-fx
 :update-browser
 [re-frame/trim-v]
 (fn [{:keys [now] :as cofx} [browser]]
   (let [new-browser (get-new-browser browser now)]
     (-> (add-browser-fx cofx new-browser)
         (update-in [:db :browser/options] #(assoc % :browser-id (:browser-id new-browser)))))))

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
