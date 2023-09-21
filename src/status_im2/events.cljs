(ns status-im2.events
  (:require [status-im.bottom-sheet.events]
            [status-im.keycard.core :as keycard]
            status-im2.common.font
            [status-im2.common.json-rpc.events]
            status-im2.common.password-authentication.events
            status-im2.common.theme.events
            [status-im2.common.toasts.events]
            [status-im2.contexts.add-new-contact.events]
            status-im2.contexts.chat.events
            status-im2.contexts.chat.photo-selector.events
            status-im2.contexts.communities.overview.events
            status-im2.contexts.emoji-picker.events
            status-im2.contexts.onboarding.events
            status-im2.contexts.profile.events
            status-im2.contexts.shell.share.events
            status-im2.contexts.syncing.events
            status-im2.contexts.onboarding.common.overlay.events
            [status-im2.db :as db]
            [utils.re-frame :as rf]))

(rf/defn start-app
  {:events [:app-started]}
  [cofx]
  (rf/merge cofx
            {:db                                     db/app-db
             :theme/init-theme                       nil
             :network/listen-to-network-info         nil
             :biometric/get-supported-biometric-type nil
             ;;app starting flow continues in get-profiles-overview
             :profile/get-profiles-overview          #(rf/dispatch
                                                       [:profile/get-profiles-overview-success %])
             :font/get-font-file-for-initials-avatar #(rf/dispatch
                                                       [:font/init-font-file-for-initials-avatar %])}
            (keycard/init)))
