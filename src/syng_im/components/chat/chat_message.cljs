(ns syng-im.components.chat.chat-message
  (:require [clojure.string :as s]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.components.react :refer [android?
                                              view
                                              text
                                              image
                                              touchable-highlight
                                              navigator]]
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

(def style-message-text {:fontSize   14
                         :fontFamily font
                         :lineHeight 21
                         :color      text1-color})

(def style-sub-text {:top        -2
                     :fontFamily font
                     :fontSize   12
                     :color      text2-color
                     :lineHeight 14
                     :height     16})

(defn message-date [{:keys [date]}]
  [view {}
   [view {:style {:backgroundColor   color-light-blue-transparent
                  :height            24
                  :borderRadius      50
                  :alignSelf         :center
                  :marginTop         20
                  :marginBottom      20
                  :paddingTop        5
                  :paddingHorizontal 12}}
    [text {:style (assoc style-sub-text :textAlign :center)}
     date]]])

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
    [view {:position        :absolute
           :top             44
           :left            44
           :width           24
           :height          24
           :borderRadius    50
           :backgroundColor online-color
           :borderWidth     2
           :borderColor     color-white}
     [view {:position        :absolute
            :top             8
            :left            5
            :width           4
            :height          4
            :borderRadius    50
            :backgroundColor color-white}]
     [view {:position        :absolute
            :top             8
            :left            11
            :width           4
            :height          4
            :borderRadius    50
            :backgroundColor color-white}]]))


(defn message-content-status [{:keys [from content]}]
  [view {:style {:flex       1
                 :alignSelf  :center
                 :alignItems :center
                 :width      249}}
   [view {:style {:marginTop 20}}
    [contact-photo {}]
    [contact-online {:online true}]]
   [text {:style {:marginTop  20
                  :fontSize   18
                  :fontFamily font
                  :color      text1-color}}
    from]
   [text {:style {:marginTop  10
                  :fontFamily font
                  :fontSize   14
                  :lineHeight 20
                  :textAlign  :center
                  :color      text2-color}}
    content]])

(defn message-content-audio [_]
  [view {:style {:flexDirection :row
                 :alignItems    :center}}
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
    [view {:style {:position        :absolute
                   :top             4
                   :width           120
                   :height          2
                   :backgroundColor "#EC7262"}}]
    [view {:style {:position        :absolute
                   :left            0
                   :top             0
                   :width           2
                   :height          10
                   :backgroundColor "#4A5258"}}]
    [text {:style {:position      :absolute
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
            {:keys [command content]}
            (parse-command-msg-content commands content)]
        [view {:style {:flexDirection :column}}
         [view {:style {:flexDirection :row
                        :marginRight   32}}
          [view {:style {:backgroundColor   (:color command)
                         :height            24
                         :borderRadius      50
                         :paddingTop        3
                         :paddingHorizontal 12}}
           [text {:style {:fontSize   12
                          :fontFamily font
                          :color      color-white}}
            (:text command)]]]
         [image {:source (:icon command)
                 :style  {:position :absolute
                          :top      4
                          :right    0
                          :width    12
                          :height   13}}]
         [text {:style (merge style-message-text
                              {:marginTop        8
                               :marginHorizontal 0})}
          ;; TODO isn't smart
          (if (= (:command command) :keypair-password)
            "******"
            content)]]))))

(defn set-chat-command [msg-id command]
  (dispatch [:set-response-chat-command msg-id (:command command)]))


(defn message-content-command-request
  [{:keys [msg-id content outgoing group-chat from]}]
  (let [commands-atom (subscribe [:get-commands])]
    (fn [{:keys [msg-id content outgoing group-chat from]}]
      (let [commands @commands-atom
            {:keys [command content]} (parse-command-request-msg-content commands content)]
        [touchable-highlight {:onPress        #(set-chat-command msg-id command)
                              :underlay-color :transparent}
         [view {:style {:paddingRight 16}}
          [view {:style (merge {:borderRadius    14
                                :padding         12
                                :backgroundColor color-white})}
           (when (and group-chat (not outgoing))
             [text {:style (merge style-sub-text
                                  {:marginBottom 2})}
              from])
           [text {:style style-message-text}
            content]]
          [view {:style {:position        :absolute
                         :top             12
                         :right           0
                         :width           32
                         :height          32
                         :borderRadius    50
                         :backgroundColor (:color command)}}
           [image {:source (:request-icon command)
                   :style  {:position :absolute
                            :top      9
                            :left     10
                            :width    12
                            :height   13}}]]
          (when (:request-text command)
            [view {:style {:marginTop 4
                           :height    14}}
             [text {:style style-sub-text}
              (:request-text command)]])]]))))

(defn message-content-plain [content outgoing group-chat]
  [text {:style (merge style-message-text
                       {:marginTop (if (and group-chat (not outgoing))
                                     4
                                     0)}
                       (when (and outgoing group-chat)
                         {:color color-white}))}
   content])


#_(defn message-content [{:keys [msg-id from content-type content outgoing
                                 group-chat selected]}]
    (if (= content-type content-type-command-request)
      [message-content-command-request msg-id from content outgoing group-chat]
      [view {:style (merge {:borderRadius    14
                            :padding         12
                            :backgroundColor color-white}
                           (when (= content-type content-type-command)
                             {:paddingTop    10
                              :paddingBottom 14})
                           (if outgoing
                             (when (and group-chat (= content-type text-content-type))
                               {:backgroundColor color-blue})
                             (when selected
                               {:backgroundColor selected-message-color})))}
       (when (and group-chat (not outgoing))
         [text {:style (merge style-sub-text
                              {:marginBottom 2})}
          from])
       (cond
         (or (= content-type text-content-type)
             (= content-type content-type-status))
         [message-content-plain content outgoing group-chat]
         (= content-type content-type-command)
         [message-content-command content]
         :else [message-content-audio {:content      content
                                       :content-type content-type}])]))

(defn message-view
  [{:keys [content-type outgoing background-color group-chat selected]} content]
  [view {:style (merge {:borderRadius    14
                        :padding         12
                        :backgroundColor color-white}
                       (when (= content-type content-type-command)
                         {:paddingTop    10
                          :paddingBottom 14})
                       (if outgoing
                         (when (and group-chat (= content-type text-content-type))
                           {:backgroundColor color-blue})
                         (when selected
                           {:backgroundColor selected-message-color})))}
   #_(when (and group-chat (not outgoing))
       [text {:style {:marginTop  0
                      :fontSize   12
                      :fontFamily font}}
        "Justas"])
   content])

(defmulti message-content (fn [_ message]
                            (message :content-type)))

(defmethod message-content content-type-command-request
  [wrapper message]
  [wrapper message [message-content-command-request message]])

(defn text-message
  [{:keys [content outgoing group-chat] :as message}]
  [message-view message
   [text {:style (merge style-message-text
                        {:marginTop (if (and group-chat (not outgoing))
                                      4
                                      0)}
                        (when (and outgoing group-chat)
                          {:color color-white}))}
    content]])

(defmethod message-content text-content-type
  [wrapper message]
  [wrapper message [text-message message]])

(defmethod message-content content-type-status
  [_ message]
  [message-content-status message])

(defmethod message-content content-type-command
  [wrapper {:keys [content] :as message}]
  [wrapper message
   [message-view message [message-content-command content]]])

(defmethod message-content :default
  [wrapper {:keys [content-type content] :as message}]
  [wrapper message
   [message-view message
    [message-content-audio {:content      content
                            :content-type content-type}]]])

(defn message-delivery-status [{:keys [delivery-status]}]
  [view {:style {:flexDirection :row
                 :marginTop     2}}

   [image {:source (case delivery-status
                     :delivered {:uri :icon_ok_small}
                     :seen {:uri :icon_ok_small}
                     :seen-by-everyone {:uri :icon_ok_small}
                     :failed res/delivery-failed-icon)
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


(defn incoming-group-message-body
  [{:keys [selected new-day same-author same-direction last-msg typing]}
   content]
  (let [delivery-status :seen-by-everyone]
    [view {:style {:flexDirection :column}}
     (when selected
       [text {:style {:marginTop  18
                      :marginLeft 40
                      :fontFamily font
                      :fontSize   12
                      :color      text2-color}}
        "Mar 7th, 15:22"])
     [view {:style (merge {:flexDirection :row
                           :alignSelf     :flex-start
                           :marginTop     (cond
                                            new-day 0
                                            same-author 4
                                            same-direction 20
                                            :else 10)
                           :paddingRight  8
                           :paddingLeft   8}
                          (when (and last-msg (not typing))
                            {:paddingBottom 20}))}
      [view {:style {:width 24}}
       (when (not same-author)
         [member-photo {}])]
      [view {:style {:flexDirection :column
                     :width         260
                     :paddingLeft   8
                     :alignItems    :flex-start}}
       content
       ;; TODO show for last or selected
       (when (and selected delivery-status)
         [message-delivery-status {:delivery-status delivery-status}])]]]))

(defn message-body
  [{:keys [outgoing new-day same-author same-direction last-msg typing]}
   content]
  (let [delivery-status :seen
        align           (if outgoing :flex-end :flex-start)]
    [view {:style (merge {:flexDirection :column
                          :width         260
                          :paddingTop    (cond
                                           new-day 0
                                           same-author 4
                                           same-direction 20
                                           :else 10)
                          :paddingRight  8
                          :paddingLeft   8
                          :alignSelf     align
                          :alignItems    align}
                         (when (and last-msg (not typing))
                           {:paddingBottom 20}))}
     content
     (when (and outgoing delivery-status)
       [message-delivery-status {:delivery-status delivery-status}])]))

(defn chat-message
  [{:keys [msg-id outgoing delivery-status date new-day group-chat]
    :as   message}
   last-msg-id]
  [view {}
   (when new-day
     [message-date {:date date}])
   (let [msg-data
         (merge message {:delivery-status (keyword delivery-status)
                         :last-msg        (= last-msg-id msg-id)})]
     [view {}
      [message-content
       (if (and group-chat (not outgoing))
         incoming-group-message-body
         message-body)
       msg-data]])])
