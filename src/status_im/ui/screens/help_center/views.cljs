(ns status-im.ui.screens.help-center.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.screens.help-center.styles :as styles]
            [status-im.ui.screens.profile.components.views :as profile.components]))

(views/defview help-center []
  [react/view styles/wrapper
   [status-bar/status-bar]
   [toolbar/simple-toolbar
    (i18n/label :t/help-center)]
   [react/scroll-view
    [react/view
     [profile.components/settings-item-separator]
     [profile.components/settings-item {:label-kw            :t/faq
                                        :accessibility-label :faq-button
                                        :action-fn           #(.openURL react/linking "https://wiki.status.im/Questions_around_beta#firstHeading")}]
     [profile.components/settings-item-separator]
     [profile.components/settings-item {:label-kw            :t/ask-in-status
                                        :accessibility-label :submit-bug-button
                                        :action-fn           #(re-frame/dispatch [:chat.ui/start-public-chat "status" {:navigation-reset? false}])}]]]])
