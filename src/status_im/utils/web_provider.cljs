(ns status-im.utils.web-provider
  (:require [taoensso.timbre :as log]
            [status-im.components.status :as status]))

(defn get-provider [rpc-url]
  #js {:sendAsync
       (fn [payload callback]
         (status/call-web3
           rpc-url
           (.stringify js/JSON payload)
           (fn [response]
             (let [response' (.parse js/JSON response)]
               (if (and (.-error response')
                        ;; "there is no running node" error
                        (= 0 (.. response' -error -code)))
                 (log/warn :there-is-no-running-node)
                 (callback nil response'))))))})
