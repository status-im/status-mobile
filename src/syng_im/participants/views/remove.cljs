(ns syng-im.participants.views.remove
  (:require-macros [syng-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [syng-im.resources :as res]
            [syng-im.components.react :refer [view text-input text image
                                              touchable-highlight list-view
                                              list-item]]
            [syng-im.components.toolbar :refer [toolbar]]
            [syng-im.utils.listview :refer [to-datasource]]
            [syng-im.participants.views.contact
             :refer [participant-contact]]
            [reagent.core :as r]
            [syng-im.participants.styles :as st]))

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
