(ns status-im2.contexts.profile.profiles.views
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [utils.debounce :refer [dispatch-and-chill]]
            [status-im.ui.components.list-selection :as list-selection]
            [react-native.core :as rn]
            [status-im2.contexts.profile.profiles.components :as components]
            [status-im2.contexts.profile.profiles.list-items :as profile-list-item]
            [status-im2.contexts.profile.profiles.style :as styles]
            [status-im.utils.universal-links.utils :as universal-links]
            [status-im2.config :as config]))

(defn items
  [{:keys [mnemonic]}]
  [rn/view
   {:style styles/container-style}

   profile-list-item/personal-info-group

   [profile-list-item/activity-settings-group
    {:mnemonic mnemonic}]

   [profile-list-item/device-settings-group
    {:local-pairing-mode-enabled? config/local-pairing-mode-enabled?}]

   profile-list-item/advanced-settings-group

   profile-list-item/about-help-group

   profile-list-item/logout-item])

(defn my-profile
  []
  (fn []
    (let [{:keys [public-key
                  compressed-key
                  ens-verified
                  emoji-hash
                  mnemonic
                  ens-name address
                  key-uid]
           :as   account}
          @(re-frame/subscribe [:profile/multiaccount])
          customization-color (or (:color @(re-frame/subscribe [:onboarding-2/profile]))
                                  @(re-frame/subscribe [:profile/customization-color key-uid]))
          on-share #(dispatch-and-chill [:open-modal :share-shell] 1000)
          has-picture @(re-frame/subscribe [:profile/has-picture])
          link (universal-links/generate-link :user :external (or ens-name address))]

      [rn/view {:flex 1 :style styles/container-style}

       components/top-background-view

       [components/fixed-toolbar
        {:on-share          #(list-selection/open-share {:message link})
         :on-close          #(re-frame/dispatch [:navigate-back])
         ;;  TODO: No action for switch account
         :on-switch-profile nil
         :on-show-qr        on-share}]

       [rn/scroll-view
        [components/user-info
         {:on-share            on-share
          :has-picture         has-picture
          :customization-color customization-color
          :account             account
          :emoji-hash          (string/join emoji-hash)
          :ens-verified        ens-verified
          :public-key          public-key
          :compressed-key      compressed-key}]
        [items
         {:mnemonic mnemonic}]]])))
