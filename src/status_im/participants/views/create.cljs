(ns status-im.participants.views.create
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.resources :as res]
            [status-im.components.react :refer [view list-view list-item]]
            [status-im.components.toolbar :refer [toolbar]]
            [status-im.utils.listview :refer [to-datasource]]
            [status-im.participants.views.contact :refer [participant-contact]]
            [reagent.core :as r]
            [status-im.participants.styles :as st]))

(defn new-participants-toolbar []
  [toolbar
   {:title     "Add Participants"
    :action    {:image   {:source res/v                     ;; {:uri "icon_search"}
                          :style  st/new-participant-image}
                :handler #(dispatch [:add-new-participants])}}])

(defn new-participants-row
  [row _ _]
  (list-item [participant-contact row]))

(defn new-participants []
  (let [contacts (subscribe [:all-new-contacts])]
    (fn []
      (let [contacts-ds (to-datasource @contacts)]
        [view st/participants-container
         [new-participants-toolbar]
         [list-view {:dataSource contacts-ds
                     :renderRow  new-participants-row
                     :style      st/participants-list}]]))))
