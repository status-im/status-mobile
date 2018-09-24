(ns status-im.accounts.db
  (:require status-im.utils.db
            status-im.ui.screens.network-settings.db
            status-im.ui.screens.bootnodes-settings.db
            [cljs.spec.alpha :as spec]
            [status-im.constants :as const])
  (:require-macros [status-im.utils.db :refer [allowed-keys]]))

(defn logged-in? [cofx]
  (boolean
   (get-in cofx [:db :account/account])))

(defn credentials [cofx]
  (select-keys (get-in cofx [:db :accounts/login]) [:address :password :save-password?]))

(defn valid-length? [password]
  (>= (count password) const/min-password-length))

(spec/def ::password  (spec/and :global/not-empty-string valid-length?))

(spec/def :account/address :global/address)
(spec/def :account/name :global/not-empty-string)
(spec/def :account/public-key :global/public-key)
;;not used
(spec/def :account/email nil?)
(spec/def :account/signed-up? (spec/nilable boolean?))
(spec/def :account/last-updated (spec/nilable int?))
(spec/def :account/last-sign-in (spec/nilable int?))
(spec/def :account/last-request (spec/nilable int?))
(spec/def :account/photo-path (spec/nilable string?))
(spec/def :account/debug? (spec/nilable boolean?))
(spec/def :account/status (spec/nilable string?))
(spec/def :account/network (spec/nilable string?))
(spec/def :account/networks (spec/nilable :networks/networks))
(spec/def :account/bootnodes (spec/nilable :bootnodes/bootnodes))
(spec/def :account/wnode (spec/nilable string?))
(spec/def :account/settings (spec/nilable (spec/map-of keyword? any?)))
(spec/def :account/signing-phrase :global/not-empty-string)
(spec/def :account/mnemonic (spec/nilable string?))
(spec/def :account/sharing-usage-data? (spec/nilable boolean?))
(spec/def :account/dev-mode? (spec/nilable boolean?))
(spec/def :account/seed-backed-up? (spec/nilable boolean?))
(spec/def :account/installation-id :global/not-empty-string)
(spec/def :account/wallet-set-up-passed? (spec/nilable boolean?))
(spec/def :account/mainnet-warning-shown? (spec/nilable boolean?))

(spec/def :accounts/account (allowed-keys
                             :req-un [:account/name :account/address :account/public-key
                                      :account/photo-path :account/signing-phrase
                                      :account/installation-id]
                             :opt-un [:account/debug? :account/status :account/last-updated
                                      :account/email :account/signed-up? :account/network
                                      :account/networks :account/settings :account/wnode
                                      :account/last-sign-in :account/sharing-usage-data? :account/dev-mode?
                                      :account/seed-backed-up? :account/mnemonic
                                      :account/wallet-set-up-passed? :account/last-request
                                      :account/bootnodes :account/mainnet-warning-shown?]))

(spec/def :accounts/accounts (spec/nilable (spec/map-of :account/address :accounts/account)))

;;used during creating account
(spec/def :accounts/create (spec/nilable map?))
;;used during recovering account
(spec/def :accounts/recover (spec/nilable map?))
;;used during logging
(spec/def :accounts/login (spec/nilable map?))
;;logged in account
(spec/def :account/account (spec/nilable :accounts/account))
