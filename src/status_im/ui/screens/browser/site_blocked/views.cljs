(ns status-im.ui.screens.browser.site-blocked.views
  (:require [re-frame.core :as re-frame]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.browser.site-blocked.styles :as styles]
            [quo.core :as quo])
  (:require-macros [status-im.utils.views :as views]))

(views/defview view [{:keys [can-go-back?]}]
  [react/scroll-view {:keyboard-should-persist-taps :always
                      :bounces                      false
                      :content-container-style      styles/container}
   [react/view styles/container-root-view
    [icons/icon :main-icons/info {:color colors/red}]
    [react/text {:style styles/title-text}
     (i18n/label :t/browsing-site-blocked-title)]
    [react/nested-text {:style styles/description-text}
     (i18n/label :t/browsing-site-blocked-description1)
     [{:on-press #(.openURL ^js react/linking "status-im://chat/public/status")
       :style    styles/chat-link-text}
      "#status"]
     (i18n/label :t/browsing-site-blocked-description2)]
    [react/view styles/buttons-container
     [quo/button {:on-press (fn []
                              (let [handler (if can-go-back?
                                              :browser.ui/previous-page-button-pressed
                                              :navigate-back)]
                                (re-frame/dispatch [handler])))}
      (i18n/label :t/browsing-site-blocked-go-back)]]
    [quo/button {:theme    :negative
                 :on-press #(re-frame/dispatch [:browser/ignore-unsafe])}
     (i18n/label :t/continue-anyway)]]])
