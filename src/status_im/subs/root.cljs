(ns status-im.subs.root
  (:require
    [re-frame.core :as re-frame]
    status-im.subs.bootnodes
    status-im.subs.browser
    status-im.subs.ens
    status-im.subs.keycard
    status-im.subs.mailservers
    status-im.subs.networks
    status-im.subs.stickers
    status-im.subs.wallet.search
    status-im.subs.wallet.signing
    status-im.subs.wallet.transactions
    status-im.subs.wallet.wallet))

(defn reg-root-key-sub
  [sub-name db-key]
  (re-frame/reg-sub sub-name (fn [db] (get db db-key))))

;;general
(reg-root-key-sub :visibility-status-popover/popover :visibility-status-popover/popover)
(reg-root-key-sub :visibility-status-updates :visibility-status-updates)
(reg-root-key-sub :fleets/custom-fleets :custom-fleets)
(reg-root-key-sub :ui/search :ui/search)
(reg-root-key-sub :network/type :network/type)
(reg-root-key-sub :network-status :network-status)
(reg-root-key-sub :peers-count :peers-count)
(reg-root-key-sub :peers-summary :peers-summary)
(reg-root-key-sub :web3-node-version :web3-node-version)

;;keycard
(reg-root-key-sub :keycard :keycard)
(reg-root-key-sub :keycard/banner-hidden :keycard/banner-hidden)

;;bottom sheet old
(reg-root-key-sub :bottom-sheet/show? :bottom-sheet/show?)
(reg-root-key-sub :bottom-sheet/view :bottom-sheet/view)
(reg-root-key-sub :bottom-sheet/options :bottom-sheet/options)

;;browser
(reg-root-key-sub :browsers :browser/browsers)
(reg-root-key-sub :browser/options :browser/options)
(reg-root-key-sub :dapps/permissions :dapps/permissions)
(reg-root-key-sub :bookmarks :bookmarks/bookmarks)
(reg-root-key-sub :browser/screen-id :browser/screen-id)

;;stickers
(reg-root-key-sub :stickers/selected-pack :stickers/selected-pack)
(reg-root-key-sub :stickers/packs :stickers/packs)
(reg-root-key-sub :stickers/recent-stickers :stickers/recent-stickers)

;;mailserver
(reg-root-key-sub :mailserver/current-id :mailserver/current-id)
(reg-root-key-sub :mailserver/mailservers :mailserver/mailservers)
(reg-root-key-sub :mailserver.edit/mailserver :mailserver.edit/mailserver)
(reg-root-key-sub :mailserver/state :mailserver/state)
(reg-root-key-sub :mailserver/pending-requests :mailserver/pending-requests)
(reg-root-key-sub :mailserver/request-error? :mailserver/request-error)
(reg-root-key-sub :mailserver/fetching-gaps-in-progress :mailserver/fetching-gaps-in-progress)

;;contacts
(reg-root-key-sub :contacts/contacts-raw :contacts/contacts)
(reg-root-key-sub :contacts/current-contact-identity :contacts/identity)
(reg-root-key-sub :contacts/current-contact-ens-name :contacts/ens-name)
(reg-root-key-sub :contacts/new-identity :contacts/new-identity)
(reg-root-key-sub :group/selected-contacts :group/selected-contacts)
(reg-root-key-sub :contacts/search-query :contacts/search-query)

;;wallet
(reg-root-key-sub :wallet :wallet)
(reg-root-key-sub :prices :prices)
(reg-root-key-sub :prices-loading? :prices-loading?)
(reg-root-key-sub :wallet.transactions :wallet.transactions)
(reg-root-key-sub :wallet/custom-token-screen :wallet/custom-token-screen)
(reg-root-key-sub :wallet/prepare-transaction :wallet/prepare-transaction)
(reg-root-key-sub :wallet-service/manual-setting :wallet-service/manual-setting)
(reg-root-key-sub :wallet/recipient :wallet/recipient)
(reg-root-key-sub :wallet/favourites :wallet/favourites)
(reg-root-key-sub :wallet/refreshing-history? :wallet/refreshing-history?)
(reg-root-key-sub :wallet/fetching-error :wallet/fetching-error)
(reg-root-key-sub :wallet/non-archival-node :wallet/non-archival-node)
(reg-root-key-sub :wallet/current-base-fee :wallet/current-base-fee)
(reg-root-key-sub :wallet/slow-base-fee :wallet/slow-base-fee)
(reg-root-key-sub :wallet/normal-base-fee :wallet/normal-base-fee)
(reg-root-key-sub :wallet/fast-base-fee :wallet/fast-base-fee)
(reg-root-key-sub :wallet/current-priority-fee :wallet/current-priority-fee)
(reg-root-key-sub :wallet/transactions-management-enabled? :wallet/transactions-management-enabled?)
(reg-root-key-sub :wallet/all-tokens :wallet/all-tokens)
(reg-root-key-sub :wallet/collectible-collections :wallet/collectible-collections)
(reg-root-key-sub :wallet/fetching-collection-assets :wallet/fetching-collection-assets)
(reg-root-key-sub :wallet/collectible-assets :wallet/collectible-assets)
(reg-root-key-sub :wallet/selected-collectible :wallet/selected-collectible)
(reg-root-key-sub :wallet/modal-selecting-source-token? :wallet/modal-selecting-source-token?)
(reg-root-key-sub :wallet/swap-from-token :wallet/swap-from-token)
(reg-root-key-sub :wallet/swap-to-token :wallet/swap-to-token)
(reg-root-key-sub :wallet/swap-from-token-amount :wallet/swap-from-token-amount)
(reg-root-key-sub :wallet/swap-to-token-amount :wallet/swap-to-token-amount)
(reg-root-key-sub :wallet/swap-advanced-mode? :wallet/swap-advanced-mode?)
(reg-root-key-sub :add-account :add-account)
(reg-root-key-sub :buy-crypto/on-ramps :buy-crypto/on-ramps)

;;; Link previews
(reg-root-key-sub :link-previews-whitelist :link-previews-whitelist)
(reg-root-key-sub :chat/link-previews :chat/link-previews)

;;commands
(reg-root-key-sub :commands/select-account :commands/select-account)

;;ethereum
(reg-root-key-sub :ethereum/current-block :ethereum/current-block)

;;ens
(reg-root-key-sub :ens/registration :ens/registration)
(reg-root-key-sub :ens/registrations :ens/registrations)
(reg-root-key-sub :ens/names :ens/names)

;;signing
(reg-root-key-sub :signing/sign :signing/sign)
(reg-root-key-sub :signing/tx :signing/tx)
(reg-root-key-sub :signing/edit-fee :signing/edit-fee)

;; wallet connect
(reg-root-key-sub :wallet-connect/proposal-metadata :wallet-connect/proposal-metadata)
(reg-root-key-sub :wallet-connect/enabled? :wallet-connect/enabled?)
(reg-root-key-sub :wallet-connect/session-connected :wallet-connect/session-connected)
(reg-root-key-sub :wallet-connect/showing-app-management-sheet?
                  :wallet-connect/showing-app-management-sheet?)
(reg-root-key-sub :wallet-connect/sessions :wallet-connect/sessions)
(reg-root-key-sub :wallet-connect/session-managed :wallet-connect/session-managed)
(reg-root-key-sub :contact-requests/pending :contact-requests/pending)

(reg-root-key-sub :bug-report/description-error :bug-report/description-error)
(reg-root-key-sub :bug-report/details :bug-report/details)

(reg-root-key-sub :backup/performing-backup :backup/performing-backup)
