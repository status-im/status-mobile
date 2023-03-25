(ns status-im2.contexts.chat.messages.composer.controls.view
  (:require [react-native.core :as rn]
            [react-native.background-timer :as background-timer]
            [utils.re-frame :as rf]
            [utils.i18n :as i18n]
            [quo2.core :as quo]
            [status-im2.common.not-implemented :as not-implemented]
            [status-im2.contexts.chat.messages.composer.controls.style :as style]
            [status-im2.contexts.chat.messages.list.view :as messages.list]
            [status-im.ui2.screens.chat.composer.images.view :as composer-images]
            [status-im.utils.utils :as utils-old]
            [status-im.ui2.screens.chat.composer.input :as input]
            [status-im2.common.alert.events :as alert]
            [react-native.permissions :as permissions]
            [react-native.safe-area :as safe-area]
            [quo.react :as quo.react]))

(defn send-button
  [send-ref {:keys [chat-id images]} on-send]
  [rn/view
   {:ref   send-ref
    :style (when-not (or (seq (get @input/input-texts chat-id)) (seq images))
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
  [insets]
  [quo/button
   {:on-press (fn []
                (permissions/request-permissions
                 {:permissions [:read-external-storage :write-external-storage]
                  :on-allowed  #(rf/dispatch
                                 [:open-modal :photo-selector {:insets insets}])
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

(defn record-audio
  [record-ref chat-id]
  [safe-area/consumer
   (fn [insets]
     [rn/view
      {:ref            record-ref
       :style          (style/record-audio-container insets)
       :pointer-events :box-none}
      [quo/record-audio
       {:record-audio-permission-granted    @input/record-audio-permission-granted
        :on-init                            (fn [init-fn]
                                              (reset! input/record-audio-reset-fn init-fn)
                                              (reset! input/recording-audio?
                                                (some? (get @input/reviewing-audio-filepath chat-id)))
                                              (when (seq (get @input/input-texts chat-id))
                                                (js/setTimeout #(quo.react/set-native-props
                                                                 record-ref
                                                                 #js {:right nil :left -1000}))))
        :on-start-recording                 #(reset! input/recording-audio? true)
        :audio-file                         (get @input/reviewing-audio-filepath chat-id)
        :on-reviewing-audio                 (fn [audio-file]
                                              (swap! input/reviewing-audio-filepath assoc
                                                chat-id
                                                audio-file)
                                              (reset! input/reviewing-audio? true))
        :on-send                            (fn
                                              [{:keys [file-path duration]}]
                                              (rf/dispatch [:chat/send-audio file-path duration])
                                              (reset! input/recording-audio? false)
                                              (reset! input/reviewing-audio? false)
                                              (swap! input/reviewing-audio-filepath dissoc chat-id))
        :on-cancel                          (fn []
                                              (reset! input/recording-audio? false)
                                              (reset! input/reviewing-audio? false)
                                              (swap! input/reviewing-audio-filepath dissoc chat-id))
        :on-check-audio-permissions         (fn []
                                              (permissions/permission-granted?
                                               :record-audio
                                               #(reset! input/record-audio-permission-granted %)
                                               #(reset! input/record-audio-permission-granted false)))
        :on-request-record-audio-permission (fn []
                                              (rf/dispatch
                                               [:request-permissions
                                                {:permissions [:record-audio]
                                                 :on-allowed
                                                 #(reset! input/record-audio-permission-granted true)
                                                 :on-denied
                                                 #(js/setTimeout
                                                   (fn []
                                                     (alert/show-popup
                                                      (i18n/label :t/audio-recorder-error)
                                                      (i18n/label
                                                       :t/audio-recorder-permissions-error)))
                                                   50)}]))}]])])

(defn view
  [send-ref record-ref params insets chat-id images edit on-send]
  [rn/view {:style (style/controls insets)}
   [composer-images/images-list images]
   [rn/view {:style style/buttons-container}
    (when (and (not @input/recording-audio?)
               (nil? (get @input/reviewing-audio-filepath chat-id)))
      [:<>
       [image-button insets]
       [rn/view {:width 12}]
       [reactions-button]
       [rn/view {:flex 1}]
       [send-button send-ref params on-send]])]
   (when (and (not edit) (not (seq images)))
     [record-audio record-ref chat-id])])
