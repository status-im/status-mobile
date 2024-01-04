(ns status-im.contexts.chat.home.add-new-contact.events
  (:require
    [clojure.string :as string]
    [status-im.common.validators :as validators]
    [status-im.contexts.chat.contacts.events :as data-store.contacts]
    status-im.contexts.chat.home.add-new-contact.effects
    [status-im.navigation.events :as navigation]
    [utils.ens.stateofus :as stateofus]
    [utils.ethereum.chain :as chain]
    [utils.re-frame :as rf]
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

(defn dispatcher [event input] (fn [arg] (rf/dispatch [event input arg])))

(rf/defn set-new-identity
  {:events [:contacts/set-new-identity]}
  [{:keys [db]} input scanned]
  (let [user-public-key (get-in db [:profile/profile :public-key])
        {:keys [input id ens state]
         :as   contact} (-> {:user-public-key user-public-key
                             :input           input
                             :scanned         scanned}
                            init-contact
                            validate-contact)]
    (case state

      :empty            {:db (dissoc db :contacts/new-identity)}
      (:valid :invalid) {:db (assoc db :contacts/new-identity contact)}
      :decompress-key   {:db (assoc db :contacts/new-identity contact)
                         :effects.contacts/decompress-public-key
                         {:compressed-key id
                          :on-success
                          (dispatcher :contacts/set-new-identity-success input)
                          :on-error
                          (dispatcher :contacts/set-new-identity-error input)}}
      :resolve-ens      {:db (assoc db :contacts/new-identity contact)
                         :effects.contacts/resolve-public-key-from-ens
                         {:chain-id (chain/chain-id db)
                          :ens ens
                          :on-success
                          (dispatcher :contacts/set-new-identity-success input)
                          :on-error
                          (dispatcher :contacts/set-new-identity-error input)}})))



(rf/defn build-contact
  {:events [:contacts/build-contact]}
  [_ pubkey ens open-profile-modal?]
  {:json-rpc/call [{:method      "wakuext_buildContact"
                    :params      [{:publicKey pubkey
                                   :ENSName   ens}]
                    :js-response true
                    :on-success  #(rf/dispatch [:contacts/contact-built
                                                pubkey
                                                open-profile-modal?
                                                (data-store.contacts/<-rpc-js %)])}]})

(rf/defn contact-built
  {:events [:contacts/contact-built]}
  [{:keys [db]} pubkey open-profile-modal? contact]
  (merge {:db (assoc-in db [:contacts/contacts pubkey] contact)}
         (when open-profile-modal?
           {:dispatch [:open-modal :profile]})))

(rf/defn set-new-identity-success
  {:events [:contacts/set-new-identity-success]}
  [{:keys [db]} input pubkey]
  (let [contact (get-in db [:contacts/new-identity])]
    (when (= (:input contact) input)
      (rf/merge {:db (assoc db
                            :contacts/new-identity
                            (->state (assoc contact :public-key pubkey)))}
                (build-contact pubkey (:ens contact) false)))))

(rf/defn set-new-identity-error
  {:events [:contacts/set-new-identity-error]}
  [{:keys [db]} input err]
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
        {:db (assoc db :contacts/new-identity (merge contact state))}))))

(rf/defn clear-new-identity
  {:events [:contacts/clear-new-identity :contacts/new-chat-focus]}
  [{:keys [db]}]
  {:db (dissoc db :contacts/new-identity)})

(rf/defn qr-code-scanned
  {:events [:contacts/qr-code-scanned]}
  [{:keys [db] :as cofx} scanned]
  (rf/merge cofx
            (set-new-identity scanned scanned)
            (navigation/navigate-back)))

(rf/defn set-new-identity-reconnected
  [{:keys [db]}]
  (let [input (get-in db [:contacts/new-identity :input])]
    (rf/dispatch [:contacts/set-new-identity input])))
