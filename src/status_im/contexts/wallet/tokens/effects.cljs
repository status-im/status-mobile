(ns status-im.contexts.wallet.tokens.effects
  (:require [promesa.core :as promesa]
            [status-im.contexts.wallet.tokens.rpc :as rpc]
            [utils.re-frame :as rf]))

(rf/reg-fx
 :effects.wallet.tokens/fetch-additional-token-data
 (fn [{:keys [symbols currency on-success on-error]}]
   (-> (rpc/fetch-additional-token-data symbols currency)
       (promesa/then (partial rf/call-continuation on-success))
       (promesa/catch (partial rf/call-continuation on-error)))))

(rf/reg-fx
 :effects.wallet.tokens/fetch-token-list
 (fn [{:keys [on-success on-error]}]
   (-> (rpc/fetch-token-list)
       (promesa/then (partial rf/call-continuation on-success))
       (promesa/catch (partial rf/call-continuation on-error)))))
