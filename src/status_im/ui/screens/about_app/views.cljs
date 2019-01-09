(ns status-im.ui.screens.about-app.views
  (:require-macros [status-im.utils.views :as views])
  (:require [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.profile.components.views :as profile.components]
            [re-frame.core :as re-frame]))

(views/defview about-app []
  (views/letsubs [app-version [:get-app-short-version]
                  node-version [:get-app-node-version]]
    [react/view {:flex 1}
     [status-bar/status-bar]
     [toolbar/simple-toolbar (i18n/label :t/about-app)]
     [react/scroll-view
      [react/view
       [profile.components/settings-item-separator]
       [profile.components/settings-item
        {:label-kw            :t/privacy-policy
         :accessibility-label :privacy-policy
         :action-fn           #(re-frame/dispatch [:privacy-policy/privacy-policy-button-pressed])}]
       (when status-im.utils.platform/ios?
         [profile.components/settings-item-separator])
       [profile.components/settings-item
        {:item-text           (i18n/label :t/version {:version app-version})
         :accessibility-label :version
         :hide-arrow?         true}]
       (when status-im.utils.platform/ios?
         [profile.components/settings-item-separator])
       [profile.components/settings-item
        {:item-text           node-version
         :accessibility-label :version
         :hide-arrow?         true}]]]]))
