(ns status-im.router.core
  (:require [bidi.bidi :as bidi]
            [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im2.contexts.chat.events :as chat.events]
            [status-im2.constants :as constants]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.eip681 :as eip681]
            [status-im.ethereum.ens :as ens]
            [status-im.utils.types :as types]
            [native-module.core :as native-module]
            [status-im.ethereum.stateofus :as stateofus]
            [utils.validators :as validators]
            [status-im.utils.http :as http]
            [status-im.utils.wallet-connect :as wallet-connect]
            [taoensso.timbre :as log]
            [utils.security.core :as security]))

(def ethereum-scheme "ethereum:")

(def uri-schemes ["status-im://" "status-im:"])

(def web-prefixes ["https://" "http://" "https://www." "http://www."])

(def web2-domain "join.status.im")

(def web-urls (map #(str % web2-domain "/") web-prefixes))

(def handled-schemes (set (into uri-schemes web-urls)))

(def browser-extractor
  {[#"(.*)" :domain] {""  :browser
                      "/" :browser}})

(def group-chat-extractor
  {[#"(.*)" :params] {""  :group-chat
                      "/" :group-chat}})

(def eip-extractor
  {#{[:prefix "-" :address]
     [:address]}
   {#{["@" :chain-id] ""}
    {#{["/" :function] ""}
     :ethereum}}})

(def routes
  [""
   {handled-schemes {"b/"                  browser-extractor
                     "browser/"            browser-extractor
                     ["p/" :chat-id]       :private-chat
                     ["cr/" :community-id] :community-requests
                     ["c/" :community-id]  :community
                     ["cc/" :chat-id]      :community-chat
                     "g/"                  group-chat-extractor
                     ["wallet/" :account]  :wallet-account
                     ["u/" :user-id]       :user
                     ["user/" :user-id]    :user}
    ethereum-scheme eip-extractor}])

(defn parse-query-params
  [url]
  (let [url (goog.Uri. url)]
    (http/query->map (.getQuery url))))

(defn match-uri
  [uri]
  (assoc (bidi/match-route routes uri) :uri uri :query-params (parse-query-params uri)))

(defn match-contact-async
  [chain {:keys [user-id ens-name]} callback]
  (let [valid-public-key?     (and (validators/valid-public-key? user-id)
                                   (not= user-id ens/default-key))
        valid-compressed-key? (validators/valid-compressed-key? user-id)]
    (cond
      valid-public-key?
      (callback {:type       :contact
                 :public-key user-id
                 :ens-name   ens-name})

      valid-compressed-key?
      (native-module/compressed-key->public-key
       user-id
       constants/deserialization-key
       (fn [response]
         (let [{:keys [error]} (types/json->clj response)]
           (when-not error
             (match-contact-async
              chain
              {:user-id (str "0x" (subs response 5)) :ens-name ens-name}
              callback)))))

      (and (not valid-public-key?)
           (string? user-id)
           (not (string/blank? user-id))
           (not= user-id "0x"))
      (let [chain-id   (ethereum/chain-keyword->chain-id chain)
            ens-name   (stateofus/ens-name-parse user-id)
            on-success #(match-contact-async chain {:user-id % :ens-name ens-name} callback)]
        (ens/pubkey chain-id ens-name on-success))

      :else
      (callback {:type  :contact
                 :error :not-found}))))

(defn match-group-chat
  [chats {:strs [a a1 a2]}]
  (let [[admin-pk encoded-chat-name chat-id] [a a1 a2]
        chat-id-parts                        (when (not (string/blank? chat-id))
                                               (string/split chat-id #"-"))
        chat-name                            (when (not (string/blank? encoded-chat-name))
                                               (js/decodeURI encoded-chat-name))]
    (cond
      (and (not (string/blank? chat-id))
           (not (string/blank? admin-pk))
           (not (string/blank? chat-name))
           (> (count chat-id-parts) 1)
           (not (string/blank? (first chat-id-parts)))
           (validators/valid-public-key? admin-pk)
           (validators/valid-public-key? (last chat-id-parts)))
      {:type             :group-chat
       :chat-id          chat-id
       :invitation-admin admin-pk
       :chat-name        chat-name}

      (and (not (string/blank? chat-id))
           (chat.events/group-chat? (get chats chat-id)))
      (let [{:keys [chat-name invitation-admin]} (get chats chat-id)]
        {:type             :group-chat
         :chat-id          chat-id
         :invitation-admin invitation-admin
         :chat-name        chat-name})

      :else
      {:error :invalid-group-chat-data})))

(defn match-private-chat-async
  [chain {:keys [chat-id]} cb]
  (match-contact-async chain
                       {:user-id chat-id}
                       (fn [{:keys [public-key]}]
                         (if public-key
                           (cb {:type    :private-chat
                                :chat-id public-key})
                           (cb {:type  :private-chat
                                :error :invalid-chat-id})))))

(defn match-browser
  [uri {:keys [domain]}]
  ;; NOTE: We rebuild domain from original URI and matched domain
  (let [domain (->> (string/split uri domain)
                    second
                    (str domain))]
    (if (security/safe-link? domain)
      {:type :browser
       :url  domain}
      {:type  :browser
       :error :unsafe-link})))

(defn match-browser-string
  [domain]
  (if (security/safe-link? domain)
    {:type :browser
     :url  domain}
    {:type  :browser
     :error :unsafe-link}))

;; NOTE(Ferossgp): Better to handle eip681 also with router instead of regexp.
(defn match-eip681
  [uri]
  (if-let [message (eip681/parse-uri uri)]
    (let [{:keys [paths ens-names]}
          (reduce (fn [acc path]
                    (let [address (get-in message path)]
                      (if (ens/is-valid-eth-name? address)
                        (-> acc
                            (update :paths conj path)
                            (update :ens-names conj address))
                        acc)))
                  {:paths [] :ens-names []}
                  [[:address] [:function-arguments :address]])]
      (if (empty? ens-names)
        ;; if there are no ens-names, we dispatch request-uri-parsed immediately
        {:type    :eip681
         :message message
         :uri     uri}
        {:type      :eip681
         :uri       uri
         :message   message
         :paths     paths
         :ens-names ens-names}))
    {:type  :eip681
     :uri   uri
     :error :cannot-parse}))

(defn address->eip681
  [address]
  (match-eip681 (str ethereum-scheme address)))

(defn match-wallet-account
  [{:keys [account]}]
  {:type    :wallet-account
   :account (when account (string/lower-case account))})

(defn community-route-type
  [route-params]
  (if (string/starts-with? (:community-id route-params) "z")
    :desktop-community
    :community))

(defn handle-uri
  [chain chats uri cb]
  (let [{:keys [handler route-params query-params]} (match-uri uri)]
    (log/info "[router] uri " uri " matched " handler " with " route-params)
    (cond

      (= handler :browser)
      (cb (match-browser uri route-params))

      (= handler :ethereum)
      (cb (match-eip681 uri))

      (= handler :user)
      (match-contact-async chain route-params cb)

      (= handler :private-chat)
      (match-private-chat-async chain route-params cb)

      (= handler :group-chat)
      (cb (match-group-chat chats query-params))

      (validators/valid-public-key? uri)
      (match-contact-async chain {:user-id uri} cb)

      (= handler :community-requests)
      (cb {:type handler :community-id (:community-id route-params)})

      (= handler :community)
      (cb {:type         (community-route-type route-params)
           :community-id (:community-id route-params)})

      (= handler :community-chat)
      (cb {:type handler :chat-id (:chat-id route-params)})

      (= handler :wallet-account)
      (cb (match-wallet-account route-params))

      (ethereum/address? uri)
      (cb (address->eip681 uri))

      (http/url? uri)
      (cb (match-browser-string uri))

      (wallet-connect/url? uri)
      (cb {:type :wallet-connect :data uri})

      (string/starts-with? uri constants/local-pairing-connection-string-identifier)
      (cb {:type :localpairing :data uri})

      :else
      (cb {:type :undefined
           :data uri}))))

(re-frame/reg-fx
 ::handle-uri
 (fn [{:keys [chain chats uri cb]}]
   (handle-uri chain chats uri cb)))
