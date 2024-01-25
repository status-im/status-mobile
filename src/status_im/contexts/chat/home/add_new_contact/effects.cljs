(ns status-im.contexts.chat.home.add-new-contact.effects
  (:require
    [legacy.status-im.ethereum.ens :as ens]
    [utils.re-frame :as rf]))

(rf/reg-fx :effects.contacts/resolve-public-key-from-ens
 (fn [{:keys [chain-id ens on-success on-error]}]
   (ens/pubkey chain-id ens on-success on-error)))
