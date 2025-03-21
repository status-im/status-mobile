(ns status-im.contexts.wallet.common.utils.networks-test
  (:require
    [cljs.test :refer [are deftest]]
    [status-im.contexts.wallet.common.utils.networks :as utils]))

(deftest short-names->network-preference-prefix-test
  (are [expected short-names]
   (= expected (utils/short-names->network-preference-prefix short-names))
   "eth:"           ["eth"]
   "eth:oeth:"      ["eth" "oeth"]
   "eth:oeth:arb1:" ["eth" "oeth" "arb1"]))

(deftest network-preference-prefix->network-names-test
  (are [expected short-names]
   (= expected (utils/network-preference-prefix->network-names short-names))
   (seq [:mainnet])                     "eth"
   (seq [:mainnet :optimism])           "eth:oeth"
   (seq [:mainnet :optimism :arbitrum]) "eth:oeth:arb1"
   (seq [:mainnet :arbitrum])           "eth:sol:arb1"))

(deftest network-names->network-preference-prefix-test
  (are [expected network-names]
   (= expected (utils/network-names->network-preference-prefix network-names))
   "eth:"           [:mainnet]
   "eth:oeth:"      [:mainnet :optimism]
   "eth:oeth:arb1:" [:mainnet :optimism :arbitrum]
   "eth:arb1:"      [:mainnet :polygon :arbitrum]
   ""               []
   ""               nil))
