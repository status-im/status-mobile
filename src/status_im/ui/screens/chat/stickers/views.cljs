(ns status-im.ui.screens.chat.stickers.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.stickers.core :as stickers]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.components.colors :as colors]
            [status-im.i18n.i18n :as i18n]
            [quo.core :as quo]
            [status-im.ui.screens.chat.stickers.styles :as styles]
            [status-im.utils.contenthash :as contenthash]
            [status-im.utils.debounce :as debounce]))

(def icon-size 28)
(def icon-horizontal-margin 8)
(def indicator-width 16)
(def dx (- (+ icon-horizontal-margin (/ icon-size 2)) (/ indicator-width 2)))
(def icon-container  (+ (* icon-horizontal-margin 2) icon-size))
(def scroll-x (reagent/atom 0))

(defn- no-stickers-yet-panel []
  [react/view {:style {:flex 1 :align-items :center :justify-content :center}}
   [icons/icon :stickers-icons/stickers-big {:color  colors/gray
                                             :width  64
                                             :height 64}]
   [react/text {:style {:margin-vertical 8 :font-size 17}} (i18n/label :t/you-dont-have-stickers)]
   [quo/button {:type     :secondary
                :on-press #(do
                             (re-frame/dispatch [:stickers/load-packs])
                             (re-frame/dispatch [:navigate-to :stickers]))}
    (i18n/label :t/get-stickers)]])

(defn- stickers-panel [stickers window-width]
  [react/view {:width window-width :flex 1}
   [react/scroll-view
    [react/view {:style styles/stickers-panel}
     (for [{:keys [hash] :as sticker} stickers]
       ^{:key (str hash)}
       [react/touchable-highlight {:style    {:height 75 :width 75 :margin 5}
                                   :on-press #(debounce/dispatch-and-chill [:chat/send-sticker sticker] 1000)}
        [react/image {:style {:resize-mode :cover :width "100%" :height "100%"}
                      :accessibility-label :sticker-icon
                      :source {:uri (contenthash/url (str "0x" hash))}}]])]]])

(defview recent-stickers-panel [window-width]
  (letsubs [stickers [:stickers/recent]]
    (if (seq stickers)
      [stickers-panel stickers window-width]
      [react/view {:style {:flex            1
                           :align-items     :center
                           :justify-content :center
                           :width           window-width}}
       [icons/icon :stickers-icons/sticker-history {:width  64
                                                    :height 64
                                                    :color  colors/gray}]
       [react/text {:style {:margin-top 12
                            :font-size  17}}
        (i18n/label :t/recently-used-stickers)]])))

(defn update-scroll-position [^js ref installed-packs selected-pack window-width animated?]
  (when ref
    ;; bug on Android https://github.com/facebook/react-native/issues/24531
    (js/setTimeout
     (fn []
       (let [x (if (= selected-pack :recent)
                 0
                 (* (inc (some #(when (= selected-pack (:id (second %))) (first %))
                               (map-indexed vector installed-packs)))
                    window-width))]
         (.scrollTo ref #js {:x x :animated animated?})))
     1)))

(defn on-scroll [^js e installed-packs window-width]
  (let [num     (/ (.-nativeEvent.contentOffset.x e) window-width)
        pack-id (if (zero? num)
                  :recent
                  (get-in (vec installed-packs) [(dec num) :id]))]
    (when pack-id
      (re-frame/dispatch [:stickers/select-pack pack-id]))))

(defview stickers-paging-panel [installed-packs selected-pack]
  (letsubs [ref   (atom nil)
            width [:dimensions/window-width]]
    {:component-will-update (fn [_ [_ installed-packs selected-pack]]
                              (update-scroll-position @ref installed-packs selected-pack width true))
     :component-did-mount   #(update-scroll-position @ref installed-packs selected-pack width false)}
    [react/scroll-view {:style                             {:flex 1}
                        :horizontal                        true
                        :paging-enabled                    true
                        :ref                               #(reset! ref %)
                        :shows-horizontal-scroll-indicator false
                        :on-momentum-scroll-end            #(on-scroll % installed-packs width)
                        :scroll-event-throttle             8
                        :scroll-to-overflow-enabled        true
                        :on-scroll                         #(reset! scroll-x (.-nativeEvent.contentOffset.x ^js %))}
     ^{:key "recent"}
     [recent-stickers-panel width]
     (for [{:keys [stickers id]} installed-packs]
       ^{:key (str "sticker" id)}
       [stickers-panel (map #(assoc % :pack id) (filter stickers/valid-sticker? stickers)) width])]))

(defn pack-icon [{:keys [id on-press background-color]
                  :or   {on-press #(re-frame/dispatch [:stickers/select-pack id])}}
                 icon]
  [react/touchable-highlight {:on-press on-press}
   [react/view {:style {:align-items :center}}
    [react/view {:style (styles/pack-icon background-color icon-size icon-horizontal-margin)}
     icon]]])

(defview scroll-indicator []
  (letsubs [window-width [:dimensions/window-width]]
    [react/view {:style {:height           2
                         :width            indicator-width
                         :border-radius    1
                         :margin-left      (+ dx (* icon-container (/ @scroll-x window-width)))
                         :background-color colors/blue}}]))

(defview stickers-view []
  (letsubs [selected-pack     [:stickers/selected-pack]
            installed-packs   [:stickers/installed-packs-vals]]
    [react/view {:style {:background-color colors/white
                         :flex             1}}
     (cond
       (= selected-pack :recent)   [stickers-paging-panel installed-packs selected-pack]
       (not (seq installed-packs)) [no-stickers-yet-panel]
       :else                       [stickers-paging-panel installed-packs selected-pack])
     [react/view {:style {:flex-direction :row :padding-horizontal 4}}
      [pack-icon {:on-press  #(do
                                (re-frame/dispatch [:stickers/load-packs])
                                (re-frame/dispatch [:navigate-to :stickers]))
                  :selected? false :background-color colors/blue}
       [icons/icon :main-icons/add {:width 20 :height 20 :color colors/white-persist}]]
      [react/view {:width 2}]
      [react/scroll-view {:horizontal true :style {:padding-left 2}}
       [react/view
        [react/view {:style {:flex-direction :row}}
         [pack-icon {:id :recent :background-color colors/white}
          [icons/icon :stickers-icons/recent {:color  colors/gray
                                              :width  44
                                              :height 44}]]
         (for [{:keys [id thumbnail]} installed-packs]
           ^{:key id}
           [pack-icon {:id               id
                       :background-color colors/white}
            [react/image {:style  {:width icon-size :height icon-size :border-radius (/ icon-size 2)}
                          :source {:uri (contenthash/url thumbnail)}}]])]
        [scroll-indicator]]]]]))
