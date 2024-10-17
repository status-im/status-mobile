(ns status-im.events
  (:require
    status-im.common.alert-banner.events
    status-im.common.alert.effects
    status-im.common.async-storage.effects
    status-im.common.emoji-picker.events
    status-im.common.font.events
    status-im.common.image-crop-picker.events
    [status-im.common.json-rpc.events]
    status-im.common.log
    status-im.common.password-authentication.events
    status-im.common.peer-stats.events
    status-im.common.shared-urls.events
    status-im.common.signals.events
    status-im.common.theme.effects
    status-im.common.theme.events
    [status-im.common.toasts.events]
    status-im.common.universal-links
    status-im.contexts.chat.contacts.events
    status-im.contexts.chat.events
    [status-im.contexts.chat.home.add-new-contact.events]
    status-im.contexts.chat.messenger.composer.events
    status-im.contexts.chat.messenger.messages.link-preview.events
    status-im.contexts.chat.messenger.photo-selector.events
    status-im.contexts.communities.events
    status-im.contexts.communities.overview.events
    status-im.contexts.communities.sharing.events
    status-im.contexts.contact.blocking.events
    status-im.contexts.keycard.effects
    status-im.contexts.keycard.events
    status-im.contexts.network.effects
    status-im.contexts.network.events
    status-im.contexts.onboarding.common.overlay.events
    status-im.contexts.onboarding.events
    status-im.contexts.profile.events
    status-im.contexts.profile.settings.events
    status-im.contexts.settings.language-and-currency.events
    status-im.contexts.settings.wallet.saved-addresses.events
    status-im.contexts.shell.qr-reader.events
    status-im.contexts.shell.share.events
    status-im.contexts.syncing.events
    status-im.contexts.wallet.add-account.add-address-to-watch.events
    status-im.contexts.wallet.add-account.create-account.events
    status-im.contexts.wallet.buy-crypto.events
    status-im.contexts.wallet.collectible.events
    status-im.contexts.wallet.common.wizard.events
    status-im.contexts.wallet.effects
    status-im.contexts.wallet.events
    status-im.contexts.wallet.send.events
    status-im.contexts.wallet.signals
    status-im.contexts.wallet.swap.events
    status-im.contexts.wallet.wallet-connect.events.core
    [status-im.db :as db]
    status-im.navigation.effects
    status-im.navigation.events
    [utils.re-frame :as rf]))

(rf/defn start-app
  {:events [:app-started]}
  [cofx]
  (rf/merge
   cofx
   {:db db/app-db
    :theme/init-theme nil
    :effects.network/listen-to-network-info nil
    :effects.biometric/get-supported-type nil
    :effects.keycard/register-card-events nil
    :effects.keycard/check-nfc-enabled nil
    :effects.keycard/retrieve-pairings nil
    ;;app starting flow continues in get-profiles-overview
    :profile/get-profiles-overview #(rf/dispatch [:profile/get-profiles-overview-success %])
    :effects.font/get-font-file-for-initials-avatar
    #(rf/dispatch [:font/init-font-file-for-initials-avatar %])}))
