(ns status-im.feature-flags
  (:require
    [clojure.string :as string]
    [react-native.async-storage :as async-storage]
    [react-native.config :as config]
    [reagent.core :as reagent]))

(defn- enabled-in-env?
  [k]
  (= "1" (config/get-config k)))

(def ^:private initial-flags
  {::community.edit-account-selection   (enabled-in-env? :FLAG_EDIT_ACCOUNT_SELECTION_ENABLED)

   ;; Feature toggled (off by default) because the desktop app disabled this
   ;; feature and we want both clients in sync. We keep the code because it
   ;; works and we may re-enable it by default.
   ::profile-pictures-visibility        (enabled-in-env? :FLAG_PROFILE_PICTURES_VISIBILITY_ENABLED)
   ::settings.import-all-keypairs       (enabled-in-env?
                                         :FLAG_WALLET_SETTINGS_IMPORT_ALL_KEYPAIRS)
   ::wallet.add-watched-address         (enabled-in-env? :FLAG_ADD_WATCHED_ADDRESS)
   ::wallet.advanced-sending            (enabled-in-env? :FLAG_ADVANCED_SENDING)
   ::wallet.assets-modal-hide           (enabled-in-env? :FLAG_ASSETS_MODAL_HIDE)
   ::wallet.assets-modal-manage-tokens  (enabled-in-env? :FLAG_ASSETS_MODAL_MANAGE_TOKENS)
   ::wallet.bridge-token                (enabled-in-env? :FLAG_BRIDGE_TOKEN_ENABLED)
   ::wallet.contacts                    (enabled-in-env? :FLAG_CONTACTS_ENABLED)
   ::wallet.edit-derivation-path        (enabled-in-env? :FLAG_EDIT_DERIVATION_PATH)
   ::wallet.graph                       (enabled-in-env? :FLAG_GRAPH_ENABLED)
   ::wallet.import-private-key          (enabled-in-env? :FLAG_IMPORT_PRIVATE_KEY_ENABLED)
   ::wallet.long-press-watch-only-asset (enabled-in-env? :FLAG_LONG_PRESS_WATCH_ONLY_ASSET_ENABLED)
   ::wallet.saved-addresses             (enabled-in-env? :WALLET_SAVED_ADDRESSES)
   ::wallet.wallet-connect              (enabled-in-env? :FLAG_WALLET_CONNECT_ENABLED)
   ::wallet.custom-network-amounts      (enabled-in-env? :FLAG_WALLET_CUSTOM_NETWORK_AMOUNTS_ENABLED)
   ::wallet.transaction-params          (enabled-in-env? :FLAG_WALLET_TRANSACTION_PARAMS_ENABLED)})

(defonce ^:private feature-flags-config
  (reagent/atom initial-flags))

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
  (let [new-flags (update @feature-flags-config flag not)]
    (async-storage/set-item!
     :feature-flags
     new-flags
     (fn []
       (reset! feature-flags-config new-flags)))))

(defn load-flags
  []
  (async-storage/get-item
   :feature-flags
   (fn [flags]
     (when flags
       (reset! feature-flags-config flags)))))

(defn reset-flags
  []
  (async-storage/set-item!
   :feature-flags
   initial-flags
   (fn []
     (reset! feature-flags-config initial-flags))))

(defn alert
  [flag action]
  (if (enabled? flag)
    (action)
    (js/alert (str flag " is currently feature flagged off"))))
