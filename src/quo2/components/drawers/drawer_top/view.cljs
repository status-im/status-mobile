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
            [quo2.foundations.colors :as colors]
            [utils.i18n :as i18n]))

(defn- left-image
  [{:keys [type customization-color account-avatar-emoji icon-avatar profile-picture]}]
  (case type
    :account         [account-avatar/view
                      {:customization-color customization-color
                       :size                32
                       :emoji               account-avatar-emoji
                       :type                :default}]
    :keypair         [icon-avatar/icon-avatar
                      {:icon    icon-avatar
                       :border? true
                       :color   :neutral}]

    :default-keypair [user-avatar/user-avatar
                      {:size              :small
                       :status-indicator? false
                       :profile-picture   profile-picture}]
    nil))

(defn- network-view
  [network]
  [text/text
   {:size   :paragraph-2
    :weight :regular
    :style  (style/network-text-color network)}
   (str (subs (name network) 0 3) ":")])

(defn- keypair-subtitle
  [{:keys [theme blur? keycard?]}]
  [rn/view {:style style/row}
   [text/text
    {:size   :paragraph-2
     :weight :regular
     :style  (style/description theme blur?)}
    (if keycard?
      (i18n/label :t/on-keycard)
      (i18n/label :t/on-device))]
   (when keycard?
     [icons/icon
      :i/keycard-card
      {:color           (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)
       :size            16
       :container-style style/keycard-icon}])])

(defn- acocunt-subtitle
  [{:keys [networks theme blur? description]}]
  [rn/view {:style style/row}
   (for [network networks]
     ^{:key (str network)}
     [network-view network])
   [text/text
    {:size   :paragraph-2
     :weight :regular
     :style  (style/description theme blur?)}
    description]])

(defn- default-keypair-subtitle
  [{:keys [description theme blur?]}]
  [text/text
   {:accessibility-label :default-keypair-text
    :size                :paragraph-2
    :weight              :regular
    :style               (style/description theme blur?)}
   (str description " · " (i18n/label :t/on-device))])

(defn- context-tag-subtitle
  [{:keys [community-logo community-name]}]
  [rn/view
   {:accessibility-label :context-tag-wrapper
    :style               {:flex-wrap :wrap}}
   [context-tag/view
    {:type           :community
     :community-name community-name
     :community-logo community-logo
     :size           24}]])

(defn- description-subtitle
  [{:keys [theme blur? description]}]
  [text/text
   {:size   :paragraph-1
    :weight :regular
    :style  (style/description theme blur?)}
   description])

(defn- subtitle
  [{:keys [type theme blur? keycard? networks description community-name community-logo]}]
  (cond
    (= :keypair type)
    [keypair-subtitle
     {:theme    theme
      :blur?    blur?
      :keycard? keycard?}]

    (= :account type)
    [acocunt-subtitle
     {:networks    networks
      :theme       theme
      :blur?       blur?
      :description description}]

    (= :default-keypair type)
    [default-keypair-subtitle
     {:description description
      :theme       theme
      :blur?       blur?}]

    (= :context-tag type)
    [context-tag-subtitle
     {:community-logo community-logo
      :community-name community-name}]

    (and (not= :label type) description)
    [description-subtitle
     {:theme       theme
      :blur?       blur?
      :description description}]))

(defn- right-icon
  [{:keys [theme type on-button-press on-button-long-press button-disabled? button-icon]}]
  (cond
    (= :info type)
    [icons/icon
     :i/info
     {:accessibility-label :info-icon
      :size                20
      :color               (colors/theme-colors colors/neutral-50
                                                colors/neutral-40
                                                theme)}]
    (and (= :context-tag type) button-icon)
    [button/button
     {:accessibility-label :button-icon
      :on-press            on-button-press
      :on-long-press       on-button-long-press
      :disabled?           button-disabled?
      :type                :primary
      :size                24
      :icon-only?          true}
     button-icon]))

(defn- left-title
  [{:keys [type label title theme blur?]}]
  (case type
    :label [text/text
            {:weight :medium
             :size   :paragraph-2
             :style  (style/description theme blur?)}
            label]
    [text/text
     {:size   :heading-2
      :weight :semi-bold}
     title]))

(defn- view-internal
  [{:keys [title type theme description blur? community-name community-logo button-icon on-button-press
           on-button-long-press
           button-disabled? account-avatar-emoji customization-color icon-avatar
           profile-picture keycard? networks label]}]
  [rn/view {:style style/container}
   [rn/view {:style style/left-container}
    [left-image
     {:type                 type
      :customization-color  customization-color
      :account-avatar-emoji account-avatar-emoji
      :icon-avatar          icon-avatar
      :profile-picture      profile-picture}]]
   [rn/view {:style style/body-container}
    [left-title
     {:type  type
      :label label
      :title title
      :theme theme
      :blur? blur?}]
    [subtitle
     {:type           type
      :theme          theme
      :blur?          blur?
      :keycard?       keycard?
      :networks       networks
      :description    description
      :community-name community-name
      :community-logo community-logo}]]
   [right-icon
    {:theme                theme
     :type                 type
     :on-button-press      on-button-press
     :on-button-long-press on-button-long-press
     :button-disabled?     button-disabled?
     :button-icon          button-icon}]])

(def view (quo.theme/with-theme view-internal))
