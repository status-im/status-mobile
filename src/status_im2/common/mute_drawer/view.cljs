(ns status-im2.common.mute-drawer.view
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
  [{:keys [id community? muted? muted-type]}]
  (rf/dispatch [:hide-bottom-sheet])
  (if community?
    (rf/dispatch [:community/set-muted id muted? muted-type])
    (rf/dispatch [:chat.ui/mute id muted? muted-type])))

(defn mute-drawer
  [{:keys [id accessibility-label muted? chat-type community?]}]
  [rn/view {:accessibility-label accessibility-label}
   [quo/text
    {:weight :medium
     :size   :paragraph-2
     :style  (style/header-text)}
    (i18n/label
     (if community?
       :t/mute-community
       (if (not-community-chat? chat-type)
         :t/mute-chat-capitialized
         :t/mute-channel)))]
   [quo/menu-item
    {:type                       :transparent
     :title                      (i18n/label :t/mute-for-15-mins)
     :icon-bg-color              :transparent
     :container-padding-vertical 12
     :title-column-style         {:margin-left 2}
     :on-press                   (fn []
                                   (hide-sheet-and-dispatch
                                    {:id         id
                                     :chat-type  chat-type
                                     :muted?     muted?
                                     :community? community?
                                     :muted-type constants/mute-for-15-mins-type}))}]
   [quo/menu-item
    {:type                       :transparent
     :title                      (i18n/label :t/mute-for-1-hour)
     :icon-bg-color              :transparent
     :container-padding-vertical 12
     :title-column-style         {:margin-left 2}
     :on-press                   (fn []
                                   (hide-sheet-and-dispatch
                                    {:id         id
                                     :chat-type  chat-type
                                     :muted?     muted?
                                     :community? community?
                                     :muted-type constants/mute-for-1-hour-type}))}]
   [quo/menu-item
    {:type                       :transparent
     :title                      (i18n/label :t/mute-for-8-hours)
     :icon-bg-color              :transparent
     :container-padding-vertical 12
     :title-column-style         {:margin-left 2}
     :on-press                   (fn []
                                   (hide-sheet-and-dispatch
                                    {:id         id
                                     :chat-type  chat-type
                                     :muted?     muted?
                                     :community? community?
                                     :muted-type constants/mute-for-8-hours-type}))}]
   [quo/menu-item
    {:type                       :transparent
     :title                      (i18n/label :t/mute-for-1-week)
     :icon-bg-color              :transparent
     :container-padding-vertical 12
     :title-column-style         {:margin-left 2}
     :on-press                   (fn []
                                   (hide-sheet-and-dispatch
                                    {:id         id
                                     :chat-type  chat-type
                                     :muted?     muted?
                                     :community? community?
                                     :muted-type constants/mute-for-1-week}))}]
   [quo/menu-item
    {:type                       :transparent
     :title                      (i18n/label :t/mute-till-unmute)
     :icon-bg-color              :transparent
     :container-padding-vertical 12
     :title-column-style         {:margin-left 2}
     :on-press                   (fn []
                                   (hide-sheet-and-dispatch
                                    {:id         id
                                     :chat-type  chat-type
                                     :muted?     muted?
                                     :community? community?
                                     :muted-type constants/mute-till-unmuted}))}]])
