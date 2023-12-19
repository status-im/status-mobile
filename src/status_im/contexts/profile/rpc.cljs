(ns status-im.contexts.profile.rpc
  (:require
    [clojure.string :as string]
    [utils.ens.core :as utils.ens]))

(defn rpc->profiles-overview
  [{:keys [customizationColor keycard-pairing] :as profile}]
  (if (map? profile)
    (-> profile
        (dissoc :customizationColor)
        (assoc :customization-color (keyword customizationColor))
        (assoc :ens-name? (utils.ens/is-valid-eth-name? (:name profile)))
        (assoc :keycard-pairing (when-not (string/blank? keycard-pairing) keycard-pairing)))
    profile))
