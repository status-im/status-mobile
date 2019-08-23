(ns status-im.multiaccounts.db
  (:require status-im.utils.db
            status-im.network.module
            status-im.ui.screens.bootnodes-settings.db
            [cljs.spec.alpha :as spec]
            [status-im.constants :as const]))

(defn valid-length? [password]
  (>= (count password) const/min-password-length))

(spec/def ::password  (spec/and :global/not-empty-string valid-length?))

(spec/def :multiaccount/root-address (spec/nilable string?))
(spec/def :multiaccount/accounts (spec/nilable vector?))

(spec/def :multiaccount/address :global/address)
(spec/def :multiaccount/name :global/not-empty-string)
(spec/def :multiaccount/public-key :global/public-key)
(spec/def :multiaccount/signed-up? (spec/nilable boolean?))
(spec/def :multiaccount/last-updated (spec/nilable int?))
(spec/def :multiaccount/last-sign-in (spec/nilable int?))
(spec/def :multiaccount/timestamp (spec/nilable int?))
(spec/def :multiaccount/last-request (spec/nilable int?))
(spec/def :multiaccount/photo-path (spec/nilable string?))
(spec/def :multiaccount/debug? (spec/nilable boolean?))
(spec/def :multiaccount/chaos-mode? (spec/nilable boolean?))
(spec/def :multiaccount/bootnodes (spec/nilable :bootnodes/bootnodes))
(spec/def :multiaccount/mailserver (spec/nilable string?))
(spec/def :multiaccount/settings (spec/nilable (spec/map-of keyword? any?)))
(spec/def :multiaccount/signing-phrase :global/not-empty-string)
(spec/def :multiaccount/mnemonic (spec/nilable string?))
(spec/def :multiaccount/sharing-usage-data? (spec/nilable boolean?))
(spec/def :multiaccount/desktop-notifications? (spec/nilable boolean?))
(spec/def :multiaccount/dev-mode? (spec/nilable boolean?))
(spec/def :multiaccount/seed-backed-up? (spec/nilable boolean?))
(spec/def :multiaccount/installation-id :global/not-empty-string)
(spec/def :multiaccount/wallet-set-up-passed? (spec/nilable boolean?))
(spec/def :multiaccount/desktop-alpha-release-warning-shown? (spec/nilable boolean?))
(spec/def :multiaccount/keycard-instance-uid (spec/nilable string?))
(spec/def :multiaccount/keycard-key-uid (spec/nilable string?))
(spec/def :multiaccount/keycard-pairing (spec/nilable string?))
(spec/def :multiaccount/keycard-paired-on (spec/nilable int?))

(spec/def :multiaccount/multiaccount (spec/keys :opt-un [:multiaccount/name :multiaccount/address
                                                         :multiaccount/photo-path
                                                         :multiaccount/signing-phrase
                                                         :multiaccount/installation-id
                                                         :multiaccount/debug? :multiaccount/last-updated :multiaccount/public-key
                                                         :multiaccount/email :multiaccount/signed-up?
                                                         :multiaccount/settings :multiaccount/mailserver
                                                         :multiaccount/sharing-usage-data?
                                                         :multiaccount/seed-backed-up? :multiaccount/mnemonic :multiaccount/desktop-notifications?
                                                         :multiaccount/chaos-mode?
                                                         :multiaccount/wallet-set-up-passed? :multiaccount/last-request
                                                         :multiaccount/bootnodes
                                                         :multiaccount/desktop-alpha-release-warning-shown?
                                                         :multiaccount/keycard-instance-uid
                                                         :multiaccount/keycard-key-uid
                                                         :multiaccount/keycard-pairing
                                                         :multiaccount/keycard-paired-on
                                                         :multiaccount/root-address
                                                         :multiaccount/accounts]))

;;used during recovering multiaccount
(spec/def :multiaccounts/recover (spec/nilable map?))
;;used during logging
(spec/def :multiaccounts/login (spec/nilable map?))
;;before login
(spec/def :multiaccounts/multiaccount (spec/keys :req-un [:multiaccount/name :multiaccount/address]
                                                 :opt-un [:multiaccount/timestamp]))
(spec/def :multiaccounts/multiaccounts (spec/nilable (spec/map-of :multiaccount/address :multiaccounts/multiaccount)))
