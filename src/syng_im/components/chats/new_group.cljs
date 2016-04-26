(ns syng-im.components.chats.new-group
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.resources :as res]
            [syng-im.components.react :refer [view
                                              toolbar-android
                                              android?
                                              text-input
                                              text
                                              image
                                              touchable-highlight]]
            [syng-im.components.styles :refer [font
                                               text1-color
                                               text2-color
                                               color-white
                                               color-purple]]
            [syng-im.components.realm :refer [list-view]]
            [syng-im.utils.listview :refer [to-realm-datasource]]
            [syng-im.components.chats.new-group-contact :refer [new-group-contact]]
            [reagent.core :as r]
            [syng-im.navigation :refer [nav-pop]]))

(defn new-group [{:keys [navigator]}]
  (let [contacts   (subscribe [:all-contacts])
        group-name (atom nil)]
    (fn []
      (let [contacts-ds (to-realm-datasource @contacts)]
        [view {:style {:flex            1
                       :flexDirection   "column"
                       :backgroundColor color-white}}
         (when android?
           ;; TODO add IOS version
           [toolbar-android {:navIcon          {:uri "icon_back"}
                             :style            {:backgroundColor color-white
                                                :height          56
                                                :elevation       2}
                             :onIconClicked    (fn []
                                                 (nav-pop navigator))
                             :actions          [{:title "Create"
                                                 ;; :icon  res/icon-ok
                                                 :show  "always"
                                                 :showWithText true}]
                             :onActionSelected (fn [position]
                                                 (dispatch [:create-new-group @group-name navigator]))}
            [view {:style {:flex            1
                           :alignItems      "center"
                           :justifyContent  "center"
                           :marginRight     112
                           :backgroundColor "transparent"}}
             [text {:style {:marginTop  -2.5
                            :color      text1-color
                            :fontSize   16
                            :fontFamily font}}
              "New group chat"]]])
         [view {:style {:marginHorizontal 16}}
          [text {:style {:marginTop    24
                         :marginBottom 16
                         :color        text2-color
                         :fontFamily   font
                         :fontSize     14
                         :lineHeight   20}}
           "Chat name"]
          [text-input {:underlineColorAndroid color-purple
                       :style                 {:marginLeft  -4
                                               :fontSize    14
                                               :fontFamily  font
                                               :color       text1-color}
                       :autoFocus             true
                       :placeholder           "Group Name"
                       :placeholderTextColor  text2-color
                       :onChangeText          (fn [new-text]
                                                (reset! group-name new-text)
                                                (r/flush))
                       :onSubmitEditing       (fn [e]
                                        ;(dispatch [:send-chat-msg @chat-id @text])
                                                (reset! group-name nil))}
           @group-name]
          [text {:style {:marginTop    24
                         :marginBottom 16
                         :color        text2-color
                         :fontFamily   font
                         :fontSize     14
                         :lineHeight   20}}
           "Members"]
          [touchable-highlight {:on-press (fn [])
                                :underlay-color :transparent}
           [view {:style {:flexDirection "row"
                          :marginBottom  16}}
            [image {:source {:uri "icon_add_gray"}
                    :style  {:marginVertical   19
                             :marginHorizontal 3
                             :width            17
                             :height           17}}]
            [text {:style {:marginTop    18
                           :marginLeft   32
                           :color        text2-color
                           :fontFamily   font
                           :fontSize     14
                           :lineHeight   20}}
             "Add members"]]]
          [list-view {:dataSource contacts-ds
                      :renderRow  (fn [row section-id row-id]
                                    (r/as-element [new-group-contact (js->clj row :keywordize-keys true) navigator]))
                      :style      {:backgroundColor "white"}}]]]))))
