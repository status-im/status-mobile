(ns status-im.utils.js-resources
  (:require-macros [status-im.utils.slurp :refer [slurp]])
  (:require [status-im.utils.types :refer [json->clj]]
            [clojure.string :as s]))

(def local-protocol "local://")

(defn local-resource? [url]
  (and (string? url) (s/starts-with? url local-protocol)))

(def webview-js (slurp "resources/js/webview.js"))
(def web3-file (slurp "resources/js/web3.min.js"))
(def web3
  (memoize
   (fn []
     (str "; if (typeof Web3 == 'undefined') {"
          (web3-file)
          "}"))))

(def web3-init-file (slurp "resources/js/web3_init.js"))
(defn web3-init [current-account-address network-id]
  (str "var currentAccountAddress = \"" current-account-address "\";"
       "var networkId = \"" network-id "\";"
       (web3-init-file)))

(def web3-opt-in-init-file (slurp "resources/js/web3_opt_in.js"))
(defn web3-opt-in-init [network-id]
  (str "var networkId = \"" network-id "\";"
       (web3-opt-in-init-file)))

(defn local-storage-data [data]
  (str "var localStorageData = " (or data "{}") ";"))

(defn network-id [id]
  (str "status.ethereumNetworkId = " (or id "null") "; "))
