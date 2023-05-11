(ns status-im2.contexts.chat.composer.actions.view
  (:require
    [quo2.core :as quo]
    [react-native.core :as rn]
    [react-native.permissions :as permissions]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [reagent.core :as reagent]
    [status-im2.common.alert.events :as alert]
    [status-im2.contexts.chat.composer.constants :as constants]
    [status-im2.contexts.chat.messages.list.view :as messages.list]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [status-im2.contexts.chat.composer.actions.style :as style]))

(defn send-message
  [{:keys [text-value focused? maximized?]}
   {:keys [height saved-height last-height opacity background-y container-opacity]}
   window-height]
  (reanimated/animate height constants/input-height)
  (reanimated/set-shared-value saved-height constants/input-height)
  (reanimated/set-shared-value last-height constants/input-height)
  (reanimated/animate opacity 0)
  (when-not @focused?
    (js/setTimeout #(reanimated/animate container-opacity constants/empty-opacity) 300))
  (js/setTimeout #(reanimated/set-shared-value background-y
                                               (- window-height))
                 300)
  (rf/dispatch [:chat.ui/send-current-message])
  (rf/dispatch [:chat.ui/set-input-maximized false])
  (rf/dispatch [:chat.ui/set-input-content-height constants/input-height])
  (rf/dispatch [:chat.ui/set-chat-input-text nil])
  (reset! maximized? false)
  (reset! text-value "")
  (messages.list/scroll-to-bottom))

(defn f-send-button
  [{:keys [text-value] :as state}
   animations window-height images?
   btn-opacity z-index]
  (rn/use-effect (fn []
                   (if (or (seq @text-value) images?)
                     (when (or (not= @z-index 1) (not= (reanimated/get-shared-value btn-opacity) 1))
                       (reset! z-index 1)
                       (js/setTimeout #(reanimated/animate btn-opacity 1) 50))
                     (when (or (not= @z-index 0) (not= (reanimated/get-shared-value btn-opacity) 0))
                       (reanimated/animate btn-opacity 0)
                       (js/setTimeout #(when (and (empty? @text-value) (not images?)) (reset! z-index 0))
                                      300))))
                 [(and (empty? @text-value) (not images?))])
  [reanimated/view
   {:style (style/send-button btn-opacity @z-index)}
   [quo/button
    {:icon                true
     :size                32
     :accessibility-label :send-message-button
     :on-press            #(send-message state animations window-height)}
    :i/arrow-up]])

(defn send-button
  [{:keys [text-value] :as state} animations window-height images?]
  (let [btn-opacity (reanimated/use-shared-value 0)
        z-index     (reagent/atom (if (and (empty? @text-value) (not images?)) 0 1))]
    [:f> f-send-button state animations window-height images? btn-opacity z-index]))

(defn audio-button
  [{:keys [record-reset-fn]}
   {:keys [record-permission? recording? gesture-enabled? focused?]}
   {:keys [container-opacity]}]
  (let [audio (rf/sub [:chats/sending-audio])]
    [rn/view
     {:style          (style/record-audio-container)
      :pointer-events :box-none}
     [quo/record-audio
      {:record-audio-permission-granted    @record-permission?
       :on-init                            (fn [reset-fn]
                                             (reset! record-reset-fn reset-fn))
       :on-start-recording                 (fn []
                                             (reset! recording? true)
                                             (reset! gesture-enabled? false)
                                             (reanimated/animate container-opacity 1))
       :audio-file                         audio
       :on-reviewing-audio                 (fn [file]
                                             (rf/dispatch [:chat.ui/set-input-audio file]))
       :on-send                            (fn [{:keys [file-path duration]}]
                                             (reset! recording? false)
                                             (reset! gesture-enabled? true)
                                             (rf/dispatch [:chat/send-audio file-path duration])
                                             (when-not @focused?
                                               (reanimated/animate container-opacity
                                                                   constants/empty-opacity))
                                             (rf/dispatch [:chat.ui/set-input-audio nil]))
       :on-cancel                          (fn []
                                             (when @recording?
                                               (reset! recording? false)
                                               (reset! gesture-enabled? true)
                                               (when-not @focused?
                                                 (reanimated/animate container-opacity
                                                                     constants/empty-opacity))
                                               (rf/dispatch [:chat.ui/set-input-audio nil])))
       :on-check-audio-permissions         (fn []
                                             (permissions/permission-granted?
                                              :record-audio
                                              #(reset! record-permission? %)
                                              #(reset! record-permission? false)))
       :on-request-record-audio-permission (fn []
                                             (rf/dispatch
                                              [:request-permissions
                                               {:permissions [:record-audio]
                                                :on-allowed
                                                #(reset! record-permission? true)
                                                :on-denied
                                                #(js/setTimeout
                                                  (fn []
                                                    (alert/show-popup
                                                     (i18n/label :t/audio-recorder-error)
                                                     (i18n/label
                                                      :t/audio-recorder-permissions-error)))
                                                  50)}]))}]]))


(defn camera-button
  []
  [quo/button
   {:on-press #(js/alert "to be implemented")
    :icon     true
    :type     :outline
    :size     32
    :style    {:margin-right 12}}
   :i/camera])

(defn open-photo-selector
  [{:keys [input-ref]}
   {:keys [height]}
   insets]
  (permissions/request-permissions
   {:permissions [:read-external-storage :write-external-storage]
    :on-allowed  (fn []
                   (when (and platform/android? @input-ref)
                     (.blur ^js @input-ref))
                   (rf/dispatch [:chat.ui/set-input-content-height
                                 (reanimated/get-shared-value height)])
                   (rf/dispatch [:open-modal :photo-selector {:insets insets}]))
    :on-denied   (fn []
                   (alert/show-popup (i18n/label :t/error)
                                     (i18n/label
                                      :t/external-storage-denied)))}))

(defn image-button
  [props animations insets]
  [quo/button
   {:on-press #(open-photo-selector props animations insets)
    :icon     true
    :type     :outline
    :size     32
    :style    {:margin-right 12}}
   :i/image])

(defn reaction-button
  []
  [quo/button
   {:on-press #(js/alert "to be implemented")
    :icon     true
    :type     :outline
    :size     32
    :style    {:margin-right 12}}
   :i/reaction])

(defn format-button
  []
  [quo/button
   {:on-press #(js/alert "to be implemented")
    :icon     true
    :type     :outline
    :size     32}
   :i/format])

(defn view
  [props state animations window-height insets {:keys [edit images]}]
  [rn/view {:style style/actions-container}
   [rn/view
    {:style {:flex-direction :row
             :display        (if @(:recording? state) :none :flex)}}
    [camera-button]
    [image-button props animations insets]
    [reaction-button]
    [format-button]]
   [:f> send-button state animations window-height images]
   (when (and (not edit) (not images))
     [audio-button props state animations])])
