(ns status-im.ui.screens.bootnodes-settings.edit-bootnode.views
  (:require-macros [status-im.utils.views :as views])
  (:require
   [re-frame.core :as re-frame]
   [status-im.ui.components.react :as react]
   [status-im.i18n :as i18n]
   [status-im.ui.components.styles :as components.styles]
   [status-im.ui.components.common.common :as components.common]
   [status-im.ui.components.colors :as colors]
   [status-im.ui.components.icons.vector-icons :as vector-icons]
   [status-im.ui.components.status-bar.view :as status-bar]
   [status-im.ui.components.toolbar.view :as toolbar]
   [status-im.ui.components.text-input.view :as text-input]
   [status-im.ui.screens.bootnodes-settings.edit-bootnode.styles :as styles]))

(def qr-code
  [react/touchable-highlight {:on-press #(re-frame/dispatch [:scan-qr-code
                                                             {:toolbar-title (i18n/label :t/add-bootnode)}
                                                             :set-bootnode-from-qr])
                              :style    styles/qr-code}
   [react/view
    [vector-icons/icon :icons/qr {:color colors/blue}]]])

(views/defview edit-bootnode []
  (views/letsubs [manage-bootnode [:get-manage-bootnode]
                  is-valid?       [:manage-bootnode-valid?]]
    (let [url  (get-in manage-bootnode [:url :value])
          name (get-in manage-bootnode [:name :value])]

      [react/view components.styles/flex
       [status-bar/status-bar]
       [react/keyboard-avoiding-view components.styles/flex
        [toolbar/simple-toolbar (i18n/label :t/add-bootnode)]
        [react/scroll-view
         [react/view styles/edit-bootnode-view
          [text-input/text-input-with-label
           {:label           (i18n/label :t/name)
            :placeholder     (i18n/label :t/specify-name)
            :style           styles/input
            :container       styles/input-container
            :default-value   name
            :on-change-text  #(re-frame/dispatch [:bootnode-set-input :name %])
            :auto-focus      true}]
          [text-input/text-input-with-label
           {:label           (i18n/label :t/bootnode-address)
            :placeholder     (i18n/label :t/specify-bootnode-address)
            :content         qr-code
            :style           styles/input
            :container       styles/input-container
            :default-value   url
            :on-change-text  #(re-frame/dispatch [:bootnode-set-input :url %])}]]]
        [react/view styles/bottom-container
         [react/view components.styles/flex]
         [components.common/bottom-button
          {:forward?  true
           :label     (i18n/label :t/save)
           :disabled? (not is-valid?)
           :on-press  #(re-frame/dispatch [:save-new-bootnode])}]]]])))
