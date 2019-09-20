(ns status-im.ui.screens.routing.profile-stack
  (:require [status-im.utils.config :as config]))

(def profile-stack
  {:name    :profile-stack
   :screens (cond-> [:my-profile
                     :contacts-list
                     :ens-main
                     :ens-register
                     :ens-terms
                     :ens-name-details
                     :blocked-users-list
                     :profile-photo-capture
                     :bootnodes-settings
                     :installations
                     :edit-bootnode
                     :offline-messaging-settings
                     :edit-mailserver
                     :dapps-permissions
                     :privacy-and-security
                     :language-settings
                     :notifications-settings
                     :sync-settings
                     :advanced-settings
                     :help-center
                     :about-app
                     :manage-dapps-permissions
                     :network-settings
                     :network-details
                     :edit-network
                     :log-level-settings
                     :fleet-settings
                     :mobile-network-settings
                     :backup-seed
                     :tribute-to-talk
                     :qr-scanner
                     :my-profile-ext-settings]

              config/hardwallet-enabled?
              (concat [:keycard-settings
                       :reset-card
                       :keycard-connection-lost
                       :enter-pin-settings]))
   :config  {:initialRouteName :my-profile}})
