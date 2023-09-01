(ns quo2.components.wallet.account-origin.view
  (:require [quo2.theme :as quo.theme]
            [react-native.core :as rn]
            [quo2.components.wallet.account-origin.style :as style]
            [quo2.components.markdown.text :as text]
            [quo2.components.icon :as icons]
            [quo2.components.avatars.user-avatar.view :as user-avatar]
            [utils.i18n :as i18n]
            [quo2.foundations.colors :as colors]))

(defn- row-title
  [type user-name]
  [text/text
   {:weight :medium
    :size   :paragraph-1}
   (case type
     :default-keypair (i18n/label :t/user-keypair {:name user-name})
     :derivation-path (i18n/label :t/derivation-path)
     (i18n/label :t/trip-accounts))])

(defn- row-icon
  [profile-picture type secondary-color]
  (case type
    :default-keypair [user-avatar/user-avatar
                      {:size            :xxs
                       :ring?           false
                       :profile-picture profile-picture}]
    :recovery-phrase [icons/icon
                      :i/seed
                      {:accessibility-label :recovery-phrase-icon
                       :color               secondary-color}]
    :private-key     [icons/icon
                      :i/key
                      {:accessibility-label :private-key-icon
                       :color               secondary-color}]
    :derivation-path [icons/icon
                      :i/derivated-path
                      {:accessibility-label :derivation-path-icon
                       :color               secondary-color}]
    nil))

(defn- row-view
  [{:keys [type theme secondary-color profile-picture title stored subtitle on-press]}]
  [rn/view {:style (style/row-container type theme)}
   [rn/view {:style style/icon-container}
    [row-icon profile-picture type secondary-color]]
   [rn/view
    {:style style/row-content-container}
    [row-title type title]
    [rn/view {:style style/row-subtitle-container}
     [text/text
      {:weight :regular
       :size   :paragraph-2
       :style  (style/stored-title theme)}
      subtitle]
     (when (= :on-keycard stored)
       [icons/icon
        :i/keycard-card
        {:color secondary-color}])]]
   (when (= :derivation-path type)
     [rn/pressable
      {:accessibility-label :derivation-path-button
       :on-press            on-press
       :style               style/right-icon-container}
      [icons/icon
       :i/options
       {:color secondary-color}]])])

(defn- list-view
  [{:keys [type stored profile-picture user-name theme secondary-color]}]
  (let [stored-name (if (= :on-device stored)
                      (i18n/label :t/on-device)
                      (i18n/label :t/on-keycard))]
    [row-view
     {:type            type
      :stored          stored
      :profile-picture profile-picture
      :title           user-name
      :subtitle        stored-name
      :theme           theme
      :secondary-color secondary-color}]))

(defn- card-view
  [theme derivation-path secondary-color on-press]
  [row-view
   {:type            :derivation-path
    :subtitle        derivation-path
    :theme           theme
    :on-press        on-press
    :secondary-color secondary-color}])

(defn view-internal
  [{:keys [type theme derivation-path on-press] :as props}]
  (let [secondary-color (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)]
    [rn/view {:style (style/container theme)}
     [text/text
      {:weight :regular
       :size   :paragraph-2
       :style  (style/title secondary-color)}
      (i18n/label :t/origin)]
     [list-view (assoc props :secondary-color secondary-color)]
     (when (not= :private-key type)
       [card-view theme derivation-path secondary-color on-press])]))

(def view
  "Create an account-origin UI component.
  | key               | values                                         |
  | ------------------|------------------------------------------------|
  | :type             | :default-keypair :recovery-phrase :private-key 
  | :stored           | :on-device :on-keycard
  | :profile-picture  | image source
  | :derivation-path  | string
  | :user-name        | string
  | :on-press         | function"
  (quo.theme/with-theme view-internal))
