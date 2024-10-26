(ns status-im.subs.wallet.screens.input-amount
  (:require
    [clojure.string :as string]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.common.utils :as utils]
    [status-im.contexts.wallet.send.input-amount.controlled-input-logic :as controlled-input-logic]
    [utils.money :as money]
    [utils.number :as number]
    [utils.re-frame :as rf]))

(rf/reg-sub :send-input-amount-screen/state
 :<- [:wallet/wallet-send]
 :-> :input-amount-screen)


(comment
  (inc 1)
  (rf/sub [:wallet/wallet-send])
  (rf/sub [:send-input-amount-screen/state])
)
