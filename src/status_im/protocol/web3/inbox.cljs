(ns status-im.protocol.web3.inbox
  (:require [status-im.protocol.web3.utils :as utils]
            [taoensso.timbre :as log]))

;; TODO(oskarth): NYI yet (both in web3.js and status-go)
(defn request-messages! [web3 opts callback]
  (log/info "offline-inbox: request-messages")
  (.requestMessages (utils/shh web3)
                    (clj->js opts)
                    callback
                    #(log/warn :request-messages-error
                               (.stringify js/JSON (clj->js opts)) %)))

