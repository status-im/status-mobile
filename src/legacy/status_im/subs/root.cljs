(ns legacy.status-im.subs.root
  (:require
    legacy.status-im.subs.bootnodes
    legacy.status-im.subs.browser
    legacy.status-im.subs.ens
    legacy.status-im.subs.keycard
    legacy.status-im.subs.mailservers
    legacy.status-im.subs.networks
    legacy.status-im.subs.stickers
    legacy.status-im.subs.wallet.search
    legacy.status-im.subs.wallet.signing
    legacy.status-im.subs.wallet.transactions
    legacy.status-im.subs.wallet.wallet
    [re-frame.core :as re-frame]))

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
(reg-root-key-sub :prices :prices)
(reg-root-key-sub :prices-loading? :prices-loading?)
(reg-root-key-sub :add-account :add-account)
(reg-root-key-sub :buy-crypto/on-ramps :buy-crypto/on-ramps)

(reg-root-key-sub :wallet-legacy :wallet-legacy)
(reg-root-key-sub :wallet-legacy.transactions :wallet-legacy.transactions)
(reg-root-key-sub :wallet-legacy/custom-token-screen :wallet-legacy/custom-token-screen)
(reg-root-key-sub :wallet-legacy/prepare-transaction :wallet-legacy/prepare-transaction)
(reg-root-key-sub :wallet-legacy/recipient :wallet-legacy/recipient)
(reg-root-key-sub :wallet-legacy/favourites :wallet-legacy/favourites)
(reg-root-key-sub :wallet-legacy/refreshing-history? :wallet-legacy/refreshing-history?)
(reg-root-key-sub :wallet-legacy/fetching-error :wallet-legacy/fetching-error)
(reg-root-key-sub :wallet-legacy/non-archival-node :wallet-legacy/non-archival-node)
(reg-root-key-sub :wallet-legacy/current-base-fee :wallet-legacy/current-base-fee)
(reg-root-key-sub :wallet-legacy/slow-base-fee :wallet-legacy/slow-base-fee)
(reg-root-key-sub :wallet-legacy/normal-base-fee :wallet-legacy/normal-base-fee)
(reg-root-key-sub :wallet-legacy/fast-base-fee :wallet-legacy/fast-base-fee)
(reg-root-key-sub :wallet-legacy/current-priority-fee :wallet-legacy/current-priority-fee)
(reg-root-key-sub :wallet-legacy/transactions-management-enabled?
                  :wallet-legacy/transactions-management-enabled?)
(reg-root-key-sub :wallet-legacy/all-tokens :wallet-legacy/all-tokens)
(reg-root-key-sub :wallet-legacy/collectible-collections :wallet-legacy/collectible-collections)
(reg-root-key-sub :wallet-legacy/fetching-collection-assets :wallet-legacy/fetching-collection-assets)
(reg-root-key-sub :wallet-legacy/collectible-assets :wallet-legacy/collectible-assets)
(reg-root-key-sub :wallet-legacy/selected-collectible :wallet-legacy/selected-collectible)
(reg-root-key-sub :wallet-legacy/modal-selecting-source-token?
                  :wallet-legacy/modal-selecting-source-token?)
(reg-root-key-sub :wallet-legacy/swap-from-token :wallet-legacy/swap-from-token)
(reg-root-key-sub :wallet-legacy/swap-to-token :wallet-legacy/swap-to-token)
(reg-root-key-sub :wallet-legacy/swap-from-token-amount :wallet-legacy/swap-from-token-amount)
(reg-root-key-sub :wallet-legacy/swap-to-token-amount :wallet-legacy/swap-to-token-amount)
(reg-root-key-sub :wallet-legacy/swap-advanced-mode? :wallet-legacy/swap-advanced-mode?)

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

(reg-root-key-sub :contact-requests/pending :contact-requests/pending)

(reg-root-key-sub :bug-report/description-error :bug-report/description-error)
(reg-root-key-sub :bug-report/details :bug-report/details)

(reg-root-key-sub :backup/performing-backup :backup/performing-backup)
