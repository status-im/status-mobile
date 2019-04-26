(ns status-im.utils.js-resources
  (:require-macros [status-im.utils.slurp :refer [slurp slurp-bot]])
  (:require [status-im.utils.types :refer [json->clj]]
            [clojure.string :as s]))

(def local-protocol "local://")

(defn local-resource? [url]
  (and (string? url) (s/starts-with? url local-protocol)))

(def webview-js (slurp "resources/js/webview.js"))
(def web3 (str "; if (typeof Web3 == 'undefined') {"
               (slurp "node_modules/web3/dist/web3.min.js")
               "}"))
(defn web3-init [current-account-address network-id]
  (str "var currentAccountAddress = \"" current-account-address "\";"
       "var networkId = \"" network-id "\";"
       (slurp "resources/js/web3_init.js")))

(defn web3-opt-in-init [network-id]
  (str "var networkId = \"" network-id "\";"
       (slurp "resources/js/web3_opt_in.js")))

(defn local-storage-data [data]
  (str "var localStorageData = " (or data "{}") ";"))

(defn network-id [id]
  (str "status.ethereumNetworkId = " (or id "null") "; "))
