(ns status-im2.contexts.chat.messages.composer.controls.view
  (:require [react-native.core :as rn]
            [react-native.background-timer :as background-timer]
            [utils.re-frame :as rf]
            [i18n.i18n :as i18n]
            [quo2.core :as quo]
            [status-im2.common.not-implemented :as not-implemented]
            [status-im2.contexts.chat.photo-selector.view :as photo-selector]
            [status-im2.contexts.chat.messages.composer.controls.style :as style]
            [status-im2.contexts.chat.messages.list.view :as messages.list]
            [status-im.ui.components.permissions :as permissions]
            [status-im.ui2.screens.chat.composer.images.view :as composer-images]
            [status-im.utils.utils :as utils-old]))

(defn send-button
  [send-ref {:keys [images]} on-send]
  [rn/view
   {:ref   send-ref
    :style (when (seq images)
             {:width 0
              :right -100})}
   [quo/button
    {:icon                true
     :size                32
     :accessibility-label :send-message-button
     :on-press            (fn []
                            (on-send)
                            (messages.list/scroll-to-bottom)
                            (rf/dispatch [:chat.ui/send-current-message]))}
    :i/arrow-up]])

(defn reactions-button
  []
  [not-implemented/not-implemented
   [quo/button
    {:icon true
     :type :outline
     :size 32} :i/reaction]])

(defn image-button
  [chat-id]
  [quo/button
   {:on-press (fn []
                (permissions/request-permissions
                 {:permissions [:read-external-storage :write-external-storage]
                  :on-allowed  #(rf/dispatch
                                 [:bottom-sheet/show-sheet
                                  {:content (fn []
                                              (photo-selector/photo-selector chat-id))}])
                  :on-denied   (fn []
                                 (background-timer/set-timeout
                                  #(utils-old/show-popup (i18n/label :t/error)
                                                         (i18n/label
                                                          :t/external-storage-denied))
                                  50))}))
    :icon     true
    :type     :outline
    :size     32}
   :i/image])

(defn view
  [send-ref params insets chat-id images on-send]
  [rn/view {:style (style/controls insets)}
   [composer-images/images-list images]
   [rn/view {:style {:flex-direction :row :margin-top 12}}
    [image-button chat-id]
    [rn/view {:width 12}]
    [reactions-button]
    [rn/view {:flex 1}]
    [send-button send-ref params on-send]]])
