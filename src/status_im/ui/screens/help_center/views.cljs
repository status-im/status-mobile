(ns status-im.ui.screens.help-center.views
  (:require-macros [status-im.utils.views :as views])
  (:require [status-im.i18n :as i18n]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.screens.help-center.styles :as styles]
            [status-im.ui.screens.profile.components.views :as profile.components]
            [status-im.utils.instabug :as instabug]))

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
     [profile.components/settings-item {:label-kw            :t/submit-bug
                                        :accessibility-label :submit-bug-button
                                        :action-fn           #(instabug/submit-bug)}]
     [profile.components/settings-item-separator]
     [profile.components/settings-item {:label-kw            :t/request-feature
                                        :accessibility-label :request-feature-button
                                        :action-fn           #(instabug/request-feature)}]]]])
