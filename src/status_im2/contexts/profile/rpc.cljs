(ns status-im2.contexts.profile.rpc
  (:require [clojure.string :as string]
            [status-im.ethereum.ens :as ens]))

(defn rpc->profiles-overview
  [{:keys [customizationColor keycard-pairing] :as profile}]
  (-> profile
      (dissoc :customizationColor)
      (assoc :customization-color (keyword customizationColor))
      (assoc :ens-name? (ens/is-valid-eth-name? (:name profile)))
      (assoc :keycard-pairing (when-not (string/blank? keycard-pairing) keycard-pairing))))
