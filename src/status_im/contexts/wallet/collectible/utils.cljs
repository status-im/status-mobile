(ns status-im.contexts.wallet.collectible.utils
  (:require [status-im.config :as config]
            [status-im.constants :as constants]
            [status-im.contexts.wallet.common.utils.networks :as network-utils]
            [taoensso.timbre :as log]))

(defn collectible-balance
  [collectible]
  (let [balance (-> collectible
                    :ownership
                    first
                    :balance
                    js/parseInt)]
    (if (js/Number.isNaN balance) 0 balance)))

(def supported-collectible-types
  #{"image/jpeg"
    "image/bmp"
    "image/png"
    "image/webp"
    "image/avif"})

(defn supported-file?
  [collectible-type]
  (if (supported-collectible-types collectible-type)
    true
    (do
      (log/debug "unsupported collectible file type:" (or collectible-type "Unknown type"))
      false)))

(defn collectible-owned-counter
  [total]
  (when (> total 1) (str "x" total)))

(defn- get-opensea-network-name
  [chain-id test-networks-enabled? is-goerli-enabled?]
  (let [network-kw   (network-utils/id->network chain-id)
        network-name (name network-kw)
        mainnet?     (= :mainnet network-kw)]
    (cond (and test-networks-enabled? is-goerli-enabled? mainnet?)
          (:goerli constants/opensea-url-names)

          (and test-networks-enabled? is-goerli-enabled?)
          (str network-name "-" (:goerli constants/opensea-url-names))

          (and test-networks-enabled? mainnet?)
          (:sepolia constants/opensea-url-names)

          test-networks-enabled?
          (str network-name "-" (:sepolia constants/opensea-url-names))

          mainnet?
          (:ethereum constants/opensea-url-names)

          :else
          network-name)))

(defn- get-opensea-base-url
  [test-networks-enabled?]
  (cond
    test-networks-enabled? config/opensea-tesnet-link
    :else                  config/opensea-link))

(defn get-opensea-collectible-url
  [{:keys [chain-id token-id contract-address
           test-networks-enabled? is-goerli-enabled?]}]
  (let [base-link            (get-opensea-base-url test-networks-enabled?)
        opensea-network-name (get-opensea-network-name chain-id
                                                       test-networks-enabled?
                                                       is-goerli-enabled?)]
    (str base-link "/assets/" opensea-network-name "/" contract-address "/" token-id)))

(defn get-collectible-unique-id
  [{:keys [id]}]
  (let [chain-id         (-> id :contract-id :chain-id)
        contract-address (-> id :contract-id :address)
        token-id         (-> id :token-id)]
    (str chain-id contract-address token-id)))
