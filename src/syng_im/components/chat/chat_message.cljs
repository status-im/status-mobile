(ns syng-im.components.chat.chat-message
  (:require [clojure.string :as s]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.components.react :refer [android?
                                              view
                                              text
                                              image
                                              touchable-highlight
                                              navigator
                                              toolbar-android]]
            [syng-im.components.styles :refer [font
                                               color-light-blue-transparent
                                               color-white
                                               color-black
                                               color-blue
                                               selected-message-color
                                               online-color
                                               text1-color
                                               text2-color]]
            [syng-im.models.commands :refer [parse-command-msg-content
                                             parse-command-request-msg-content]]
            [syng-im.utils.logging :as log]
            [syng-im.navigation :refer [nav-pop]]
            [syng-im.resources :as res]
            [syng-im.constants :refer [text-content-type
                                       content-type-status
                                       content-type-command
                                       content-type-command-request]]))

(defn message-date [{:keys [date]}]
  [view {:style {:backgroundColor   color-light-blue-transparent
                 :height            24
                 :borderRadius      50
                 :alignSelf         "center"
                 :marginVertical    15
                 :paddingTop        3
                 :paddingHorizontal 12}}
   [text {:style {:fontFamily     font
                  :fontSize       12
                  :color          text2-color
                  :textAlign      "center"}}
    date]])

(defn contact-photo [{:keys [photo-path]}]
  [view {:borderRadius 50}
   [image {:source (if (s/blank? photo-path)
                     res/user-no-photo
                     {:uri photo-path})
           :style  {:borderRadius 50
                    :width        64
                    :height       64}}]])

(defn contact-online [{:keys [online]}]
  (when online
    [view {:position        "absolute"
           :top             44
           :left            44
           :width           24
           :height          24
           :borderRadius    50
           :backgroundColor online-color
           :borderWidth     2
           :borderColor     color-white}
     [view {:position        "absolute"
            :top             8
            :left            5
            :width           4
            :height          4
            :borderRadius    50
            :backgroundColor color-white}]
     [view {:position        "absolute"
            :top             8
            :left            11
            :width           4
            :height          4
            :borderRadius    50
            :backgroundColor color-white}]]))


(defn message-content-status [from content]
  [view {:style {:flex         1
                 :marginBottom 20
                 :alignSelf    "center"
                 :alignItems   "center"
                 :width        249}}
   [view {:style {:marginTop 20}}
    [contact-photo {}]
    [contact-online {:online true}]]
   [text {:style {:marginTop 20
                  :fontSize   18
                  :fontFamily font
                  :color text1-color}}
    from]
   [text {:style {:marginTop  10
                  :fontFamily font
                  :fontSize   14
                  :lineHeight 20
                  :textAlign  "center"
                  :color      text2-color}}
    content]])

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
                         :paddingTop        3
                         :paddingHorizontal 12}}
           [text {:style {:fontSize         12
                          :fontFamily       font
                          :color            color-white}}
            (:text command)]]]
         [image {:source (:icon command)
                 :style {:position "absolute"
                         :top      4
                         :right    0
                         :width    12
                         :height   13}}]
         [text {:style {:marginTop        5
                        :marginHorizontal 0
                        :fontSize         14
                        :fontFamily       font
                        :color            color-black}}
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
                                         (set-chat-command msg-id command))
                              :underlay-color :transparent}
         [view {:style {:paddingRight 16}}
          [view {:style (merge {:borderRadius 14
                                :padding      12}
                               (if outgoing
                                 {:backgroundColor color-white}
                                 {:backgroundColor background-color}))}
           [text {:style (merge {:fontSize   14
                                 :fontFamily font}
                                (if outgoing
                                  {:color color-black}
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
                            :left     10
                            :width    12
                            :height   13}}]]
          [text {:style {:marginTop  2
                         :fontFamily font
                         :fontSize   12
                         :color      text2-color}}
           (str (:request-text command))]]]))))

(defn message-content [{:keys [msg-id content-type content outgoing text-color background-color group-chat selected]}]
  (if (= content-type content-type-command-request)
    [message-content-command-request msg-id content outgoing text-color background-color]
    [view {:style (merge {:borderRadius 14
                          :padding      12}
                         (if outgoing
                           (if (and group-chat (= content-type text-content-type))
                             {:backgroundColor color-blue}
                             {:backgroundColor color-white})
                           (if selected
                             {:backgroundColor selected-message-color}
                             {:backgroundColor background-color})))}
     (when (and group-chat (not outgoing))
       [text {:style {:marginTop  0
                      :fontSize   12
                      :fontFamily font}}
        "Justas"])
     (cond
       (or (= content-type text-content-type)
           (= content-type content-type-status))
       [text {:style (merge {:marginTop (if (and group-chat (not outgoing))
                                          4
                                          0)
                             :fontSize   14
                             :fontFamily font}
                            (if outgoing
                              (if group-chat
                                {:color color-white}
                                {:color text1-color})
                              {:color text-color}))}
        content]
       (= content-type content-type-command)
       [message-content-command content]
       :else [message-content-audio {:content      content
                                     :content-type content-type}])]))

(defn message-delivery-status [{:keys [delivery-status]}]
  [view {:style {:flexDirection "row"
                 :marginTop     2
                 :marginBottom  8}}
   [image {:source (case delivery-status
                     :delivered        {:uri "icon_ok_small"}
                     :seen             {:uri "icon_ok_small"}
                     :seen-by-everyone {:uri "icon_ok_small"}
                     :failed           res/delivery-failed-icon)
           :style  {:marginTop 6
                    :width     9
                    :height    7}}]
   [text {:style {:fontFamily font
                  :fontSize   12
                  :color      text2-color
                  :marginLeft 5}}
    (case delivery-status
      :delivered "Delivered"
      :seen "Seen"
      :seen-by-everyone "Seen by everyone"
      :failed "Failed")]])

(defn member-photo [{:keys [photo-path]}]
  [view {:borderRadius 50}
   [image {:source (if (s/blank? photo-path)
                     res/user-no-photo
                     {:uri photo-path})
           :style  {:borderRadius 50
                    :width        24
                    :height       24}}]])

(defn incoming-group-message-body [{:keys [msg-id content content-type outgoing delivery-status text-color background-color selected]}]
  (let [delivery-status :seen-by-everyone]
    [view {:style {:flexDirection "column"}}
     (when selected
       [text {:style {:marginTop  18
                      :marginLeft 40
                      :fontFamily font
                      :fontSize   12
                      :color      text2-color}}
        "Mar 7th, 15:22"])
     [view {:style {:flexDirection "row"
                    :alignSelf     "flex-start"
                    :paddingTop    2
                    :paddingRight  8
                    :paddingBottom 2
                    :paddingLeft   8}}
      [member-photo {}]
      [view {:style {:flexDirection "column"
                     :width         260
                     :paddingLeft   8
                     :alignItems "flex-start"}}
       [message-content {:msg-id           msg-id
                         :content-type     content-type
                         :content          content
                         :outgoing         outgoing
                         :text-color       text-color
                         :background-color background-color
                         :group-chat       true
                         :selected         selected}]
       ;; TODO show for last or selected
       (when (and selected delivery-status)
         [message-delivery-status {:delivery-status delivery-status}])]]]))

(defn message-body [{:keys [msg-id content content-type outgoing delivery-status text-color background-color group-chat]}]
  (let [delivery-status :seen]
    [view {:style (merge {:flexDirection "column"
                          :width         260
                          :paddingTop    2
                          :paddingRight  8
                          :paddingBottom 2
                          :paddingLeft   8}
                         (if outgoing
                           {:alignSelf  "flex-end"
                            :alignItems "flex-end"}
                           {:alignItems "flex-start"
                            :alignSelf  "flex-start"}))}
     [message-content {:msg-id           msg-id
                       :content-type     content-type
                       :content          content
                       :outgoing         outgoing
                       :text-color       text-color
                       :background-color background-color
                       :group-chat       group-chat}]
     (when (and outgoing delivery-status)
       [message-delivery-status {:delivery-status delivery-status}])]))

(defn chat-message [{:keys [msg-id from content content-type outgoing delivery-status date new-day text-color background-color group-chat selected] :as msg}]
  [view {}
   (when new-day
     [message-date {:date date}])
   (let [msg-data   {:msg-id           msg-id
                     :content          content
                     :content-type     content-type
                     :outgoing         outgoing
                     :text-color       color-black ;text-color
                     :background-color color-white ;background-color
                     :delivery-status  (keyword delivery-status)
                     :group-chat       group-chat
                     :selected         selected}]
     [view {}
      (when (= content-type content-type-status)
        [message-content-status from content])
      (if (and group-chat (not outgoing))
        [incoming-group-message-body msg-data]
        [message-body msg-data])])])
