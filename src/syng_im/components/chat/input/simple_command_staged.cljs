(ns syng-im.components.chat.input.simple-command-staged
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.components.react :refer [android?
                                              view
                                              image
                                              text
                                              text-input
                                              touchable-highlight]]
            [syng-im.components.styles :refer [font
                                               color-black
                                               color-white
                                               chat-background]]
            [syng-im.utils.utils :refer [log toast http-post]]
            [syng-im.utils.logging :as log]
            [syng-im.resources :as res]
            [reagent.core :as r]))

(defn cancel-command-input [chat-id staged-command]
  (dispatch [:unstage-command chat-id staged-command]))

(defn simple-command-staged-view [staged-command]
  (let [chat-id-atom (subscribe [:get-current-chat-id])]
    (fn [staged-command]
      (let [chat-id @chat-id-atom
            command (:command staged-command)]
        [view {:style {:flex 1
                       :alignItems "flex-start"
                       :flexDirection   "column"
                       :backgroundColor color-white}}
         [view {:style {:flexDirection   "column"
                        :margin          16
                        :padding         12
                        :backgroundColor chat-background
                        :borderRadius    14}}
          [view {:style {:flexDirection "row"}}
           [view {:style {:backgroundColor   (:color command)
                          :height            24
                          :borderRadius      50
                          :marginRight 32
                          :paddingTop        3
                          :paddingHorizontal 12}}
            [text {:style {:fontSize         12
                           :fontFamily       font
                           :color            color-white}}
             (:text command)]]
           [touchable-highlight {:style {:position    "absolute"
                                         :top         7
                                         :right       4}
                                 :onPress (fn []
                                            (cancel-command-input chat-id staged-command))
                                 :underlay-color :transparent}
            [image {:source res/icon-close-gray
                    :style  {:width  10
                             :height 10}}]]]
          [text {:style {:marginTop        5
                         :marginHorizontal 0
                         :fontSize         14
                         :fontFamily       font
                         :color            color-black}}
           ;; TODO isn't smart
           (if (= (:command command) :keypair-password)
             "******"
             (:content staged-command))]]]))))
