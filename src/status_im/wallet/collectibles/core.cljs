(ns status-im.wallet.collectibles.core
  (:require [re-frame.core :as re-frame]
            [status-im.browser.core :as browser]
            [status-im.constants :as constants]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.erc721 :as erc721]
            [status-im.ethereum.tokens :as tokens]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.money :as money]
            [status-im.utils.http :as http]
            [clojure.string :as string]
            [status-im.utils.types :as types]))

;;TODO: REPLACE ALL HANDLERS BY FX/DEFN

(defmulti load-collectible-fx (fn [_ symbol _] symbol))

(defmethod load-collectible-fx :default [_ _ _] nil)

(defmulti load-collectibles-fx (fn [_ symbol _ _] symbol))

(defmethod load-collectibles-fx :default [all-tokens symbol items-number address chain-id]
  {:load-collectibles-fx [all-tokens symbol items-number address chain-id]})

(defn load-token [i items-number contract address symbol]
  (when (< i items-number)
    (erc721/token-of-owner-by-index contract address i
                                    (fn [response]
                                      (load-token (inc i) items-number contract address symbol)
                                      (re-frame/dispatch [:load-collectible symbol response])))))

(re-frame/reg-fx
 :load-collectibles-fx
 (fn [[all-tokens symbol items-number address chain-id]]
   (let [chain (ethereum/chain-id->chain-keyword chain-id)
         contract (:address (tokens/symbol->token all-tokens chain symbol))]
     (load-token 0 items-number contract address symbol))))

(handlers/register-handler-fx
 :show-collectibles-list
 (fn [{:keys [db]} [_ {:keys [symbol amount] :as collectible}]]
   (let [chain-id            (get-in constants/default-networks [(:network db) :config :NetworkId])
         all-tokens          (:wallet/all-tokens db)
         items-number        (money/to-number amount)
         loaded-items-number (count (get-in db [:collectibles symbol]))]
     (merge (when (not= items-number loaded-items-number)
              (load-collectibles-fx all-tokens symbol items-number (ethereum/default-address db) chain-id))
            {:dispatch [:navigate-to :collectibles-list collectible]}))))

;; Crypto Kitties
(def ck :CK)

(handlers/register-handler-fx
 :load-kitties
 (fn [{db :db} [_ ids]]
   {:db db
    :http-get-n (mapv (fn [id]
                        {:url (str "https://api.cryptokitties.co/kitties/" id)
                         :success-event-creator (fn [o]
                                                  [:load-collectible-success ck {id (http/parse-payload o)}])
                         :failure-event-creator (fn [o]
                                                  [:load-collectible-failure ck {id (http/parse-payload o)}])})
                      ids)}))

;; TODO(andrey) Each HTTP call will return up to 100 kitties. Maybe we need to implement some kind of paging later
(defmethod load-collectibles-fx ck [_ _ items-number address _]
  {:http-get {:url                   (str "https://api.cryptokitties.co/kitties?offset=0&limit="
                                          items-number
                                          "&owner_wallet_address="
                                          address
                                          "&parents=false")
              :success-event-creator (fn [o]
                                       [:load-kitties (map :id (:kitties (http/parse-payload o)))])
              :failure-event-creator (fn [o]
                                       [:load-collectibles-failure (http/parse-payload o)])
              :timeout-ms            10000}})

;; Crypto Strikers
(def strikers :STRK)

(defmethod load-collectible-fx strikers [_ _ id]
  {:http-get {:url                   (str "https://us-central1-cryptostrikers-prod.cloudfunctions.net/cards/" id)
              :success-event-creator (fn [o]
                                       [:load-collectible-success strikers {id (http/parse-payload o)}])
              :failure-event-creator (fn [o]
                                       [:load-collectible-failure strikers {id (http/parse-payload o)}])}})

;;Etheremona
(def emona :EMONA)

(defmethod load-collectible-fx emona [_ _ id]
  {:http-get {:url                   (str "https://www.etheremon.com/api/monster/get_data?monster_ids=" id)
              :success-event-creator (fn [o]
                                       [:load-collectible-success emona (:data (http/parse-payload o))])
              :failure-event-creator (fn [o]
                                       [:load-collectible-failure emona {id (http/parse-payload o)}])}})

;;Kudos
(def kudos :KDO)

(defmethod load-collectible-fx kudos [{db :db} symbol id]
  (let [chain-id   (get-in constants/default-networks [(:network db) :config :NetworkId])
        all-tokens (:wallet/all-tokens db)]
    {:erc721-token-uri [all-tokens symbol id chain-id]}))

(re-frame/reg-fx
 :erc721-token-uri
 (fn [[all-tokens symbol tokenId chain-id]]
   (let [chain (ethereum/chain-id->chain-keyword chain-id)
         contract (:address (tokens/symbol->token all-tokens chain symbol))]
     (erc721/token-uri contract
                       tokenId
                       #(re-frame/dispatch [:token-uri-success
                                            tokenId
                                            (when %
                                              (subs % (.indexOf % "http")))]))))) ;; extra chars in rinkeby

;;Superrare
(def superrare :SUPR)

(defmethod load-collectible-fx superrare [_ _ ids]
  {:http-get-n (mapv (fn [id]
                       {:url id
                        :success-event-creator (fn [o]
                                                 [:load-collectible-success superrare {id (http/parse-payload o)}])
                        :failure-event-creator (fn [o]
                                                 [:load-collectible-failure superrare {id (http/parse-payload o)}])})
                     ids)})

(def graphql-url "https://api.pixura.io/graphql")

(defn graphql-query [address]
  (str "{
         collectiblesByOwner: allErc721Tokens(condition: {owner: \"" address "\"}) {
           collectibles: nodes {
            tokenId,
            metadata: erc721MetadatumByTokenId {
              metadataUri,
              description,
              name,
              imageUri
            }}}}"))

(defmethod load-collectibles-fx superrare [_ _ _ address _]
  {:http-post {:url                   graphql-url
               :data                  (types/clj->json {:query (graphql-query (ethereum/naked-address address))})
               :opts                  {:headers {"Content-Type" "application/json"}}
               :success-event-creator (fn [{:keys [response-body]}]
                                        [:store-collectibles superrare
                                         (get-in (http/parse-payload response-body) [:data :collectiblesByOwner :collectibles])])
               :failure-event-creator (fn [{:keys [response-body]}]
                                        [:load-collectibles-failure (http/parse-payload response-body)])
               :timeout-ms            10000}})

(handlers/register-handler-fx
 :token-uri-success
 (fn [_ [_ tokenId token-uri]]
   {:http-get {:url
               token-uri
               :success-event-creator
               (fn [o]
                 [:load-collectible-success kudos {tokenId (update (http/parse-payload o)
                                                                   :image
                                                                   string/replace
                                                                   #"http:"
                                                                   "https:")}]) ;; http in mainnet
               :failure-event-creator
               (fn [o]
                 [:load-collectible-failure kudos {tokenId (http/parse-payload o)}])}}))

;;


(handlers/register-handler-fx
 :load-collectible
 (fn [cofx [_ symbol token-id]]
   (load-collectible-fx cofx symbol token-id)))

(handlers/register-handler-fx
 :store-collectibles
 (fn [{db :db} [_ symbol collectibles]]
   {:db (update-in db [:collectibles symbol] merge
                   (reduce #(assoc %1 (:tokenId %2) %2) {} collectibles))}))

(handlers/register-handler-fx
 :load-collectible-success
 (fn [{db :db} [_ symbol collectibles]]
   {:db (update-in db [:collectibles symbol] merge collectibles)}))

(handlers/register-handler-fx
 :load-collectibles-failure
 (fn [{db :db} [_ reason]]
   {:db (update-in db [:collectibles symbol :errors] merge reason)}))

(handlers/register-handler-fx
 :load-collectible-failure
 (fn [{db :db} [_]]
   {:db db}))

(handlers/register-handler-fx
 :open-collectible-in-browser
 (fn [cofx [_ url]]
   (browser/open-url cofx url)))