(ns status-im.contexts.wallet.temp
  (:require [clojure.string :as string]
            [quo.foundations.resources :as quo.resources]
            [status-im.common.resources :as resources]
            [status-im.contexts.wallet.item-types :as types]))

(def ens-local-suggestion-saved-address-mock
  {:type                types/saved-address
   :name                "Pedro"
   :ens                 "pedro.eth"
   :address             "0x4732894732894738294783294723894723984"
   :customization-color :purple
   :networks            [{:network-name :ethereum
                          :short-name   "eth"}
                         {:network-name :optimism
                          :short-name   "opt"}]})

(def ens-local-suggestion-mock
  {:type     types/address
   :ens      "pedro.eth"
   :address  "0x4732894732894738294783294723894723984"
   :networks [{:network-name :ethereum
               :short-name   "eth"}
              {:network-name :optimism
               :short-name   "opt"}]})

(def address-local-suggestion-saved-contact-address-mock
  {:type                types/saved-contact-address
   :customization-color :blue
   :accounts            [{:name                "New House"
                          :address             "0x62cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2"
                          :emoji               "🍔"
                          :customization-color :blue}]
   :contact-props       {:full-name           "Mark Libot"
                         :profile-picture     (resources/get-mock-image :user-picture-male4)
                         :customization-color :purple}})

(def address-local-suggestion-saved-address-mock
  {:type                types/saved-address
   :name                "Peter Lamborginski"
   :address             "0x12FaBc34De56Ef78A9B0Cd12Ef3456AbC7D8E9F0"
   :customization-color :magenta
   :networks            [{:network-name :ethereum
                          :short-name   "eth"}
                         {:network-name :optimism
                          :short-name   "opt"}]})

(def address-local-suggestion-mock
  {:type     types/address
   :address  "0x1233cD34De56Ef78A9B0Cd12Ef3456AbC7123dee"
   :networks [{:network-name :ethereum
               :short-name   "eth"}
              {:network-name :optimism
               :short-name   "opt"}]})

(defn find-matching-addresses
  [substring]
  (let [all-addresses [address-local-suggestion-saved-address-mock
                       address-local-suggestion-mock]]
    (vec (filter #(string/starts-with? (:address %) substring) all-addresses))))

(def collectible-activities
  [{:transaction       :receive
    :timestamp         "Today 22:20"
    :status            :finalised
    :counter           1
    :first-tag         {:size               24
                        :type               :collectible
                        :collectible        (resources/mock-images :collectible)
                        :collectible-name   "Collectible"
                        :collectible-number "123"}
    :second-tag-prefix :t/from
    :second-tag        {:size            24
                        :type            :default
                        :full-name       "Aretha Gosling"
                        :profile-picture (resources/mock-images :user-picture-female2)}
    :third-tag-prefix  :t/to
    :third-tag         {:size         24
                        :type         :account
                        :account-name "Piggy bank"
                        :emoji        "🐷"}

    :fourth-tag-prefix :t/via
    :fourth-tag        {:size         24
                        :type         :network
                        :network-logo (quo.resources/get-network :ethereum)
                        :network-name "Mainnet"}}
   {:transaction :mint
    :timestamp "Yesterday"
    :status :finalised
    :counter 1
    :first-tag {:size               24
                :type               :collectible
                :collectible        (resources/mock-images :collectible)
                :collectible-name   "Collectible"
                :collectible-number "123"}
    :second-tag-prefix :t/at
    :second-tag
    {:size 24
     :type :address
     :address
     "0x04fcf40c526b09ff9fb22f4a5dbd08490ef9b64af700870f8a0ba2133f4251d5607ed83cd9047b8c2796576bc83fa0de23a13a4dced07654b8ff137fe744047917"}
    :third-tag-prefix :t/on
    :third-tag {:size         24
                :type         :network
                :network-logo (quo.resources/get-network :ethereum)
                :network-name "Mainnet"}}])
