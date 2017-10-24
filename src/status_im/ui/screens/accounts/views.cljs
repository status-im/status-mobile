(ns status-im.ui.screens.accounts.views
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.ui.screens.accounts.styles :as styles]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.actions :as act]
            [status-im.ui.components.common.common :as common]
            [status-im.ui.components.action-button.action-button :refer [action-button]]
            [status-im.constants :refer [console-chat-id]]
            [status-im.ui.components.react :as react]
            [status-im.i18n :as i18n]))


(defn account-badge [address photo-path name]
  [react/view styles/account-badge
   [react/image {:source {:uri (if (string/blank? photo-path) :avatar photo-path)}
                 :style  styles/photo-image}]
   [react/view styles/account-badge-text-view
    [react/text {:style styles/account-badge-text
                 :numberOfLines 1}
     (or name address)]]])

(defn account-view [{:keys [address photo-path name] :as account}]
  [react/view
   [react/touchable-highlight {:on-press #(re-frame/dispatch [:open-login address photo-path name])}
    [react/view styles/account-view
     [account-badge address photo-path name]]]])

(defview accounts []
  [accounts [:get-accounts]]
  [react/view styles/accounts-container
   [status-bar/status-bar {:type :transparent}]
   [react/view styles/account-title-conatiner
    [react/text {:style styles/account-title-text
                 :font :toolbar-title}
     (i18n/label :t/sign-in-to-status)]]
   [react/view styles/accounts-list-container
    [list/flat-list {:data      (vals accounts)
                     :render-fn (fn [account] [account-view account])
                     :separator [react/view {:height 10}]}]]
   [react/view styles/bottom-actions-container
    [action-button (merge
                     {:label     (i18n/label :t/create-new-account)
                      :icon      :icons/add
                      :icon-opts {:color :white}
                      :on-press  #(re-frame/dispatch [:create-new-account-handler])}
                     styles/accounts-action-button)]
    [common/separator styles/accounts-separator styles/accounts-separator-wrapper]
    [action-button (merge
                     {:label     (i18n/label :t/recover-access)
                      :icon      :icons/dots-horizontal
                      :icon-opts {:color :white}
                      :on-press  #(re-frame/dispatch [:navigate-to :recover])}
                     styles/accounts-action-button)]]])
