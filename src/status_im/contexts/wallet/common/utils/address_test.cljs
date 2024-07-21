(ns status-im.contexts.wallet.common.utils.address-test
  (:require
   [cljs.test :refer-macros [deftest is testing run-tests]]
   [status-im.contexts.wallet.common.utils.address :as utils]))

(def valid-metamask-addresses
  ["ethereum:0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2@0x1"
   "ethereum:0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2@0xa4b1"
   "ethereum:0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2@0xa"])

(def invalid-metamask-addresses
  ["ethe:0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2@0x1"
   ":0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2@0xa4b1"
   "0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2@0xa"
   "ethereum:0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2@0x1d"
   "ethereum:0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd20xa4b1"
   "ethereum:0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2:0xa"
   "ethereum:0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2"])

(def metamask-to-status
  [{:metamask  "ethereum:0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2@0x1" :status "eth:0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2"}
   {:metamask  "ethereum:0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2@0xa4b1" :status "arb1:0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2"}
   {:metamask  "ethereum:0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2@0xa" :status "oeth:0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2"}
   {:metamask "ethe:0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2@0x1" :status nil}
   {:metamask ":0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2@0xa4b1" :status nil}
   {:metamask "0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2@0xa" :status nil}
   {:metamask "ethereum:0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2@0x1d" :status nil}
   {:metamask "ethereum:0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd20xa4b1" :status nil}
   {:metamask "ethereum:0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2:0xa" :status nil}
   {:metamask "ethereum:0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2" :status nil}])

(deftest is-metamask-address?-test
  (testing "Check valid metamask addresses"
    (dorun
     (for [address valid-metamask-addresses]
       (is (utils/is-metamask-address? address)))))
  (testing "Check invalid metamask addresses"
    (dorun
     (for [address invalid-metamask-addresses]
       (is (not (utils/is-metamask-address? address)))))))

(deftest metamask-address->status-address-test
  (testing "Check metamask to status address conversion is valid"
    (dorun
     (for [[metamask-address status-address] metamask-to-status]
       (is (= status-address (utils/metamask-address->status-address metamask-address)))))))

