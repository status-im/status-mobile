(ns status-im.chat.views.staged-command
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.components.react :refer [view
                                                image
                                                icon
                                                text
                                                touchable-highlight]]
            [status-im.resources :as res]
            [status-im.chat.styles.input :as st]
            [status-im.chat.styles.command-pill :as pill-st]))

(defn cancel-command-input [staged-command]
  (dispatch [:unstage-command staged-command]))

(defn simple-command-staged-view
  [{:keys [command params] :as staged-command}]
  (let [{:keys [type name]} command]
    [view st/staged-command-container
     [view st/staged-command-background
      [view {:flex-direction :row}
       [view st/staged-command-info-container
        [view (pill-st/pill command)
         [text {:style pill-st/pill-text}
          (str
            (if (= :command type) "!" "?")
            name)]]]
       [touchable-highlight {:style   st/staged-command-cancel
                             :onPress #(cancel-command-input staged-command)}
        [view [icon :close_small_gray
          st/staged-command-cancel-icon]]]]
      (if-let [preview (:preview staged-command)]
        preview
        [text {:style st/staged-command-content}
         (if (= 1 (count params))
           (first (vals params))
           (str params))])]]))
