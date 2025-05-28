(ns status-im.effects
  (:require [promesa.core :as promesa]
            [utils.re-frame :as rf]))

(rf/reg-fx :fx.promise
 (fn [{:keys [promise args on-success on-error]
       :or   {args       []
              on-success identity
              on-error   identity}}]
   (-> (apply promise args)
       (promesa/then (partial rf/call-continuation on-success))
       (promesa/catch (partial rf/call-continuation on-error)))))
