(ns status-im.accounts.screen
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.components.react :refer [view
                                                scroll-view
                                                text
                                                list-view
                                                list-item
                                                image
                                                linear-gradient
                                                touchable-highlight]]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.toolbar.view :refer [toolbar]]
            [status-im.components.toolbar.actions :as act]
            [status-im.components.styles :refer [color-white
                                                 icon-search
                                                 icon-plus
                                                 white-form-text-input]]
            [status-im.components.toolbar.styles :refer [toolbar-title-container
                                                         toolbar-title-text]]
            [status-im.utils.listview :as lw]
            [status-im.accounts.views.account :refer [account-view]]
            [status-im.i18n :refer [label]]
            [status-im.accessibility-ids :as id]
            [status-im.accounts.styles :as st]
            [status-im.constants :refer [console-chat-id]]))

(defn toolbar-title []
  (let [style (merge toolbar-title-text {:color color-white})]
    [view toolbar-title-container
     [text {:style style
            :font  :medium}
      (label :t/switch-users)]]))

(defn render-row [row _ _]
  (list-item [account-view row]))

(defn create-account [_]
  (dispatch-sync [:reset-app])
  (dispatch [:navigate-to :chat console-chat-id]))

(defview accounts []
  [accounts [:get :accounts]
   stack [:get :navigation-stack]]
  (let [accounts          (vals accounts)
        show-back?        (> (count stack) 1)]
    [view st/screen-container
     [linear-gradient {:colors    ["rgba(182, 116, 241, 1)"
                                   "rgba(107, 147, 231, 1)"
                                   "rgba(43, 171, 238, 1)"]
                       :start     [0, 0]
                       :end       [0.5, 1]
                       :locations [0, 0.8, 1]
                       :style     st/gradient-background}
      [status-bar {:type :transparent}]
      [toolbar {:background-color :transparent
                :nav-action       (if show-back?
                                    (act/back-white #(dispatch [:navigate-back]))
                                    act/nothing)
                :custom-content   [toolbar-title]
                :actions          [{:image   {:style icon-search}
                                    :handler #()}]}]]
     [list-view {:dataSource            (lw/to-datasource accounts)
                 :enableEmptySections   true
                 :renderRow             render-row
                 :bounces               false
                 :style                 st/account-list
                 :contentContainerStyle (st/account-list-content (count accounts))}]
     [view st/bottom-actions-container
      [view st/recover-button-container
       [touchable-highlight
        {:on-press #(dispatch [:navigate-to :recover])}
        [view st/recover-button
         [text {:style st/recover-button-text}
          (label :t/recover-access)]]]]
      [view st/add-account-button-container
       [touchable-highlight {:on-press            create-account
                             :accessibility-label id/accounts-create-button}
        [view st/add-account-button
         [image {:source {:uri :icon_add_white}
                 :style  st/icon-plus}]
         [text {:style st/add-account-text
                :font  :default}
          (label :t/add-account)]]]]]]))
