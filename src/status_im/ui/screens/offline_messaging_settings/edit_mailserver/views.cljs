(ns status-im.ui.screens.offline-messaging-settings.edit-mailserver.views
  (:require-macros [status-im.utils.views :as views])
  (:require
   [re-frame.core :as re-frame]
   [status-im.ui.components.react :as react]
   [status-im.i18n :as i18n]
   [status-im.ui.components.styles :as components.styles]
   [status-im.ui.components.common.common :as components.common]
   [status-im.ui.components.status-bar.view :as status-bar]
   [status-im.ui.components.toolbar.view :as toolbar]
   [status-im.ui.components.list.views :as list]
   [status-im.ui.components.text-input.view :as text-input]
   [status-im.ui.screens.offline-messaging-settings.edit-mailserver.styles :as styles]))

(views/defview edit-mailserver []
  (views/letsubs [manage-mailserver [:get-manage-mailserver]
                  is-valid?         [:manage-mailserver-valid?]]
    [react/view components.styles/flex
     [status-bar/status-bar]
     [react/keyboard-avoiding-view components.styles/flex
      [toolbar/simple-toolbar (i18n/label :t/add-mailserver)]
      [react/scroll-view
       [react/view styles/edit-mailserver-view
        [text-input/text-input-with-label
         {:label           (i18n/label :t/name)
          :placeholder     (i18n/label :t/specify-name)
          :container       styles/input-container
          :default-value   (get-in manage-mailserver [:name :value])
          :on-change-text  #(re-frame/dispatch [:mailserver-set-input :name %])
          :auto-focus      true}]
        [text-input/text-input-with-label
         {:label           (i18n/label :t/mailserver-address)
          :placeholder     (i18n/label :t/specify-mailserver-address)
          :container       styles/input-container
          :default-value   (get-in manage-mailserver [:url :value])
          :on-change-text  #(re-frame/dispatch [:mailserver-set-input :url %])}]]]
      [react/view styles/bottom-container
       [react/view components.styles/flex]
       [components.common/bottom-button
        {:forward?  true
         :label     (i18n/label :t/save)
         :disabled? (not is-valid?)
         :on-press  #(re-frame/dispatch [:save-new-mailserver])}]]]]))
