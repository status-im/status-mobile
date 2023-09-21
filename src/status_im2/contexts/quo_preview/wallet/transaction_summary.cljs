(ns status-im2.contexts.quo-preview.wallet.transaction-summary
  (:require [quo2.core :as quo]
            [reagent.core :as reagent]
            [status-im2.common.resources :as resources]
            [status-im2.contexts.quo-preview.preview :as preview]
            [quo2.foundations.resources :as quo.resources]))

(def asset-snt
  {:size       24
   :type       :token
   :token-name "SNT"
   :amount     1500
   :token-logo (quo.resources/get-token :snt)})

(def asset-dai
  {:size       24
   :type       :token
   :token-name "DAI"
   :amount     2400
   :token-logo (quo.resources/get-token :dai)})

(def asset-collectible
  {:size               24
   :type               :collectible
   :collectible        (resources/mock-images :collectible)
   :collectible-name   "Collectible"
   :collectible-number "123"})

(def trip-to-vegas
  {:size         24
   :type         :account
   :account-name "Trip to Vegas"
   :emoji        "🤑"})

(def piggy-bank
  {:size         24
   :type         :account
   :account-name "Piggy bank"
   :emoji        "🐷"})

(def aretha-gosling
  {:size            24
   :type            :default
   :full-name       "Aretha Gosling"
   :profile-picture (resources/mock-images :user-picture-female2)})

(def james-bond
  {:size            24
   :type            :default
   :full-name       "James Bond"
   :profile-picture (resources/mock-images :user-picture-male4)})

(def mainnet
  {:size         24
   :type         :network
   :network-logo (quo.resources/get-network :ethereum)
   :network-name "Mainnet"})

(def optimism
  {:size         24
   :type         :network
   :network-logo (quo.resources/get-network :optimism)
   :network-name "Optimism"})

(def arbitrum
  {:size         24
   :type         :network
   :network-logo (quo.resources/get-network :arbitrum)
   :network-name "Arbitrum"})

(def multinetwork
  {:size     24
   :type     :multinetwork
   :networks [(quo.resources/get-network :ethereum)
              (quo.resources/get-network :arbitrum)
              (quo.resources/get-network :optimism)]})

(def moonpay
  {:size         24
   :type         :network
   :network-logo (quo.resources/get-network :ethereum)
   :network-name "Moonpay"})

(def binance
  {:size         24
   :type         :network
   :network-logo (quo.resources/get-network :ethereum)
   :network-name "Binance"})

(def context-tags
  [{:key   asset-snt
    :value "SNT"}
   {:key   asset-dai
    :value "UNK"}
   {:key   asset-collectible
    :value "Collectible"}
   {:key   trip-to-vegas
    :value "Account: Trip to Vegas"}
   {:key   piggy-bank
    :value "Account: Piggy bank"}
   {:key   aretha-gosling
    :value "Person: Aretha Gosling"}
   {:key   james-bond
    :value "Person: James Bond"}
   {:key   mainnet
    :value "Network: Mainnet"}
   {:key   optimism
    :value "Network: Optimism"}
   {:key   arbitrum
    :value "Network: Arbitrum"}
   {:key   multinetwork
    :value "Network: Multinetwork"}
   {:key   moonpay
    :value "Market: Moonpay"}
   {:key   binance
    :value "Market: Binance"}])

(def prefixes
  [{:key   :t/to
    :value "To"}
   {:key   :t/in
    :value "In"}
   {:key   :t/via
    :value "Via"}
   {:key   :t/from
    :value "From"}
   {:key   :t/on
    :value "On"}
   {:key   :t/at
    :value "At"}])

(def descriptor

  (concat
   [{:key     :transaction
     :type    :select
     :options [{:key :send}
               {:key :swap}
               {:key :bridge}]}
    {:label   "Slot 1"
     :key     :first-tag
     :type    :select
     :options context-tags}
    {:label   "Slot 2 prefix"
     :key     :second-tag-prefix
     :type    :select
     :options prefixes}
    {:label   "Slot 2"
     :key     :second-tag
     :type    :select
     :options context-tags}
    {:label   "Slot 3 prefix"
     :key     :third-tag-prefix
     :type    :select
     :options prefixes}
    {:label   "Slot 3"
     :key     :third-tag
     :type    :select
     :options context-tags}
    {:label   "Slot 4 prefix"
     :key     :fourth-tag-prefix
     :type    :select
     :options prefixes}
    {:label   "Slot 4"
     :key     :fourth-tag
     :type    :select
     :options context-tags}
    {:label   "Slot 5"
     :key     :fifth-tag
     :type    :select
     :options context-tags}
    {:key  :max-fees
     :type :text}
    {:key  :nonce
     :type :number}
    {:key  :input-data
     :type :text}]))

(defn view
  []
  (let [component-state (reagent/atom {:transaction       :send
                                       :first-tag         asset-snt
                                       :second-tag-prefix :t/from
                                       :second-tag        piggy-bank
                                       :third-tag-prefix  nil
                                       :third-tag         aretha-gosling
                                       :fourth-tag-prefix :t/via
                                       :fourth-tag        mainnet
                                       :fifth-tag         optimism
                                       :max-fees          "€55.57"
                                       :nonce             26
                                       :input-data        "Hello from Porto"})]
    (fn []
      [preview/preview-container
       {:state                     component-state
        :descriptor                descriptor
        :show-blur-background?     true
        :component-container-style {:align-self :center}}
       [quo/transaction-summary
        (merge {:on-press #(js/alert "Dropdown pressed")}
               @component-state)]])))
