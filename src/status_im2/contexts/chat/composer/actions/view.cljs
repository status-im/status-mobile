(ns status-im2.contexts.chat.composer.actions.view
  (:require
    [quo2.core :as quo]
    [quo2.foundations.colors :as colors]
    [react-native.core :as rn]
    [react-native.permissions :as permissions]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [reagent.core :as reagent]
    [status-im2.common.alert.events :as alert]
    [status-im2.contexts.chat.composer.constants :as comp-constants]
    [status-im2.common.device-permissions :as device-permissions]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [status-im2.contexts.chat.composer.actions.style :as style]
    [status-im2.constants :as constants]))

(defn send-message
  [{:keys [sending-images? sending-links?]}
   {:keys [text-value focused? maximized?]}
   {:keys [height saved-height last-height opacity background-y container-opacity]}
   window-height
   edit
   scroll-to-bottom-fn]
  (reanimated/animate height comp-constants/input-height)
  (reanimated/set-shared-value saved-height comp-constants/input-height)
  (reanimated/set-shared-value last-height comp-constants/input-height)
  (reanimated/animate opacity 0)
  (when-not @focused?
    (js/setTimeout #(reanimated/animate container-opacity comp-constants/empty-opacity) 300))
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
  (when (and (not (some? edit)) scroll-to-bottom-fn)
    (scroll-to-bottom-fn)))

(defn f-send-button
  [props state animations window-height images? btn-opacity scroll-to-bottom-fn z-index edit]
  (let [{:keys [text-value]} state
        customization-color  (rf/sub [:profile/customization-color])]
    (rn/use-effect (fn []
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
      {:icon-only? true
       :size 32
       :customization-color customization-color
       :accessibility-label :send-message-button
       :on-press #(send-message props state animations window-height edit scroll-to-bottom-fn)}
      :i/arrow-up]]))

(defn send-button
  [props {:keys [text-value] :as state} animations window-height images? edit btn-opacity
   scroll-to-bottom-fn]
  (let [z-index (reagent/atom (if (and (empty? @text-value) (not images?)) 0 1))]
    [:f> f-send-button props state animations window-height images? btn-opacity scroll-to-bottom-fn
     z-index edit]))

(defn disabled-audio-button
  [opacity]
  [reanimated/view {:style (reanimated/apply-animations-to-style {:opacity opacity} {})}
   [quo/composer-button
    {:on-press #(js/alert "to be implemented")
     :icon     :i/audio}]])

(defn audio-button
  [{:keys [record-reset-fn input-ref]}
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
                                             (rf/dispatch [:chat.ui/set-recording true])
                                             (reset! recording? true)
                                             (reset! gesture-enabled? false)
                                             (reanimated/animate container-opacity 1))
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
                                             (if-not @focused?
                                               (reanimated/animate container-opacity
                                                                   comp-constants/empty-opacity)
                                               (js/setTimeout #(when @input-ref (.focus ^js @input-ref))
                                                              300))
                                             (rf/dispatch [:chat.ui/set-input-audio nil]))
       :on-cancel                          (fn []
                                             (when @recording?
                                               (rf/dispatch [:chat.ui/set-recording false])
                                               (reset! recording? false)
                                               (reset! gesture-enabled? true)
                                               (if-not @focused?
                                                 (reanimated/animate container-opacity
                                                                     comp-constants/empty-opacity)
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
                                                    (alert/show-popup
                                                     (i18n/label :t/audio-recorder-error)
                                                     (i18n/label
                                                      :t/audio-recorder-permissions-error)
                                                     nil
                                                     {:text (i18n/label :t/settings)
                                                      :accessibility-label :settings-button
                                                      :onPress (fn [] (permissions/open-settings))}))
                                                  50)}]))
       :max-duration-ms                    constants/audio-max-duration-ms}]]))

(defn images-limit-toast
  []
  (rf/dispatch [:toasts/upsert
                {:id              :random-id
                 :icon            :info
                 :icon-color      colors/danger-50-opa-40
                 :container-style {:top (when platform/ios? 20)}
                 :text            (i18n/label :t/only-6-images)}]))


(defn go-to-camera
  [images-count]
  (device-permissions/camera #(if (>= images-count constants/max-album-photos)
                                (images-limit-toast)
                                (rf/dispatch [:navigate-to :camera-screen]))))

(defn camera-button
  []
  (let [images-count (count (vals (rf/sub [:chats/sending-image])))]
    [quo/composer-button
     {:on-press        #(go-to-camera images-count)
      :icon            :i/camera
      :container-style {:margin-right 12}}]))


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
  [quo/composer-button
   {:on-press            #(open-photo-selector props animations insets)
    :accessibility-label :open-images-button
    :container-style     {:margin-right 12}
    :icon                :i/image}])

(defn reaction-button
  []
  [quo/composer-button
   {:icon            :i/reaction
    :on-press        #(js/alert "to be implemented")
    :container-style {:margin-right 12}}])

(defn format-button
  []
  [quo/composer-button
   {:on-press #(js/alert "to be implemented")
    :icon     :i/format}])

(defn view
  [props state animations window-height insets scroll-to-bottom-fn {:keys [edit images]}]
  (let [send-btn-opacity  (reanimated/use-shared-value 0)
        audio-btn-opacity (reanimated/interpolate send-btn-opacity [0 1] [1 0])]
    [rn/view {:style style/actions-container}
     [rn/view
      {:style {:flex-direction :row
               :display        (if @(:recording? state) :none :flex)}}
      [camera-button]
      [image-button props animations insets]
      [reaction-button]
      [format-button]]
     [:f> send-button props state animations window-height images edit send-btn-opacity
      scroll-to-bottom-fn]
     (when (and (not edit) (not images))
       ;; TODO(alwx): needs to be replaced with an `audio-button` later.
       ;; See https://github.com/status-im/status-mobile/issues/16084 for more details.
       [:f> disabled-audio-button audio-btn-opacity])]))
