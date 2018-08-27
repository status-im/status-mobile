(ns status-im.ui.screens.accounts.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.chat.views.photos :as photos]
            [status-im.ui.screens.accounts.styles :as styles]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.react :as react]
            [status-im.i18n :as i18n]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.common.common :as components.common]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.screens.privacy-policy.views :as privacy-policy]))

(defn account-view [{:keys [address photo-path name public-key]}]
  [react/touchable-highlight {:on-press #(re-frame/dispatch [:ui/open-login address photo-path name])}
   [react/view styles/account-view
    [photos/photo photo-path {:size styles/account-image-size}]
    [react/view styles/account-badge-text-view
     [react/text {:style         styles/account-badge-text
                  :numberOfLines 1}
      name]
     [react/text {:style          styles/account-badge-pub-key-text
                  :ellipsize-mode :middle
                  :numberOfLines  1}
      public-key]]
    [react/view {:flex 1}]
    [icons/icon :icons/forward {:color (colors/alpha colors/gray-icon 0.4)}]]])

(defview accounts []
  (letsubs [accounts [:get-accounts]]
    [react/view styles/accounts-view
     [status-bar/status-bar]
     [toolbar/toolbar nil nil
      [toolbar/content-title (i18n/label :t/sign-in-to-status)]]
     [react/view styles/accounts-container
      [react/view styles/accounts-list-container
       [list/flat-list {:data      (vals accounts)
                        :key-fn    :address
                        :render-fn (fn [account] [account-view account])
                        :separator [react/view {:height 12}]}]]
      [react/view
       [components.common/button {:on-press #(re-frame/dispatch [:navigate-to :create-account])
                                  :label    (i18n/label :t/create-new-account)}]
       [react/view styles/bottom-button-container
        [components.common/button {:on-press    #(re-frame/dispatch [:navigate-to :recover])
                                   :label       (i18n/label :t/add-existing-account)
                                   :background? false}]]
       [privacy-policy/privacy-policy-button]]]]))
