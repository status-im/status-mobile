(ns status-im.commands.events.loading
  (:require [clojure.string :as string]
            [clojure.set :as s]
            [re-frame.core :as re-frame]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.js-resources :as js-resources]
            [status-im.utils.types :as types]
            [status-im.utils.utils :as utils]
            [status-im.utils.config :as config]
            [status-im.native-module.core :as status]
            [status-im.data-store.local-storage :as local-storage]
            [status-im.bots.events :as bots-events]
            [taoensso.timbre :as log]))

;; COFX
(re-frame/reg-cofx
  :get-local-storage-data
  (fn [cofx]
    (assoc cofx :get-local-storage-data local-storage/get-data)))

;; FX
(re-frame/reg-fx
  ::evaluate-jail-n
  (fn [jail-data]
    (doseq [{:keys [jail-id jail-resource]} jail-data]
      (status/parse-jail
       jail-id jail-resource
       (fn [jail-response]
         (let [converted (types/json->clj jail-response)]
           (re-frame/dispatch [::proceed-loading jail-id (if config/jsc-enabled?
                                                           (update converted :result types/json->clj)
                                                           converted)])))))))

(re-frame/reg-fx
  ::show-popup
  (fn [{:keys [title msg]}]
    (utils/show-popup title msg)))

;; Handlers
(defn- valid-network-resource?
  [response]
  (some-> (.. response -headers)
          (get "Content-type")
          (string/includes? "application/javascript")))

(defn- evaluate-commands-in-jail
  [{:keys [db get-local-storage-data]} commands-resource whisper-identity]
  (let [data                  (get-local-storage-data whisper-identity)
        local-storage-snippet (js-resources/local-storage-data data)
        network-id            (get-in db [:networks/networks (:network db) :raw-config :NetworkId])
        ethereum-id-snippet   (js-resources/network-id network-id)
        commands-snippet      (str local-storage-snippet ethereum-id-snippet commands-resource)]
    {::evaluate-jail-n [{:jail-id       whisper-identity
                         :jail-resource commands-snippet}]}))

(defn load-commands
  "This function takes coeffects, effects and contact and adds effects
  for loading all commands/responses/subscriptions.

  It's currently working only for bots, eq we are not evaluating
  dapp resources in jail at all."
  [cofx fx {:keys [whisper-identity bot-url]}]
  (if bot-url
    (if-let [commands-resource (js-resources/get-resource bot-url)]
      (merge-with into fx (evaluate-commands-in-jail cofx commands-resource whisper-identity))
      (update fx :http-get-n conj {:url                   bot-url
                                   :response-validator    valid-network-resource?
                                   :success-event-creator (fn [commands-resource]
                                                            [::evaluate-commands-in-jail commands-resource whisper-identity])
                                   :failure-event-creator (fn [error-response]
                                                            [::proceed-loading whisper-identity {:error error-response}])}))
    fx))

(defn- add-exclusive-choices [initial-scope exclusive-choices]
  (reduce (fn [scopes-set exclusive-choices]
            (reduce (fn [scopes-set scope]
                      (let [exclusive-match (s/intersection scope exclusive-choices)]
                        (if (seq exclusive-match)
                          (reduce conj
                                  (disj scopes-set scope)
                                  (map (partial conj (s/difference scope exclusive-match))
                                       exclusive-match))
                          scopes-set)))
                    scopes-set
                    scopes-set))
          #{initial-scope}
          exclusive-choices))

(defn- create-access-scopes
  "Based on command owner and command scope, create set of access-scopes which can be used to directly
  look up any commands/subscriptions relevant for actual context (type of chat opened, registered user
  or not, etc.)"
  [jail-id scope]
  (let [member-scope (cond-> scope
                       (not (scope :global)) (conj jail-id))]
    (add-exclusive-choices member-scope [#{:personal-chats :group-chats}
                                         #{:anonymous :registered}
                                         #{:dapps :humans :public-chats}])))

(defn- index-by-access-scope-type
  [init jail-id items]
  (reduce (fn [acc {:keys [scope name type ref]}]
            (let [access-scopes (create-access-scopes jail-id scope)]
              (reduce (fn [acc access-scope]
                        (assoc-in acc [access-scope type name] ref))
                      acc
                      access-scopes)))
          init
          items))

(defn- enrich
  [jail-id type [_ {:keys [scope-bitmask scope name] :as props}]]
  (-> props
      (assoc :scope    (into #{} (map keyword) scope)
             :owner-id jail-id
             :bot      jail-id
             :type     type
             :ref      [jail-id type scope-bitmask name])))

(defn add-jail-result
  "This function add commands/responses/subscriptions from jail-evaluated resource
  into the database"
  [db jail-id {:keys [commands responses subscriptions]}]
  (let [enriched-commands (map (partial enrich jail-id :command) commands)
        enriched-responses (map (partial enrich jail-id :response) responses)
        new-db (reduce (fn [acc {:keys [ref] :as props}]
                         (assoc-in acc (into [:contacts/contacts] ref) props))
                       db
                       (concat enriched-commands enriched-responses))]
    (-> new-db
        (update :access-scope->commands-responses (fn [acc]
                                                    (-> (or acc {})
                                                        (index-by-access-scope-type jail-id enriched-commands)
                                                        (index-by-access-scope-type jail-id enriched-responses))))
        (update-in [:contacts/contacts jail-id] assoc
                   :subscriptions (bots-events/transform-bot-subscriptions subscriptions)
                   :jail-loaded? true))))

(handlers/register-handler-fx
  ::evaluate-commands-in-jail
  [re-frame/trim-v (re-frame/inject-cofx :get-local-storage-data)]
  (fn [cofx [commands-resource whisper-identity]]
    (evaluate-commands-in-jail cofx commands-resource whisper-identity)))

(handlers/register-handler-fx
  ::proceed-loading
  [re-frame/trim-v]
  (fn [{:keys [db]} [jail-id {:keys [error result]}]]
    (if error
      (let [message (string/join "\n" ["bot.js loading failed"
                                       jail-id
                                       error])]
        {::show-popup {:title "Error"
                       :msg   message}})
      (let [jail-loaded-events (get-in db [:contacts/contacts jail-id :jail-loaded-events])]
        (cond-> {:db (add-jail-result db jail-id result)
                 :call-jail-function {:chat-id jail-id
                                      :function :init :context
                                      {:from (get-in db [:accounts/account :address])}}}
          (seq jail-loaded-events)
          (-> (assoc :dispatch-n jail-loaded-events)
              (update-in [:db :contacts/contacts jail-id] dissoc :jail-loaded-events)))))))
