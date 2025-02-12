(ns status-im.contexts.profile.rpc
  (:require
    clojure.set
    [clojure.string :as string]
    [utils.ens.core :as utils.ens]))

(defn rpc->wakuv2-config
  [wakuv2-config]
  (clojure.set/rename-keys wakuv2-config {:LightClient :light-client}))

(defn rpc->profiles-overview
  [{:keys [customizationColor keycard-pairing] :as profile}]
  (if (map? profile)
    (-> profile
        (update :wakuv2-config rpc->wakuv2-config)
        (dissoc :customizationColor)
        (assoc :customization-color (keyword customizationColor))
        (assoc :ens-name? (utils.ens/is-valid-eth-name? (:name profile)))
        (assoc :keycard-pairing (when-not (string/blank? keycard-pairing) keycard-pairing)))
    profile))
