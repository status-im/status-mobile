(ns syng-im.participants.views.add
  (:require-macros [syng-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [syng-im.resources :as res]
            [syng-im.components.react :refer [view list-view list-item]]
            [syng-im.components.toolbar :refer [toolbar]]
            [syng-im.utils.listview :refer [to-datasource]]
            [syng-im.participants.views.contact :refer [participant-contact]]
            [reagent.core :as r]
            [syng-im.participants.styles :as st]))

(defn new-participants-toolbar []
  [toolbar
   {:title  "Add Participants"
    :action {:image   {:source res/v                     ;; {:uri "icon_search"}
                       :style  st/new-participant-image}
             :handler #(do (dispatch [:add-new-participants])
                           (dispatch [:navigate-back]))}}])

(defn new-participants-row
  [row _ _]
  (list-item [participant-contact row]))

(defview new-participants []
  [contacts [:all-new-contacts]]
  [view st/participants-container
   [new-participants-toolbar]
   [list-view {:dataSource (to-datasource contacts)
               :renderRow  new-participants-row
               :style      st/participants-list}]])
