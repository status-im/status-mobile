(ns status-im.ui2.screens.chat.components.edit.view
  (:require [utils.i18n :as i18n]
            [quo.gesture-handler :as gesture-handler]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [re-frame.core :as rf]
            [react-native.core :as rn]
            [status-im.ui2.screens.chat.components.edit.style :as style]))

(defn edit-message
  [on-cancel]
  [rn/view
   {:style               style/container
    :accessibility-label :edit-message}
   [rn/view {:style style/content-container}
    [quo/icon :i/connector
     {:size            16
      :color           (colors/theme-colors colors/neutral-40 colors/neutral-60)
      :container-style style/icon-container}]
    [rn/view {:style style/text-container}
     [quo/text
      {:weight :medium
       :size   :paragraph-2}
      (i18n/label :t/editing-message)]]]
   [gesture-handler/touchable-without-feedback
    {:accessibility-label :reply-cancel-button
     :on-press            #(do (on-cancel)
                               (rf/dispatch [:chat.ui/cancel-message-edit]))}
    [quo/button
     {:width 24
      :size  24
      :type  :outline}
     [quo/icon :i/close
      {:size  16
       :color (colors/theme-colors colors/neutral-100 colors/neutral-40)}]]]])
