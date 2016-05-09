(ns syng-im.chat.views.staged-command
  (:require [re-frame.core :refer [subscribe dispatch]]
            [syng-im.components.react :refer [view
                                              image
                                              text
                                              touchable-highlight]]
            [syng-im.resources :as res]
            [syng-im.chat.styles.input :as st]))

(defn cancel-command-input [staged-command]
  (dispatch [:unstage-command staged-command]))

(defn simple-command-staged-view [staged-command]
  (let [command (:command staged-command)]
    [view st/staged-command-container
     [view st/staged-command-background
      [view st/staged-command-info-container
       [view (st/staged-command-text-container command)
        [text {:style st/staged-command-text} (:text command)]]
       [touchable-highlight {:style   st/staged-command-cancel
                             :onPress #(cancel-command-input staged-command)}
        [image {:source res/icon-close-gray
                :style  st/staged-command-cancel-icon}]]]
      [text {:style st/staged-command-content}
       ;; TODO isn't smart
       (if (= (:command command) :keypair-password)
         "******"
         (:content staged-command))]]]))
