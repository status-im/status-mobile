(ns status-im.ui.screens.stickers.views
  (:require [quo.design-system.colors :as colors]
            [re-frame.core :as re-frame]
            [utils.i18n :as i18n]
            [status-im.ui.components.fast-image :as fast-image]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.stickers.styles :as styles]
            [utils.re-frame :as rf]
            [utils.money :as money])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defn cache
  [url]
  (str url "&download=true"))

(defn- thumbnail-icon
  [uri size]
  [fast-image/fast-image
   {:style  {:width size :height size :border-radius (/ size 2)}
    :source {:uri (cache uri)}}])

(defn price-badge
  [_]
  (let [chain   (rf/sub [:ethereum/chain-keyword])
        balance (rf/sub [:balance-default])]
    (fn [{:keys [id price pending owned installed]}]
      (let [snt             (if (= :mainnet chain) (:SNT balance) (:STT balance))
            price           (money/bignumber price)
            not-enough-snt? (or (nil? snt) (money/equal-to snt 0) (money/greater-than price snt))
            free            (money/equal-to price 0)]
        (if installed
          [react/view styles/installed-icon
           [icons/icon :main-icons/check {:color colors/white-persist :height 20 :width 20}]]
          [react/touchable-highlight
           {:on-press #(cond pending nil
                             (or owned free)
                             (re-frame/dispatch [:stickers/install-pack id])
                             not-enough-snt? nil
                             :else (re-frame/dispatch [:stickers/buy-pack id]))}
           [react/view (styles/price-badge (and (not (or owned free)) not-enough-snt?))
            (when (and (not free) (not owned))
              [icons/tiny-icon :tiny-icons/tiny-snt
               {:color colors/white-persist :container-style {:margin-right 6}}])
            (if pending
              [react/activity-indicator
               {:animating true
                :color     colors/white-persist}]
              [react/text
               {:style               {:color colors/white-persist}
                :accessibility-label :sticker-pack-price}
               (cond owned (i18n/label :t/install)
                     free  (i18n/label :t/free)
                     :else (str (money/wei-> :eth price)))])]])))))

(defn pack-badge
  [{:keys [name author thumbnail preview id] :as pack}]
  [react/touchable-highlight {:on-press #(re-frame/dispatch [:navigate-to :stickers-pack {:id id}])}
   [react/view {:margin-bottom 27}
    [fast-image/fast-image {:style {:height 200 :border-radius 20} :source {:uri (cache preview)}}]
    [react/view {:height 64 :align-items :center :flex-direction :row}
     [thumbnail-icon thumbnail 40]
     [react/view {:padding-horizontal 16 :flex 1}
      [react/text {:accessibility-label :sticker-pack-name} name]
      [react/text
       {:style               {:color colors/gray :margin-top 6}
        :accessibility-label :sticker-pack-author} author]]
     [price-badge pack]]]])

(defview packs
  []
  (letsubs [all-packs [:stickers/all-packs]]
    [react/view styles/screen
     [react/keyboard-avoiding-view {:flex 1}
      (if (seq all-packs)
        [react/scroll-view {:keyboard-should-persist-taps :handled :style {:padding 16}}
         [react/view
          (for [pack all-packs]
            ^{:key pack}
            [pack-badge pack])]]
        [react/view {:flex 1 :align-items :center :justify-content :center}
         [react/activity-indicator {:animating true}]])]]))

(def sticker-icon-size 60)

(defview pack-main
  []
  (letsubs [{:keys [name author thumbnail stickers]
             :as   pack}
            [:stickers/get-current-pack]]
    [react/keyboard-avoiding-view {:flex 1}
     (if pack
       [react/view {:flex 1}
        [react/view {:height 74 :align-items :center :flex-direction :row :padding-horizontal 16}
         [thumbnail-icon thumbnail 64]
         [react/view {:padding-horizontal 16 :flex 1}
          [react/text {:style {:typography :header}} name]
          [react/text {:style {:color colors/gray :margin-top 6}} author]]
         [price-badge pack]]
        [react/view {:style {:padding-top 8 :flex 1}}
         [react/scroll-view {:keyboard-should-persist-taps :handled :style {:flex 1}}
          [react/view {:flex-direction :row :flex-wrap :wrap}
           (for [{:keys [url]} stickers]
             ^{:key url}
             [fast-image/fast-image
              {:style  (styles/sticker-image sticker-icon-size)
               :source {:uri (cache url)}}])]]]]
       [react/view {:flex 1 :align-items :center :justify-content :center}
        [react/activity-indicator {:animating true}]])]))

(defview pack
  []
  [pack-main])
