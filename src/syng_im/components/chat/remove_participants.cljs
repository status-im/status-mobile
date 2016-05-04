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
            [syng-im.utils.listview :refer [to-realm-datasource]]
            [syng-im.components.chats.new-participant-contact :refer [new-participant-contact]]
            [reagent.core :as r]
            [syng-im.navigation :refer [nav-pop]]))

(defn toolbar [navigator]
  [view {:style {:flexDirection   "row"
                 :backgroundColor toolbar-background1
                 :height          56
                 :elevation       2}}
   [touchable-highlight {:on-press (fn []
                                     (nav-pop navigator))
                         :underlay-color :transparent}
    [view {:width  56
           :height 56}
     [image {:source {:uri "icon_back"}
             :style  {:marginTop  21
                      :marginLeft 23
                      :width      8
                      :height     14}}]]]
   [view {:style {:flex 1
                  :alignItems "center"
                  :justifyContent "center"}}
    [text {:style {:marginTop  -2.5
                   :color      text1-color
                   :fontSize   16
                   :fontFamily font}}
     "Remove Participants"]]
   [touchable-highlight {:on-press (fn []
                                     )
                         :underlay-color :transparent}
    [view {:width  56
           :height 56}
     [image {:source res/trash-icon ;; {:uri "icon_search"}
             :style  {:marginTop 13
                      :marginHorizontal 17
                      :width  22
                      :height 30}}]]]])

(defn remove-participants [{:keys [navigator]}]
  (let [contacts (subscribe [:current-chat-contacts])]
    (fn []
      (let [contacts-ds (to-realm-datasource @contacts)]
        [view {:style {:flex            1
                       :backgroundColor "white"}}
         [toolbar navigator]
         [list-view {:dataSource contacts-ds
                     :renderRow  (fn [row section-id row-id]
                                   (r/as-element [new-participant-contact (js->clj row :keywordize-keys true) navigator]))
                     :style      {:backgroundColor "white"}}]]))))
