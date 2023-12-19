(ns status-im2.common.router
  (:require
    [bidi.bidi :as bidi]
    [clojure.string :as string]
    [legacy.status-im.ethereum.ens :as ens]
    [native-module.core :as native-module]
    [re-frame.core :as re-frame]
    [status-im2.constants :as constants]
    [status-im2.contexts.chat.events :as chat.events]
    [taoensso.timbre :as log]
    [utils.address :as address]
    [utils.ens.core :as utils.ens]
    [utils.ens.stateofus :as stateofus]
    [utils.ethereum.chain :as chain]
    [utils.ethereum.eip.eip681 :as eip681]
    [utils.security.core :as security]
    [utils.transforms :as transforms]
    [utils.url :as url]
    [utils.validators :as validators]))

(def ethereum-scheme "ethereum:")

(def uri-schemes ["status-app://"])

(def web-prefixes ["https://" "http://" "https://www." "http://www."])

(def web2-domain "status.app")

(def web-urls (map #(str % web2-domain "/") web-prefixes))

(def handled-schemes (set (into uri-schemes web-urls)))

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
   {handled-schemes {["c/" :community-data]  :community
                     ["cc/" :community-data] :community-chat
                     ["p/" :chat-id]         :private-chat
                     ["cr/" :community-id]   :community-requests
                     "g/"                    group-chat-extractor
                     ["wallet/" :account]    :wallet-account
                     ["u/" :user-data]       :user
                     "c"                     :community
                     "u"                     :user}
    ethereum-scheme eip-extractor}])

(defn parse-query-params
  [url]
  (let [url (goog.Uri. url)]
    (url/query->map (.getQuery url))))

(defn parse-fragment
  [url]
  (let [url      (goog.Uri. url)
        fragment (.getFragment url)]
    (when-not (string/blank? fragment)
      fragment)))

(defn match-uri
  [uri]
  (let [;; bidi has trouble parse path with `=` in it extract `=` here and add back to parsed
        ;; base64url regex based on https://datatracker.ietf.org/doc/html/rfc4648#section-5 may
        ;; include invalid base64 (invalid length, length of any base64 encoded string must be a
        ;; multiple of 4)
        ;; equal-end-of-base64url can be `=`, `==`, `nil`
        equal-end-of-base64url
        (last (re-find #"^(https|status-app)://(status\.app/)?(c|cc|u)/([a-zA-Z0-9_-]+)(={0,2})#" uri))

        uri-without-equal-in-path
        (if equal-end-of-base64url (string/replace-first uri equal-end-of-base64url "") uri)

        ;; fragment is the one after `#`, usually user-id, ens-name, community-id
        fragment (parse-fragment uri)
        ens? (utils.ens/is-valid-eth-name? fragment)

        {:keys [handler route-params] :as parsed}
        (assoc (bidi/match-route routes uri-without-equal-in-path)
               :uri          uri
               :query-params (parse-query-params uri))]
    (cond-> parsed
      ens?
      (assoc-in [:route-params :ens-name] fragment)

      (and (or (= handler :community) (= handler :community-chat)) fragment)
      (assoc-in [:route-params :community-id] fragment)

      (and equal-end-of-base64url (= handler :community) (:community-data route-params))
      (update-in [:route-params :community-data] #(str % equal-end-of-base64url))

      (and equal-end-of-base64url (= handler :community-chat) (:community-data route-params))
      (update-in [:route-params :community-data] #(str % equal-end-of-base64url))

      (and fragment (= handler :community-chat) (:community-data route-params))
      (assoc-in [:route-params :community-id] fragment)

      (and fragment
           (= handler :community-chat)
           (:community-data route-params)
           (string? (:community-data route-params))
           (re-find constants/regx-starts-with-uuid (:community-data route-params)))
      (assoc-in [:route-params :community-channel-id] (:community-data route-params))

      (and equal-end-of-base64url (= handler :user) (:user-data route-params))
      (update-in [:route-params :user-data] #(str % equal-end-of-base64url))

      (and (= handler :user) fragment)
      (assoc-in [:route-params :user-id] fragment))))

(defn match-contact-async
  [chain {:keys [user-id ens-name]} callback]
  (let [valid-public-key?     (and (validators/valid-public-key? user-id)
                                   (not= user-id utils.ens/default-key))
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
         (let [{:keys [error]} (transforms/json->clj response)]
           (when-not error
             (match-contact-async
              chain
              {:user-id (str "0x" (subs response 5)) :ens-name ens-name}
              callback)))))

      (and (not valid-public-key?)
           (string? user-id)
           (not (string/blank? user-id))
           (not= user-id "0x"))
      (let [chain-id   (chain/chain-keyword->chain-id chain)
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

(defn match-community-channel-async
  [{:keys [community-channel-id community-id]} cb]
  (if (validators/valid-compressed-key? community-id)
    (native-module/deserialize-and-compress-key
     community-id
     #(cb {:type :community-chat :chat-id (str % community-channel-id)}))
    (cb {:type  :community-chat
         :error :not-found})))

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
                      (if (utils.ens/is-valid-eth-name? address)
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

      ;; ;; NOTE: removed in `match-uri`, might need this in the future
      ;; (= handler :browser)
      ;; (cb (match-browser uri route-params))

      (= handler :ethereum)
      (cb (match-eip681 uri))

      (and (= handler :user) (:user-id route-params))
      (match-contact-async chain route-params cb)

      ;; NOTE: removed in `match-uri`, might need this in the future
      (= handler :private-chat)
      (match-private-chat-async chain route-params cb)

      ;; NOTE: removed in `match-uri`, might need this in the future
      (= handler :group-chat)
      (cb (match-group-chat chats query-params))

      (validators/valid-public-key? uri)
      (match-contact-async chain {:user-id uri} cb)

      ;; NOTE: removed in `match-uri`, might need this in the future
      (= handler :community-requests)
      (cb {:type handler :community-id (:community-id route-params)})

      (and (= handler :community) (:community-id route-params))
      (cb {:type         (community-route-type route-params)
           :community-id (:community-id route-params)})

      (and (= handler :community-chat) (:community-channel-id route-params) (:community-id route-params))
      (match-community-channel-async route-params cb)

      (and (= handler :community-chat) (:community-id route-params))
      (cb {:type         (community-route-type route-params)
           :community-id (:community-id route-params)})

      ;; NOTE: removed in `match-uri`, might need this in the future
      (= handler :wallet-account)
      (cb (match-wallet-account route-params))

      (address/address? uri)
      (cb (address->eip681 uri))

      (url/url? uri)
      (cb (match-browser-string uri))

      (string/starts-with? uri constants/local-pairing-connection-string-identifier)
      (cb {:type :localpairing :data uri})

      :else
      (cb {:type :undefined
           :data uri}))))

(re-frame/reg-fx
 :router/handle-uri
 (fn [{:keys [chain chats uri cb]}]
   (handle-uri chain chats uri cb)))
