(ns status-im.accounts.screen
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.components.react :refer [view
                                                text
                                                list-view
                                                list-item
                                                image
                                                linear-gradient
                                                touchable-highlight]]
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
            [status-im.i18n :refer [label]]
            [status-im.accounts.styles :as st]
            [status-im.utils.logging :as log]))

(def toolbar-title
  [view toolbar-title-container
   [text {:style (merge toolbar-title-text {:color color-white})}
    (label :t/switch-users)]])

(defn render-row [row _ _]
  (list-item [account-view row]))

(defn render-separator [_ row-id _]
  (list-item [view {:style st/row-separator
                    :key   row-id}]))

(defn create-account [event]
  (dispatch [:console-create-account])
  (dispatch [:navigate-to :chat "console"]))

(defview accounts []
  [accounts [:get :accounts]]
  (let [accounts (vals accounts)]
  [view st/screen-container
   [linear-gradient {:colors ["rgba(182, 116, 241, 1)" "rgba(107, 147, 231, 1)" "rgba(43, 171, 238, 1)"]
                     :start [0, 0]
                     :end [0.5, 1]
                     :locations [0, 0.8 ,1]
                     :style  st/gradient-background}]

   [toolbar {:background-color :transparent
             :nav-action     {:image   {:source {:uri :icon_back_white}
                                        :style  icon-back}
                              :handler  #(dispatch [:navigate-back])}
             :custom-content   toolbar-title
             :action           {:image   {:style  icon-search}
                               :handler #()}}]
   [view st/accounts-container
    [view st/account-list-view-container
     [list-view {:dataSource          (lw/to-datasource accounts)
                 :enableEmptySections true
                 :renderRow           render-row
                 ;:renderSeparator     render-separator
                 :style               st/account-list}]]]
   [view st/add-account-button-container
    [touchable-highlight {:on-press create-account}
     [view st/add-account-button
      [image {:source {:uri :icon_add}
              :style  st/icon-plus}]
      [text {:style st/add-account-text} (label :t/add-account)]]]]]))


;(re-frame.core/dispatch [:set :view-id :users])
;{:name (label :t/add-account)
;:address "0x0"
;:photo-path :icon_plus}
