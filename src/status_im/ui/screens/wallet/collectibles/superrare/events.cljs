(ns status-im.ui.screens.wallet.collectibles.superrare.events
  (:require [status-im.ui.screens.wallet.collectibles.events :as collectibles]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.http :as http]
            [status-im.utils.types :as types]))

(def superrare :SUPR)

(defmethod collectibles/load-collectible-fx superrare [_ ids]
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

(defmethod collectibles/load-collectibles-fx superrare [_ _ _ address]
  {:http-post {:url                   graphql-url
               :data                  (types/clj->json {:query (graphql-query (ethereum/naked-address address))})
               :opts                  {:headers {"Content-Type" "application/json"}}
               :success-event-creator (fn [o]
                                        [:store-collectibles superrare
                                         (get-in (http/parse-payload o) [:data :collectiblesByOwner :collectibles])])
               :failure-event-creator (fn [o]
                                        [:load-collectibles-failure (http/parse-payload o)])
               :timeout-ms            10000}})
