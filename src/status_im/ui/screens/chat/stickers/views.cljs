(ns status-im.ui.screens.chat.stickers.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.colors :as colors]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.chat.stickers.styles :as styles]
            [status-im.ui.components.animation :as anim]
            [reagent.core :as reagent]
            [status-im.utils.platform :as platform]))

(def icon-size 28)
(def icon-horizontal-margin 8)
(def indicator-width 16)
(def dx (- (+ icon-horizontal-margin (/ icon-size 2)) (/ indicator-width 2)))
(def icon-container  (+ (* icon-horizontal-margin 2) icon-size))
(def scroll-x (reagent/atom 0))

(defn button [show-stickers?]
  [react/touchable-highlight
   {:on-press (fn [_]
                (re-frame/dispatch [:chat.ui/set-chat-ui-props {:show-stickers? (not show-stickers?)}])
                (when-not platform/desktop? (js/setTimeout #(react/dismiss-keyboard!) 100)))
    :accessibility-label :show-stickers-icon}
   [vector-icons/icon :main-icons/stickers {:container-style {:margin 14 :margin-right 6}
                                            :color           (if show-stickers? colors/blue colors/gray)}]])

(defn- no-stickers-yet-panel []
  [react/view {:style {:flex 1 :align-items :center :justify-content :center}}
   [vector-icons/icon :stickers-icons/stickers-big {:color colors/gray}]
   [react/text {:style {:margin-top 8 :font-size 17}} (i18n/label :t/you-dont-have-stickers)]
   [react/touchable-opacity {:on-press #(do
                                          (re-frame/dispatch [:stickers/load-packs])
                                          (re-frame/dispatch [:navigate-to :stickers]))}
    [react/view {:margin-top 6 :height 44 :justify-content :center}
     [react/text {:style {:color colors/blue}}
      (i18n/label :t/get-stickers)]]]])

(defn- stickers-panel [stickers window-width]
  [react/view {:width window-width :flex 1}
   [react/scroll-view
    [react/view {:style styles/stickers-panel}
     (for [{:keys [uri] :as sticker} stickers]
       ^{:key uri}
       [react/touchable-highlight {:style    {:height 75 :width 75 :margin 5}
                                   :on-press #(re-frame/dispatch [:chat/send-sticker sticker])}
        [react/image {:style {:resize-mode :cover :width "100%" :height "100%"}
                      :accessibility-label :sticker-icon
                      :source {:uri uri}}]])]]])

(defview recent-stickers-panel [window-width]
  (letsubs [stickers [:stickers/recent]]
    (if (seq stickers)
      [stickers-panel stickers window-width]
      [react/view {:style {:flex 1 :align-items :center :justify-content :center :width window-width}}
       [vector-icons/icon :stickers-icons/stickers-big {:color colors/gray}]
       [react/text {:style {:margin-top 8 :font-size 17}} (i18n/label :t/recently-used-stickers)]])))

(defn update-scroll-position [ref installed-packs selected-pack window-width]
  (when ref
    (let [x (if (= selected-pack :recent)
              0
              (* (inc (some #(when (= selected-pack (:id (second %))) (first %))
                            (map-indexed vector installed-packs)))
                 window-width))]
      (.scrollTo ref (clj->js {:x x :animated true})))))

(defn on-scroll [e installed-packs window-width]
  (let [num     (/ (.-nativeEvent.contentOffset.x e) window-width)
        pack-id (if (zero? num)
                  :recent
                  (get-in (vec installed-packs) [(dec num) :id]))]
    (when pack-id
      (re-frame/dispatch [:stickers/select-pack pack-id]))))

(defview stickers-paging-panel [installed-packs selected-pack]
  (letsubs [ref          (atom nil)
            window-width [:dimensions/window-width]]
    {:component-will-update (fn [_ [_ installed-packs selected-pack]]
                              (update-scroll-position @ref installed-packs selected-pack window-width))
     :component-did-mount   #(update-scroll-position @ref installed-packs selected-pack window-width)}
    [react/scroll-view {:style                             {:flex 1} :horizontal true :paging-enabled true
                        :ref                               #(reset! ref %)
                        :shows-horizontal-scroll-indicator false
                        :on-momentum-scroll-end            #(on-scroll % installed-packs window-width)
                        :scrollEventThrottle               8
                        :on-scroll                         #(reset! scroll-x (.-nativeEvent.contentOffset.x %))}
     ^{:key "recent"}
     [recent-stickers-panel window-width]
     (for [{:keys [stickers id]} installed-packs]
       ^{:key (str "sticker" id)}
       [stickers-panel (map #(assoc % :pack id) stickers) window-width])]))

(defn pack-icon [{:keys [id on-press background-color]
                  :or   {on-press #(re-frame/dispatch [:stickers/select-pack id])}}
                 icon]
  [react/touchable-highlight {:on-press on-press}
   [react/view {:style {:align-items :center}}
    [react/view {:style (styles/pack-icon background-color icon-size icon-horizontal-margin)}
     icon]]])

(defn pack-stickers [packs pack-id]
  (let [{:keys [stickers id]} (some #(when (= pack-id (:id %)) %) packs)]
    (map #(assoc % :pack id) stickers)))

(defn show-panel-anim
  [bottom-anim-value alpha-value]
  (anim/start
   (anim/parallel
    [(anim/spring bottom-anim-value {:toValue (styles/stickers-panel-height)})
     (anim/timing alpha-value {:toValue  1
                               :duration 500})])))

(defview scroll-indicator []
  (letsubs [window-width [:dimensions/window-width]]
    [react/view {:style {:margin-bottom    5 :height 2 :width indicator-width :border-radius 1
                         :margin-left      (+ dx (* icon-container (/ @scroll-x window-width)))
                         :background-color colors/blue}}]))

(defview stickers-view []
  (letsubs [selected-pack   [:stickers/selected-pack]
            installed-packs [:stickers/installed-packs-vals]
            input-focused?   [:chats/current-chat-ui-prop :input-focused?]
            bottom-anim-value  (anim/create-value 0)
            alpha-value        (anim/create-value 0)]
    {:component-will-mount #(if (not input-focused?)
                              (show-panel-anim bottom-anim-value alpha-value)
                              (do
                                (anim/set-value bottom-anim-value (styles/stickers-panel-height))
                                (anim/set-value alpha-value 1)))}
    [react/animated-view {:style {:background-color :white :height (if input-focused? 0 bottom-anim-value)
                                  :opacity alpha-value}}
     (cond
       (= selected-pack :recent) [stickers-paging-panel installed-packs selected-pack]
       (not (seq installed-packs)) [no-stickers-yet-panel]
       :else [stickers-paging-panel installed-packs selected-pack])
     [react/view {:style {:flex-direction :row :padding-horizontal 4}}
      [pack-icon {:on-press #(do
                               (re-frame/dispatch [:stickers/load-packs])
                               (re-frame/dispatch [:navigate-to :stickers]))
                  :selected? false :background-color colors/blue}
       [vector-icons/icon :main-icons/add {:width 20 :height 20 :color colors/white}]]
      [react/view {:width 2}]
      [react/scroll-view {:horizontal true :style {:padding-left 2}}
       [react/view
        [react/view {:style {:flex-direction :row}}
         [pack-icon {:id :recent :background-color colors/white}
          [vector-icons/icon :stickers-icons/recent {:color colors/gray}]]
         (for [{:keys [id thumbnail]} installed-packs]
           ^{:key id}
           [pack-icon {:id id
                       :background-color colors/white}
            [react/image {:style {:width icon-size :height icon-size :border-radius (/ icon-size 2)}
                          :source {:uri thumbnail}}]])]
        [scroll-indicator]]]]]))
