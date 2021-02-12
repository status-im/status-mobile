(ns status-im.router.core
  (:require [re-frame.core :as re-frame]
            [clojure.string :as string]
            [bidi.bidi :as bidi]
            [taoensso.timbre :as log]
            [status-im.add-new.db :as public-chat.db]
            [status-im.utils.security :as security]
            [status-im.ethereum.eip681 :as eip681]
            [status-im.ethereum.ens :as ens]
            [status-im.ethereum.resolver :as resolver]
            [cljs.spec.alpha :as spec]
            [status-im.ethereum.core :as ethereum]
            [status-im.utils.db :as utils.db]
            [status-im.utils.http :as http]
            [status-im.chat.models :as chat.models]))

(def ethereum-scheme "ethereum:")

(def uri-schemes ["status-im://" "status-im:"])

(def web-prefixes ["https://" "http://" "https://www." "http://wwww."])

(def web2-domain "join.status.im")

(def web-urls (map #(str % web2-domain "/") web-prefixes))

(def handled-schemes (set (into uri-schemes web-urls)))

(def browser-extractor {[#"(.*)" :domain] {""  :browser
                                           "/" :browser}})

(def group-chat-extractor {[#"(.*)" :params] {""  :group-chat
                                              "/" :group-chat}})

(def eip-extractor {#{[:prefix "-" :address]
                      [:address]}
                    {#{["@" :chain-id] ""}
                     {#{["/" :function] ""}
                      :ethereum}}})

(def routes ["" {handled-schemes {["" :chat-id]           :public-chat
                                  "chat"                  {["/public/" :chat-id] :public-chat}
                                  "b/"                    browser-extractor
                                  "browser/"              browser-extractor
                                  ["p/" :chat-id]         :private-chat
                                  "g/"                    group-chat-extractor
                                  ["wallet/" :account]    :wallet-account
                                  ["u/" :user-id]         :user
                                  ["user/" :user-id]      :user
                                  ["referral/" :referrer] :referrals}
                 ethereum-scheme eip-extractor}])

(defn parse-query-params
  [url]
  (let [url (goog.Uri. url)]
    (http/query->map (.getQuery url))))

(defn match-uri [uri]
  (assoc (bidi/match-route routes uri) :uri uri :query-params (parse-query-params uri)))

(defn resolve-public-key
  [{:keys [chain contact-identity cb]}]
  (let [registry (get ens/ens-registries chain)
        ens-name (resolver/ens-name-parse contact-identity)]
    (resolver/pubkey registry ens-name cb)))

(defn match-contact-async
  [chain {:keys [user-id]} callback]
  (let [public-key? (and (string? user-id)
                         (string/starts-with? user-id "0x"))
        valid-key   (and (spec/valid? :global/public-key user-id)
                         (not= user-id ens/default-key))]
    (cond
      (and public-key? valid-key)
      (callback {:type       :contact
                 :public-key user-id})

      (and (not public-key?) (string? user-id))
      (let [registry   (get ens/ens-registries chain)
            ens-name   (resolver/ens-name-parse user-id)
            on-success #(match-contact-async chain {:user-id %} callback)]
        (resolver/pubkey registry ens-name on-success))

      :else
      (callback {:type  :contact
                 :error :not-found}))))

(defn match-public-chat [{:keys [chat-id]}]
  (if (public-chat.db/valid-topic? chat-id)
    {:type  :public-chat
     :topic chat-id}
    {:type  :public-chat
     :error :invalid-topic}))

(defn match-group-chat [chats {:strs [a a1 a2]}]
  (let [[admin-pk encoded-chat-name chat-id] [a a1 a2]
        chat-id-parts (when (not (string/blank? chat-id)) (string/split chat-id #"-"))
        chat-name (when (not (string/blank? encoded-chat-name)) (js/decodeURI encoded-chat-name))]
    (cond (and (not (string/blank? chat-id)) (not (string/blank? admin-pk)) (not (string/blank? chat-name))
               (> (count chat-id-parts) 1)
               (not (string/blank? (first chat-id-parts)))
               (utils.db/valid-public-key? admin-pk)
               (utils.db/valid-public-key? (last chat-id-parts)))
          {:type             :group-chat
           :chat-id          chat-id
           :invitation-admin admin-pk
           :chat-name        chat-name}

          (and (not (string/blank? chat-id))
               (chat.models/group-chat? (get chats chat-id)))
          (let [{:keys [chat-name invitation-admin]} (get chats chat-id)]
            {:type             :group-chat
             :chat-id          chat-id
             :invitation-admin invitation-admin
             :chat-name        chat-name})

          :else
          {:error :invalid-group-chat-data})))

(defn match-private-chat-async [chain {:keys [chat-id]} cb]
  (match-contact-async chain
                       {:user-id chat-id}
                       (fn [{:keys [public-key]}]
                         (if public-key
                           (cb {:type    :private-chat
                                :chat-id public-key})
                           (cb {:type  :private-chat
                                :error :invalid-chat-id})))))

(defn match-browser [uri {:keys [domain]}]
  ;; NOTE: We rebuild domain from original URI and matched domain
  (let [domain (->> (string/split uri domain)
                    second
                    (str domain))]
    (if (security/safe-link? domain)
      {:type :browser
       :url  domain}
      {:type  :browser
       :error :unsafe-link})))

;; NOTE(Ferossgp): Better to handle eip681 also with router instead of regexp.
(defn match-eip681 [uri]
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

(defn address->eip681 [address]
  (match-eip681 (str ethereum-scheme address)))

(defn match-referral [{:keys [referrer]}]
  {:type     :referrals
   :referrer referrer})

(defn match-wallet-account [{:keys [account]}]
  {:type    :wallet-account
   :account (when account (string/lower-case account))})

(defn handle-uri [chain chats uri cb]
  (let [{:keys [handler route-params query-params]} (match-uri uri)]
    (log/info "[router] uri " uri " matched " handler " with " route-params)
    (cond
      (= handler :public-chat)
      (cb (match-public-chat route-params))

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

      (spec/valid? :global/public-key uri)
      (match-contact-async chain {:user-id uri} cb)

      (= handler :referrals)
      (cb (match-referral route-params))

      (= handler :wallet-account)
      (cb (match-wallet-account route-params))

      (ethereum/address? uri)
      (cb (address->eip681 uri))

      :else
      (cb {:type :undefined
           :data uri}))))

(re-frame/reg-fx
 ::handle-uri
 (fn [{:keys [chain chats uri cb]}]
   (handle-uri chain chats uri cb)))
