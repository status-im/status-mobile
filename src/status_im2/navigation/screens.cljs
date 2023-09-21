(ns status-im2.navigation.screens
  (:require
    [status-im.ui.screens.screens :as old-screens]
    [status-im2.config :as config]
    [status-im2.contexts.add-new-contact.views :as add-new-contact]
    [status-im2.contexts.chat.camera.view :as camera-screen]
    [status-im2.contexts.chat.group-details.view :as group-details]
    [status-im2.contexts.chat.lightbox.view :as lightbox]
    [status-im2.contexts.chat.messages.view :as chat]
    [status-im2.contexts.chat.new-chat.view :as new-chat]
    [status-im2.contexts.chat.photo-selector.view :as photo-selector]
    [status-im2.contexts.communities.actions.request-to-join.view :as join-menu]
    [status-im2.contexts.communities.discover.view :as communities.discover]
    [status-im2.contexts.communities.overview.view :as communities.overview]
    [status-im2.contexts.emoji-picker.view :as emoji-picker]
    [status-im2.contexts.onboarding.create-password.view :as create-password]
    [status-im2.contexts.onboarding.create-profile.view :as create-profile]
    [status-im2.contexts.onboarding.enable-biometrics.view :as enable-biometrics]
    [status-im2.contexts.onboarding.enable-notifications.view :as enable-notifications]
    [status-im2.contexts.onboarding.enter-seed-phrase.view :as enter-seed-phrase]
    [status-im2.contexts.onboarding.generating-keys.view :as generating-keys]
    [status-im2.contexts.onboarding.identifiers.view :as identifiers]
    [status-im2.contexts.onboarding.intro.view :as intro]
    [status-im2.contexts.onboarding.new-to-status.view :as new-to-status]
    [status-im2.contexts.onboarding.sign-in.view :as sign-in]
    [status-im2.contexts.onboarding.syncing.progress.view :as syncing-devices]
    [status-im2.contexts.onboarding.syncing.results.view :as syncing-results]
    [status-im2.contexts.onboarding.welcome.view :as welcome]
    [status-im2.contexts.profile.profiles.view :as profiles]
    [status-im2.contexts.quo-preview.main :as quo.preview]
    [status-im2.contexts.shell.activity-center.view :as activity-center]
    [status-im2.contexts.shell.jump-to.view :as shell]
    [status-im2.contexts.shell.share.view :as share]
    [status-im2.contexts.syncing.find-sync-code.view :as find-sync-code]
    [status-im2.contexts.syncing.how-to-pair.view :as how-to-pair]
    [status-im2.contexts.syncing.scan-sync-code-page.view :as scan-sync-code-page]
    [status-im2.contexts.syncing.setup-syncing.view :as settings-setup-syncing]
    [status-im2.contexts.syncing.syncing-devices-list.view :as settings-syncing]
    [status-im2.contexts.wallet.account.view :as wallet-accounts]
    [status-im2.contexts.wallet.collectible.view :as wallet-collectibles]
    [status-im2.contexts.wallet.create-account.view :as wallet-create-account]
    [status-im2.contexts.wallet.saved-address.view :as wallet-saved-address]
    [status-im2.contexts.wallet.saved-addresses.view :as wallet-saved-addresses]
    [status-im2.contexts.wallet.send.select-address.view :as wallet-select-address]
    [status-im2.navigation.options :as options]
    [status-im2.navigation.transitions :as transitions]))

(defn screens
  []
  (concat
   (old-screens/screens)

   [{:name      :activity-center
     :options   options/transparent-screen-options
     :component activity-center/view}

    {:name      :share-shell
     :options   options/transparent-screen-options
     :component share/view}

    {:name      :shell-stack
     :component shell/shell-stack}

    {:name      :chat
     :options   {:insets     {:top? true}
                 :popGesture false}
     :component chat/chat}

    {:name      :start-a-new-chat
     :options   {:sheet? true}
     :component new-chat/view}

    {:name      :group-add-manage-members
     :options   {:sheet? true}
     :component group-details/add-manage-members}

    {:name      :community-requests-to-join
     :options   {:sheet? true}
     :component join-menu/request-to-join}

    {:name      :lightbox
     :options   options/lightbox
     :component lightbox/lightbox}

    {:name      :photo-selector
     :options   {:sheet? true}
     :component photo-selector/photo-selector}

    {:name      :camera-screen
     :options   options/camera-screen
     :component camera-screen/camera-screen}

    {:name      :new-contact
     :options   {:sheet? true}
     :component add-new-contact/new-contact}

    {:name      :how-to-pair
     :options   (assoc options/dark-screen :sheet? true)
     :component how-to-pair/view}

    {:name      :find-sync-code
     :options   (assoc options/dark-screen :sheet? true)
     :component find-sync-code/view}

    {:name      :discover-communities
     :component communities.discover/view}

    {:name      :community-overview
     :component communities.overview/overview}

    {:name      :settings-syncing
     :options   (merge options/dark-screen {:insets {:top? true}})
     :component settings-syncing/view}

    {:name      :settings-setup-syncing
     :options   (merge options/dark-screen {:insets {:top? true}})
     :component settings-setup-syncing/view}

    ;; Onboarding
    {:name      :intro
     :options   {:theme :dark}
     :on-focus  [:onboarding/overlay-dismiss]
     :component intro/view}

    {:name      :profiles
     :options   {:theme  :dark
                 :layout options/onboarding-layout}
     :on-focus  [:onboarding/overlay-dismiss]
     :component profiles/view}

    {:name      :new-to-status
     :options   {:theme                  :dark
                 :layout                 options/onboarding-transparent-layout
                 :animations             (merge
                                          transitions/new-to-status-modal-animations
                                          transitions/push-animations-for-transparent-background)
                 :popGesture             false
                 :modalPresentationStyle :overCurrentContext}
     :component new-to-status/new-to-status}

    {:name      :create-profile
     :options   {:theme      :dark
                 :layout     options/onboarding-transparent-layout
                 :animations transitions/push-animations-for-transparent-background
                 :popGesture false}
     :component create-profile/create-profile}

    {:name      :create-profile-password
     :options   {:theme      :dark
                 :insets     {:top false}
                 :layout     options/onboarding-transparent-layout
                 :animations transitions/push-animations-for-transparent-background
                 :popGesture false}
     :component create-password/create-password}

    {:name      :enable-biometrics
     :options   {:theme      :dark
                 :layout     options/onboarding-transparent-layout
                 :animations transitions/push-animations-for-transparent-background
                 :popGesture false}
     :component enable-biometrics/enable-biometrics}

    {:name      :generating-keys
     :options   {:theme              :dark
                 :layout             options/onboarding-transparent-layout
                 :animations         transitions/push-animations-for-transparent-background
                 :popGesture         false
                 :hardwareBackButton {:dismissModalOnPress false
                                      :popStackOnPress     false}}
     :component generating-keys/generating-keys}

    {:name      :enter-seed-phrase
     :options   {:theme      :dark
                 :layout     options/onboarding-transparent-layout
                 :animations transitions/push-animations-for-transparent-background
                 :popGesture false}
     :component enter-seed-phrase/enter-seed-phrase}

    {:name      :enable-notifications
     :options   {:theme                  :dark
                 :layout                 options/onboarding-transparent-layout
                 :animations             (merge transitions/new-to-status-modal-animations
                                                transitions/push-animations-for-transparent-background)
                 :modalPresentationStyle :overCurrentContext}
     :component enable-notifications/enable-notifications}

    {:name      :identifiers
     :component identifiers/view
     :options   {:theme              :dark
                 :layout             options/onboarding-transparent-layout
                 :animations         transitions/push-animations-for-transparent-background
                 :popGesture         false
                 :hardwareBackButton {:dismissModalOnPress false
                                      :popStackOnPress     false}}}

    {:name      :scan-sync-code-page
     :options   options/dark-screen
     :component scan-sync-code-page/view}

    {:name      :sign-in-intro
     :options   {:layout                 options/onboarding-transparent-layout
                 :animations             (merge
                                          transitions/sign-in-modal-animations
                                          transitions/push-animations-for-transparent-background)
                 :modalPresentationStyle :overCurrentContext}
     :component sign-in/animated-view}

    {:name      :sign-in
     :options   {:theme                  :dark
                 :modalPresentationStyle :overCurrentContext
                 :layout                 options/onboarding-layout}
     :component sign-in/view}

    {:name      :syncing-progress
     :options   {:theme      :dark
                 :layout     options/onboarding-layout
                 :popGesture false}
     :component syncing-devices/view}

    {:name      :syncing-progress-intro
     :options   {:theme      :dark
                 :layout     options/onboarding-transparent-layout
                 :animations transitions/push-animations-for-transparent-background
                 :popGesture false}
     :component syncing-devices/view-onboarding}

    {:name      :syncing-results
     :options   {:theme :dark}
     :component syncing-results/view}

    {:name      :welcome
     :options   {:theme      :dark
                 :layout     options/onboarding-transparent-layout
                 :animations transitions/push-animations-for-transparent-background}
     :component welcome/view}

    {:name      :emoji-picker
     :options   {:sheet? true}
     :component emoji-picker/view}

    {:name      :wallet-accounts
     :component wallet-accounts/view}

    {:name      :wallet-collectibles
     :component wallet-collectibles/view}

    {:name      :wallet-create-account
     :component wallet-create-account/view}

    {:name      :wallet-saved-address
     :component wallet-saved-address/view}

    {:name      :wallet-saved-addresses
     :component wallet-saved-addresses/view}

    {:name      :wallet-select-address
     :options   {:modalPresentationStyle :overCurrentContext}
     :component wallet-select-address/view}]

   (when config/quo-preview-enabled?
     quo.preview/screens)

   (when config/quo-preview-enabled?
     quo.preview/main-screens)))
