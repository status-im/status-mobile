(ns status-im.wallet.accounts.core
  (:require [re-frame.core :as re-frame]
            [status-im.ethereum.core :as ethereum]
            [status-im.utils.fx :as fx]
            [status-im.ethereum.eip55 :as eip55]
            [status-im.ui.components.list-selection :as list-selection]))

(re-frame/reg-fx
 :list.selection/open-share
 (fn [obj]
   (list-selection/open-share obj)))

(fx/defn set-symbol-request
  {:events [:wallet.accounts/share]}
  [{:keys [db]}]
  {:list.selection/open-share {:message (eip55/address->checksum (ethereum/current-address db))}})