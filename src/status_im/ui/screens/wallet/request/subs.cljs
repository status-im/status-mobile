(ns status-im.ui.screens.wallet.request.subs
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub :wallet.request/transaction
                  :<- [:wallet]
                  :request-transaction)
