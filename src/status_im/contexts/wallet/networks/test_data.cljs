(ns status-im.contexts.wallet.networks.test-data)

(def mainnet
  {:chain-id           1
   :network-name       :mainnet
   :block-explorer-url "https://mainnet.block-explorer/"})
(def optimism
  {:chain-id           10
   :network-name       :optimism
   :block-explorer-url "https://optimism.block-explorer/"})
(def sepolia
  {:chain-id           11155111
   :network-name       :mainnet
   :block-explorer-url "https://sepolia.block-explorer/"})
(def optimism-sepolia
  {:chain-id           11155420
   :network-name       :optimism
   :block-explorer-url "https://optimism-sepolia.block-explorer/"})

(defn get-db
  ([]
   (get-db {:testnet? false}))
  ([{:keys [testnet?]}]
   {:profile/profile {:test-networks-enabled? testnet?}
    :wallet          {:networks-by-id {1        mainnet
                                       10       optimism
                                       11155111 sepolia
                                       11155420 optimism-sepolia}
                      :networks       {:prod [mainnet optimism]
                                       :test [sepolia optimism-sepolia]}}}))
