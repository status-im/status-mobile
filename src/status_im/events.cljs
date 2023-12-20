(ns status-im.events
  (:require
    [legacy.status-im.bottom-sheet.events]
    [legacy.status-im.keycard.core :as keycard]
    status-im.common.alert.effects
    status-im.common.async-storage.effects
    status-im.common.font.events
    [status-im.common.json-rpc.events]
    status-im.common.password-authentication.events
    status-im.common.signals.events
    status-im.common.theme.events
    [status-im.common.toasts.events]
    [status-im.contexts.add-new-contact.events]
    status-im.contexts.chat.composer.events
    status-im.contexts.chat.events
    status-im.contexts.chat.photo-selector.events
    status-im.contexts.communities.events
    status-im.contexts.communities.overview.events
    status-im.contexts.emoji-picker.events
    status-im.contexts.onboarding.common.overlay.events
    status-im.contexts.onboarding.events
    status-im.contexts.profile.events
    status-im.contexts.profile.settings.events
    status-im.contexts.shell.share.events
    status-im.contexts.syncing.events
    status-im.contexts.wallet.events
    status-im.contexts.wallet.send.events
    [status-im.db :as db]
    [utils.re-frame :as rf]))

(rf/defn start-app
  {:events [:app-started]}
  [cofx]
  (rf/merge
   cofx
   {:db db/app-db
    :theme/init-theme nil
    :network/listen-to-network-info nil
    :biometric/get-supported-biometric-type nil
    ;;app starting flow continues in get-profiles-overview
    :profile/get-profiles-overview #(rf/dispatch [:profile/get-profiles-overview-success %])
    :effects.font/get-font-file-for-initials-avatar
    #(rf/dispatch [:font/init-font-file-for-initials-avatar %])}
   (keycard/init)))
