(ns status-im2.contexts.shell.jump-to.view
  (:require [quo2.core :as quo]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]
            [react-native.core :as rn]
            [status-im2.config :as config]
            [status-im2.contexts.shell.jump-to.utils :as utils]
            [status-im2.navigation.state :as navigation.state]
            [status-im2.contexts.shell.jump-to.animation :as animation]
            [status-im2.contexts.shell.jump-to.constants :as shell.constants]
            [status-im2.contexts.shell.jump-to.shared-values :as shared-values]
            [status-im2.contexts.shell.jump-to.components.home-stack.view :as home-stack]
            [status-im2.contexts.shell.jump-to.components.bottom-tabs.view :as bottom-tabs]
            [status-im2.contexts.shell.jump-to.components.jump-to-screen.view :as jump-to-screen]
            [status-im2.contexts.shell.jump-to.components.floating-screens.view :as floating-screens]
            re-frame.db))

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

(defn floating-button
  [shared-values]
  [quo/floating-shell-button
   {:jump-to {:on-press            #(animation/close-home-stack true)
              :label               (i18n/label :t/jump-to)
              :customization-color (rf/sub [:profile/customization-color])}}
   {:position :absolute
    :bottom   (utils/bottom-tabs-container-height)}
   (:home-stack-opacity shared-values)])

(defn f-shell-stack
  []
  (let [shared-values (shared-values/calculate-and-set-shared-values)]
    (rn/use-effect
     (fn []
       (rn/hw-back-add-listener navigate-back-handler)
       #(rn/hw-back-remove-listener navigate-back-handler))
     [])
    [:<>
     [jump-to-screen/view]
     [:f> bottom-tabs/f-bottom-tabs]
     [:f> home-stack/f-home-stack]
     [floating-button shared-values]
     (when-not config/shell-navigation-disabled?
       [floating-screens/view])]))

(defn shell-stack
  []
  [:f> f-shell-stack])
