(ns status-im.chat.views.staged-command
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.components.react :refer [view
                                                image
                                                text
                                                touchable-highlight]]
            [status-im.resources :as res]
            [status-im.chat.styles.input :as st]))

(defn cancel-command-input [staged-command]
  (dispatch [:unstage-command staged-command]))

(defn simple-command-staged-view [staged-command]
  (let [{:keys [type name] :as command} (:command staged-command)]
    [view st/staged-command-container
     [view st/staged-command-background
      [view st/staged-command-info-container
       [view (st/staged-command-text-container command)
        [text {:style st/staged-command-text}
         (if (= :command type)
           (str "!" name)
           name)]]
       [touchable-highlight {:style   st/staged-command-cancel
                             :onPress #(cancel-command-input staged-command)}
        [image {:source res/icon-close-gray
                :style  st/staged-command-cancel-icon}]]]
      (if-let [preview (:preview staged-command)]
        preview
        [text {:style st/staged-command-content}
         (:content staged-command)])]]))
