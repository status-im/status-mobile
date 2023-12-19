(ns quo.components.drawers.drawer-top.view
  (:require
    [quo.components.avatars.account-avatar.view :as account-avatar]
    [quo.components.avatars.icon-avatar :as icon-avatar]
    [quo.components.avatars.user-avatar.view :as user-avatar]
    [quo.components.buttons.button.view :as button]
    [quo.components.drawers.drawer-top.style :as style]
    [quo.components.icon :as icons]
    [quo.components.markdown.text :as text]
    [quo.components.tags.context-tag.view :as context-tag]
    [quo.components.wallet.address-text.view :as address-text]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [utils.i18n :as i18n]))

(def ^:private left-image-supported-types #{:account :keypair :default-keypair})

(defn- left-image
  [{:keys [type customization-color account-avatar-emoji account-avatar-type icon-avatar
           profile-picture]}]
  (case type
    :account         [account-avatar/view
                      {:customization-color customization-color
                       :size                32
                       :emoji               account-avatar-emoji
                       :type                (or account-avatar-type :default)}]
    :keypair         [icon-avatar/icon-avatar
                      {:icon    icon-avatar
                       :border? true
                       :color   :neutral}]

    :default-keypair [user-avatar/user-avatar
                      {:size              :small
                       :status-indicator? false
                       :profile-picture   profile-picture}]
    nil))

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

(defn- account-subtitle
  [{:keys [networks theme blur? description]}]
  (if networks
    [address-text/view
     {:networks networks
      :address  description
      :format   :short}]
    [text/text
     {:size   :paragraph-2
      :weight :regular
      :style  (style/description theme blur?)}
     description]))

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
    [account-subtitle
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
  [{:keys [type label title title-icon theme blur?]}]
  (case type
    :label [text/text
            {:weight :medium
             :size   :paragraph-2
             :style  (style/description theme blur?)}
            label]
    [rn/view {:style style/title-container}
     [text/text
      {:size   :heading-2
       :weight :semi-bold}
      title]
     (when title-icon
       [icons/icon title-icon
        {:container-style style/title-icon
         :size            20
         :color           (if blur?
                            colors/white-opa-40
                            (colors/theme-colors colors/neutral-50 colors/neutral-40 theme))}])]))

(defn- view-internal
  [{:keys [title title-icon type theme description blur? community-name community-logo button-icon
           on-button-press
           on-button-long-press
           button-disabled? account-avatar-emoji account-avatar-type customization-color icon-avatar
           profile-picture keycard? networks label]}]
  [rn/view {:style style/container}
   (when (left-image-supported-types type)
     [rn/view {:style style/left-container}
      [left-image
       {:type                 type
        :customization-color  customization-color
        :account-avatar-emoji account-avatar-emoji
        :account-avatar-type  account-avatar-type
        :icon-avatar          icon-avatar
        :profile-picture      profile-picture}]])
   [rn/view {:style style/body-container}
    [left-title
     {:type       type
      :label      label
      :title      title
      :title-icon title-icon
      :theme      theme
      :blur?      blur?}]
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
