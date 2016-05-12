(ns syng-im.components.group-settings
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.resources :as res]
            [syng-im.components.react :refer [view
                                              text-input
                                              text
                                              image
                                              touchable-highlight]]
            [syng-im.components.styles :refer [font
                                               title-font
                                               color-white
                                               color-purple
                                               text1-color
                                               text2-color
                                               toolbar-background1]]
            [syng-im.components.toolbar :refer [toolbar]]
            [syng-im.components.realm :refer [list-view]]
            [syng-im.utils.listview :refer [to-realm-datasource]]
            [syng-im.components.contact-list.contact-inner :refer [contact-inner-view]]
            [syng-im.components.chats.new-group-contact :refer [new-group-contact]]
            [reagent.core :as r]))

(defn set-group-settings-name [chat-name]
  (dispatch [:set-group-settings-name chat-name]))

(defn chat-members [members]
  [view {:style {:marginBottom 10}}
   (for [member members]
     ^{:key member} [contact-inner-view member]
     ;; [new-group-contact member nil]
     )])

(defn new-group-toolbar [chat-name]
  [toolbar {:title  "Chat settings"
            :action {:image {:source res/v ;; {:uri "icon_search"}
                             :style  {:width  20
                                      :height 18}}
                     :handler (fn []
                                (dispatch [:save-group-chat chat-name]))}}])

(defn group-settings []
  (let [chat-name (subscribe [:group-settings-name])
        members   (subscribe [:group-settings-members])]
    (fn []
      [view {:style {:flex            1
                     :flexDirection   "column"
                     :backgroundColor color-white}}
       [new-group-toolbar @chat-name]
       [view {:style {:marginHorizontal 16}}
        [text {:style {:marginTop    24
                       :marginBottom 16
                       :color        text2-color
                       :fontFamily   font
                       :fontSize     14
                       :lineHeight   20}}
         "Chat name"]
        [text-input {:underlineColorAndroid color-purple
                     :style                 {:marginLeft -4
                                             :fontSize   14
                                             :fontFamily font
                                             :color      text1-color}
                     :autoFocus             true
                     :placeholderTextColor  text2-color
                     :onChangeText          set-group-settings-name}
         @chat-name]
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
        [chat-members (vals (js->clj @members :keywordize-keys true))]
        [text {:style {:marginTop    24
                       :marginBottom 16
                       :color        text2-color
                       :fontFamily   font
                       :fontSize     14
                       :lineHeight   20}}
         "Settings"]]])))
