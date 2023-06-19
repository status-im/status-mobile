(ns status-im2.contexts.shell.view
  (:require [re-frame.db]
            [quo2.core :as quo]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]
            [react-native.core :as rn]
            [status-im2.config :as config]
            [status-im2.contexts.shell.utils :as utils]
            [status-im2.navigation.state :as navigation.state]
            [status-im2.contexts.shell.animation :as animation]
            [status-im2.contexts.shell.constants :as shell.constants]
            [status-im2.contexts.shell.shared-values :as shared-values]
            [status-im2.contexts.shell.components.home-stack.view :as home-stack]
            [status-im2.contexts.shell.components.bottom-tabs.view :as bottom-tabs]
            [status-im2.contexts.shell.components.shell-screen.view :as shell-screen]
            [status-im2.contexts.shell.components.floating-screens.view :as floating-screens]))

(defn navigate-back-handler
  []
  (let [chat-screen-open? (and config/shell-navigation-disabled?
                               (= (get @re-frame.db/app-db :view-id) :chat))]
    (if (and (not @navigation.state/curr-modal)
             (or
              chat-screen-open?
              (utils/floating-screen-open? shell.constants/community-screen)
              (utils/floating-screen-open? shell.constants/chat-screen)))
      (do
        (when chat-screen-open? (rf/dispatch [:chat/close]))
        (rf/dispatch [:navigate-back])
        true)
      false)))

(defn f-shell-stack
  []
  (let [shared-values       (shared-values/calculate-and-set-shared-values)
        {:keys [key-uid]}   (rf/sub [:multiaccount])
        profile-color       (:color (rf/sub [:onboarding-2/profile]))
        customization-color (if profile-color ;; Todo - 1. Use single sub for customization color
                              profile-color   ;; Todo - 2. Move sub to child view
                              (rf/sub [:profile/customization-color key-uid]))]
    (rn/use-effect
     (fn []
       (rn/hw-back-add-listener navigate-back-handler)
       #(rn/hw-back-remove-listener navigate-back-handler))
     [])
    [:<>
     [shell-screen/view customization-color]
     [:f> bottom-tabs/f-bottom-tabs]
     [:f> home-stack/f-home-stack]
     [quo/floating-shell-button
      {:jump-to {:on-press            #(animation/close-home-stack true)
                 :label               (i18n/label :t/jump-to)
                 :customization-color customization-color}}
      {:position :absolute
       :bottom   (+ (utils/bottom-tabs-container-height) 12)}
      (:home-stack-opacity shared-values)]
     (when-not config/shell-navigation-disabled?
       [floating-screens/view])]))

(defn shell-stack
  []
  [:f> f-shell-stack])
