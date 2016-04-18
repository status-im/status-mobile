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
  (let [{:keys [text-color background-color]} (get contact-by-identity from)]
    (assoc msg :text-color text-color
               :background-color background-color)))

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

(defn chat [{:keys [navigator]}]
  (let [messages (subscribe [:get-chat-messages])
        chat     (subscribe [:get-current-chat])]
    (fn []
      (let [msgs                @messages
            ;_                 (log/debug "messages=" msgs)
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
                             :onIconClicked (fn []
                                              (nav-pop navigator))}
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
               (or (@chat :name)
                   "Chat name")]
              [text {:style {:marginTop  1
                             :color      text2-color
                             :fontSize   12
                             :fontFamily font}}
               "Active a minute ago"]]
             [view {:style {:position "absolute"
                            :top      10
                            :right    66}}
              [chat-photo {}]
              [contact-online {:online true}]]]])
         [list-view {:dataSource            datasource
                     :renderScrollComponent (fn [props]
                                              (invertible-scroll-view nil))
                     :renderRow             (fn [row section-id row-id]
                                              (let [msg (-> (js->clj row :keywordize-keys true)
                                                            (add-msg-color contact-by-identity))]
                                                (r/as-element [chat-message msg])))
                     :style                 {:backgroundColor "white"}}]
         [chat-message-new]]))))
