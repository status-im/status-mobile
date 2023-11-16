(ns status-im2.contexts.wallet.temp
  (:require [clojure.string :as string]
            [status-im2.common.resources :as resources]
            [status-im2.contexts.wallet.item-types :as types]))

(def ens-local-suggestion-saved-address-mock
  {:type     types/saved-address
   :name     "Pedro"
   :ens      "pedro.eth"
   :address  "0x4732894732894738294783294723894723984"
   :networks [:ethereum :optimism]})

(def ens-local-suggestion-mock
  {:type     types/address
   :ens      "pedro.eth"
   :address  "0x4732894732894738294783294723894723984"
   :networks [:ethereum :optimism]})

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
   :networks            [:ethereum :optimism]})

(def address-local-suggestion-mock
  {:type     types/address
   :address  "0x1233cD34De56Ef78A9B0Cd12Ef3456AbC7123dee"
   :networks [:ethereum :optimism]})

(defn find-matching-addresses
  [substring]
  (let [all-addresses [address-local-suggestion-saved-address-mock
                       address-local-suggestion-mock]]
    (vec (filter #(string/starts-with? (:address %) substring) all-addresses))))
