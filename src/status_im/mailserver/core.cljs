(ns status-im.mailserver.core
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.data-store.mailservers :as data-store.mailservers]
            [status-im.i18n :as i18n]
            [status-im.fleet.core :as fleet]
            [status-im.accounts.update.core :as accounts.update]
            [status-im.utils.fx :as fx]
            [status-im.ui.screens.navigation :as navigation]))

(def enode-address-regex #"enode://[a-zA-Z0-9]+\@\b\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}\b:(\d{1,5})")
(def enode-url-regex #"enode://[a-zA-Z0-9]+:(.+)\@\b\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}\b:(\d{1,5})")

(defn- extract-address-components [address]
  (rest (re-matches #"enode://(.*)@(.*)" address)))

(defn- extract-url-components [address]
  (rest (re-matches #"enode://(.*?):(.*)@(.*)" address)))

(defn valid-enode-url? [address]
  (re-matches enode-url-regex address))

(defn valid-enode-address? [address]
  (re-matches enode-address-regex address))

(defn build-url [address password]
  (let [[initial host] (extract-address-components address)]
    (str "enode://" initial ":" password "@" host)))

(fx/defn set-input [{:keys [db]} input-key value]
  {:db (update
        db
        :mailservers/manage
        assoc
        input-key
        {:value value
         :error (case input-key
                  :id   false
                  :name (string/blank? value)
                  :url  (not (valid-enode-url? value)))})})

(defn- address->mailserver [address]
  (let [[enode password url :as response] (extract-url-components address)]
    (cond-> {:address      (if (seq response)
                             (str "enode://" enode "@" url)
                             address)
             :user-defined true}
      password (assoc :password password))))

(defn- build [id mailserver-name address]
  (assoc (address->mailserver address)
         :id id
         :name mailserver-name))

(defn connected? [{:keys [db]} id]
  (= (:inbox/current-id db) id))

(defn fetch [{:keys [db] :as cofx} id]
  (get-in db [:inbox/wnodes (fleet/current-fleet db) id]))

(defn fetch-current [{:keys [db] :as cofx}]
  (fetch cofx (:inbox/current-id db)))

(defn preferred-mailserver-id [{:keys [db] :as cofx}]
  (get-in db [:account/account :settings :wnode (fleet/current-fleet db)]))

(defn- round-robin
  "Find the choice and pick the next one, default to first if not found"
  [choices current-id]
  (let [next-index (reduce
                    (fn [index choice]
                      (if (= current-id choice)
                        (reduced (inc index))
                        (inc index)))
                    0
                    choices)]
    (nth choices
         (mod
          next-index
          (count choices)))))

(defn selected-or-random-id
  "Use the preferred mailserver if set & exists, otherwise picks one randomly
  if current-id is not set, else round-robin"
  [{:keys [db] :as cofx}]
  (let [current-fleet (fleet/current-fleet db)
        current-id    (:inbox/current-id db)
        preference    (preferred-mailserver-id cofx)
        choices       (-> db :inbox/wnodes current-fleet keys)]
    (if (and preference
             (fetch cofx preference))
      preference
      (if current-id
        (round-robin choices current-id)
        (rand-nth choices)))))

(def default? (comp not :user-defined fetch))

(fx/defn delete
  [{:keys [db] :as cofx} id]
  (merge (when-not (or
                    (default? cofx id)
                    (connected? cofx id))
           {:db            (update-in db [:inbox/wnodes (fleet/current-fleet db)] dissoc id)
            :data-store/tx [(data-store.mailservers/delete-tx id)]})
         {:dispatch [:navigate-back]}))

(fx/defn set-current-mailserver
  [{:keys [db] :as cofx}]
  {:db (assoc db :inbox/current-id
              (selected-or-random-id cofx))})

(fx/defn set-initial-last-request
  [{:keys [db now] :as cofx}]
  {:db (update-in db [:account/account :last-request]
                  (fnil identity (quot now 1000)))})

(fx/defn add-custom-mailservers
  [{:keys [db]} mailservers]
  {:db (reduce (fn [db {:keys [id fleet] :as mailserver}]
                 (assoc-in db [:inbox/wnodes fleet id]
                           (-> mailserver
                               (dissoc :fleet)
                               (assoc :user-defined true))))
               db
               mailservers)})

(fx/defn edit [{:keys [db] :as cofx} id]
  (let [{:keys [id
                address
                password
                name]}   (fetch cofx id)
        url              (when address (build-url address password))]
    (fx/merge cofx
              (set-input :id id)
              (set-input :url (str url))
              (set-input :name (str name))
              (navigation/navigate-to-cofx :edit-mailserver nil))))

(fx/defn upsert
  [{{:mailservers/keys [manage] :account/keys [account] :as db} :db :as cofx}]
  (let [{:keys [name url id]} manage
        current-fleet         (fleet/current-fleet db)
        mailserver            (build
                               (or (:value id)
                                   (keyword (string/replace (:random-id cofx) "-" "")))
                               (:value name)
                               (:value url))
        current               (connected? cofx (:id mailserver))]
    {:db (-> db
             (dissoc :mailservers/manage)
             (assoc-in [:inbox/wnodes current-fleet (:id mailserver)] mailserver))
     :data-store/tx [{:transaction
                      (data-store.mailservers/save-tx (assoc
                                                       mailserver
                                                       :fleet
                                                       current-fleet))
                      ;; we naively logout if the user is connected to the edited mailserver
                      :success-event (when current [:accounts.logout.ui/logout-confirmed])}]
     :dispatch [:navigate-back]}))

(fx/defn show-connection-confirmation
  [{:keys [db]} mailserver-id]
  (let [current-fleet (fleet/current-fleet db)]
    {:ui/show-confirmation
     {:title               (i18n/label :t/close-app-title)
      :content             (i18n/label :t/connect-wnode-content
                                       {:name (get-in db [:inbox/wnodes  current-fleet mailserver-id :name])})
      :confirm-button-text (i18n/label :t/close-app-button)
      :on-accept           #(re-frame/dispatch [:mailserver.ui/connect-confirmed current-fleet mailserver-id])
      :on-cancel           nil}}))

(fx/defn show-delete-confirmation
  [{:keys [db]} mailserver-id]
  {:ui/show-confirmation
   {:title               (i18n/label :t/delete-mailserver-title)
    :content             (i18n/label :t/delete-mailserver-are-you-sure)
    :confirm-button-text (i18n/label :t/delete-mailserver)
    :on-accept           #(re-frame/dispatch [:mailserver.ui/delete-confirmed mailserver-id])}})

(fx/defn save-settings
  [{:keys [db] :as cofx} current-fleet mailserver-id]
  (let [settings (get-in db [:account/account :settings])]
    (accounts.update/update-settings cofx
                                     (assoc-in settings [:wnode current-fleet] mailserver-id)
                                     {:success-event [:accounts.update.callback/save-settings-success]})))

(fx/defn set-url-from-qr
  [cofx url]
  (assoc (set-input :url url cofx)
         :dispatch [:navigate-back]))
