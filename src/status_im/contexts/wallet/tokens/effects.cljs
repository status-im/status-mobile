(ns status-im.contexts.wallet.tokens.effects
  (:require [promesa.core :as promesa]
            [status-im.contexts.wallet.tokens.rpc :as rpc]
            [utils.re-frame :as rf]))

(rf/reg-fx
 :effects.wallet.tokens/fetch-market-values
 (fn [{:keys [symbols currency on-success on-error]}]
   (-> (rpc/fetch-market-values symbols currency)
       (promesa/then (partial rf/call-continuation on-success))
       (promesa/catch (partial rf/call-continuation on-error)))))

(rf/reg-fx
 :effects.wallet.tokens/fetch-details
 (fn [{:keys [symbols on-success on-error]}]
   (-> (rpc/fetch-details symbols)
       (promesa/then (partial rf/call-continuation on-success))
       (promesa/catch (partial rf/call-continuation on-error)))))

(rf/reg-fx
 :effects.wallet.tokens/fetch-prices
 (fn [{:keys [symbols currencies on-success on-error]}]
   (-> (rpc/fetch-prices symbols currencies)
       (promesa/then (partial rf/call-continuation on-success))
       (promesa/catch (partial rf/call-continuation on-error)))))
