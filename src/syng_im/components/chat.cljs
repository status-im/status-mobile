(ns syng-im.components.chat
  (:require [clojure.string :as s]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.components.react :refer [android?
                                              view
                                              text
                                              image
                                              touchable-highlight
                                              navigator
                                              toolbar-android]]
            [syng-im.components.realm :refer [list-view]]
            [syng-im.components.styles :refer [font
                                               title-font
                                               color-white
                                               chat-background
                                               online-color
                                               selected-message-color
                                               text1-color
                                               text2-color]]
            [syng-im.utils.logging :as log]
            [syng-im.navigation :refer [nav-pop]]
            [syng-im.resources :as res]
            [syng-im.constants :refer [content-type-status]]
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
  [view {:borderRadius 50}
   [image {:source (if (s/blank? photo-path)
                     res/user-no-photo
                     {:uri photo-path})
           :style  {:borderRadius 50
                    :width        36
                    :height       36}}]])

(defn contact-online [{:keys [online]}]
  (when online
    [view {:position        "absolute"
           :top             20
           :left            20
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
                 :paddingTop    2
                 :paddingBottom 8
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
  [view {:style {:marginBottom 12}}
   (for [member ["Geoff" "Justas"]]
     ^{:key member} [typing member])])

(defn toolbar-content-chat [chat]
  (let [group? (:group-chat chat)]
    [view {:style {:flex 1
                   :flexDirection "row"
                   :backgroundColor "transparent"}}
     [view {:style {:flex 1
                    :alignItems "flex-start"
                    :justifyContent "center"
                    :marginRight 112}}
      [text {:style {:marginTop  -2.5
                     :color      text1-color
                     :fontSize   16
                     :fontFamily font}}
       (or (chat :name)
           "Chat name")]
      (if group?
        [view {:style {:flexDirection "row"}}
         [image {:source res/icon-group
                 :style  {:marginTop 4}}]
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
     [view {:style {:position "absolute"
                    :top      10
                    :right    66}}
      [chat-photo {}]
      (when (not group?)
        [contact-online {:online true}])]]))

(defn chat [{:keys [navigator]}]
  (let [messages (subscribe [:get-chat-messages])
        chat     (subscribe [:get-current-chat])]
    (fn []
      (let [msgs                @messages
                                        ;_                 (log/debug "messages=" msgs)
            ;; temp to show first status
            msgs-clj (assoc (js->clj msgs) "-1"
                            {:msg-id "-1"
                             :content (str "The brash businessman’s braggadocio "
                                           "and public exchange with candidates "
                                           "in the US presidential election")
                             :delivery-status "seen"
                             :from "Status"
                             :chat-id "-"
                             :content-type content-type-status
                             :timestamp 1
                             :outgoing false
                             :to nil})
            msgs (clj->js msgs-clj)
            ;; end temp
            datasource          (to-realm-datasource msgs)
            contacts            (:contacts @chat)
            contact-by-identity (contacts-by-identity contacts)]
        [view {:style {:flex            1
                       :backgroundColor chat-background}}
         (when android?
           ;; TODO add IOS version
           [toolbar-android {:navIcon       res/icon-back
                             :style         {:backgroundColor color-white
                                             :height          56
                                             :elevation       2}
                             :actions          (when (and (:group-chat @chat)
                                                          (:is-active @chat))
                                                 [{:title        "Add Contact to chat"
                                                   :icon         res/add-icon
                                                   :showWithText true}
                                                  {:title        "Remove Contact from chat"
                                                   :icon         res/trash-icon
                                                   :showWithText true}
                                                  {:title        "Leave Chat"
                                                   :icon         res/leave-icon
                                                   :showWithText true}])
                             :onActionSelected (fn [position]
                                                 (case position
                                                   0 (dispatch [:show-add-participants navigator])
                                                   1 (dispatch [:show-remove-participants navigator])
                                                   2 (dispatch [:leave-group-chat navigator])))
                             :onIconClicked    (fn []
                                                 (nav-pop navigator))}
            [toolbar-content-chat @chat]])
         [list-view {:dataSource            datasource
                     :renderScrollComponent (fn [props]
                                              (invertible-scroll-view nil))
                     :renderRow             (fn [row section-id row-id]
                                              (let [msg (-> (js->clj row :keywordize-keys true)
                                                            (add-msg-color contact-by-identity)
                                                            (assoc :group-chat (:group-chat @chat)))]
                                                (r/as-element [chat-message msg])))
                     :style                 {:backgroundColor "white"}}]
         (when (:group-chat @chat)
           [typing-all])
         (when (:is-active @chat)
           [chat-message-new])]))))
