(ns status-im.feature-flags
  (:require
    [clojure.string :as string]
    [react-native.config :as config]
    [reagent.core :as reagent]))

(defn- enabled-in-env?
  [k]
  (= "1" (config/get-config k)))

(defonce ^:private feature-flags-config
  (reagent/atom
   {::wallet.bridge-token                (enabled-in-env? :FLAG_BRIDGE_TOKEN_ENABLED)
    ::wallet.edit-derivation-path        (enabled-in-env? :FLAG_EDIT_DERIVATION_PATH)
    ::wallet.remove-account              (enabled-in-env? :FLAG_REMOVE_ACCOUNT_ENABLED)
    ::wallet.long-press-watch-only-asset (enabled-in-env? :FLAG_LONG_PRESS_WATCH_ONLY_ASSET_ENABLED)
    ::wallet.assets-modal-manage-tokens  (enabled-in-env? :FLAG_ASSETS_MODAL_MANAGE_TOKENS)
    ::wallet.assets-modal-hide           (enabled-in-env? :FLAG_ASSETS_MODAL_HIDE)
    ::community.edit-account-selection   (enabled-in-env? :FLAG_EDIT_ACCOUNT_SELECTION_ENABLED)}))

(defn feature-flags [] @feature-flags-config)

(def feature-flags-categories
  (set (map
        (fn [k]
          (first (string/split (str (name k)) ".")))
        (keys @feature-flags-config))))

(defn enabled?
  [flag]
  (get (feature-flags) flag))

(defn toggle
  [flag]
  (swap! feature-flags-config update flag not))

(defn alert
  [flag action]
  (if (enabled? flag)
    (action)
    (js/alert (str flag " is currently feature flagged off"))))
