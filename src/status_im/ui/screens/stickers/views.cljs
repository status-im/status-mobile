(ns status-im.ui.screens.stickers.views
  (:require [re-frame.core :as re-frame]
            [status-im.ethereum.core :as ethereum]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.screens.stickers.styles :as styles]
            [status-im.utils.contenthash :as contenthash]
            [status-im.utils.money :as money]
            [status-im.ui.components.topbar :as topbar])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defn- thumbnail-icon [uri size]
  [react/image {:style  {:width size :height size :border-radius (/ size 2)}
                :source {:uri uri}}])

(defn- installed-icon []
  [react/view styles/installed-icon
   [icons/icon :main-icons/check {:color colors/white-persist :height 20 :width 20}]])

(defview price-badge [price id owned? pending]
  (letsubs [chain   [:ethereum/chain-keyword]
            balance [:balance-default]]
    (let [snt             (money/to-number (if (= :mainnet chain) (:SNT balance) (:STT balance)))
          not-enough-snt? (> price snt)
          no-snt?         (or (nil? snt) (zero? snt))]
      [react/touchable-highlight {:on-press #(cond pending nil
                                                   (or owned? (zero? price))
                                                   (re-frame/dispatch [:stickers/install-pack id])
                                                   (or no-snt? not-enough-snt?) nil
                                                   :else (re-frame/dispatch [:stickers/buy-pack id price]))}
       [react/view (styles/price-badge (and (not (or owned? (zero? price))) (or no-snt? not-enough-snt?)))
        (when (and (not (zero? price)) (not owned?))
          [icons/tiny-icon :tiny-icons/tiny-snt {:color colors/white-persist :container-style {:margin-right 6}}])
        (if pending
          [react/activity-indicator {:animating true
                                     :color     colors/white-persist}]
          [react/text {:style {:color colors/white-persist}
                       :accessibility-label :sticker-pack-price}
           (cond owned? (i18n/label :t/install)
                 (zero? price) (i18n/label :t/free)
                 :else (str (money/wei-> :eth price)))])]])))

(defn pack-badge [{:keys [name author price thumbnail preview id installed owned pending]}]
  [react/touchable-highlight {:on-press #(re-frame/dispatch [:navigate-to :stickers-pack {:id id}])}
   [react/view {:margin-bottom 27}
    [react/image {:style {:height 200 :border-radius 20} :source {:uri (contenthash/url preview)}}]
    [react/view {:height 64 :align-items :center :flex-direction :row}
     [thumbnail-icon (contenthash/url thumbnail) 40]
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
     [react/keyboard-avoiding-view components.styles/flex
      [topbar/topbar {:title :t/sticker-market}]
      (if (seq packs)
        [react/scroll-view {:keyboard-should-persist-taps :handled :style {:padding 16}}
         [react/view
          (for [pack packs]
            ^{:key pack}
            [pack-badge pack])]]
        [react/view {:flex 1 :align-items :center :justify-content :center}
         [react/activity-indicator {:animating true}]])]]))

(def sticker-icon-size 60)

(defview pack-main [modal?]
  (letsubs [{:keys [id name author price thumbnail
                    stickers installed owned pending]
             :as pack}
            [:stickers/get-current-pack]]
    [react/keyboard-avoiding-view components.styles/flex
     [topbar/topbar {:modal? modal?}]
     (if pack
       [react/view {:flex 1}
        [react/view {:height 74 :align-items :center :flex-direction :row :padding-horizontal 16}
         [thumbnail-icon (contenthash/url thumbnail) 64]
         [react/view {:padding-horizontal 16 :flex 1}
          [react/text {:style {:typography :header}} name]
          [react/text {:style {:color colors/gray :margin-top 6}} author]]
         (if installed
           [installed-icon]
           [price-badge price id owned pending])]
        [react/view {:style {:padding-top 8 :flex 1}}
         [react/scroll-view {:keyboard-should-persist-taps :handled :style {:flex 1}}
          [react/view {:flex-direction :row :flex-wrap :wrap}
           (for [{:keys [hash]} stickers]
             ^{:key hash}
             [react/image {:style (styles/sticker-image sticker-icon-size)
                           :source {:uri (contenthash/url hash)}}])]]]]
       [react/view {:flex 1 :align-items :center :justify-content :center}
        [react/activity-indicator {:animating true}]])]))

(defview pack []
  [pack-main false])

(defview pack-modal []
  [pack-main true])
