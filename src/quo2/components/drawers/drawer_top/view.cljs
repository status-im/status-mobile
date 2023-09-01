(ns quo2.components.drawers.drawer-top.view 
  (:require [quo2.theme :as quo.theme]
            [quo2.components.markdown.text :as text]
            [quo2.components.drawers.drawer-top.style :as style]
            [quo2.components.buttons.button.view :as button]
            [quo2.components.tags.context-tag.view :as context-tag]
            [react-native.core :as rn]
            [quo2.components.icon :as icons]
            [quo2.components.avatars.account-avatar.view :as account-avatar]
            [quo2.components.avatars.icon-avatar :as icon-avatar]
            [quo2.components.avatars.user-avatar.view :as user-avatar]
            [quo2.foundations.colors :as colors]))

(defn- view-internal
  [{:keys [title type theme description blur? community-name community-logo button-icon on-button-press on-button-long-press 
           button-disabled account-avatar-emoji account-avatar-customization-color icon-avatar profile-picture] 
    :or {button-disabled false}}]
  [rn/view {:style style/container}
   [rn/view {:style style/left-container}
    (case type
      :account [account-avatar/view {:customization-color account-avatar-customization-color
                                     :size                32
                                     :emoji               account-avatar-emoji
                                     :type                :default}]
      :keypair      [icon-avatar/icon-avatar  {:size  :medium
                                               :icon  icon-avatar
                                               :color :neutral}]

      :default-keypair [user-avatar/user-avatar
                        {:size            :small
                         :profile-picture profile-picture}]
      nil)]
   [rn/view {:style style/body-container}
    [text/text
     {:size :heading-2
      :weight :semi-bold}
     title]
    (cond
      (= :keypair type) [text/text {:size :paragraph-1
                                    :weight :regular
                                    :style (style/description theme blur?)}
                         description]
      description [text/text {:size :paragraph-1
                              :weight :regular
                              :style (style/description theme blur?)}
                   description]
      community-name [rn/view {:style {:flex-wrap :wrap}}
                      [context-tag/view
                       {:type :community
                        :community-name community-name
                        :community-logo      community-logo
                        :size 24}]])]
   (case type
     :info [icons/icon :i/info
            {:accessibility-label :description-icon
             :size                20
             :color  (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)}]
     :button [button/button
              {:on-press on-button-press
               :on-long-press on-button-long-press
               :disabled? button-disabled
               :type   :primary
               :size   24
               :icon-only?   true}
              button-icon]
     nil)])

(def view
  " Create an account-origin UI component.
| key               | values                                         |
| ------------------|------------------------------------------------|
| :type             | :default-keypair :recovery-phrase :private-key
| :stored           | :on-device :on-keycard
| :profile-picture  | image source
| :derivation-path  | string
| :user-name        | string
| :on-press         | function "
  (quo.theme/with-theme view-internal))