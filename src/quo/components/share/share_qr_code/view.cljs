(ns quo.components.share.share-qr-code.view
  (:require [clojure.set]
            [clojure.string :as string]
            [oops.core :as oops]
            [quo.components.avatars.account-avatar.view :as account-avatar]
            [quo.components.avatars.user-avatar.view :as user-avatar]
            [quo.components.avatars.wallet-user-avatar.view :as wallet-user-avatar]
            [quo.components.buttons.button.view :as button]
            [quo.components.gradient.gradient-cover.view :as gradient-cover]
            [quo.components.icon :as icons]
            [quo.components.markdown.text :as text]
            [quo.components.share.qr-code.view :as qr-code]
            [quo.components.share.share-qr-code.style :as style]
            [quo.components.tabs.tab.view :as tab]
            [quo.foundations.colors :as colors]
            [quo.theme]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [utils.i18n :as i18n]))

(defn- header
  [{:keys [share-qr-type on-legacy-press on-multichain-press]}]
  [rn/view {:style style/header-container}
   [tab/view
    {:accessibility-label         :share-qr-code-legacy-tab
     :id                          :wallet-legacy-tab
     :active-item-container-style style/header-tab-active
     :item-container-style        style/header-tab-inactive
     :size                        24
     :active                      (= :wallet-legacy share-qr-type)
     :on-press                    on-legacy-press}
    (i18n/label :t/legacy)]
   [rn/view {:style style/space-between-tabs}]
   [tab/view
    {:accessibility-label         :share-qr-code-multichain-tab
     :id                          :wallet-multichain-tab
     :active-item-container-style style/header-tab-active
     :item-container-style        style/header-tab-inactive
     :size                        24
     :active                      (= :wallet-multichain share-qr-type)
     :on-press                    on-multichain-press}
    (i18n/label :t/multichain)]])

(defn- info-label
  [share-qr-code-type]
  [text/text {:size :paragraph-2 :weight :medium :style style/title}
   (when (= share-qr-code-type :profile)
     (i18n/label :t/link-to-profile))])

(defn- info-text
  [{:keys [width on-press on-long-press ellipsize?]} qr-data-text]
  [rn/pressable
   {:accessibility-label :share-qr-code-info-text
    :style               (style/data-text width)
    :on-press            on-press
    :on-long-press       on-long-press}
   [text/text
    (cond-> {:size   :paragraph-1
             :weight :monospace}
      ellipsize? (assoc :number-of-lines 1
                        :ellipsize-mode  :middle))
    qr-data-text]])

(defn- share-button
  [{:keys [alignment on-press]}]
  [rn/view {:style (style/share-button-container alignment)}
   [button/button
    {:icon-only?          true
     :type                :grey
     :background          :blur
     :size                style/share-button-size
     :accessibility-label :link-to-profile
     :on-press            on-press}
    :i/share]])

(defn- network-colored-text
  [network-short-name]
  [text/text {:style (style/network-short-name-text network-short-name)}
   (str network-short-name ":")])

(defn- wallet-multichain-colored-address
  [full-address]
  (let [[networks address]  (as-> full-address $
                              (string/split $ ":")
                              [(butlast $) (last $)])
        ->network-hiccup-xf (map #(vector network-colored-text %))]
    (as-> networks $
      (into [:<>] ->network-hiccup-xf $)
      (conj $ address))))

(defn- profile-bottom
  [{:keys [component-width qr-data on-text-press on-text-long-press share-qr-type]}]
  [rn/view
   [info-label share-qr-type]
   [info-text
    {:width         component-width
     :ellipsize?    true
     :on-press      on-text-press
     :on-long-press on-text-long-press}
    qr-data]])

(defn- wallet-legacy-bottom
  [{:keys [component-width qr-data on-text-press on-text-long-press]}]
  [info-text
   {:width         component-width
    :on-press      on-text-press
    :on-long-press on-text-long-press}
   qr-data])

(defn wallet-multichain-bottom
  [{:keys [component-width qr-data on-text-press on-text-long-press on-settings-press]}]
  [rn/view
   {:style style/wallet-multichain-container}
   [info-text
    {:width         component-width
     :on-press      on-text-press
     :on-long-press on-text-long-press}
    [wallet-multichain-colored-address qr-data]]
   [button/button
    {:icon-only?          true
     :type                :grey
     :background          :blur
     :size                32
     :accessibility-label :share-qr-code-settings
     :on-press            on-settings-press}
    :i/advanced]])

(defn- header-icon
  [{:keys [share-qr-type customization-color emoji profile-picture wallet-user-avatar]}]
  (case share-qr-type
    :profile [user-avatar/user-avatar {:size              :small
                                       :status-indicator? false
                                       :profile-picture   profile-picture
                                       :customization-color customization-color}]
    (:wallet :watched-address)  [account-avatar/view {:customization-color customization-color
                                                      :emoji               emoji
                                                      :size                32}]
    :saved-address [wallet-user-avatar/wallet-user-avatar {:size :size-32
                                                           :customization-color customization-color
                                                           :full-name wallet-user-avatar}]
    nil))

(defn- share-qr-code
  [{:keys [share-qr-type qr-image-uri component-width customization-color full-name
           profile-picture emoji on-share-press watched-account?]
    :as   props}]
  [:<>
   [rn/view {:style style/gradient-bg}
    [gradient-cover/view {:customization-color customization-color :height 463}]]
   [rn/view {:style style/content-container}
    [rn/view
     {:style style/share-qr-inner-container}
     [header-icon props]
     [text/text
      {:size   :heading-2
       :weight :semi-bold
       :style  {:margin-left 8}} full-name]
     (when (and watched-account? (not= share-qr-type :profile))
       [icons/icon
        :i/reveal
        {:color           colors/white-opa-40
         :container-style style/watched-account-icon}])]
    [share-button {:on-press on-share-press}]]
   (when (#{:wallet-legacy :wallet-multichain} share-qr-type)
     [header props])
   [quo.theme/provider {:theme :light}
    [qr-code/view
     {:qr-image-uri        qr-image-uri
      :size                (style/qr-code-size component-width)
      :avatar              (if (= share-qr-type :profile)
                             :profile
                             :wallet-account)
      :customization-color customization-color
      :full-name           full-name
      :profile-picture     profile-picture
      :emoji               emoji}]]
   [rn/view {:style style/bottom-container}
    (case share-qr-type
      :profile           [profile-bottom props]
      :wallet-legacy     [wallet-legacy-bottom props]
      :wallet-multichain [wallet-multichain-bottom props]
      nil)]])

(defn- view-internal
  "Receives the following properties:
   - type:                :profile | :wallet-legacy | :wallet-multichain
   - qr-image-uri:        Image source value.
   - qr-data:             Text to show below the QR code.
   - on-text-press:       Callback for the `qr-data` text.
   - on-text-long-press:  Callback for the `qr-data` text.
   - on-share-press:      Callback for the share button.
   - customization-color: Custom color for the QR code component.
   - unblur-on-android?:  [Android only] disables blur for this component.

   Depending on the `type`, different properties are accepted:
   `:profile`
     - full-name:       User full name.
     - profile-picture: map ({:source image-source}) or any image source.
   `:wallet-legacy`
     - emoji:               Emoji in a string to show in the QR code.
     - on-legacy-press:     Callback for the legacy tab.
     - on-multichain-press: Callback for the multichain tab.
     - watched-account?     Boolean
   `:wallet-multichain`
     - networks:            A vector of network names as keywords (`[:ethereum, :my-net, ...]`).
     - on-settings-press:   Callback for the settings button.
     - emoji:               Emoji in a string to show in the QR code.
     - on-legacy-press:     Callback for the legacy tab.
     - watched-account?     Boolean
     - on-multichain-press: Callback for the multichain tab.

     WARNING on Android:
     Sometimes while using a blur layer on top of another on Android, this component looks
     bad because of the `blur/view`, so we can set `unblur-on-android? true` to fix it.
     "
  [props]
  (reagent/with-let [component-width     (reagent/atom nil)
                     container-component [rn/view {:background-color style/overlay-color}]]
    [quo.theme/provider {:theme :dark}
     [rn/view
      {:accessibility-label :share-qr-code
       :style               style/outer-container
       :on-layout           #(reset! component-width (oops/oget % "nativeEvent.layout.width"))}
      (conj container-component
            (when @component-width
              [share-qr-code
               (-> props
                   (assoc :component-width @component-width)
                   (clojure.set/rename-keys {:type :share-qr-type}))]))]]))


(def view (quo.theme/with-theme view-internal))
