(ns status-im.contexts.wallet.tokens.effects
  (:require [promesa.core :as promesa]
            [re-frame.core :as rf]
            [status-im.contexts.wallet.tokens.rpc :as rpc]))

(rf/reg-fx
 :effects.wallet.tokens/fetch-market-values
 (fn [{:keys [symbols currency on-success on-error]}]
   (-> (rpc/fetch-market-values symbols currency)
       (promesa/then on-success)
       (promesa/catch on-error))))

(rf/reg-fx
 :effects.wallet.tokens/fetch-details
 (fn [{:keys [symbols on-success on-error]}]
   (-> (rpc/fetch-details symbols)
       (promesa/then on-success)
       (promesa/catch on-error))))

(rf/reg-fx
 :effects.wallet.tokens/fetch-prices
 (fn [{:keys [symbols currencies on-success on-error]}]
   (-> (rpc/fetch-prices symbols currencies)
       (promesa/then on-success)
       (promesa/catch on-error))))
