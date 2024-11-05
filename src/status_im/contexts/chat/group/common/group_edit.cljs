(ns status-im.contexts.chat.group.common.group-edit
  (:require [quo.core :as quo]
            [quo.theme]
            [react-native.core :as rn]
            [status-im.common.avatar-picture-picker.view :as avatar-picture-picker]
            [status-im.common.floating-button-page.view :as floating-button-page]
            [status-im.constants :as constants]
            [status-im.contexts.chat.group.common.style :as style]
            [status-im.contexts.profile.utils :as profile.utils]
            [utils.debounce :as debounce]
            utils.emojilib
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]
            [utils.responsiveness :as responsiveness]))

(defn avatar
  [{:keys [customization-color group-image set-group-image]}]
  (let [on-press (rn/use-callback (fn []
                                    (rf/dispatch
                                     [:show-bottom-sheet
                                      {:content (fn []
                                                  [avatar-picture-picker/view
                                                   {:on-result set-group-image}])}])))]
    [rn/view {:style style/avatar}
     ;;NOTE with hole-view group-avatar doesn't change it's background color
     #_[hole-view/hole-view
        {:holes [style/hole]}]
     [quo/group-avatar
      {:customization-color customization-color
       :size                :size-80
       :picture             group-image}]
     [quo/button
      {:on-press        on-press
       :container-style style/camera
       :icon-only?      true
       :type            :grey
       :background      :photo
       :size            32}
      :i/camera]]))

(defn view
  [{:keys [default-group-name default-group-color default-group-image contacts
           submit-button-label submit-event back-button-icon chat-id close on-success editing-group?]}]
  (let [theme                         (quo.theme/use-theme)
        {window-width :width}         (rn/get-window)
        contacts-count                (count contacts)
        [group-name set-group-name]   (rn/use-state default-group-name)
        [error-message
         set-error-message]           (rn/use-state nil)
        group-name-empty?             (not (and (string? group-name) (not-empty group-name)))
        [group-color set-group-color] (rn/use-state default-group-color)
        [group-image set-group-image] (rn/use-state default-group-image)
        on-submit                     (rn/use-callback #(debounce/throttle-and-dispatch
                                                         [submit-event
                                                          {:group-name  group-name
                                                           :group-color group-color
                                                           :group-image (when (not= group-image
                                                                                    default-group-image)
                                                                          group-image)
                                                           :chat-id     chat-id
                                                           :on-success  on-success}]
                                                         300)
                                                       [group-name group-color group-image])
        on-change-text                (rn/use-callback
                                       (fn [text]
                                         (if (boolean (re-find utils.emojilib/emoji-regex text))
                                           (set-error-message (i18n/label :t/are-not-allowed
                                                                          {:check (i18n/label
                                                                                   :t/emojis)}))
                                           (set-error-message nil))
                                         (set-group-name text)))
        disabled?                     (or group-name-empty?
                                          error-message
                                          (and editing-group?
                                               (= group-name default-group-name)
                                               (= group-color default-group-color)
                                               (= group-image default-group-image)))]
    [floating-button-page/view
     {:customization-color    group-color
      :gradient-cover?        true
      :header-container-style {:margin-top 8}
      :header                 [quo/page-nav
                               {:background :photo
                                :type       :no-title
                                :icon-name  back-button-icon
                                :on-press   close}]
      :footer                 [quo/button
                               {:customization-color group-color
                                :disabled?           disabled?
                                :on-press            on-submit}
                               submit-button-label]}
     [:<>
      [avatar
       {:customization-color group-color
        :group-image         group-image
        :set-group-image     set-group-image}]
      [quo/title-input
       {:on-change-text  on-change-text
        :default-value   default-group-name
        :container-style {:padding-horizontal 20 :padding-top 12 :padding-bottom (if error-message 0 16)}
        :placeholder     (i18n/label :t/name-your-group)
        :max-length      constants/max-group-chat-name-length}]
      (when error-message
        [quo/info-message
         {:status          :error
          :icon            :i/info
          :size            :default
          :container-style {:margin-top 8 :margin-left 20 :margin-bottom 16}}
         error-message])
      [quo/divider-line]
      [rn/view
       {:style {:padding-vertical 7}}
       [quo/text
        {:size   :paragraph-2
         :weight :medium
         :style  (style/color-label theme)}
        (i18n/label :t/accent-colour)]
       [quo/color-picker
        {:default-selected group-color
         :on-change        set-group-color
         :container-style  {:padding-top    15
                            :padding-bottom 9
                            :padding-left   (responsiveness/iphone-11-Pro-20-pixel-from-width
                                             window-width)}}]]
      [quo/divider-label
       (i18n/label :t/n-m-people {:n contacts-count :m constants/max-group-chat-participants})]
      [rn/view {:style style/tags}
       (for [contact contacts]
         ^{:key contact}
         [quo/context-tag
          {:container-style style/tag
           :size            24
           :profile-picture (profile.utils/photo contact)
           :full-name       (profile.utils/displayed-name contact)}])]]]))
