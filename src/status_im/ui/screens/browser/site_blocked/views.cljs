(ns status-im.ui.screens.browser.site-blocked.views
  (:require-macros [status-im.utils.views :as views])
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.button.view :as button]
            [status-im.ui.screens.browser.site-blocked.styles :as styles]
            [status-im.i18n :as i18n]
            [status-im.ui.components.common.common :as components.common]
            [status-im.ui.components.icons.vector-icons :as vector-icons]))

(defn chat-link []
  [react/text {:on-press #(.openURL react/linking "status-im://chat/public/status")
               :style    styles/chat-link-text}
   "#status"])

(views/defview view [{:keys [can-go-back?]}]
  [react/scroll-view {:keyboard-should-persist-taps :always
                      :bounces                      false
                      :content-container-style      styles/container}
   [react/view styles/container-root-view
    [vector-icons/icon :icons/info {:color colors/red}]
    [react/text {:style styles/title-text}
     (i18n/label :t/browsing-site-blocked-title)]
    [react/text {:style styles/description-text}
     (i18n/label :t/browsing-site-blocked-description1)
     [chat-link]
     [react/text (i18n/label :t/browsing-site-blocked-description2)]]
    [react/view styles/buttons-container
     [components.common/button {:on-press (fn []
                                            (let [handler (if can-go-back?
                                                            :browser-nav-back
                                                            :navigate-back)]
                                              (re-frame/dispatch [handler])))
                                :label    (i18n/label :t/browsing-site-blocked-go-back)}]]]])
