(ns quo2.components.profile.profile-card.view
  (:require [utils.i18n :as i18n]
            [react-native.core :as rn]
            [quo2.components.icon :as icon]
            [quo2.components.tags.tag :as tag]
            [quo2.foundations.colors :as colors]
            [quo2.components.markdown.text :as text]
            [quo2.components.buttons.button :as button]
            [quo2.components.avatars.user-avatar :as user-avatar]
            [quo2.components.profile.profile-card.style :as style]))

(defn profile-card
  [{:keys [key-card? profile-picture name hash customization-color
           emoji-hash on-options-press show-emoji-hash? padding-bottom
           show-options-button? show-user-hash? show-logged-in? on-card-press]
    :or   {show-emoji-hash?     false
           show-user-hash?      false
           customization-color  :turquoise
           show-options-button? false
           show-logged-in?      false
           key-card?            false}}]
  [rn/touchable-without-feedback
   {:on-press            on-card-press
    :flex                1
    :accessibility-label :profile-card}
   [rn/view
    (style/card-container
     customization-color
     (or padding-bottom (if show-emoji-hash? 12 10)))
    [rn/view
     {:style style/card-header}
     [user-avatar/user-avatar
      {:full-name         name
       :profile-picture   profile-picture
       :override-theme    :dark
       :size              :medium
       :status-indicator? false}]
     [rn/view {:flex-direction :row}
      (when show-logged-in?
        [tag/tag
         {:type                :icon
          :size                32
          :blurred?            true
          :labelled?           true
          :resource            :main-icons2/check
          :accessibility-label :logged-in-tag
          :icon-color          colors/success-50
          :override-theme      :dark
          :label               (i18n/label :t/logged-in)}])
      (when show-options-button?
        [button/button
         {:size                32
          :type                :blur-bg
          :icon                true
          :override-theme      :dark
          :style               style/option-button
          :on-press            on-options-press
          :accessibility-label :profile-card-options}
         :i/options])]]
    [rn/view
     {:style style/name-container}
     [text/text
      {:size            :heading-2
       :weight          :semi-bold
       :number-of-lines 1
       :style           style/user-name} name]
     (when key-card?
       (icon/icon
        :i/keycard
        style/keycard-icon))]
    (when show-user-hash?
      [text/text
       {:weight          :monospace
        :number-of-lines 1
        :style           style/user-hash} hash])
    (when (and show-emoji-hash? emoji-hash)
      [text/text
       {:weight          :monospace
        :number-of-lines 1
        :style           style/emoji-hash} emoji-hash])]])
