(ns status-im.ui.screens.discover.events
  (:require [re-frame.core :as re-frame]
            [status-im.ui.screens.discover.navigation]
            [status-im.utils.handlers :as handlers]
            [clojure.string :as string]))

(def request-discoveries-interval-s 600)
(def maximum-number-of-discoveries 1000)

;; EFFECTS

(re-frame/reg-fx
  ::send-portions
  (fn [{:keys [current-public-key web3 contacts to discoveries]}]
    ))

(re-frame/reg-fx
  ::request-discoveries
  (fn [{:keys [identities web3 current-public-key message-id]}]
    ))

(re-frame/reg-fx
  ::broadcast-status
  (fn [{:keys [identities web3 message]}]
    ))

;; HELPER-FN

(defn send-portions-when-contact-exists
  "Takes fx map and adds send-portions if contact exists"
  [{{:keys [current-public-key web3 discoveries]
     :contacts/keys [contacts]} :db :as fx}
   to]
  (cond-> fx
    (get contacts to)
    (assoc ::send-portions {:current-public-key current-public-key
                            :web3               web3
                            :contacts           contacts
                            :to                 to
                            :discoveries        (mapv #(dissoc % :tags) (vals discoveries))})))

(defn add-discover [db {:keys [message-id] :as discover}]
  (assoc-in db [:discoveries message-id] discover))

(defn add-discovers [db discovers]
  (reduce add-discover db discovers))

(defn new-discover? [discoveries {:keys [message-id]}]
  (not (get discoveries message-id)))


;; EVENTS

(defn navigate-to-discover-search-results [db search-tags]
  {:db       (assoc db :discover-search-tags search-tags)
   :dispatch [:navigate-to :discover-search-results]})

(handlers/register-handler-fx
  :discover/search-tags-results-view
  (fn [{:keys [db]} [_ search-text]]
    (navigate-to-discover-search-results db (reduce (fn [acc tag]
                                                      (conj acc (-> tag
                                                                    (string/replace #"#" "")
                                                                    string/lower-case
                                                                    keyword)))
                                                    #{}
                                                    (re-seq #"[^ !?,;:.]+" search-text)))))

(handlers/register-handler-fx
  :discover/search-tag-results-view
  (fn [{:keys [db]} [_ tag]]
    (navigate-to-discover-search-results db #{(keyword tag)})))

(handlers/register-handler-fx
  :discover/popular-tags-view
  (fn [{:keys [db]} [_ popular-tags]]
    {:db       (assoc db :discover-search-tags (into #{} popular-tags))
     :dispatch [:navigate-to :discover-all-hashtags]}))

(handlers/register-handler-fx
  :broadcast-status
  [(re-frame/inject-cofx :random-id)]
  (fn [{{:keys [current-public-key web3]
         :accounts/keys [account]
         :contacts/keys [contacts]} :db
        random-id :random-id}
       [_ status]]
    (when-let [hashtags (seq (handlers/get-hashtags status))]
      (let [{:keys [name photo-path]} account
            message    {:message-id random-id
                        :from       current-public-key
                        :payload    {:message-id random-id
                                     :status     status
                                     :hashtags   (vec hashtags)
                                     :profile    {:name          name
                                                  :profile-image photo-path}}}]

        {::broadcast-status {:web3       web3
                             :message    message
                             :identities (handlers/identities contacts)}
         :dispatch          [:status-received message]}))))

(handlers/register-handler-fx
  :init-discoveries
  [(re-frame/inject-cofx :data-store/discoveries)]
  (fn [{:keys [data-store/discoveries db]} _]
    {:db       (assoc db :discoveries discoveries)
     :dispatch [:request-discoveries]}))

(handlers/register-handler-fx
  :request-discoveries
  [(re-frame/inject-cofx :random-id)]
  (fn [{{:keys [current-public-key web3]
         :contacts/keys [contacts]} :db
        random-id :random-id} [this-event]]
    ;; this event calls itself at regular intervals
    ;; TODO (yenda): this was previously using setInterval explicitly, with
    ;; dispatch-later it is using it implicitly. setInterval is
    ;; problematic for such long period of time and will cause a warning
    ;; for Android in latest versions of react-nativexb
    {::request-discoveries {:current-public-key current-public-key
                            :web3               web3
                            :identities         (handlers/identities contacts)
                            :message-id         random-id}
     :dispatch-later       [{:ms       (* request-discoveries-interval-s 1000)
                             :dispatch [this-event]}]}))

(handlers/register-handler-fx
  :discoveries-send-portions
  (fn [{:keys [db]} [_ to]]
    (send-portions-when-contact-exists {:db db} to)))

(handlers/register-handler-fx
  :discoveries-request-received
  (fn [{:keys [db]} [_ {:keys [from]}]]
    (send-portions-when-contact-exists {:db db} from)))

(handlers/register-handler-fx
  :discoveries-response-received
  [(re-frame/inject-cofx :now)]
  (fn [{{:keys [discoveries]
         :contacts/keys [contacts] :as db} :db
        now :now}
       [_ {:keys [payload from]}]]
    (when (get contacts from)
      (when-let [discovers (some->> (:data payload)
                                    (filter #(new-discover? discoveries %))
                                    (map #(assoc %
                                                 :created-at now
                                                 :tags (handlers/get-hashtags (:status %)))))]
        {:db                           (add-discovers db discovers)
         :data-store.discover/save-all [discovers maximum-number-of-discoveries]}))))

(handlers/register-handler-fx
  :status-received
  [(re-frame/inject-cofx :now)]
  (fn [{{:keys [discoveries] :as db} :db
        now :now}
       [_ {{:keys [message-id status profile] :as payload} :payload
           from :from}]]
    (when (new-discover? discoveries payload)
      (let [{:keys [name profile-image]} profile
            discover {:message-id message-id
                      :name       name
                      :photo-path profile-image
                      :status     status
                      :tags       (handlers/get-hashtags status)
                      :whisper-id from
                      :created-at now}]
        {:db                           (add-discover db discover)
         :data-store.discover/save-all [[discover] maximum-number-of-discoveries]}))))

(handlers/register-handler-fx
  :show-status-author-profile
  (fn [{db :db} [_ identity]]
    {:db       (assoc db :contacts/identity identity)
     :dispatch [:navigate-to :profile]}))
