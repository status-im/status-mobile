(ns quo2.components.profile.profile-card.view
  (:require
   [quo2.components.profile.profile-card.style :as style]
   [quo2.foundations.colors :as colors]
   [quo2.components.avatars.user-avatar :as user-avatar]
   [quo2.components.icon :as icon]
   [quo2.components.markdown.text :as text]
   [quo2.components.buttons.button :as button]
   [react-native.core :as rn]))


(defn profile-card
  [{:keys [show-sign-profile? key-card? profile-picture name hash customization-color sign-label
           emoji-hash on-press-dots on-press-sign show-emoji-hash?]
    :or   {show-sign-profile?  false
           show-emoji-hash?    false
           customization-color :turquoise
           key-card?           false}}]
  [rn/view
   {:style (style/card-container customization-color)}
   [rn/view
    {:style style/card-header}
    [user-avatar/user-avatar
     {:full-name         name
      :profile-picture   profile-picture
      :override-theme    :dark
      :size              :medium
      :status-indicator? false
      :ring?             true}]
    [button/button
     {:size           32
      :type           :blur-bg
      :icon           true
      :override-theme :dark
      :style          style/option-button
      :on-press       on-press-dots}
     :i/options]]
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
   [text/text
    {:weight          :monospace
     :number-of-lines 1
     :style           style/user-hash} hash]
   (when (and show-emoji-hash? emoji-hash)
     [text/text
      {:weight          :monospace
       :number-of-lines 1
       :style           style/emoji-hash} emoji-hash])
   (when show-sign-profile?
     [button/button
      {:on-press             on-press-sign
       :type                 :community
       :community-color      (colors/custom-color customization-color 60)
       :community-text-color colors/white
       :style                style/sign-button} sign-label])])
