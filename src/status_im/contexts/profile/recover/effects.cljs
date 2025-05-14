(ns status-im.contexts.profile.recover.effects
  (:require
    [native-module.core :as native-module]
    [promesa.core :as promesa]
    [utils.re-frame :as rf]
    [utils.security.core :as security]
    [utils.transforms :as transforms]))

(rf/reg-fx :effects.profile/restore-and-login
 (fn [request]
   ;;"node.login" signal will be triggered as a callback
   (native-module/restore-account-and-login request)))

(defn validate-mnemonic
  [mnemonic]
  (-> mnemonic
      (security/safe-unmask-data)
      (native-module/validate-mnemonic)
      (promesa/then (fn [result]
                      (let [{:keys [keyUID]} (transforms/json->clj result)]
                        {:key-uid keyUID})))))

(rf/reg-fx :effects.profile/validate-recovery-phrase
 (fn [[mnemonic on-success on-error]]
   (-> (validate-mnemonic mnemonic)
       (promesa/then (fn [{:keys [key-uid]}]
                       (when (fn? on-success)
                         (on-success mnemonic key-uid))))
       (promesa/catch (fn [error]
                        (when (and error (fn? on-error))
                          (on-error error)))))))
