(ns status-im.ui.screens.browser.events
  (:require status-im.ui.screens.browser.navigation
            [status-im.utils.handlers :as handlers]
            [re-frame.core :as re-frame]
            [status-im.utils.random :as random]
            [status-im.i18n :as i18n]))

(handlers/register-handler-fx
  :initialize-browsers
  [(re-frame/inject-cofx :data-store/all-browsers)]
  (fn [{:keys [db all-stored-browsers]} _]
    (let [browsers (into {} (map #(vector (:browser-id %) %) all-stored-browsers))]
      {:db (assoc db :browser/browsers browsers)})))

(defn match-url [url]
  (str (when (and url (not (re-find #"^[a-zA-Z-_]+:/" url))) "http://") url))

(defn get-new-browser [browser now]
  (cond-> browser
          true
          (assoc :timestamp now)
          (not (:browser-id browser))
          (assoc :browser-id (random/id))
          (not (:name browser))
          (assoc :name (i18n/label :t/browser))
          (:url browser)
          (update :url match-url)))

(defn add-browser-fx [{:keys [db now] :as cofx} browser]
  (let [new-browser (get-new-browser browser now)]
    {:db           (update-in db [:browser/browsers (:browser-id new-browser)] merge new-browser)
     :data-store/save-browser new-browser}))

(handlers/register-handler-fx
  :open-dapp-in-browser
  [re-frame/trim-v]
  (fn [cofx [{:keys [name dapp-url] :as contact}]]
    (let [browser {:browser-id (:whisper-identity contact)
                   :name       name
                   :dapp?      true
                   :url        dapp-url
                   :contact    (:whisper-identity contact)}]
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
  (fn [{:keys [db now] :as cofx} [browser]]
    (let [new-browser (get-new-browser browser now)]
      (-> (add-browser-fx cofx new-browser)
          (update-in [:db :browser/options] #(assoc % :browser-id (:browser-id new-browser)))))))

(handlers/register-handler-fx
  :update-browser-options
  [re-frame/trim-v]
  (fn [{:keys [db now] :as cofx} [options]]
    {:db (update db :browser/options merge options)}))

(handlers/register-handler-fx
  :remove-browser
  [re-frame/trim-v]
  (fn [{:keys [db]} [browser-id]]
    {:db (update-in db [:browser/browsers] dissoc browser-id)
     :remove-browser browser-id}))
