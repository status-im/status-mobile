(ns status-im.contexts.chat.home.add-new-contact.events
  (:require
    [clojure.string :as string]
    [re-frame.core :as re-frame]
    [status-im.common.validation.general :as validators]
    [status-im.contexts.chat.contacts.events :as data-store.contacts]
    status-im.contexts.chat.home.add-new-contact.effects
    [utils.ens.stateofus :as stateofus]
    [utils.string :as utils.string]))

(defn init-contact
  "Create a new contact (persisted to app-db as [:contacts/new-identity]).
  The following options are available:

  | key                | description |
  | -------------------|-------------|
  | `:user-public-key` | user's public key (not the contact)
  | `:input`           | raw user input (untrimmed)
  | `:scanned`         | scanned user input (untrimmed)
  | `:id`              | public-key|compressed-key|ens
  | `:type`            | :empty|:public-key|:compressed-key|:ens
  | `:ens`             | id.eth|id.ens-stateofus
  | `:public-key`      | public-key (from decompression or ens resolution)
  | `:state`           | :empty|:invalid|:decompress-key|:resolve-ens|:valid
  | `:msg`             | keyword i18n msg"
  ([]
   (-> [:user-public-key :input :scanned :id :type :ens :public-key :state :msg]
       (zipmap (repeat nil))))
  ([kv] (-> (init-contact) (merge kv))))

(def url-regex #"^https?://status.app/u(/([a-zA-Z0-9_-]+)(={0,2}))?#(.+)")

(defn ->id
  [{:keys [input] :as contact}]
  (let [trimmed-input (utils.string/safe-trim input)]
    (->> {:id (if (empty? trimmed-input)
                nil
                (if-some [id (last (re-matches url-regex trimmed-input))]
                  id
                  trimmed-input))}
         (merge contact))))

(defn ->type
  [{:keys [id] :as contact}]
  (->> (cond
         (empty? id)
         {:type :empty}

         (validators/valid-public-key? id)
         {:type       :public-key
          :public-key id}

         (validators/valid-compressed-key? id)
         {:type :compressed-key}

         :else
         {:type :ens
          :ens  (stateofus/ens-name-parse id)})
       (merge contact)))

(defn ->state
  [{:keys [id type public-key user-public-key] :as contact}]
  (->> (cond
         (empty? id)
         {:state :empty}

         (= type :public-key)
         {:state :invalid
          :msg   :t/not-a-chatkey}

         (= public-key user-public-key)
         {:state :invalid
          :msg   :t/can-not-add-yourself}

         (and (= type :compressed-key) (empty? public-key))
         {:state :decompress-key}

         (and (= type :ens) (empty? public-key))
         {:state :resolve-ens}

         (and (or (= type :compressed-key) (= type :ens))
              (validators/valid-public-key? public-key))
         {:state :valid})
       (merge contact)))

(def validate-contact (comp ->state ->type ->id))

(declare build-contact)

(defn set-new-identity
  [{:keys [db]} [{:keys [input build-success-fn failure-fn]}]]
  (let [user-public-key (get-in db [:profile/profile :public-key])
        {:keys [input id ens state]
         :as   contact} (-> {:user-public-key user-public-key
                             :input           input
                             :scanned         input}
                            init-contact
                            validate-contact)]
    (case state
      :empty            {:db (dissoc db :contacts/new-identity)}
      (:valid :invalid) {:db (assoc db :contacts/new-identity contact)}
      :decompress-key   {:db (assoc db :contacts/new-identity contact)
                         :serialization/decompress-public-key
                         {:compressed-key id
                          :on-success
                          #(re-frame/dispatch [:contacts/set-new-identity-success
                                               {:input            input
                                                :pubkey           %
                                                :build-success-fn build-success-fn}])
                          :on-error
                          #(re-frame/dispatch [:contacts/set-new-identity-error
                                               {:input      input
                                                :pubkey     %
                                                :failure-fn failure-fn}])}}
      :resolve-ens      {:db (assoc db :contacts/new-identity contact)
                         :effects.contacts/resolve-public-key-from-ens
                         {:ens ens
                          :on-success
                          #(re-frame/dispatch [:contacts/set-new-identity-success
                                               {:input            input
                                                :pubkey           %
                                                :build-success-fn build-success-fn}])
                          :on-error
                          #(re-frame/dispatch [:contacts/set-new-identity-error
                                               {:input      input
                                                :pubkey     %
                                                :failure-fn failure-fn}])}})))

(re-frame/reg-event-fx :contacts/set-new-identity set-new-identity)

(defn- set-new-identity-success
  [{:keys [db]} [{:keys [input pubkey build-success-fn]}]]
  (let [contact (get-in db [:contacts/new-identity])]
    (when (= (:input contact) input)
      {:db       (assoc db :contacts/new-identity (->state (assoc contact :public-key pubkey)))
       :dispatch [:contacts/build-contact
                  {:pubkey     pubkey
                   :ens        (:ens contact)
                   :success-fn build-success-fn}]})))

(re-frame/reg-event-fx :contacts/set-new-identity-success set-new-identity-success)

(defn- set-new-identity-error
  [{:keys [db]} [{:keys [input err failure-fn]}]]
  (let [contact (get-in db [:contacts/new-identity])]
    (when (= (:input contact) input)
      (let [state (cond
                    (and (string? err) (string/includes? err "invalid public key"))
                    {:state :invalid :msg :t/not-a-chatkey}
                    (and (string? (:message err))
                         (or (string/includes? (:message err) "fallback failed")
                             (string/includes? (:message err) "no such host")))
                    {:state :invalid :msg :t/lost-connection}
                    :else {:state :invalid})]
        (merge {:db (assoc db :contacts/new-identity (merge contact state))}
               (when failure-fn
                 (failure-fn)))))))

(re-frame/reg-event-fx :contacts/set-new-identity-error set-new-identity-error)

(defn- build-contact
  [_ [{:keys [pubkey ens success-fn]}]]
  {:json-rpc/call [{:method      "wakuext_buildContact"
                    :params      [{:publicKey pubkey
                                   :ENSName   ens}]
                    :js-response true
                    :on-success  #(re-frame/dispatch [:contacts/build-contact-success
                                                      {:pubkey     pubkey
                                                       :contact    (data-store.contacts/<-rpc-js %)
                                                       :success-fn success-fn}])}]})

(re-frame/reg-event-fx :contacts/build-contact build-contact)

(defn- build-contact-success
  [{:keys [db]} [{:keys [pubkey contact success-fn]}]]
  (merge {:db (assoc-in db [:contacts/contacts pubkey] contact)}
         (when success-fn
           (success-fn contact))))

(re-frame/reg-event-fx :contacts/build-contact-success build-contact-success)

(defn- clear-new-identity
  [{:keys [db]}]
  {:db (dissoc db :contacts/new-identity)})

(re-frame/reg-event-fx :contacts/clear-new-identity clear-new-identity)
