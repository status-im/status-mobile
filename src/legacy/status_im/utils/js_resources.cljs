(ns legacy.status-im.utils.js-resources
  (:require-macros [legacy.status-im.utils.slurp :refer [slurp]])
  (:require
    [clojure.string :as string]
    [status-im.config :as config]))

(def provider-file (slurp "resources/js/provider.js"))
(defn ethereum-provider
  [network-id bridge-token]
  (str "window.statusAppNetworkId = \""
       network-id
       "\";"
       (when config/debug-webview? "window.statusAppDebug = true;")
       (string/replace provider-file
                       "'__STATUS_APP_BRIDGE_TOKEN__'"
                       (.stringify js/JSON bridge-token))))
