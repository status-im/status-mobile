(ns status-im.ui.screens.bootnodes-settings.edit-bootnode.views
  (:require-macros [status-im.utils.views :as views])
  (:require
   [re-frame.core :as re-frame]
   [status-im.thread :as status-im.thread]
   [status-im.ui.components.react :as react]
   [status-im.i18n :as i18n]
   [status-im.utils.utils :as utils]
   [status-im.ui.components.styles :as components.styles]
   [status-im.ui.components.common.common :as components.common]
   [status-im.ui.components.colors :as colors]
   [status-im.ui.components.icons.vector-icons :as vector-icons]
   [status-im.ui.components.status-bar.view :as status-bar]
   [status-im.ui.components.toolbar.view :as toolbar]
   [status-im.ui.components.text-input.view :as text-input]
   [status-im.ui.screens.bootnodes-settings.edit-bootnode.styles :as styles]))

(defn handle-delete [id]
  (utils/show-confirmation (i18n/label :t/delete-bootnode-title)
                           (i18n/label :t/delete-bootnode-are-you-sure)
                           (i18n/label :t/delete-bootnode)
                           #(status-im.thread/dispatch [:delete-bootnode id])))

(defn delete-button [id]
  [react/touchable-highlight {:on-press #(handle-delete id)}
   [react/view styles/button-container
    [react/view {:style               styles/delete-button
                 :accessibility-label :bootnode-delete-button}
     [react/text {:style      styles/button-label
                  :uppercase? true}
      (i18n/label :t/delete)]]]])

(def qr-code
  [react/touchable-highlight {:on-press #(status-im.thread/dispatch [:scan-qr-code
                                                             {:toolbar-title (i18n/label :t/add-bootnode)}
                                                             :set-bootnode-from-qr])
                              :style    styles/qr-code}
   [react/view
    [vector-icons/icon :icons/qr {:color colors/blue}]]])

(views/defview edit-bootnode []
  (views/letsubs [manage-bootnode [:get-manage-bootnode]
                  is-valid?       [:manage-bootnode-valid?]]
    (let [url  (get-in manage-bootnode [:url :value])
          id   (get-in manage-bootnode [:id :value])
          name (get-in manage-bootnode [:name :value])]
      [react/view components.styles/flex
       [status-bar/status-bar]
       [react/keyboard-avoiding-view components.styles/flex
        [toolbar/simple-toolbar (i18n/label (if id :t/bootnode-details :t/add-bootnode))]
        [react/scroll-view {:keyboard-should-persist-taps :handled}
         [react/view styles/edit-bootnode-view
          [text-input/text-input-with-label
           {:label           (i18n/label :t/name)
            :placeholder     (i18n/label :t/specify-name)
            :style           styles/input
            :container       styles/input-container
            :default-value   name
            :on-change-text  #(status-im.thread/dispatch [:bootnode-set-input :name %])
            :auto-focus      true}]
          [text-input/text-input-with-label
           {:label           (i18n/label :t/bootnode-address)
            :placeholder     (i18n/label :t/specify-bootnode-address)
            :content         qr-code
            :style           styles/input
            :container       styles/input-container
            :default-value   url
            :on-change-text  #(status-im.thread/dispatch [:bootnode-set-input :url %])}]
          (when id
            [delete-button id])]]
        [react/view styles/bottom-container
         [react/view components.styles/flex]
         [components.common/bottom-button
          {:forward?  true
           :label     (i18n/label :t/save)
           :disabled? (not is-valid?)
           :on-press  #(status-im.thread/dispatch [:save-new-bootnode])}]]]])))
