(ns status-im.web3.events
  (:require [re-frame.core :as re-frame]
            [status-im.utils.handlers :as handlers]
            [status-im.web3.core :as web3]))

;;;; COFX
(re-frame/reg-cofx
 :web3/get-web3
 web3/get-web3)

;;;; FX
(re-frame/reg-fx
 :web3/get-syncing
 web3/get-syncing)

(re-frame/reg-fx
 :web3/set-default-account
 (fn [[web3 address]]
   (web3/set-default-account web3 address)))

(re-frame/reg-fx
 :web3/fetch-node-version
 web3/fetch-node-version)

;;;; Events
(handlers/register-handler-fx
 :web3/fetch-node-version-callback
 (fn [cofx [_ resp]]
   (web3/fetch-node-version-callback resp cofx)))
