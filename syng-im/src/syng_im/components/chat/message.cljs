(ns syng-im.components.chat.message
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.components.react :refer [view text image]]
            [syng-im.components.resources :as res]
            [syng-im.constants :refer [text-content-type]]))

(defn message-view []
  (let [greeting (subscribe [:get-greeting])] 
    (fn []
      (let [msg-id "1"
            content "1"
            content-type "1"
            outgoing true
            delivery-status :seen
            date "1"
            new-day true]
        [view {:paddingHorizontal 15}
         (when new-day
           [text {:style {:marginVertical 10
                          :fontFamily     "Avenir-Roman"
                          :fontSize       11
                          :color          "#AAB2B2"
                          :letterSpacing  1
                          :lineHeight     15
                          :textAlign      "center"
                          :opacity        0.8}}
            date])
         [view {:style (merge {:flexDirection  "column"
                               :width          260
                               :marginVertical 5}
                              (if outgoing
                                {:alignSelf  "flex-end"
                                 :alignItems "flex-end"}
                                {:alignSelf  "flex-start"
                                 :alignItems "flex-start"}))}
          [view {:style (merge {:borderRadius 6}
                               (if (= content-type text-content-type)
                                 {:paddingVertical   12
                                  :paddingHorizontal 16}
                                 {:paddingVertical   14
                                  :paddingHorizontal 10})
                               (if outgoing
                                 {:backgroundColor "#D3EEEF"}
                                 {:backgroundColor "#FBF6E3"}))}
           (if (= content-type text-content-type)
             [text {:style {:fontSize   14
                            :fontFamily "Avenir-Roman"
                            :color      "#4A5258"}}
              content]
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
                "03:39"]]])]
          (when (and outgoing delivery-status)
            [view {:style {:flexDirection "row"
                           :marginTop     2}}
             [image {:source (if (= (keyword delivery-status) :seen)
                               res/seen-icon
                               res/delivered-icon)
                     :style  {:marginTop 6
                              :opacity   0.6}}]
             [text {:style {:fontFamily "Avenir-Roman"
                            :fontSize   11
                            :color      "#AAB2B2"
                            :opacity    0.8
                            :marginLeft 5}}
              (if (= (keyword delivery-status) :seen)
                "Seen"
                "Delivered")]])]]))))
