(ns status-im.utils.js-resources
  (:require-macros [status-im.utils.slurp :refer [slurp]]))

(def webview-js (slurp "resources/js/webview.js"))
(def provider-file (slurp "resources/js/provider.js"))

(defn ethereum-provider [network-id]
  (str "var networkId = \"" network-id "\";"
       (provider-file)))