(ns status-im.utils.web-provider
  (:require [taoensso.timbre :as log]
            [status-im.components.status :as status]))

(defn get-provider [rpc-url]
  #js {:sendAsync (fn [payload callback]
                    (status/call-web3
                      rpc-url
                      (.stringify js/JSON payload)
                      (fn [response]
                        (if (= "" response)
                          (log/warn :web3-response-error)
                          (callback nil (.parse js/JSON response))))))})

(def web3 (js/require "web3"))

(defn make-web3 [rpc-url]
  (->> rpc-url
       get-provider
       web3.))
