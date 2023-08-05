(ns status-im2.contexts.profile.rpc
  (:require [clojure.string :as string]))

(defn rpc->profiles-overview
  [{:keys [customizationColor keycard-pairing] :as profile}]
  (-> profile
      (dissoc :customizationColor)
      (assoc :customization-color (keyword customizationColor))
      (assoc :keycard-pairing (when-not (string/blank? keycard-pairing) keycard-pairing))))
