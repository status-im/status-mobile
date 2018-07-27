(ns status-im.utils.web3-provider
  (:require [taoensso.timbre :as log]
            [status-im.native-module.core :as status]
            [status-im.js-dependencies :as dependencies]))

;; This Web3 object will allow access to private RPC calls
;; It should be only used for internal application needs and never provided to any
;; 3rd parties (DApps, etc)
(defn make-internal-web3 []
  (dependencies/Web3.
   #js {:sendAsync (fn [payload callback]
                     (status/call-web3-private
                      (.stringify js/JSON payload)
                      (fn [response]
                        (if (= "" response)
                          (log/warn :web3-response-error)
                          (callback nil (.parse js/JSON response))))))}))
