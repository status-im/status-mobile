(ns status-im.participants.views.remove
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.resources :as res]
            [status-im.components.react :refer [view text-input text image
                                              touchable-highlight list-view
                                              list-item]]
            [status-im.components.toolbar :refer [toolbar]]
            [status-im.utils.listview :refer [to-datasource]]
            [status-im.participants.views.contact
             :refer [participant-contact]]
            [reagent.core :as r]
            [status-im.participants.styles :as st]))

(defn remove-participants-toolbar []
  [toolbar
   {:title  "Remove Participants"
    :action {:handler #(do (dispatch [:remove-participants])
                           (dispatch [:navigate-back]))
             :image   {:source res/trash-icon            ;; {:uri "icon_search"}
                       :style  st/remove-participants-image}}}])

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
