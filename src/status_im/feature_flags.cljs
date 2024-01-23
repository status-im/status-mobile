(ns status-im.feature-flags
  (:require
    [react-native.config :as config]
    [reagent.core :as reagent]))

(defn- enabled? [v] (= "1" v))

(defn check-env [k] (enabled? (config/get-config k)))

(def ^:private feature-flags-config
  (reagent/atom
   {:wallet
    {:edit-default-keypair (check-env :DEV_FF_EDIT_DEFAULT_KEYPAIR)
     :bridge-token         (check-env :DEV_FF_BRIDGE_TOKEN)}}))

(defn feature-flags [] @feature-flags-config)

(defn get-flag
  [section flag]
  (get-in (feature-flags) [section flag]))

(defn update-flag
  [section flag]
  (swap! feature-flags-config
    (fn [a]
      (update-in a [section flag] not))))

(defn alert
  [context flag action]
  (if (get-flag context flag)
    (action)
    (js/alert (str flag " is currently feature flagged off"))))
