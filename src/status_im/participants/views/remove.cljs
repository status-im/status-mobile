(ns status-im.participants.views.remove
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.resources :as res]
            [status-im.components.react :refer [view
                                                text
                                                image
                                                touchable-highlight
                                                list-view
                                                list-item]]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.toolbar.view :refer [toolbar]]
            [status-im.utils.listview :refer [to-datasource]]
            [status-im.participants.views.contact
             :refer [participant-contact]]
            [reagent.core :as r]
            [status-im.participants.styles :as st]
            [status-im.i18n :refer [label]]
            [status-im.components.styles :as cst]))

(defn remove-participants-toolbar []
  [view
   [status-bar]
   [toolbar
    {:title   (label :t/remove-participants)
     :actions [{:handler #(do (dispatch [:remove-participants])
                              (dispatch [:navigate-back]))
                :image   {:source res/trash-icon            ;; {:uri "icon_search"}
                          :style  st/remove-participants-image}}]}]])

(defn remove-participants-row
  [row _ _]
  (r/as-element [participant-contact row]))

(defview remove-participants []
  [contacts [:current-chat-contacts]]
  [view st/participants-container
   [remove-participants-toolbar]
   [list-view {:dataSource (to-datasource contacts)
               :renderRow  remove-participants-row
               :style      st/participants-list}]])
