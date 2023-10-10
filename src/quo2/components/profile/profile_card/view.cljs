(ns quo2.components.profile.profile-card.view
  (:require [utils.i18n :as i18n]
            [react-native.core :as rn]
            [quo2.components.icon :as icon]
            [quo2.components.tags.tag :as tag]
            [quo2.foundations.colors :as colors]
            [react-native.hole-view :as hole-view]
            [quo2.components.markdown.text :as text]
            [quo2.components.buttons.button.view :as button]
            [quo2.components.profile.profile-card.style :as style]
            [quo2.components.avatars.user-avatar.view :as user-avatar]))

(defn- f-profile-card-component
  [{:keys [keycard-account? profile-picture name
           customization-color emoji-hash on-options-press
           show-emoji-hash? show-options-button? show-user-hash?
           show-logged-in? on-card-press login-card? last-item? card-style]
    :or   {show-emoji-hash?     false
           show-user-hash?      false
           customization-color  :turquoise
           show-options-button? false
           show-logged-in?      false
           keycard-account?     false
           login-card?          false
           last-item?           false
           card-style           {:padding-horizontal 20
                                 :flex               1}}
    :as   args}]
  (let [{:keys [width]}      (rn/get-window)
        padding-bottom       (cond
                               login-card?      38
                               show-emoji-hash? 12
                               :else            10)
        border-bottom-radius (if (or (not login-card?) last-item?) 16 0)]
    [rn/touchable-without-feedback
     {:on-press            on-card-press
      :accessibility-label :profile-card}
     [hole-view/hole-view
      {:key   (str name last-item?) ;; Key is required to force removal of holes
       :style (merge {:flex-direction :row} card-style)
       :holes (if (or (not login-card?) last-item?)
                []
                [{:x            20
                  :y            108
                  :width        (- width 40)
                  :height       50
                  :borderRadius 16}])}
      [rn/view
       {:style (style/card-container
                {:customization-color  customization-color
                 :padding-bottom       padding-bottom
                 :border-bottom-radius border-bottom-radius})}
       [rn/view
        {:style style/card-header}
        [user-avatar/user-avatar
         {:full-name           name
          :profile-picture     profile-picture
          :size                :medium
          :status-indicator?   false
          :customization-color customization-color
          :static?             true}]
        [rn/view {:flex-direction :row}
         (when show-logged-in?
           [tag/tag
            {:type                :icon
             :size                32
             :blurred?            true
             :labelled?           true
             :resource            :i/check
             :accessibility-label :logged-in-tag
             :icon-color          colors/success-50
             :override-theme      :dark
             :label               (i18n/label :t/logged-in)}])
         (when show-options-button?
           [button/button
            {:size                32
             :type                :grey
             :background          :blur
             :icon-only?          true
             :container-style     style/option-button
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
        (when keycard-account?
          (icon/icon
           :i/keycard
           style/keycard-icon))]
       (when show-user-hash?
         [text/text
          {:weight :monospace
           :style  style/user-hash}
          (:hash args)])
       (when (and show-emoji-hash? emoji-hash)
         [text/text
          {:weight          :monospace
           :number-of-lines 1
           :style           style/emoji-hash} emoji-hash])]]]))

(defn profile-card
  [props]
  [:f> f-profile-card-component props])
