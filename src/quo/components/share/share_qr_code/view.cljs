(ns quo.components.share.share-qr-code.view
  (:require [clojure.set]
            [clojure.string :as string]
            [oops.core :as oops]
            [quo.components.buttons.button.view :as button]
            [quo.components.icon :as icon]
            [quo.components.list-items.preview-list.view :as preview-list]
            [quo.components.markdown.text :as text]
            [quo.components.share.qr-code.view :as qr-code]
            [quo.components.share.share-qr-code.style :as style]
            [quo.components.tabs.tab.view :as tab]
            [quo.foundations.resources :as quo.resources]
            [quo.theme]
            [react-native.blur :as blur]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [utils.i18n :as i18n]))

(defn- line [] [rn/view {:style style/line}])
(defn- space [] [rn/view {:style style/line-space}])

(defn- dashed-line
  [width]
  (into [rn/view {:style style/dashed-line}]
        (take (style/number-lines-and-spaces-to-fill width))
        (cycle [[line] [space]])))

(defn- header
  [{:keys [share-qr-type on-info-press on-legacy-press on-multichain-press]}]
  [rn/view {:style style/header-container}
   [tab/view
    {:id                          :wallet-legacy-tab
     :active-item-container-style style/header-tab-active
     :item-container-style        style/header-tab-inactive
     :size                        24
     :active                      (= :wallet-legacy share-qr-type)
     :on-press                    on-legacy-press}
    "Legacy"]
   [rn/view {:style style/space-between-tabs}]
   [tab/view
    {:id                          :wallet-multichain-tab
     :active-item-container-style style/header-tab-active
     :item-container-style        style/header-tab-inactive
     :size                        24
     :active                      (= :wallet-multichain share-qr-type)
     :on-press                    on-multichain-press}
    "Multichain"]
   [rn/pressable
    {:style    style/info-icon
     :on-press on-info-press
     :hit-slop 6}
    [icon/icon :i/info
     {:size  20
      :color style/info-icon-color}]]])

(defn- info-label
  [share-qr-code-type]
  [text/text {:size :paragraph-2 :weight :medium :style style/title}
   (if (= share-qr-code-type :profile)
     (i18n/label :t/link-to-profile)
     (i18n/label :t/wallet-address))])

(defn- info-text
  [{:keys [width on-press on-long-press]} qr-data-text]
  [rn/pressable
   {:style         (style/data-text width)
    :on-press      on-press
    :on-long-press on-long-press}
   [text/text
    {:size            :paragraph-1
     :weight          :medium
     :ellipsize-mode  :middle
     :number-of-lines 1}
    qr-data-text]])

(defn- share-button
  [{:keys [alignment on-press]}]
  [rn/view {:style (style/share-button-container alignment)}
   [button/button
    {:icon-only?          true
     :type                :grey
     :background          :blur
     :size                style/share-button-size
     :accessibility-label :share-profile
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
  [{:keys [component-width qr-data on-text-press on-text-long-press on-share-press share-qr-type]}]
  [:<>
   [rn/view
    [info-label share-qr-type]
    [info-text
     {:width         component-width
      :on-press      on-text-press
      :on-long-press on-text-long-press}
     qr-data]]
   [share-button
    {:alignment :center
     :on-press  on-share-press}]])

(defn- wallet-legacy-bottom
  [{:keys [share-qr-type component-width qr-data on-text-press on-text-long-press on-share-press]}]
  [rn/view {:style style/wallet-legacy-container}
   [info-label share-qr-type]
   [rn/view {:style style/wallet-data-and-share-container}
    [info-text
     {:width         component-width
      :on-press      on-text-press
      :on-long-press on-text-long-press}
     qr-data]
    [share-button
     {:alignment :top
      :on-press  on-share-press}]]])

(def ^:private known-networks #{:ethereum :optimism :arbitrum})

(defn wallet-multichain-bottom
  [{:keys [share-qr-type component-width qr-data on-text-press on-text-long-press
           on-share-press networks on-settings-press]}]
  (let [network-image-source (fn [network]
                               {:source (-> known-networks
                                            (get network :unknown)
                                            (quo.resources/get-network))})]
    [rn/view {:style style/wallet-multichain-container}
     [rn/view {:style style/wallet-multichain-networks}
      [preview-list/view {:type :network :size :size-32}
       (map network-image-source networks)]
      [button/button
       {:icon-only?          true
        :type                :grey
        :background          :blur
        :size                32
        :accessibility-label :qr-network-settings
        :on-press            on-settings-press}
       :i/advanced]]
     [rn/view {:style style/divider-container}
      [dashed-line component-width]]
     [rn/view {:style style/wallet-multichain-data-container}
      [info-label share-qr-type]
      [rn/view {:style style/wallet-data-and-share-container}
       [info-text
        {:width         component-width
         :on-press      on-text-press
         :on-long-press on-text-long-press}
        [wallet-multichain-colored-address qr-data]]
       [share-button
        {:alignment :top
         :on-press  on-share-press}]]]]))

(defn- share-qr-code
  [{:keys [share-qr-type qr-image-uri component-width customization-color full-name
           profile-picture emoji]
    :as   props}]
  [rn/view {:style style/content-container}
   (when (#{:wallet-legacy :wallet-multichain} share-qr-type)
     [header props])
   [qr-code/view
    {:qr-image-uri        qr-image-uri
     :size                (style/qr-code-size component-width)
     :avatar              (if (= share-qr-type :profile)
                            :profile
                            :wallet-account)
     :customization-color (style/qr-image-background-color customization-color)
     :full-name           full-name
     :profile-picture     profile-picture
     :emoji               emoji}]
   [rn/view {:style style/bottom-container}
    (case share-qr-type
      :profile           [profile-bottom props]
      :wallet-legacy     [wallet-legacy-bottom props]
      :wallet-multichain [wallet-multichain-bottom props]
      nil)]])

(defn view
  "
  [share-qr-type qr-image-uri qr-data component-width customization-color
   on-share-press on-text-press on-text-long-press full-name profile-picture
   emoji on-info-press networks on-settings-press blur?]
  "
  [{:keys [blur?] :as props}] ;;TODO: only for android
  (reagent/with-let [component-width     (reagent/atom nil)
                     container-component (if blur?
                                           [blur/view
                                            {:blur-radius   20
                                             ;:blur-amount 20 ;; TODO: set it on iOS
                                             :overlay-color style/overlay-color}]
                                           [rn/view {:background-color style/overlay-color}])]
    [quo.theme/provider {:theme :dark}
     [rn/view
      {:style     style/outer-container
       :on-layout #(reset! component-width (oops/oget % "nativeEvent.layout.width"))}
      (conj container-component
            (when @component-width
              [share-qr-code
               (-> props
                   (assoc :component-width @component-width)
                   (clojure.set/rename-keys {:type :share-qr-type}))]))]]))

;;TODO: check missing accessibility ids
