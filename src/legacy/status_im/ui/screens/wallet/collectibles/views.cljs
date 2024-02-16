(ns legacy.status-im.ui.screens.wallet.collectibles.views
  (:require
    ["react-native-svg" :refer (SvgUri)]
    [clojure.string :as string]
    [legacy.status-im.multiaccounts.update.core :as multiaccounts.update]
    [legacy.status-im.react-native.resources :as resources]
    [legacy.status-im.ui.components.accordion :as accordion]
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.core :as quo]
    [legacy.status-im.ui.components.icons.icons :as icons]
    [legacy.status-im.ui.components.list.item :as list.item]
    [legacy.status-im.ui.components.react :as react]
    [legacy.status-im.ui.components.toastable-highlight :refer [toastable-highlight-view]]
    [legacy.status-im.ui.components.topbar :as topbar]
    [legacy.status-im.ui.screens.wallet.components.views :as wallet.components]
    [legacy.status-im.wallet.core :as wallet-legacy]
    [re-frame.core :as re-frame]
    [reagent.core :as reagent]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(def svg-uri (reagent/adapt-react-class SvgUri))

(defn is-image?
  [nft]
  (and (seq (:image_url nft))
       (not (string/ends-with? (:image_url nft) ".svg"))
       (not (string/ends-with? (:image_url nft) ".mp4"))))

(defn is-vector?
  [nft]
  (and (seq (:image_url nft))
       (string/ends-with? (:image_url nft) ".svg")))

(defn missing-image-placeholder
  []
  [react/view
   {:style {:width            "100%"
            :flex             1
            :align-items      :center
            :border-radius    16
            :background-color colors/gray-lighter
            :justify-content  :center
            :aspect-ratio     1}}
   [icons/icon :photo {:color colors/gray}]])

(defn nft-assets-skeleton
  [num-assets]
  [:<>
   (for [i (range num-assets)]
     ^{:key i}
     [react/view
      {:style {:width         "48%"
               :margin-bottom 16}}
      [react/view
       {:style {:flex             1
                :aspect-ratio     1
                :border-width     1
                :background-color colors/gray-transparent-10
                :border-color     colors/gray-lighter
                :border-radius    16}}]])])

(defn nft-trait-card
  [trait]
  [react/view
   {:style {:border-width       1
            :border-radius      12
            :margin-right       8
            :padding-vertical   4
            :padding-horizontal 8
            :border-color       colors/gray-lighter}}
   [quo/text
    {:size  :small
     :color :secondary}
    (:trait_type trait)]
   [quo/text {}
    (:value trait)]])

(defn nft-traits-scroller
  [traits]
  [react/scroll-view
   {:horizontal true
    :deceleration-rate "fast"
    :snap-to-alignment "start"
    :shows-horizontal-scroll-indicator
    false
    :scroll-event-throttle 64
    :style {:padding-left    16
            :margin-vertical 16
            :padding-bottom  8}}
   (for [trait traits]
     ^{:key (:trait_type trait)}
     [nft-trait-card trait])

   ;; spacer
   [react/view
    {:style {:height 40
             :width  40}}]])

(defn no-assets-error
  []
  [react/view
   {:style {:flex            1
            :justify-content :center
            :padding         16
            :margin-vertical 16
            :align-items     :center
            :border-radius   16}}
   [icons/icon :photo {:color colors/red}]
   [quo/text
    {:color :secondary
     :style {:magin-top 8}}
    (i18n/label :t/no-collectibles)]])

(defn render-asset
  [{:keys [asset width clickable?]}]
  [(if clickable? react/touchable-opacity react/view)
   {:style
    {:width         width
     :border-radius 16
     :margin-bottom 16}
    :on-press (when clickable? #(re-frame/dispatch [::wallet-legacy/show-nft-details asset]))
    :accessibility-label
    :nft-asset}
   (cond
     ;; pngs and jpegs
     (is-image? asset)
     [react/image
      {:style  {:flex          1
                :aspect-ratio  1
                :border-width  1
                :border-color  colors/gray-lighter
                :border-radius 16}
       :source {:uri (:image_url asset)}}]

     ;; vectors
     (is-vector? asset)
     [react/view
      {:style {:flex          1
               :aspect-ratio  1
               :border-width  1
               :border-color  colors/gray-lighter
               :border-radius 16}}
      [svg-uri
       {:uri    (:image_url asset)
        :width  "100%"
        :height "100%"}]]

     ;; ¯\_(ツ)_/¯
     :else [missing-image-placeholder])])

(defn nft-assets
  [{:keys [num-assets address collectible-slug]}]
  (let [assets    (rf/sub [:wallet-legacy/collectible-assets-by-collection-and-address address
                           collectible-slug])
        fetching? (rf/sub [:wallet-legacy/fetching-assets-by-collectible-slug collectible-slug])]
    [react/view
     {:flex            1
      :flex-wrap       :wrap
      :justify-content :space-between
      :flex-direction  :row
      :style           {:padding-horizontal 16}}
     (cond
       fetching? [nft-assets-skeleton num-assets]

       ;; <shivekkhurana> OpenSea sometimes doesn't return an asset. This condition handles it
       (and (not fetching?)
            (not (seq assets)))
       [no-assets-error]

       (seq assets)
       (for [asset assets]
         ^{:key (:id asset)}
         [render-asset
          {:asset      asset
           :clickable? true
           :width      "48%"}]))]))

(defn nft-collections
  [address]
  (let [collection (rf/sub [:wallet-legacy/collectible-collection address])]
    [:<>
     (for [[index collectible] (map-indexed vector collection)]
       ^{:key (:slug collectible)}
       [accordion/section
        {:title
         [react/view {:flex 1}
          [list.item/list-item
           {:title (:name collectible)
            :text-size :large
            :accessibility-label
            (keyword (str "collection-" index))
            :icon (if (seq (:image_url collectible))
                    [wallet.components/token-icon
                     {:style  {:border-radius 40
                               :overflow      :hidden
                               :border-width  1
                               :border-color  colors/gray-lighter}
                      :source {:uri (:image_url collectible)}}]
                    :main-icons/photo)
            :accessory :text
            :accessory-text (:owned_asset_count collectible)}]]
         :padding-vertical 0
         :dropdown-margin-left -12
         :open-container-style {:border-top-width    8
                                :border-bottom-width 8
                                :border-color        colors/gray-lighter}
         :on-open #(re-frame/dispatch
                    [::wallet-legacy/fetch-collectible-assets-by-owner-and-collection
                     address
                     (:slug collectible)
                     (:owned_asset_count collectible)])
         :content [nft-assets
                   {:address          address
                    :num-assets       (:owned_asset_count collectible)
                    :collectible-slug (:slug collectible)}]}])]))

(defn enable-opensea-view
  []
  [react/view {:style {:padding 16}}
   [react/view
    {:style {:border-color  colors/gray-lighter
             :border-width  1
             :align-self    :center
             :padding       4
             :border-radius 12}}
    [react/image
     {:source (resources/get-theme-image :collectible)
      :style  {:align-self  :center
               :resize-mode :contain}}]]
   [quo/text
    {:align :center
     :style {:margin-vertical 16}}
    (i18n/label :t/collectibles-leak-metadata)]
   [react/view {:align-items :center}
    [quo/button
     {:accessibility-label :enable-opensea-nft-visibility
      :on-press
      #(re-frame/dispatch
        [::multiaccounts.update/toggle-opensea-nfts-visiblity true])
      :theme :main
      :type :primary}
     (i18n/label :t/display-collectibles)]]
   [quo/text
    {:size  :small
     :color :secondary
     :align :center
     :style {:margin-top 10}}
    (i18n/label :t/disable-later-in-settings)]])

(defn nft-details-modal
  []
  (let [nft (rf/sub [:wallet-legacy/selected-collectible])]
    [react/scroll-view
     [topbar/topbar
      {:navigation    {:icon :main-icons/close}
       :border-bottom false}]
     [react/view {:padding-horizontal 16}
      [quo/text
       {:size   :large
        :weight :bold}
       (:name nft)]
      [quo/text
       {:size  :small
        :color :secondary
        :style {:margin-top 4}}
       (-> nft :collection :name)]

      [render-asset
       {:asset      nft
        :clickable? false
        :width      "100%"}]

      [quo/text {:style {:margin-top 12}}
       (:description nft)]]

     (when (seq (:traits nft))
       [nft-traits-scroller (:traits nft)])

     ;; seperator
     [react/view
      {:style {:border-bottom-width 1
               :padding-top         8
               :border-color        colors/gray-lighter}}]

     ;; TODO <shivekkhurana>: Enable txns
     ;; [list/list-item {:title    (i18n/label :t/wallet-send)
     ;;                 :icon     :main-icons/send :accessibility-label :nft-send :theme    :accent
     ;;                 :on-press #()}]

     ;; TODO <shivekkhurana>: What to do with share? Share links or share image?
     ;; [list/list-item {:title    (i18n/label :t/share)
     ;;                 :theme    :accent
     ;;                 :accessibility-label
     ;;                 :nft-share
     ;;                 :on-press #()
     ;;                 :icon     :main-icons/share}]
     [list.item/list-item
      {:title    (i18n/label :t/view-on-opensea)
       :theme    :accent
       :icon     :main-icons/browser
       :on-press #(re-frame/dispatch [:browser.ui/open-url (:permalink nft)])}]
     (when (is-image? nft)
       [toastable-highlight-view
        ;; the last string is an emoji. It might not show up in all editors but its there
        {:toast-label (str (i18n/label :t/profile-picture-updated)) " " "😎"}
        [list.item/list-item
         {:title (i18n/label :t/use-as-profile-picture)
          :theme :accent
          :on-press #(re-frame/dispatch [:profile.settings/save-profile-picture-from-url
                                         (:image_url nft)])
          :icon :main-icons/profile
          :accessibility-label
          :set-nft-as-pfp}]])]))
