(ns status-im2.contexts.add-new-contact.effects
  (:require
    [legacy.status-im.ethereum.ens :as ens]
    [native-module.core :as native-module]
    [status-im2.constants :as constants]
    [utils.re-frame :as rf]
    [utils.transforms :as transforms]))

(rf/reg-fx :effects.contacts/decompress-public-key
 (fn [{:keys [compressed-key on-success on-error]}]
   (native-module/compressed-key->public-key
    compressed-key
    constants/deserialization-key
    (fn [resp]
      (let [{:keys [error]} (transforms/json->clj resp)]
        (if error
          (on-error error)
          (on-success (str "0x" (subs resp 5)))))))))

(rf/reg-fx :effects.contacts/resolve-public-key-from-ens
 (fn [{:keys [chain-id ens on-success on-error]}]
   (ens/pubkey chain-id ens on-success on-error)))
