(ns syng-im.components.chat
  (:require [clojure.string :as s]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.components.react :refer [view
                                              text
                                              image
                                              navigator
                                              touchable-highlight
                                              toolbar-android
                                              list-view
                                              list-item
                                              android?]]
            [syng-im.components.styles :refer [font
                                               title-font
                                               color-white
                                               chat-background
                                               online-color
                                               selected-message-color
                                               separator-color
                                               text1-color
                                               text2-color
                                               toolbar-background1]]
            [syng-im.utils.logging :as log]
            [syng-im.resources :as res]
            [syng-im.constants :refer [content-type-status]]
            [syng-im.utils.listview :refer [to-datasource
                                            to-datasource2]]
            [syng-im.components.invertible-scroll-view :refer [invertible-scroll-view]]
            [syng-im.components.chat.chat-message :refer [chat-message]]
            [syng-im.components.chat.chat-message-new :refer [chat-message-new]]))


(defn contacts-by-identity [contacts]
  (->> contacts
       (map (fn [{:keys [identity] :as contact}]
              [identity contact]))
       (into {})))

(defn add-msg-color [{:keys [from] :as msg} contact-by-identity]
  (if (= "system" from)
    (assoc msg :text-color "#4A5258"
               :background-color "#D3EEEF")
    (let [{:keys [text-color background-color]} (get contact-by-identity from)]
      (assoc msg :text-color text-color
                 :background-color background-color))))

(defn chat-photo [{:keys [photo-path]}]
  [view {:margin       10
         :borderRadius 50}
   [image {:source (if (s/blank? photo-path)
                     res/user-no-photo
                     {:uri photo-path})
           :style  {:borderRadius 50
                    :width        36
                    :height       36}}]])

(defn contact-online [{:keys [online]}]
  (when online
    [view {:position        "absolute"
           :top             30
           :left            30
           :width           20
           :height          20
           :borderRadius    50
           :backgroundColor online-color
           :borderWidth     2
           :borderColor     color-white}
     [view {:position        "absolute"
            :top             6
            :left            3
            :width           4
            :height          4
            :borderRadius    50
            :backgroundColor color-white}]
     [view {:position        "absolute"
            :top             6
            :left            9
            :width           4
            :height          4
            :borderRadius    50
            :backgroundColor color-white}]]))

(defn typing [member]
  [view {:style {:width        260
                 :marginTop    10
                 :paddingLeft  8
                 :paddingRight 8
                 :alignItems   "flex-start"
                 :alignSelf    "flex-start"}}
   [view {:style {:borderRadius    14
                  :padding         12
                  :height          38
                  :backgroundColor selected-message-color}}
    [text {:style {:marginTop  -2
                   :fontSize   12
                   :fontFamily font
                   :color      text2-color}}
     (str member " is typing")]]])

(defn typing-all []
  [view {:style {:marginBottom 20}}
   (for [member ["Geoff" "Justas"]]
     ^{:key member} [typing member])])

(defn toolbar-content-chat [group-chat]
  (let
    [contacts (subscribe [:chat :contacts])
     name     (subscribe [:chat :name])]
    (fn [group-chat]
      [view {:style {:flex            1
                     :flexDirection   "row"
                     :backgroundColor "transparent"}}
       [view {:style {:flex           1
                      :alignItems     "flex-start"
                      :justifyContent "center"
                      :marginRight    112}}
        [text {:style {:marginTop  -2.5
                       :color      text1-color
                       :fontSize   16
                       :fontFamily font}}
         (or @name "Chat name")]
        (if group-chat
          [view {:style {:flexDirection "row"}}
           [image {:source {:uri :icon_group}
                   :style  {:marginTop 4
                            :width     14
                            :height    9}}]
           [text {:style {:marginTop  -0.5
                          :marginLeft 4
                          :fontFamily font
                          :fontSize   12
                          :color      text2-color}}
            (let [cnt (count @contacts)]
              (str cnt
                   (if (< 1 cnt)
                     ;; TODO https://github.com/r0man/inflections-clj
                     " members"
                     " member")
                   ", " cnt " active"))]]
          [text {:style {:marginTop  1
                         :color      text2-color
                         :fontSize   12
                         :fontFamily font}}
           "Active a minute ago"])]
       (when-not group-chat
         [view {:style {:position "absolute"
                        :top      10
                        :right    66}}
          [chat-photo {}]
          [contact-online {:online true}]])])))

(defn message-row [contact-by-identity group-chat]
  (fn [row _ _]
    (let [msg (-> row
                  (add-msg-color contact-by-identity)
                  (assoc :group-chat group-chat))]
      (list-item [chat-message msg]))))

(def group-caht-actions
  [{:title        "Add Contact to chat"
    :icon         res/add-icon
    :showWithText true}
   {:title        "Remove Contact from chat"
    :icon         res/trash-icon
    :showWithText true}
   {:title        "Leave Chat"
    :icon         res/leave-icon
    :showWithText true}])

(defn on-action-selected [position]
  (case position
    0 (dispatch [:show-add-participants #_navigator])
    1 (dispatch [:show-remove-participants #_navigator])
    2 (dispatch [:leave-group-chat #_navigator])))

(defn overlay [{:keys [on-click-outside]} items]
  [view {:position :absolute
         :top      0
         :bottom   0
         :left     0
         :right    0}
   [touchable-highlight {:on-press       on-click-outside
                         :underlay-color :transparent
                         :style          {:flex 1}}
    [view nil]]
   items])

(defn action-view [action]
  [touchable-highlight {:on-press (fn []
                                    (dispatch [:set-show-actions false])
                                    ((:handler action)))
                        :underlay-color :transparent}
   [view {:style {:flexDirection   "row"
                  :height          56}}
    [view {:width  56
           :height 56
           :alignItems "center"
           :justifyContent "center"}
     [image {:source {:uri (:icon action)}
             :style  (:icon-style action)}]]
    [view {:style {:flex 1
                   :alignItems "flex-start"
                   :justifyContent "center"}}
     [text {:style {:marginTop  -2.5
                    :color      text1-color
                    :fontSize   14
                    :fontFamily font}}
      (:title action)]
     (when-let [subtitle (:subtitle action)]
       [text {:style {:marginTop  1
                      :color      text2-color
                      :fontSize   12
                      :fontFamily font}}
        subtitle])]]])

(defn actions-list-view []
  (let [{:keys [group-chat active]}
        (subscribe [:chat-properties [:group-chat :name :contacts :active]])]
    (when-let [actions (when (and @group-chat @active)
                         [{:title      "Add Contact to chat"
                           :icon       :icon_menu_group
                           :icon-style {:width  25
                                        :height 19}
                           :handler    nil #_#(dispatch [:show-add-participants
                                                         navigator])}
                          {:title      "Remove Contact from chat"
                           :subtitle   "Alex, John"
                           :icon       :icon_search_gray_copy
                           :icon-style {:width  17
                                        :height 17}
                           :handler    nil #_#(dispatch
                                               [:show-remove-participants navigator])}
                          {:title      "Leave Chat"
                           :icon       :icon_muted
                           :icon-style {:width  18
                                        :height 21}
                           :handler    nil #_#(dispatch [:leave-group-chat
                                                         navigator])}
                          {:title      "Settings"
                           :subtitle   "Not implemented"
                           :icon       :icon_settings
                           :icon-style {:width  20
                                        :height 13}
                           :handler    (fn [])}])]
      [view {:style {:backgroundColor toolbar-background1
                     :elevation       2
                     :position        :absolute
                     :top             56
                     :left            0
                     :right           0}}
       [view {:style {:marginLeft      16
                      :height          1.5
                      :backgroundColor separator-color}}]
       [view {:style {:marginVertical 10}}
        (for [action actions]
          ^{:key action} [action-view action])]])))

(defn actions-view []
  [overlay {:on-click-outside #(dispatch [:set-show-actions false])}
   [actions-list-view]])

(defn toolbar []
  (let [{:keys [group-chat name contacts]}
        (subscribe [:chat-properties [:group-chat :name :contacts]])
        show-actions (subscribe [:show-actions])]
    (fn []
      [view {:style {:flexDirection   "row"
                     :height          56
                     :backgroundColor toolbar-background1
                     :elevation       2}}
       (when (not @show-actions)
         [touchable-highlight {:on-press       #(dispatch [:navigate-back])
                               :underlay-color :transparent}
          [view {:width  56
                 :height 56}
           [image {:source {:uri "icon_back"}
                   :style  {:marginTop  21
                            :marginLeft 23
                            :width      8
                            :height     14}}]]])
       [view {:style {:flex           1
                      :marginLeft     (if @show-actions 16 0)
                      :alignItems     "flex-start"
                      :justifyContent "center"}}
        [text {:style {:marginTop  -2.5
                       :color      text1-color
                       :fontSize   16
                       :fontFamily font}}
         (or @name "Chat name")]
        (if @group-chat
          [view {:style {:flexDirection :row}}
           [image {:source {:uri :icon_group}
                   :style  {:marginTop 4
                            :width     14
                            :height    9}}]
           [text {:style {:marginTop  -0.5
                          :marginLeft 4
                          :fontFamily font
                          :fontSize   12
                          :color      text2-color}}
            (let [cnt (count @contacts)]
              (str cnt
                   (if (< 1 cnt)
                     " members"
                     " member")
                   ", " cnt " active"))]]
          [text {:style {:marginTop  1
                         :color      text2-color
                         :fontSize   12
                         :fontFamily font}}
           "Active a minute ago"])]
       (if @show-actions
         [touchable-highlight
          {:on-press       #(dispatch [:set-show-actions false])
           :underlay-color :transparent}
          [view {:style {:width  56
                         :height 56}}
           [image {:source {:uri :icon_up}
                   :style  {:marginTop  23
                            :marginLeft 21
                            :width      14
                            :height     8}}]]]
         [touchable-highlight
          {:on-press       #(dispatch [:set-show-actions true])
           :underlay-color :transparent}
          [view {:style {:width  56
                         :height 56}}
           [chat-photo {}]
           [contact-online {:online true}]]])])))

(defn messages-view [group-chat]
  (let [messages (subscribe [:chat :messages])
        contacts (subscribe [:chat :contacts])]
    (fn [group-chat]
      (let [contacts' (contacts-by-identity @contacts)]
        [list-view {:renderRow             (message-row contacts' group-chat)
                    :renderScrollComponent #(invertible-scroll-view (js->clj %))
                    :onEndReached          #(dispatch [:load-more-messages])
                    :dataSource            (to-datasource2 @messages)}]))))

(defn chat []
  (let [is-active         (subscribe [:chat :is-active])
        group-chat        (subscribe [:chat :group-chat])
        show-actions-atom (subscribe [:show-actions])]
    (fn []
      [view {:style {:flex            1
                     :backgroundColor chat-background}}
       [toolbar]
       [messages-view @group-chat]
       (when @group-chat [typing-all])
       (when is-active [chat-message-new])
       (when @show-actions-atom [actions-view])])))
