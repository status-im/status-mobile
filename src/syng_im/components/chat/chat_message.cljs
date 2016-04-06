(ns syng-im.components.chat.chat-message
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.components.react :refer [android?
                                              view
                                              text
                                              image
                                              touchable-highlight
                                              navigator
                                              toolbar-android]]
            [syng-im.models.commands :as commands]
            [syng-im.utils.logging :as log]
            [syng-im.navigation :refer [nav-pop]]
            [syng-im.resources :as res]
            [syng-im.constants :refer [text-content-type
                                       content-type-command
                                       content-type-command-request]]))


(defn message-date [{:keys [date]}]
  [text {:style {:marginVertical 10
                 :fontFamily     "Avenir-Roman"
                 :fontSize       11
                 :color          "#AAB2B2"
                 :letterSpacing  1
                 :lineHeight     15
                 :textAlign      "center"
                 :opacity        0.8}}
   date])

(defn message-content-audio [{:keys [content-type content-type]}]
  [view {:style {:flexDirection "row"
                 :alignItems    "center"}}
   [view {:style {:width        33
                  :height       33
                  :borderRadius 50
                  :elevation    1}}
    [image {:source res/play
            :style  {:width  33
                     :height 33}}]]
   [view {:style {:marginTop  10
                  :marginLeft 10
                  :width      120
                  :height     26
                  :elevation  1}}
    [view {:style {:position        "absolute"
                   :top             4
                   :width           120
                   :height          2
                   :backgroundColor "#EC7262"}}]
    [view {:style {:position        "absolute"
                   :left            0
                   :top             0
                   :width           2
                   :height          10
                   :backgroundColor "#4A5258"}}]
    [text {:style {:position      "absolute"
                   :left          1
                   :top           11
                   :fontFamily    "Avenir-Roman"
                   :fontSize      11
                   :color         "#4A5258"
                   :letterSpacing 1
                   :lineHeight    15}}
     "03:39"]]])


(defn message-content-command [content]
  (let [{:keys [command content]} (commands/parse-command-msg-content content)]
    [view {:style {:flexDirection "column"}}
     [view {:style {:marginTop       -5
                    :marginLeft      0
                    :backgroundColor (:color command)
                    :borderRadius    10}}
      [text {:style {:marginTop        0
                     :marginHorizontal 10
                     :fontSize         14
                     :fontFamily       "Avenir-Roman"
                     :color            "white"}}
       (:text command)]]
     [text {:style {:marginTop        5
                    :marginHorizontal 0
                    :fontSize         14
                    :fontFamily       "Avenir-Roman"
                    :color           "black"}}
      ;; TODO isn't smart
      (if (= (:command command) :keypair-password)
        "******"
        content)]]))

(defn set-chat-command [command]
  (dispatch [:set-chat-command (:command command)]))

(defn message-content-command-request [content outgoing]
  (let [{:keys [command content]} (commands/parse-command-request-msg-content content)]
    [view {:style {:marginTop 10}}
     [view {:style (merge {:borderRadius      6
                           :paddingVertical   12
                           :paddingHorizontal 16}
                          (if outgoing
                            {:backgroundColor "#D3EEEF"}
                            {:backgroundColor "#FBF6E3"}))}
      [text {:style {:fontSize   14
                     :fontFamily "Avenir-Roman"
                     :color      "#4A5258"}}
       content]]
     [touchable-highlight {:style {:position    "absolute"
                                   :top         -15
                                   :left        20}
                           :onPress (fn []
                                      (set-chat-command command))}
      [view {:style {:width           30
                     :height          30
                     :borderRadius    50
                     :backgroundColor (:color command)}}
       [image {:source res/att
               :style  {:width  17
                        :height 14
                        :position "absolute"
                        :top 8
                        :left 6}}]]]]))

(defn message-content [{:keys [content-type content outgoing]}]
  (if (= content-type content-type-command-request)
    [message-content-command-request content outgoing]
    [view {:style (merge {:borderRadius 6}
                         (if (= content-type text-content-type)
                           {:paddingVertical   12
                            :paddingHorizontal 16}
                           {:paddingVertical   14
                            :paddingHorizontal 10})
                         (if outgoing
                           {:backgroundColor "#D3EEEF"}
                           {:backgroundColor "#FBF6E3"}))}
     (cond
       (= content-type text-content-type)
       [text {:style {:fontSize   14
                      :fontFamily "Avenir-Roman"
                      :color      "#4A5258"}}
        content]
       (= content-type content-type-command)
       [message-content-command content]
       :else [message-content-audio {:content      content
                                     :content-type content-type}])]))

(defn message-delivery-status [{:keys [delivery-status]}]
  [view {:style {:flexDirection "row"
                 :marginTop     2}}
   [image {:source (case delivery-status
                     :delivered res/delivered-icon
                     :seen res/seen-icon
                     :failed res/delivery-failed-icon)
           :style  {:marginTop 6
                    :opacity   0.6}}]
   [text {:style {:fontFamily "Avenir-Roman"
                  :fontSize   11
                  :color      "#AAB2B2"
                  :opacity    0.8
                  :marginLeft 5}}
    (case delivery-status
      :delivered "Delivered"
      :seen "Seen"
      :failed "Failed")]])

(defn message-body [{:keys [msg-id content content-type outgoing delivery-status]}]
  [view {:style (merge {:flexDirection  "column"
                        :width          260
                        :marginVertical 5}
                       (if outgoing
                         {:alignSelf  "flex-end"
                          :alignItems "flex-end"}
                         {:alignSelf  "flex-start"
                          :alignItems "flex-start"}))}
   [message-content {:content-type content-type
                     :content      content
                     :outgoing     outgoing}]
   (when (and outgoing delivery-status)
     [message-delivery-status {:delivery-status delivery-status}])])

(defn chat-message [{:keys [msg-id content content-type outgoing delivery-status date new-day] :as msg}]
  [view {:paddingHorizontal 15}
   (when new-day
     [message-date {:date date}])
   [message-body {:msg-id          msg-id
                  :content         content
                  :content-type    content-type
                  :outgoing        outgoing
                  :delivery-status (keyword delivery-status)}]])
