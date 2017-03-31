(ns status-im.utils.js-resources
  (:require-macros [status-im.utils.slurp :refer [slurp]])
  (:require [status-im.utils.types :refer [json->clj]]))

(def default-contacts (json->clj (slurp "resources/default_contacts.json")))
(def default-contact-groups (json->clj (slurp "resources/default_contact_groups.json")))

(def commands-js (slurp "resources/commands.js"))
(def console-js (slurp "resources/console.js"))
(def status-js (slurp "resources/status.js"))
(def wallet-js (str commands-js (slurp "resources/wallet.js")))
(def dapp-js (str (slurp "resources/dapp.js")))

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
