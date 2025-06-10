(ns quo.components.share.share-qr-code.view
  (:require [clojure.set]
            [oops.core :as oops]
            [quo.components.avatars.account-avatar.view :as account-avatar]
            [quo.components.avatars.channel-avatar.view :as channel-avatar]
            [quo.components.avatars.user-avatar.view :as user-avatar]
            [quo.components.avatars.wallet-user-avatar.view :as wallet-user-avatar]
            [quo.components.buttons.button.view :as button]
            [quo.components.gradient.gradient-cover.view :as gradient-cover]
            [quo.components.icon :as icons]
            [quo.components.markdown.text :as text]
            [quo.components.share.qr-code.view :as qr-code]
            [quo.components.share.share-qr-code.schema :as component-schema]
            [quo.components.share.share-qr-code.style :as style]
            [quo.context]
            [quo.foundations.colors :as colors]
            [react-native.core :as rn]
            [schema.core :as schema]
            [utils.i18n :as i18n]))

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

(defn- wallet-bottom
  [{:keys [component-width qr-data on-text-press on-text-long-press]}]
  [info-text
   {:width         component-width
    :on-press      on-text-press
    :on-long-press on-text-long-press}
   qr-data])

(defn- header-icon
  [{:keys [share-qr-type customization-color emoji profile-picture full-name]}]
  (case share-qr-type
    :profile                   [user-avatar/user-avatar
                                {:size                :small
                                 :status-indicator?   false
                                 :profile-picture     profile-picture
                                 :customization-color customization-color}]
    (:wallet :watched-address) [account-avatar/view
                                {:customization-color customization-color
                                 :emoji               emoji
                                 :size                32}]
    :saved-address             [wallet-user-avatar/wallet-user-avatar
                                {:size                :size-32
                                 :customization-color customization-color
                                 :full-name           full-name}]
    :channel                   [channel-avatar/view
                                {:size                :size-32
                                 :emoji               emoji
                                 :locked-state        :not-set
                                 :customization-color customization-color
                                 :full-name           full-name}]
    nil))

(defn- share-qr-code
  [{:keys [share-qr-type qr-image-uri component-width customization-color full-name
           profile-picture emoji on-share-press]
    :as   props}]
  [:<>
   [gradient-cover/view {:customization-color customization-color :height 463}]
   [rn/view {:style style/content-container}
    [rn/view {:style style/share-qr-container}
     [rn/view {:style style/share-qr-inner-container}
      [header-icon props]
      [text/text
       {:size   :heading-2
        :weight :semi-bold
        :style  {:margin-left 8}} full-name]
      (when (= share-qr-type :watched-address)
        [icons/icon
         :i/reveal
         {:color           colors/white-opa-40
          :container-style style/watched-account-icon}])]
     [share-button {:on-press on-share-press}]]
    [quo.context/provider {:theme :light}
     [qr-code/view
      {:qr-image-uri        qr-image-uri
       :size                (style/qr-code-size component-width)
       :avatar              (case share-qr-type
                              :profile                   :profile
                              (:watched-address :wallet) :wallet-account
                              :saved-address             :saved-address
                              :channel                   :channel
                              nil)
       :customization-color customization-color
       :full-name           full-name
       :profile-picture     profile-picture
       :emoji               emoji}]]
    [rn/view {:style style/bottom-container}
     (case share-qr-type
       :profile
       [profile-bottom props]
       (:wallet :watched-address :saved-address)
       [wallet-bottom props]
       nil)]]])

(defn- view-internal
  [{provided-width :width :as props}]
  (let [[calculated-width
         set-component-width] (rn/use-state nil)
        on-layout             (rn/use-callback #(set-component-width
                                                 (oops/oget % "nativeEvent.layout.width")))
        props                 (-> props
                                  (assoc :component-width (or provided-width calculated-width))
                                  (clojure.set/rename-keys {:type :share-qr-type}))]
    [quo.context/provider {:theme :dark}
     [rn/view
      {:accessibility-label :share-qr-code
       :style               style/outer-container
       :on-layout           (when-not provided-width on-layout)}
      [rn/view {:style {:background-color style/overlay-color}}
       (when (:component-width props)
         [share-qr-code props])]]]))

(def view (schema/instrument #'view-internal component-schema/?schema))
