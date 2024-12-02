(ns status-im.contexts.chat.messenger.composer.actions.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.reanimated :as reanimated]
    [status-im.constants :as constants]
    [status-im.contexts.chat.messenger.composer.actions.image.view :as actions.image]
    [status-im.contexts.chat.messenger.composer.actions.style :as style]
    [utils.re-frame :as rf]))

(defn send-message
  [input-ref edit btn-opacity]
  (when @input-ref
    (.clear ^js @input-ref))
  (reanimated/animate btn-opacity 0)
  (rf/dispatch [:chat.ui/send-current-message])
  (rf/dispatch [:chat.ui/set-chat-input-text nil])
  (when-not (some? edit)
    (rf/dispatch [:chat.ui/scroll-to-bottom])))

(defn send-button
  [input-ref edit]
  (let [btn-opacity                 (reanimated/use-shared-value 0)
        chat-input                  (rf/sub [:chats/current-chat-input])
        input-text                  (:input-text chat-input)
        images?                     (boolean (seq (rf/sub [:chats/sending-image])))
        profile-customization-color (rf/sub [:profile/customization-color])
        {:keys      [chat-id chat-type]
         chat-color :color}         (rf/sub [:chats/current-chat-chat-view])
        contact-customization-color (when (= chat-type constants/one-to-one-chat-type)
                                      (rf/sub [:contacts/contact-customization-color-by-address
                                               chat-id]))
        on-press                    (rn/use-callback #(send-message input-ref edit btn-opacity) [edit])]
    (rn/use-effect (fn []
                     ;; Handle send button opacity animation and z-index when input content changes
                     (if (or (seq input-text) images?)
                       (when (not= (reanimated/get-shared-value btn-opacity) 1)
                         (js/setTimeout #(reanimated/animate btn-opacity 1) 50))
                       (when (not= (reanimated/get-shared-value btn-opacity) 0)
                         (reanimated/animate btn-opacity 0))))
                   [(and (empty? input-text) (not images?))])
    [reanimated/view
     {:style (style/send-button btn-opacity)}
     [quo/button
      {:icon-only?          true
       :size                32
       :customization-color (or contact-customization-color chat-color profile-customization-color)
       :accessibility-label :send-message-button
       :on-press            on-press}
      :i/arrow-up]]))

(defn view
  [input-ref]
  (let [edit (rf/sub [:chats/edit-message])]
    [rn/view {:style style/actions-container}
     [rn/view {:style {:flex-direction :row}}
      (when-not edit
        [:<>
         [actions.image/camera-button]
         [actions.image/image-button input-ref edit]])]
     [send-button input-ref edit]]))
