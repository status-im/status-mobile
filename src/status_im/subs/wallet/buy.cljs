(ns status-im.subs.wallet.buy
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 :wallet/crypto-on-ramps
 :<- [:wallet]
 :-> :crypto-on-ramps)

(rf/reg-sub
 :wallet/wallet-buy-crypto
 :<- [:wallet/ui]
 :-> :buy-crypto)

(rf/reg-sub
 :wallet/wallet-buy-crypto-account
 :<- [:wallet/wallet-buy-crypto]
 :-> :account)

(rf/reg-sub
 :wallet/wallet-buy-crypto-provider
 :<- [:wallet/wallet-buy-crypto]
 :-> :provider)

(rf/reg-sub
 :wallet/wallet-buy-crypto-recurrent?
 :<- [:wallet/wallet-buy-crypto]
 :-> :recurrent?)

(rf/reg-sub
 :wallet/wallet-buy-crypto-network
 :<- [:wallet/wallet-buy-crypto]
 :-> :network)
