(ns syng-im.participants.views.remove
  (:require [re-frame.core :refer [subscribe dispatch]]
            [syng-im.resources :as res]
            [syng-im.components.react :refer [view text-input text image
                                              touchable-highlight list-view
                                              list-item]]
            [syng-im.components.toolbar :refer [toolbar]]
            [syng-im.utils.listview :refer [to-datasource2]]
            [syng-im.participants.views.contact
             :refer [participant-contact]]
            [reagent.core :as r]
            [syng-im.participants.styles :as st]))

(defn remove-participants-toolbar []
  [toolbar
   {:title     "Remove Participants"
    :action    {:handler #(dispatch [:remove-selected-participants])
                :image   {:source res/trash-icon            ;; {:uri "icon_search"}
                          :style  st/remove-participants-image}}}])

(defn remove-participants-row
  [row _ _]
  (r/as-element [participant-contact row]))

(defn remove-participants []
  (let [contacts (subscribe [:current-chat-contacts])]
    (fn []
      (let [contacts-ds (to-datasource2 @contacts)]
        [view st/participants-container
         [remove-participants-toolbar]
         [list-view {:dataSource contacts-ds
                     :renderRow  remove-participants-row
                     :style      st/participants-list}]]))))
