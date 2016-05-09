(ns syng-im.components.chat.new-participants
  (:require [re-frame.core :refer [subscribe dispatch]]
            [syng-im.resources :as res]
            [syng-im.components.react :refer [view]]
            [syng-im.components.realm :refer [list-view]]
            [syng-im.components.toolbar :refer [toolbar]]
            [syng-im.utils.listview :refer [to-realm-datasource]]
            [syng-im.components.chats.new-participant-contact
             :refer [new-participant-contact]]
            [reagent.core :as r]
            [syng-im.components.chat.chat-message-styles :as st]))

(defn new-participants-toolbar [navigator]
  [toolbar
   {:navigator navigator
    :title     "Add Participants"
    :action    {:image   {:source res/v  ;; {:uri "icon_search"}
                          :style  st/new-participant-image}
                :handler #(dispatch [:add-new-participants navigator])}}])

(defn new-participants-row [navigator]
  (fn [row _ _]
    (r/as-element
      [new-participant-contact (js->clj row :keywordize-keys true) navigator])))

(defn new-participants [{:keys [navigator]}]
  (let [contacts (subscribe [:all-new-contacts])]
    (fn []
      (let [contacts-ds (to-realm-datasource @contacts)]
        [view st/participants-container
         [new-participants-toolbar navigator]
         [list-view {:dataSource contacts-ds
                     :renderRow  (new-participants-row navigator)
                     :style      st/participants-list}]]))))
