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

(spec/def :account/path string?)
(spec/def :account/color string?)
(spec/def :account/name string?)
(spec/def :account/storage keyword?)
(spec/def :account/type keyword?)
(spec/def :account/wallet boolean?)
(spec/def :account/chat boolean?)
(spec/def :account/public-key :global/public-key)
(spec/def :account/address :global/address)

(spec/def :multiaccount/account
  (spec/keys :req-un [:account/address :account/color :account/name]
             :opt-un [:account/public-key :account/path
                      :account/storage :account/type
                      :account/wallet :account/chat]))
(spec/def :multiaccount/accounts (spec/coll-of :multiaccount/account :kind vector?))

(spec/def :multiaccount/address :global/address)
(spec/def :multiaccount/key-uid string?)
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
(spec/def :multiaccount/hide-home-tooltip? (spec/nilable boolean?))
(spec/def :multiaccount/desktop-alpha-release-warning-shown? (spec/nilable boolean?))
(spec/def :multiaccount/keycard-instance-uid (spec/nilable string?))
(spec/def :multiaccount/key-uid (spec/nilable string?))
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
                                                         :multiaccount/hide-home-tooltip?
                                                         :multiaccount/bootnodes
                                                         :multiaccount/desktop-alpha-release-warning-shown?
                                                         :multiaccount/keycard-instance-uid
                                                         :multiaccount/key-uid
                                                         :multiaccount/keycard-pairing
                                                         :multiaccount/keycard-paired-on
                                                         :multiaccount/root-address
                                                         :multiaccount/accounts]))

;;used during recovering multiaccount
(spec/def :multiaccounts/recover (spec/nilable map?))
;;used during logging
(spec/def :multiaccounts/login (spec/nilable map?))
;;before login
(spec/def :multiaccounts/multiaccount (spec/keys :req-un [:multiaccount/name :multiaccount/key-uid]
                                                 :opt-un [:multiaccount/timestamp]))
(spec/def :multiaccounts/multiaccounts (spec/nilable (spec/map-of :multiaccount/key-uid :multiaccounts/multiaccount)))
