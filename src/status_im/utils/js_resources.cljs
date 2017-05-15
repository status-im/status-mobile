(ns status-im.utils.js-resources
  (:require-macros [status-im.utils.slurp :refer [slurp slurp-bot]])
  (:require [status-im.utils.types :refer [json->clj]]
            [clojure.string :as s]))

(def local-protocol "local://")

(defn local-resource? [url]
  (and (string? url) (s/starts-with? url local-protocol)))

(def default-contacts (json->clj (slurp "resources/default_contacts.json")))
(def default-contact-groups (json->clj (slurp "resources/default_contact_groups.json")))
(def default-networks (json->clj (slurp "resources/default_networks.json")))

(def wallet-js (slurp-bot :wallet))

(def console-js (slurp-bot :console "web3_metadata.js"))

(def browse-js (slurp-bot :browse))

(def mailman-js (slurp-bot :mailman))

(def demo-bot-js (slurp-bot :demo_bot))

(def commands-js wallet-js)

(def resources
  {:wallet-bot  wallet-js
   :console-bot console-js
   :browse-bot  browse-js
   :mailman-bot mailman-js
   :demo-bot    demo-bot-js})

(defn get-resource [url]
  (let [resource-name (keyword (subs url (count local-protocol)))]
    (resources resource-name)))

(def status-js (str (slurp "resources/status.js")
                    (slurp "resources/i18n.js")))

(def webview-js (slurp "resources/webview.js"))
(def jquery (str
              " if (typeof jQuery2 == 'undefined') {"
              (slurp "resources/jquery-3.1.1.min.js")
              "}"))
(def web3 (str "; if (typeof Web3 == 'undefined') {"
               (slurp "resources/web3.0_16_0.min.js")
               "}"))
(defn web3-init [provider-address]
  (str "var providerAddress = \"" provider-address "\";"
       (slurp "resources/web3_init.js")))
 ;;