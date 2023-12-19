(ns legacy.status-im.utils.js-resources
  (:require-macros [legacy.status-im.utils.slurp :refer [slurp]])
  (:require
    [status-im2.config :as config]))

(def provider-file (slurp "resources/js/provider.js"))
(defn ethereum-provider
  [network-id]
  (str "window.statusAppNetworkId = \""
       network-id
       "\";"
       (when config/debug-webview? "window.statusAppDebug = true;")
       provider-file))
