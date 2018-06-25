(ns status-im.utils.js-resources
  (:require-macros [status-im.utils.slurp :refer [slurp slurp-bot]])
  (:require [status-im.utils.types :refer [json->clj]]
            [clojure.string :as s]))

(def local-protocol "local://")

(defn local-resource? [url]
  (and (string? url) (s/starts-with? url local-protocol)))

(def default-contacts (json->clj (slurp "resources/default_contacts.json")))

(def transactor-js (slurp-bot :transactor))

(def console-js (slurp-bot :console "web3_metadata.js"))

(def demo-bot-js (slurp-bot :demo_bot))

(def resources
  {:transactor-bot transactor-js
   :console-bot    console-js
   :demo-bot       demo-bot-js})

(defn get-resource [url]
  (let [resource-name (keyword (subs url (count local-protocol)))]
    (resources resource-name)))

(def status-js (str (slurp "resources/js/status.js")
                    (slurp "resources/js/i18n.js")))

(def webview-js (slurp "resources/js/webview.js"))
(def web3 (str "; if (typeof Web3 == 'undefined') {"
               (slurp "node_modules/web3/dist/web3.min.js")
               "}"))
(defn web3-init [provider-address current-account-address network-id]
  (str "var providerAddress = \"" provider-address "\";"
       "var currentAccountAddress = \"" current-account-address "\";"
       "var networkId = \"" network-id "\";"
       (slurp "resources/js/web3_init.js")))

(defn local-storage-data [data]
  (str "var localStorageData = " (or data "{}") ";"))

(defn network-id [id]
  (str "status.ethereumNetworkId = " (or id "null") "; "))
