(ns syng-im.components.chat
  (:require [clojure.string :as s]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.components.react :refer [view
                                              text
                                              image
                                              navigator
                                              touchable-highlight]]
            [syng-im.components.realm :refer [list-view]]
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
            [syng-im.navigation :refer [nav-pop]]
            [syng-im.resources :as res]
            [syng-im.utils.listview :refer [to-realm-datasource]]
            [syng-im.components.invertible-scroll-view :refer [invertible-scroll-view]]
            [reagent.core :as r]
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
  [view {:style {:width         260
                 :marginTop     10
                 :paddingLeft   8
                 :paddingRight  8
                 :alignItems    "flex-start"
                 :alignSelf     "flex-start"}}
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

(defn actions-list-view [navigator chat]
  (when-let [actions (if (:group-chat chat)
                       [{:title      "Add Contact to chat"
                         :icon       "icon_menu_group"
                         :icon-style {:width  25
                                      :height 19}
                         :handler    #(dispatch [:show-add-participants navigator])}
                        {:title      "Remove Contact from chat"
                         :subtitle   "Alex, John"
                         :icon       "icon_search_gray_copy"
                         :icon-style {:width  17
                                      :height 17}
                         :handler    #(dispatch [:show-remove-participants navigator])}
                        {:title      "Leave Chat"
                         :icon       "icon_muted"
                         :icon-style {:width  18
                                      :height 21}
                         :handler    #(dispatch [:leave-group-chat navigator])}
                        {:title      "Settings"
                         :subtitle   "Not implemented"
                         :icon       "icon_settings"
                         :icon-style {:width  20
                                      :height 13}
                         :handler    (fn [] )}]
                       [{:title      "Profile"
                         :icon       "icon_menu_group"
                         :icon-style {:width  25
                                      :height 19}
                         :handler    #(dispatch [:show-profile navigator (:chat-id chat)])}])]
    [view {:style {:backgroundColor toolbar-background1
                   :elevation       2
                   :position        "absolute"
                   :top             56
                   :left            0
                   :right           0}}
     [view {:style {:marginLeft      16
                    :height          1.5
                    :backgroundColor separator-color}}]
     [view {:style {:marginVertical  10}}
      (for [action actions]
        ^{:key action} [action-view action])]]))

(defn overlay [{:keys [on-click-outside]} items]
  [view {:position "absolute"
         :top      0
         :bottom   0
         :left     0
         :right    0}
   [touchable-highlight {:on-press       on-click-outside
                         :underlay-color :transparent
                         :style          {:flex 1}}
    [view nil]]
   items])

(defn actions-view [navigator chat]
  [overlay {:on-click-outside (fn []
                                (dispatch [:set-show-actions false]))}
   [actions-list-view navigator chat]])

(defn toolbar [navigator chat show-actions]
  [view {:style {:flexDirection   "row"
                 :height          56
                 :backgroundColor toolbar-background1
                 :elevation       2}}
   (when (not show-actions)
     [touchable-highlight {:on-press (fn []
                                       (nav-pop navigator))
                           :underlay-color :transparent}
      [view {:width  56
             :height 56}
       [image {:source {:uri "icon_back"}
               :style  {:marginTop  21
                        :marginLeft 23
                        :width      8
                        :height     14}}]]])
   [view {:style {:flex 1
                  :marginLeft (if show-actions 16 0)
                  :alignItems "flex-start"
                  :justifyContent "center"}}
    [text {:style {:marginTop  -2.5
                   :color      text1-color
                   :fontSize   16
                   :fontFamily font}}
     (or (chat :name)
         "Chat name")]
    (if (:group-chat chat)
      [view {:style {:flexDirection "row"}}
       [image {:source {:uri "icon_group"}
               :style  {:marginTop 4
                        :width     14
                        :height    9}}]
       [text {:style {:marginTop  -0.5
                      :marginLeft 4
                      :fontFamily font
                      :fontSize   12
                      :color      text2-color}}
        (str (count (:contacts chat))
             (if (< 1 (count (:contacts chat)))
               " members"
               " member")
             ", " (count (:contacts chat)) " active")]]
      [text {:style {:marginTop  1
                     :color      text2-color
                     :fontSize   12
                     :fontFamily font}}
       "Active a minute ago"])]
   (if show-actions
     [touchable-highlight {:on-press (fn []
                                       (dispatch [:set-show-actions false]))
                           :underlay-color :transparent}
      [view {:style {:width  56
                     :height 56}}
       [image {:source {:uri "icon_up"}
               :style  {:marginTop  23
                        :marginLeft 21
                        :width      14
                        :height     8}}]]]
     [touchable-highlight {:on-press (fn []
                                       (dispatch [:set-show-actions true]))
                           :underlay-color :transparent}
      [view {:style {:width  56
                     :height 56}}
       [chat-photo {}]
       [contact-online {:online true}]]])])

(defn chat [{:keys [navigator]}]
  (let [messages          (subscribe [:get-chat-messages])
        chat              (subscribe [:get-current-chat])
        show-actions-atom (subscribe [:show-actions])]
    (fn []
      (let [msgs                @messages
                                        ;_                 (log/debug "messages=" msgs)
            ;; temp
            typing (:group-chat @chat)
            ;; end temp
            datasource          (to-realm-datasource msgs)
            contacts            (:contacts @chat)
            contact-by-identity (contacts-by-identity contacts)]
        [view {:style {:flex            1
                       :backgroundColor chat-background}}
         [toolbar navigator @chat @show-actions-atom]
         (let [last-msg-id (:last-msg-id @chat)]
           [list-view {:dataSource            datasource
                       :renderScrollComponent (fn [props]
                                                (invertible-scroll-view (js->clj props)))
                       :renderRow             (fn [row section-id row-id]
                                                (let [msg (-> (js->clj row :keywordize-keys true)
                                                              (add-msg-color contact-by-identity)
                                                              (assoc :group-chat (:group-chat @chat))
                                                              (assoc :typing typing))]
                                                  (r/as-element [chat-message msg last-msg-id])))}])
         (when (:group-chat @chat)
           [typing-all])
         (when (:is-active @chat)
           [chat-message-new])
         (when @show-actions-atom
           [actions-view navigator @chat])]))))
