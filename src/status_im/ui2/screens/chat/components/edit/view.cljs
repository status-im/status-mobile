(ns status-im.ui2.screens.chat.components.edit.view
  (:require [re-frame.core :as rf]
            [react-native.core :as rn]
            [quo2.foundations.colors :as colors]
            [i18n.i18n :as i18n]
            [status-im.ui2.screens.chat.components.edit.style :as style]
            [quo2.core :as quo]
            [quo.gesture-handler :as gesture-handler]))

(defn edit-message []
  [rn/view {:style               style/container
            :accessibility-label :edit-message}
   [rn/view {:style style/content-container}
    [quo/icon :i/connector {:size            16
                            :color           (colors/theme-colors colors/neutral-40 colors/neutral-60)
                            :container-style style/icon-container}]
    [rn/view {:style style/text-container}
     [quo/text {:weight :medium
                :size   :paragraph-2}
      (i18n/label :t/editing-message)]]]
   [gesture-handler/touchable-without-feedback
    {:accessibility-label :reply-cancel-button
     :on-press            #(rf/dispatch [:chat.ui/cancel-message-edit])}
    [quo/button {:width               24
                 :size                24
                 :type                :outline}
     [quo/icon :i/close {:size  16
                         :color (colors/theme-colors colors/neutral-100 colors/neutral-40)}]]]])
