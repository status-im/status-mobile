(ns status-im2.common.mute-chat-drawer.view
  (:require [utils.i18n :as i18n]
            [quo2.core :as quo]
            [react-native.core :as rn]
            [status-im2.constants :as constants]
            [utils.re-frame :as rf]
            [status-im2.common.mute-drawer.style :as style]))

(defn not-community-chat?
  [chat-type]
  (contains? #{constants/public-chat-type
               constants/private-group-chat-type
               constants/one-to-one-chat-type}
             chat-type))

(defn hide-sheet-and-dispatch
  [event]
  (rf/dispatch [:hide-bottom-sheet])
  (rf/dispatch event))

(defn mute-chat-drawer
  [chat-id accessibility-label chat-type]
  [rn/view {:accessibility-label accessibility-label}
   [quo/text
    {:weight :medium
     :size   :paragraph-2
     :style  (style/header-text)}
    (i18n/label
     (if (not-community-chat? chat-type)
       :t/mute-chat-capitialized
       :t/mute-channel))]
   [quo/menu-item
    {:type                       :transparent
     :title                      (i18n/label :t/mute-for-15-mins)
     :icon-bg-color              :transparent
     :container-padding-vertical 12
     :title-column-style         {:margin-left 2}
     :on-press                   (fn []
                                   (hide-sheet-and-dispatch [:chat.ui/mute chat-id true
                                                             constants/mute-for-15-mins-type]))}]
   [quo/menu-item
    {:type                       :transparent
     :title                      (i18n/label :t/mute-for-1-hour)
     :icon-bg-color              :transparent
     :container-padding-vertical 12
     :title-column-style         {:margin-left 2}
     :on-press                   (fn []
                                   (hide-sheet-and-dispatch [:chat.ui/mute chat-id true
                                                             constants/mute-for-1-hour-type]))}]
   [quo/menu-item
    {:type                       :transparent
     :title                      (i18n/label :t/mute-for-8-hours)
     :icon-bg-color              :transparent
     :container-padding-vertical 12
     :title-column-style         {:margin-left 2}
     :on-press                   (fn []
                                   (hide-sheet-and-dispatch [:chat.ui/mute chat-id true
                                                             constants/mute-for-8-hours-type]))}]
   [quo/menu-item
    {:type                       :transparent
     :title                      (i18n/label :t/mute-for-1-week)
     :icon-bg-color              :transparent
     :container-padding-vertical 12
     :title-column-style         {:margin-left 2}
     :on-press                   (fn []
                                   (hide-sheet-and-dispatch [:chat.ui/mute chat-id true
                                                             constants/mute-for-1-week]))}]
   [quo/menu-item
    {:type                       :transparent
     :title                      (i18n/label :t/mute-till-unmute)
     :icon-bg-color              :transparent
     :container-padding-vertical 12
     :title-column-style         {:margin-left 2}
     :on-press                   (fn []
                                   (hide-sheet-and-dispatch [:chat.ui/mute chat-id true
                                                             constants/mute-till-unmuted]))}]])
