(ns status-im.contexts.chat.messenger.composer.actions.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.permissions :as permissions]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [reagent.core :as reagent]
    [status-im.common.alert.effects :as alert.effects]
    [status-im.common.device-permissions :as device-permissions]
    [status-im.constants :as constants]
    [status-im.contexts.chat.messenger.composer.actions.style :as style]
    [status-im.contexts.chat.messenger.composer.constants :as comp-constants]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn send-message
  "Minimize composer, animate-out background overlay, clear input and flush state"
  [{:keys [sending-images? sending-links?]}
   {:keys [text-value maximized?]}
   {:keys [height saved-height last-height opacity background-y]}
   window-height
   edit]
  (reanimated/animate height comp-constants/input-height)
  (reanimated/set-shared-value saved-height comp-constants/input-height)
  (reanimated/set-shared-value last-height comp-constants/input-height)
  (reanimated/animate opacity 0)
  (js/setTimeout #(reanimated/set-shared-value background-y
                                               (- window-height))
                 300)
  (rf/dispatch [:chat.ui/send-current-message])
  (rf/dispatch [:chat.ui/set-input-maximized false])
  (rf/dispatch [:chat.ui/set-input-content-height comp-constants/input-height])
  (rf/dispatch [:chat.ui/set-chat-input-text nil])
  (reset! maximized? false)
  (reset! text-value "")
  (reset! sending-links? false)
  (reset! sending-images? false)
  (when-not (some? edit)
    (rf/dispatch [:chat.ui/scroll-to-bottom])))

(defn f-send-button
  [props state animations window-height images? btn-opacity z-index edit]
  (let [{:keys [text-value]}        state
        profile-customization-color (rf/sub [:profile/customization-color])
        {:keys      [chat-id chat-type]
         chat-color :color}         (rf/sub [:chats/current-chat-chat-view])
        contact-customization-color (when (= chat-type constants/one-to-one-chat-type)
                                      (rf/sub [:contacts/contact-customization-color-by-address
                                               chat-id]))]
    (rn/use-effect (fn []
                     ;; Handle send button opacity animation and z-index when input content changes
                     (if (or (seq @text-value) images?)
                       (when (or (not= @z-index 1) (not= (reanimated/get-shared-value btn-opacity) 1))
                         (reset! z-index 1)
                         (js/setTimeout #(reanimated/animate btn-opacity 1) 50))
                       (when (or (not= @z-index 0) (not= (reanimated/get-shared-value btn-opacity) 0))
                         (reanimated/animate btn-opacity 0)
                         (js/setTimeout #(when (and (empty? @text-value) (not images?))
                                           (reset! z-index 0))
                                        300))))
                   [(and (empty? @text-value) (not images?))])
    [reanimated/view
     {:style (style/send-button btn-opacity @z-index)}
     [quo/button
      {:icon-only?          true
       :size                32
       :customization-color (or contact-customization-color chat-color profile-customization-color)
       :accessibility-label :send-message-button
       :on-press            #(send-message props state animations window-height edit)}
      :i/arrow-up]]))

(defn send-button
  [props {:keys [text-value] :as state} animations window-height images? edit btn-opacity]
  (let [z-index (reagent/atom (if (and (empty? @text-value) (not images?)) 0 1))]
    [:f> f-send-button props state animations window-height images? btn-opacity z-index edit]))

(defn disabled-audio-button
  [opacity]
  [reanimated/view {:style (reanimated/apply-animations-to-style {:opacity opacity} {})}
   [quo/composer-button
    {:on-press (fn []
                 (rf/dispatch [:chat.ui/set-input-focused false])
                 (rn/dismiss-keyboard!)
                 (js/alert "to be implemented"))
     :icon     :i/audio}]])

(defn audio-button
  [{:keys [record-reset-fn input-ref]}
   {:keys [record-permission? recording? gesture-enabled? focused?]}]
  (let [audio (rf/sub [:chats/sending-audio])]
    [rn/view
     {:style          (style/record-audio-container)
      :pointer-events :box-none}
     [quo/record-audio
      {:record-audio-permission-granted    @record-permission?
       :on-init                            (fn [reset-fn]
                                             (reset! record-reset-fn reset-fn))
       :on-start-recording                 (fn []
                                             (rf/dispatch [:chat.ui/set-recording true])
                                             (reset! recording? true)
                                             (reset! gesture-enabled? false))
       :audio-file                         audio
       :on-lock                            (fn []
                                             (rf/dispatch [:chat.ui/set-recording false]))
       :on-reviewing-audio                 (fn [file]
                                             (rf/dispatch [:chat.ui/set-recording false])
                                             (rf/dispatch [:chat.ui/set-input-audio file]))
       :on-send                            (fn [{:keys [file-path duration]}]
                                             (rf/dispatch [:chat.ui/set-recording false])
                                             (reset! recording? false)
                                             (reset! gesture-enabled? true)
                                             (rf/dispatch [:chat/send-audio file-path duration])
                                             (when @focused?
                                               (js/setTimeout #(when @input-ref (.focus ^js @input-ref))
                                                              300))
                                             (rf/dispatch [:chat.ui/set-input-audio nil]))
       :on-cancel                          (fn []
                                             (when @recording?
                                               (rf/dispatch [:chat.ui/set-recording false])
                                               (reset! recording? false)
                                               (reset! gesture-enabled? true)
                                               (when @focused?
                                                 (js/setTimeout #(when @input-ref
                                                                   (.focus ^js @input-ref))
                                                                300))
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
                                                    (alert.effects/show-popup
                                                     (i18n/label :t/audio-recorder-error)
                                                     (i18n/label
                                                      :t/audio-recorder-permissions-error)
                                                     nil
                                                     {:text (i18n/label :t/settings)
                                                      :accessibility-label :settings-button
                                                      :onPress (fn [] (permissions/open-settings))}))
                                                  50)}]))
       :max-duration-ms                    constants/audio-max-duration-ms}]]))

(defn photo-limit-toast
  []
  (rf/dispatch [:toasts/upsert
                {:id   :random-id
                 :type :negative
                 :text (i18n/label :t/hit-photos-limit
                                   {:max-photos constants/max-album-photos})}]))


(defn go-to-camera
  [images-count]
  (device-permissions/camera #(if (>= images-count constants/max-album-photos)
                                (photo-limit-toast)
                                (rf/dispatch [:navigate-to :camera-screen]))))

(defn camera-button
  [edit]
  (let [images-count (count (vals (rf/sub [:chats/sending-image])))]
    [quo/composer-button
     {:on-press            (if edit
                             #(js/alert "This feature is temporarily unavailable in edit mode.")
                             #(go-to-camera images-count))
      :accessibility-label :camera-button
      :icon                :i/camera
      :container-style     {:margin-right 12}}]))


(defn open-photo-selector
  [{:keys [input-ref]}
   {:keys [height]}]
  (permissions/request-permissions
   {:permissions [(if platform/is-below-android-13? :read-external-storage :read-media-images)
                  :write-external-storage]
    :on-allowed  (fn []
                   (when (and platform/android? @input-ref)
                     (.blur ^js @input-ref))
                   (rf/dispatch [:chat.ui/set-input-content-height
                                 (reanimated/get-shared-value height)])
                   (rf/dispatch [:photo-selector/navigate-to-photo-selector]))
    :on-denied   (fn []
                   (alert.effects/show-popup (i18n/label :t/error)
                                             (i18n/label
                                              :t/external-storage-denied)))}))

(defn image-button
  [props animations edit]
  [quo/composer-button
   {:on-press            (if edit
                           #(js/alert "This feature is temporarily unavailable in edit mode.")
                           #(open-photo-selector props animations))
    :accessibility-label :open-images-button
    :container-style     {:margin-right 12}
    :icon                :i/image}])

(defn reaction-button
  []
  [quo/composer-button
   {:icon            :i/reaction
    :on-press        (fn []
                       (rf/dispatch [:chat.ui/set-input-focused false])
                       (rn/dismiss-keyboard!)
                       (js/alert "to be implemented"))
    :container-style {:margin-right 12}}])

(defn format-button
  []
  [quo/composer-button
   {:on-press (fn []
                (rf/dispatch [:chat.ui/set-input-focused false])
                (rn/dismiss-keyboard!)
                (js/alert "to be implemented"))
    :icon     :i/format}])

(defn view
  [props state animations window-height {:keys [edit images]}]
  (let [send-btn-opacity  (reanimated/use-shared-value 0)
        audio-btn-opacity (reanimated/interpolate send-btn-opacity [0 1] [1 0])]
    [rn/view {:style style/actions-container}
     [rn/view
      {:style {:flex-direction :row
               :display        (if @(:recording? state) :none :flex)}}
      [camera-button edit]
      [image-button props animations edit]
      [reaction-button]
      [format-button]]
     [:f> send-button props state animations window-height images edit send-btn-opacity]
     (when (and (not edit) (not images))
       ;; TODO(alwx): needs to be replaced with an `audio-button` later. See
       ;; https://github.com/status-im/status-mobile/issues/16084 for more details.
       [:f> disabled-audio-button audio-btn-opacity])]))
