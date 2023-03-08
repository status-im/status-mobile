(ns status-im2.common.mute-chat-drawer.view
  (:require [utils.i18n :as i18n]
            [quo2.core :as quo2]
            [react-native.core :as rn]
            [status-im2.constants :as constants]
            [utils.re-frame :as rf]
            [status-im2.common.mute-chat-drawer.style :as style]))

(defn hide-sheet-and-dispatch
  [event]
  (rf/dispatch [:bottom-sheet/hide])
  (rf/dispatch event))

(defn mute-chat-drawer [chat-id accessibility-label]
  (fn []
    [rn/view {:accessibility-label accessibility-label}
     [quo2/text {:weight :medium
                 :size   :paragraph-2
                 :style  (style/header-text)} (i18n/label :i/mute-channel)]
     [quo2/menu-item
      {:type                       :transparent
       :title                      (i18n/label :i/mute-for-1-min)
       :icon-bg-color              :transparent
       :container-padding-vertical 12
       :title-column-style         {:margin-left 2}
       :on-press                   (fn []
                                     (hide-sheet-and-dispatch [:chat.ui/mute chat-id true constants/mute-for-1-min]))}]
     [quo2/menu-item
      {:type                       :transparent
       :title                      (i18n/label :i/mute-for-15-mins)
       :icon-bg-color              :transparent
       :container-padding-vertical 12
       :title-column-style         {:margin-left 2}
       :on-press                   (fn []
                                     (hide-sheet-and-dispatch [:chat.ui/mute chat-id true constants/mute-for-15-mins-type]))}]
     [quo2/menu-item
      {:type                       :transparent
       :title                      (i18n/label :i/mute-for-1-hour)
       :icon-bg-color              :transparent
       :container-padding-vertical 12
       :title-column-style         {:margin-left 2}
       :on-press                   (fn []
                                     (hide-sheet-and-dispatch [:chat.ui/mute chat-id true constants/mute-for-1-hour-type]))}]
     [quo2/menu-item
      {:type                       :transparent
       :title                      (i18n/label :i/mute-for-8-hours)
       :icon-bg-color              :transparent
       :container-padding-vertical 12
       :title-column-style         {:margin-left 2}
       :on-press                   (fn []
                                     (hide-sheet-and-dispatch [:chat.ui/mute chat-id true constants/mute-for-8-hours-type]))}]
     [quo2/menu-item
      {:type                       :transparent
       :title                      (i18n/label :i/mute-for-1-week)
       :icon-bg-color              :transparent
       :container-padding-vertical 12
       :title-column-style         {:margin-left 2}
       :on-press                   (fn []
                                     (hide-sheet-and-dispatch [:chat.ui/mute chat-id true constants/mute-for-1-week]))}]
     [quo2/menu-item
      {:type                       :transparent
       :title                      (i18n/label :i/mute-till-unmute)
       :icon-bg-color              :transparent
       :container-padding-vertical 12
       :title-column-style         {:margin-left 2}
       :on-press                   (fn []
                                     (hide-sheet-and-dispatch [:chat.ui/mute chat-id true constants/mute-till-unmuted]))}]]))