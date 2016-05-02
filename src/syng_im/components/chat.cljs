(ns syng-im.components.chat
  (:require [clojure.string :as s]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.components.react :refer [android?
                                              view
                                              text
                                              image
                                              navigator
                                              touchable-highlight
                                              toolbar-android]]
            [syng-im.components.realm :refer [list-view]]
            [syng-im.components.styles :refer [font
                                               title-font
                                               color-white
                                               chat-background
                                               online-color
                                               selected-message-color
                                               separator-color
                                               text1-color
                                               text2-color]]
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
  [touchable-highlight {:on-press (:handler action)
                        :underlay-color :transparent}
   [view {:style {:flexDirection   "row"
                  :height          56}}
    [view {:width  56
           :height 56}
     [image {:source {:uri (:icon action)}
             :style  (merge (:icon-style action)
                            {:marginTop  21
                             :marginLeft 23})}]]
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

(defn actions-view [navigator chat]
  (when-let [actions (when (or true (and (:group-chat chat) ;; temp
                                         (:is-active chat)))
                       [{:title      "Add Contact to chat"
                         :icon       "icon_group"
                         :icon-style {:width  14
                                      :height 9}
                         :handler    #(dispatch [:show-add-participants navigator])}
                        {:title      "Remove Contact from chat"
                         :subtitle   "Alex, John"
                         :icon       "icon_search"
                         :icon-style {:width  14
                                      :height 9}
                         :handler    #(dispatch [:show-remove-participants navigator])}
                        {:title      "Leave Chat"
                         :icon       "icon_search"
                         :icon-style {:width  14
                                      :height 9}
                         :handler    #(dispatch [:leave-group-chat navigator])}])]
    [view nil
     [view {:style {:marginLeft 16
                    :height 1.5
                    :backgroundColor separator-color}}]
     [view {:style {:marginVertical  10}}
      (for [action actions]
        ^{:key action} [action-view action])]]))

(defn toolbar [navigator chat]
  (let [show-actions true]
    [view {:style {:flexDirection "column"
                   :backgroundColor color-white
                   :elevation       2}}
     [view {:style {:flexDirection   "row"
                    :height          56}}
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
      (when-not (:group-chat chat)
        [touchable-highlight {:on-press (fn []
                                          (nav-pop navigator))
                              :underlay-color :transparent}
         [view {:style {:width  56
                        :height 56}}
          [chat-photo {}]
          [contact-online {:online true}]]])]
     [actions-view navigator chat]]))

(defn chat [{:keys [navigator]}]
  (let [messages          (subscribe [:get-chat-messages])
        chat              (subscribe [:get-current-chat])]
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
         [toolbar navigator @chat]
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
           [chat-message-new])]))))
