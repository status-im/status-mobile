(ns status-im.chat.views.staged-commands
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.components.react :refer [view
                                                image
                                                icon
                                                text
                                                touchable-highlight
                                                scroll-view]]
            [status-im.chat.styles.input :as st]
            [status-im.chat.styles.command-pill :as pill-st]
            [status-im.utils.platform :refer [platform-specific]]
            [taoensso.timbre :as log]))

(defn cancel-command-input [staged-command]
  (dispatch [:unstage-command staged-command]))

(defn get-height [event]
  (.-height (.-layout (.-nativeEvent event))))

(defn simple-command-staged-view
  [{:keys [command params] :as staged-command}]
  (let [{:keys [type name]} command]
    [view st/staged-command-container
     [view st/staged-command-background
      [view st/staged-command-header
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
      [view {:padding-right 12}
       (if-let [preview (:preview staged-command)]
         preview
         [text {:style st/staged-command-content}
          (if (= 1 (count params))
            (first (vals params))
            (str params))])]]]))

(defn staged-command-view [stage-command]
  [simple-command-staged-view stage-command])

(defview staged-commands-view [staged-commands]
  [message-input-height [:get-message-input-view-height]
   input-margin [:input-margin]]
  (let [style (when (seq staged-commands)
                (get-in platform-specific [:component-styles :chat :new-message]))]
    [view {:style (merge (st/staged-commands message-input-height input-margin)
                         style)}
     [scroll-view {:bounces   false
                   :ref       #(dispatch [:set-staged-commands-scroll-view %])
                   :on-layout #(dispatch [:set-staged-commands-scroll-height (get-height %)])}
      [view {:on-layout #(dispatch [:staged-commands-scroll-to (get-height %)])}
       (for [command staged-commands]
         ^{:key command} [staged-command-view command])
       [view st/staged-commands-bottom]]]]))
