(ns status-im.contexts.profile.effects
  (:require
    [native-module.core :as native-module]
    [promesa.core :as promesa]
    [taoensso.timbre :as log]
    [utils.re-frame :as rf]))

(rf/reg-fx :effects.profile/accept-terms
 (fn [{:keys [on-success]}]
   (-> (native-module/accept-terms)
       (promesa/then (fn []
                       (rf/call-continuation on-success)))
       (promesa/catch (fn [error]
                        (log/error "Failed to accept terms" {:error error}))))))
