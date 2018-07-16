(ns status-im.ui.screens.offline-messaging-settings.edit-mailserver.views
  (:require-macros [status-im.utils.views :as views])
  (:require
   [re-frame.core :as re-frame]
   [status-im.thread :as status-im.thread]
   [status-im.ui.components.react :as react]
   [status-im.i18n :as i18n]
   [status-im.utils.utils :as utils]
   [status-im.ui.components.colors :as colors]
   [status-im.ui.components.icons.vector-icons :as vector-icons]
   [status-im.ui.components.styles :as components.styles]
   [status-im.ui.components.common.common :as components.common]
   [status-im.ui.components.status-bar.view :as status-bar]
   [status-im.ui.components.toolbar.view :as toolbar]
   [status-im.ui.components.list.views :as list]
   [status-im.ui.components.text-input.view :as text-input]
   [status-im.ui.screens.offline-messaging-settings.edit-mailserver.styles :as styles]))

(defn handle-delete [id]
  (utils/show-confirmation (i18n/label :t/delete-mailserver-title)
                           (i18n/label :t/delete-mailserver-are-you-sure)
                           (i18n/label :t/delete-mailserver)
                           #(status-im.thread/dispatch [:delete-mailserver id])))

(defn connect-button [id]
  [react/touchable-highlight {:on-press #(status-im.thread/dispatch [:connect-wnode id])}
   [react/view styles/button-container
    [react/view {:style               styles/connect-button
                 :accessibility-label :mailserver-connect-button}
     [react/text {:style      styles/button-label
                  :uppercase? true}
      (i18n/label :t/connect)]]]])

(defn delete-button [id]
  [react/touchable-highlight {:on-press #(handle-delete id)}
   [react/view styles/button-container
    [react/view {:style               styles/delete-button
                 :accessibility-label :mailserver-delete-button}
     [react/text {:style      styles/button-label
                  :uppercase? true}
      (i18n/label :t/delete)]]]])

(def qr-code
  [react/touchable-highlight {:on-press #(status-im.thread/dispatch [:scan-qr-code
                                                             {:toolbar-title (i18n/label :t/add-mailserver)}
                                                             :set-mailserver-from-qr])
                              :style    styles/qr-code}
   [react/view
    [vector-icons/icon :icons/qr {:color colors/blue}]]])

(views/defview edit-mailserver []
  (views/letsubs [manage-mailserver [:get-manage-mailserver]
                  connected?        [:get-connected-mailserver]
                  is-valid?         [:manage-mailserver-valid?]]
    (let [url  (get-in manage-mailserver [:url :value])
          id   (get-in manage-mailserver [:id :value])
          name (get-in manage-mailserver [:name :value])]
      [react/view components.styles/flex
       [status-bar/status-bar]
       [react/keyboard-avoiding-view components.styles/flex
        [toolbar/simple-toolbar (i18n/label (if id :t/mailserver-details :t/add-mailserver))]
        [react/scroll-view {:keyboard-should-persist-taps :handled}
         [react/view styles/edit-mailserver-view
          [text-input/text-input-with-label
           {:label           (i18n/label :t/name)
            :placeholder     (i18n/label :t/specify-name)
            :style           styles/input
            :container       styles/input-container
            :default-value   name
            :on-change-text  #(status-im.thread/dispatch [:mailserver-set-input :name %])
            :auto-focus      true}]
          [text-input/text-input-with-label
           {:label           (i18n/label :t/mailserver-address)
            :placeholder     (i18n/label :t/specify-mailserver-address)
            :content         qr-code
            :style           styles/input
            :container       styles/input-container
            :default-value   url
            :on-change-text  #(status-im.thread/dispatch [:mailserver-set-input :url %])}]
          (when (and id
                     (not connected?))
            [react/view
             [connect-button id]
             [delete-button id]])]]
        [react/view styles/bottom-container
         [react/view components.styles/flex]
         [components.common/bottom-button
          {:forward?  true
           :label     (i18n/label :t/save)
           :disabled? (not is-valid?)
           :on-press  #(status-im.thread/dispatch [:upsert-mailserver])}]]]])))
