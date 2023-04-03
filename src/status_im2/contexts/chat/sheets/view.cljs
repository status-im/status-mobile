(ns status-im2.contexts.chat.sheets.view
  (:require [quo2.core :as quo]
            [utils.i18n :as i18n]
            [quo2.foundations.colors :as colors]
            [utils.re-frame :as rf]))

(defn new-chat-bottom-sheet
  []
  [:<>
   [quo/menu-item
    {:type                       :transparent
     :title                      (i18n/label :t/new-chat)
     :icon-bg-color              :transparent
     :container-padding-vertical 12
     :title-column-style         {:margin-left 2}
     :style-props                {:border-bottom-width 1
                                  :border-bottom-color (colors/theme-colors colors/neutral-10
                                                                            colors/neutral-90)}
     :icon-color                 (colors/theme-colors colors/neutral-50 colors/neutral-40)
     :accessibility-label        :start-a-new-chat
     :icon                       :i/new-message
     :on-press                   (fn []
                                   (rf/dispatch [:group-chat/clear-contacts])
                                   (rf/dispatch [:open-modal :start-a-new-chat]))}]
   [quo/menu-item
    {:type                         :transparent
     :title                        (i18n/label :t/add-a-contact)
     :icon-bg-color                :transparent
     :icon-container-style         {:padding-horizontal 0}
     :container-padding-horizontal {:padding-horizontal 4}
     :style-props                  {:margin-top    18
                                    :margin-bottom 9}
     :container-padding-vertical   12
     :title-column-style           {:margin-left 2}
     :icon-color                   (colors/theme-colors colors/neutral-50 colors/neutral-40)
     :accessibility-label          :add-a-contact
     :subtitle                     (i18n/label :t/enter-a-chat-key)
     :subtitle-color               colors/neutral-50
     :icon                         :i/add-user
     :on-press                     #(rf/dispatch [:open-modal :new-contact])}]])
