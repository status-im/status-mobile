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
            [status-im.components.toolbar :refer [toolbar]]
            [status-im.components.styles :refer [color-purple
                                                 color-white
                                                 icon-search
                                                 icon-back
                                                 icon-qr
                                                 icon-plus
                                                 toolbar-background1
                                                 toolbar-title-container
                                                 toolbar-title-text
                                                 button-input-container
                                                 button-input
                                                 white-form-text-input]]
            [status-im.utils.listview :as lw]
            [status-im.accounts.views.account :refer [account-view]]
            [status-im.chat.sign-up :as sign-up-service]
            [status-im.i18n :refer [label]]
            [status-im.accounts.styles :as st]
            [status-im.utils.logging :as log]))

(defn toolbar-title [platform-specific]
  (let [style (merge toolbar-title-text {:color color-white})]
    [view toolbar-title-container
     [text {:style             style
            :platform-specific platform-specific
            :font              :medium}
      (label :t/switch-users)]]))

(defn render-row [row _ _]
  (list-item [account-view row]))

(defn render-separator [_ row-id _]
  (list-item [view {:style st/row-separator
                    :key   row-id}]))

(defn create-account [event]
  (dispatch-sync [:reset-app])
  ; add accounts screen to history ( maybe there is a better way ? )
  (dispatch [:navigate-to-clean :accounts])
  (dispatch [:navigate-to :chat "console"]))

(defview accounts [{platform-specific :platform-specific}]
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
      [status-bar {:platform-specific platform-specific
                   :type              :transparent}]
      [toolbar {:background-color  :transparent
                :nav-action        {:image   {:source (if show-back? {:uri :icon_back_white} nil)
                                              :style  icon-back}
                                    :handler (if show-back? #(dispatch [:navigate-back]) nil)}
                :custom-content    [toolbar-title platform-specific]
                :action            {:image   {:style icon-search}
                                    :handler #()}
                :platform-specific platform-specific}]
      [list-view {:dataSource            (lw/to-datasource accounts)
                  :enableEmptySections   true
                  :renderRow             render-row
                  :style                 st/account-list
                  :contentContainerStyle (st/account-list-content (count accounts))}]
      [view st/bottom-actions-container
       [view st/recover-button-container
        [touchable-highlight
         {:on-press #(dispatch [:navigate-to :recover])}
         [view st/recover-button
          [text {:style             st/recover-button-text
                 :platform-specific platform-specific}
           (label :t/recover-access)]]]]
       [view st/add-account-button-container
        [touchable-highlight {:on-press            create-account
                              :accessibility-label :create-account}
         [view st/add-account-button
          [image {:source {:uri :icon_add}
                  :style  st/icon-plus}]
          [text {:style             st/add-account-text
                 :platform-specific platform-specific
                 :font              :default}
           (label :t/add-account)]]]]
       ]]]))
