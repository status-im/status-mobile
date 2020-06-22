(ns status-im.utils.js-resources
  (:require-macros [status-im.utils.slurp :refer [slurp]]))

(def provider-file (slurp "resources/js/provider.js"))
(defn ethereum-provider [network-id]
  (str "window.statusAppNetworkId = \"" network-id "\";"
       provider-file))
