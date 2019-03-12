(ns status-im.ui.screens.stickers.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.stickers.styles :as styles]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.utils.money :as money]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.ui.components.react :as components]))

(defn- thumbnail-icon [uri size]
  [react/image {:style  {:width size :height size :border-radius (/ size 2)}
                :source {:uri uri}}])

(defn- installed-icon []
  [react/view styles/installed-icon
   [icons/icon :main-icons/check {:color colors/white :height 20 :width 20}]])

(defview price-badge [price id owned pending]
  (letsubs [network [:network]
            balance [:balance]]
    (let [chain           (ethereum/network->chain-keyword network)
          snt             (money/to-number (if (= :mainnet chain) (:SNT balance) (:STT balance)))
          not-enough-snt? (> price snt)
          no-snt?         (or (nil? snt) (zero? snt))]
      [react/touchable-highlight {:on-press #(cond pending nil
                                                   (or owned (zero? price))
                                                   (re-frame/dispatch [:stickers/install-pack id])
                                                   (or no-snt? not-enough-snt?) nil
                                                   :else (re-frame/dispatch [:stickers/buy-pack id price]))}
       [react/view (styles/price-badge (and (not (or owned (zero? price))) (or no-snt? not-enough-snt?)))
        (when (and (not (zero? price))) ;(not no-snt?))
          [icons/icon :icons/logo {:color colors/white :width 12 :height 12 :container-style {:margin-right 8}}])
        (if pending
          [components/activity-indicator {:animating true
                                          :color     colors/white}]
          [react/text {:style {:color colors/white}
                       :accessibility-label :sticker-pack-price}
           (cond (or owned (zero? price))
                 (i18n/label :t/install)
                 :else
                 (str (money/wei-> :eth price)))])]])))

(defn pack-badge [{:keys [name author price thumbnail preview id installed owned pending] :as pack}]
  [react/touchable-highlight {:on-press #(re-frame/dispatch [:navigate-to :stickers-pack pack])}
   [react/view {:margin-bottom 27}
    [react/image {:style {:height 200 :border-radius 20} :source {:uri preview}}]
    [react/view {:height 64 :align-items :center :flex-direction :row}
     [thumbnail-icon thumbnail 40]
     [react/view {:padding-horizontal 16 :flex 1}
      [react/text {:accessibility-label :sticker-pack-name} name]
      [react/text {:style {:color colors/gray :margin-top 6}
                   :accessibility-label :sticker-pack-author} author]]
     (if installed
       [installed-icon]
       [price-badge price id owned pending])]]])

(defview packs []
  (letsubs [packs [:stickers/all-packs]]
    [react/view styles/screen
     [status-bar/status-bar]
     [react/keyboard-avoiding-view components.styles/flex
      [toolbar/simple-toolbar (i18n/label :t/sticker-market)]
      [react/scroll-view {:keyboard-should-persist-taps :handled :style {:padding 16}}
       [react/view
        (for [pack packs]
          ^{:key pack}
          [pack-badge pack])]]]]))

(def sticker-icon-size 60)

(defview pack-main [modal?]
  (letsubs [{:keys [id name author price thumbnail stickers installed owned pending]} [:stickers/get-current-pack]]
    [react/view styles/screen
     [status-bar/status-bar]
     [react/keyboard-avoiding-view components.styles/flex
      [toolbar/simple-toolbar nil modal?]
      [react/view {:height 74 :align-items :center :flex-direction :row :padding-horizontal 16}
       [thumbnail-icon thumbnail 64]
       [react/view {:padding-horizontal 16 :flex 1}
        [react/text {:style {:typography :header}} name]
        [react/text {:style {:color colors/gray :margin-top 6}} author]]
       (if installed
         [installed-icon]
         [price-badge price id owned pending])]
      [react/view {:style {:padding-top 8 :flex 1}}
       [react/scroll-view {:keyboard-should-persist-taps :handled :style {:flex 1}}
        [react/view {:flex-direction :row :flex-wrap :wrap}
         (for [{:keys [uri]} stickers]
           ^{:key uri}
           [react/image {:style (styles/sticker-image sticker-icon-size)
                         :source {:uri uri}}])]]]]]))

(defview pack []
  [pack-main false])

(defview pack-modal []
  [pack-main true])
