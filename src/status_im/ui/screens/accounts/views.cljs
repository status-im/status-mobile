(ns status-im.ui.screens.accounts.views
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [dispatch dispatch-sync]]
            [status-im.ui.screens.accounts.styles :as st]
            [status-im.components.text-input-with-label.view :refer [text-input-with-label]]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.toolbar-new.actions :as act]
            [status-im.components.common.common :as common]
            [status-im.components.action-button.action-button :refer [action-button]]
            [status-im.utils.listview :as lw]
            [status-im.constants :refer [console-chat-id]]
            [status-im.components.react :refer [view
                                                text
                                                list-view
                                                list-item
                                                image
                                                touchable-highlight]]
            [status-im.i18n :as i18n]
            [clojure.string :as str]))

(defn account-badge [address photo-path name]
  [view st/account-badge
   [image {:source {:uri (if (str/blank? photo-path) :avatar photo-path)}
           :style  st/photo-image}]
   [view st/account-badge-text-view
    [text {:style st/account-badge-text
           :numberOfLines 1}
     (or name address)]]])

(defn account-view [{:keys [address photo-path name] :as account}]
  [view
   [touchable-highlight {:on-press #(dispatch [:open-login address photo-path name])}
    [view st/account-view
     [account-badge address photo-path name]]]])

(defview accounts []
  [accounts [:get-accounts]]
  [view st/accounts-container
   [status-bar {:type :transparent}]
   [view st/account-title-conatiner
    [text {:style st/account-title-text
           :font :toolbar-title}
     (i18n/label :t/sign-in-to-status)]]
   [view st/accounts-list-container
    [list-view {:dataSource      (lw/to-datasource (vals accounts))
                :renderSeparator #(list-item ^{:key %2} [view {:height 10}])
                :renderRow       #(list-item [account-view %])}]]
   [view st/bottom-actions-container
    [action-button (merge
                     {:label (i18n/label :t/create-new-account)
                      :icon [:icons/add
                             {:color :white}]
                      :on-press #(dispatch [:create-new-account-handler])}
                     st/accounts-action-button)]
    [common/separator st/accounts-separator st/accounts-separator-wrapper]
    [action-button (merge
                     {:label (i18n/label :t/recover-access)
                      :icon [:icons/dots_horizontal
                             {:color :white}]
                      :on-press #(dispatch [:navigate-to :recover])}
                     st/accounts-action-button)]]])
