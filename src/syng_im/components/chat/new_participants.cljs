(ns syng-im.components.chat.new-participants
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.resources :as res]
            [syng-im.components.react :refer [view android? text-input text image
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

(defn new-participants-toolbar [navigator]
  [toolbar {:navigator navigator
            :title     "Add Participants"
            :action    {:image {:source res/v ;; {:uri "icon_search"}
                                :style  {:width  20
                                         :height 18}}
                        :handler (fn []
                                   (dispatch [:add-new-participants navigator]))}}])

(defn new-participants [{:keys [navigator]}]
  (let [contacts (subscribe [:all-new-contacts])]
    (fn []
      (let [contacts-ds (to-realm-datasource @contacts)]
        [view {:style {:flex            1
                       :backgroundColor "white"}}
         [new-participants-toolbar navigator]
         [list-view {:dataSource contacts-ds
                     :renderRow  (fn [row section-id row-id]
                                   (r/as-element [new-participant-contact (js->clj row :keywordize-keys true) navigator]))
                     :style      {:backgroundColor "white"}}]]))))
