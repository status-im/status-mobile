(ns syng-im.components.chat.remove-participants
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.resources :as res]
            [syng-im.components.react :refer [view text-input text image
                                              touchable-highlight]]
            [syng-im.components.realm :refer [list-view]]
            [syng-im.components.styles :refer [font
                                               title-font
                                               color-white
                                               color-black
                                               color-blue
                                               text1-color
                                               text2-color
                                               toolbar-background1]]
            [syng-im.components.toolbar :refer [toolbar]]
            [syng-im.utils.listview :refer [to-realm-datasource]]
            [syng-im.components.chats.new-participant-contact :refer [new-participant-contact]]
            [reagent.core :as r]
            [syng-im.navigation :refer [nav-pop]]))

(defn remove-participants-toolbar [navigator]
  [toolbar {:navigator navigator
            :title     "Remove Participants"
            :action    {:image {:source res/trash-icon ;; {:uri "icon_search"}
                                :style  {:width  22
                                         :height 30}}
                        :handler (fn []
                                   (dispatch [:remove-selected-participants navigator]))}}])

(defn remove-participants [{:keys [navigator]}]
  (let [contacts (subscribe [:current-chat-contacts])]
    (fn []
      (let [contacts-ds (to-realm-datasource @contacts)]
        [view {:style {:flex            1
                       :backgroundColor "white"}}
         [remove-participants-toolbar navigator]
         [list-view {:dataSource contacts-ds
                     :renderRow  (fn [row section-id row-id]
                                   (r/as-element [new-participant-contact (js->clj row :keywordize-keys true) navigator]))
                     :style      {:backgroundColor "white"}}]]))))
