(ns status-im.accounts.screen
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [dispatch dispatch-sync]]
            [status-im.accounts.styles :as st]
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

(defn account-bage [address photo-path name]
  [view st/account-bage
   [image {:source {:uri (if (str/blank? photo-path) :avatar photo-path)}
           :style  st/photo-image}]
   [view st/account-bage-text-view
    [text {:style st/account-bage-text
           :numberOfLines 1}
     (or name address)]]])

(defn account-view [{:keys [address photo-path name] :as account}]
  [view
   [touchable-highlight {:on-press #(dispatch [:open-login address photo-path name])}
    [view st/account-view
     [account-bage address photo-path name]]]])

(defn create-account [_]
  (dispatch [:reset-app #(dispatch [:navigate-to :chat console-chat-id])]))

(defview accounts []
  [accounts [:get :accounts]]
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
    [action-button (i18n/label :t/create-new-account)
                   :add_white
                   create-account
                   st/accounts-action-button]
    [common/separator st/accounts-separator st/accounts-separator-wrapper]
    [action-button (i18n/label :t/recover-access)
                   :dots_horizontal_white
                   #(dispatch [:navigate-to :recover])
                   st/accounts-action-button]]])