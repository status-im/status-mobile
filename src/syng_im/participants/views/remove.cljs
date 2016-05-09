(ns syng-im.participants.views.remove
  (:require [re-frame.core :refer [subscribe dispatch]]
            [syng-im.resources :as res]
            [syng-im.components.react :refer [view text-input text image
                                              touchable-highlight]]
            [syng-im.components.realm :refer [list-view]]
            [syng-im.components.toolbar :refer [toolbar]]
            [syng-im.utils.listview :refer [to-realm-datasource]]
            [syng-im.participants.views.contact
             :refer [participant-contact]]
            [reagent.core :as r]
            [syng-im.participants.styles :as st]))

(defn remove-participants-toolbar [navigator]
  [toolbar
   {:navigator navigator
    :title     "Remove Participants"
    :action    {:handler #(dispatch [:remove-selected-participants navigator])
                :image   {:source res/trash-icon            ;; {:uri "icon_search"}
                          :style  st/remove-participants-image}}}])

(defn remove-participants-row
  [row _ _]
  (r/as-element
    [participant-contact (js->clj row :keywordize-keys true)]))

(defn remove-participants [{:keys [navigator]}]
  (let [contacts (subscribe [:current-chat-contacts])]
    (fn []
      (let [contacts-ds (to-realm-datasource @contacts)]
        [view st/participants-container
         [remove-participants-toolbar navigator]
         [list-view {:dataSource          contacts-ds
                     :enableEmptySections true
                     :renderRow           remove-participants-row
                     :style               st/participants-list}]]))))
