(ns syng-im.components.chat.chat-message
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.components.react :refer [android?
                                              view
                                              text
                                              image
                                              touchable-highlight
                                              navigator
                                              toolbar-android]]
            [syng-im.components.styles :refer [font]]
            [syng-im.models.commands :refer [parse-command-msg-content
                                             parse-command-request-msg-content]]
            [syng-im.utils.logging :as log]
            [syng-im.navigation :refer [nav-pop]]
            [syng-im.resources :as res]
            [syng-im.constants :refer [text-content-type
                                       content-type-command
                                       content-type-command-request]]))

(defn message-date [{:keys [date]}]
  [text {:style {:marginVertical 10
                 :fontFamily     font
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
                   :fontFamily    font
                   :fontSize      11
                   :color         "#4A5258"
                   :letterSpacing 1
                   :lineHeight    15}}
     "03:39"]]])


(defn message-content-command [content]
  (let [commands-atom (subscribe [:get-commands])]
    (fn [content]
      (let [commands @commands-atom
            {:keys [command content]} (parse-command-msg-content commands content)]
        [view {:style {:flexDirection "column"}}
         [view {:style {:flexDirection "row"
                        :marginRight 32}}
          [view {:style {:backgroundColor   (:color command)
                         :height            24
                         :borderRadius      50
                         :paddingTop        2
                         :paddingHorizontal 12}}
           [text {:style {:fontSize         12
                          :fontFamily       font
                          :color            "white"}}
            (:text command)]]]
         [image {:source (:icon command)
                 :style {:position "absolute"
                         :top      4
                         :right    0}}]
         [text {:style {:marginTop        5
                        :marginHorizontal 0
                        :fontSize         14
                        :fontFamily       font
                        :color            "black"}}
          ;; TODO isn't smart
          (if (= (:command command) :keypair-password)
            "******"
            content)]]))))

(defn set-chat-command [msg-id command]
  (dispatch [:set-response-chat-command msg-id (:command command)]))

(defn message-content-command-request [msg-id content outgoing text-color background-color]
  (let [commands-atom (subscribe [:get-commands])]
    (fn [msg-id content outgoing text-color background-color]
      (let [commands @commands-atom
            {:keys [command content]} (parse-command-request-msg-content commands content)]
        [touchable-highlight {:onPress (fn []
                                         (set-chat-command msg-id command))}
         [view {:style {:paddingRight 16}}
          [view {:style (merge {:borderRadius 7
                                :padding      12}
                               (if outgoing
                                 {:backgroundColor "white"}
                                 {:backgroundColor background-color}))}
           [text {:style (merge {:fontSize   14
                                 :fontFamily font}
                                (if outgoing
                                  {:color "#4A5258"}
                                  {:color text-color}))}
            content]]
          [view {:style {:position        "absolute"
                         :top             12
                         :right           0
                         :width           32
                         :height          32
                         :borderRadius    50
                         :backgroundColor (:color command)}}
           [image {:source (:request-icon command)
                   :style  {:position "absolute"
                            :top      9
                            :left     10}}]]
          [text {:style {:marginTop  2
                         :fontFamily font
                         :fontSize   12
                         :color      "#AAB2B2"
                         :opacity    0.8 }}
           (str (:request-text command))]]]))))

(defn message-content [{:keys [msg-id content-type content outgoing text-color background-color]}]
  (if (= content-type content-type-command-request)
    [message-content-command-request msg-id content outgoing text-color background-color]
    [view {:style (merge {:borderRadius 7
                          :padding 12}
                         (if outgoing
                           {:backgroundColor "white"}
                           {:backgroundColor background-color}))}
     (cond
       (= content-type text-content-type)
       [text {:style (merge {:fontSize   14
                             :fontFamily font}
                            (if outgoing
                              {:color "#4A5258"}
                              {:color text-color}))}
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
   [text {:style {:fontFamily font
                  :fontSize   12
                  :color      "#AAB2B2"
                  :opacity    0.8
                  :marginLeft 5}}
    (case delivery-status
      :delivered "Delivered"
      :seen "Seen"
      :failed "Failed")]])

(defn message-body [{:keys [msg-id content content-type outgoing delivery-status text-color background-color]}]
  (let [delivery-status :seen]
   [view {:style (merge {:flexDirection "column"
                         :width         260
                         :paddingTop    5
                         :paddingRight  8
                         :paddingBottom 5
                         :paddingLeft   8}
                        (if outgoing
                          {:alignSelf  "flex-end"
                           :alignItems "flex-end"}
                          {:alignSelf  "flex-start"
                           :alignItems "flex-start"}))}
    [message-content {:msg-id           msg-id
                      :content-type     content-type
                      :content          content
                      :outgoing         outgoing
                      :text-color       text-color
                      :background-color background-color}]
    (when (and outgoing delivery-status)
      [message-delivery-status {:delivery-status delivery-status}])]))

(defn chat-message [{:keys [msg-id content content-type outgoing delivery-status date new-day text-color background-color] :as msg}]
  [view {}
   (when new-day
     [message-date {:date date}])
   [message-body {:msg-id           msg-id
                  :content          content
                  :content-type     content-type
                  :outgoing         outgoing
                  :text-color       "black" ;text-color
                  :background-color "white" ;background-color
                  :delivery-status  (keyword delivery-status)}]])
