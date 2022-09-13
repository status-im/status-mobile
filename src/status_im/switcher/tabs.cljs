(ns status-im.switcher.tabs
  (:require [oops.core :refer [oget]]
            [quo.components.safe-area :as safe-area]
            [quo.react :as react]
            [quo.react-native :as rn]
            [quo2.components.text :as text]
            [quo2.foundations.colors :as colors]
            [quo2.reanimated :as reanimated]
            [reagent.core :as reagent]
            [status-im.switcher.animation :as animation]
            [status-im.switcher.constants :as constants]
            [status-im.switcher.styles :as styles]))

(def tabs-count (count constants/switcher-tabs-data))

(defn switcher-header-tab-text [translate-x tab-name tab-index]
  [:f>
   (fn []
     (let [header-text-style (styles/switcher-header-tab-text)
           header-text-opacity (animation/use-header-text-opacity translate-x tab-index)
           animated-style    {:opacity header-text-opacity}]
       [text/text {:size   30
                   :weight :semi-bold
                   :style header-text-style
                   :animated-style animated-style} (str tab-name)]))])

(def header-measures (reagent/atom (vec (repeat tabs-count nil))))

(defn switcher-header-tab [tab translate-x flat-list-ref]
  (let [{:keys [name index]} tab]
    ^{:key (str "switcher-tab-content-" name)}
    [rn/touchable-opacity {:style (styles/switcher-header-tab)
                           :on-press (fn [_event]
                                       (.scrollToIndex ^js (.-current ^js flat-list-ref) (clj->js {:index index})))
                           :on-layout (fn [event]
                                        (when (< index 4)
                                          (reset! header-measures (assoc @header-measures index
                                                                         (oget event "nativeEvent" "layout" "width")))))}
     [switcher-header-tab-text translate-x name index]]))

(defn switcher-header [header-tabs translate-x flat-list-ref]
  [:f>
   (fn []
     (let [insets (safe-area/use-safe-area)
           style (styles/switcher-header (:top insets))
           header-offset-left (animation/use-header-offset-left translate-x tabs-count (clj->js @header-measures) constants/switcher-header-tab-padding-horizontal)
           animated-style    (reanimated/apply-animations-to-style
                              {:left header-offset-left}
                              style)]

       [reanimated/view {:style animated-style}
        (for [tab header-tabs]
          ^{:key (:index tab)}
          [switcher-header-tab tab translate-x flat-list-ref])]))])

(defn switcher-tab [tab-name]
  [rn/view {:style (merge (styles/switcher-tab)
                          {:background-color (first (keep-indexed #(if (= (:name %2) tab-name) (:color %2) nil) constants/switcher-tabs-data))})}
   [text/text {:style {:color colors/white}} tab-name]])

(def circular-tabs-data (reagent/atom (concat constants/switcher-tabs-data constants/switcher-tabs-data)))
(def circular-tabs-data-updating (reagent/atom false))

(defn switcher-tabs []
  [:f>
   (fn []
     (let [translate-x (reanimated/use-shared-value 0)
           update-circular-data (clj->js (fn []
                                           (when-not @circular-tabs-data-updating
                                             (when (>=
                                                    (/ (.-value ^js translate-x) (:width constants/dimensions))
                                                    (* (count @circular-tabs-data) 0.75))
                                               (reset! circular-tabs-data-updating true)
                                               (swap! circular-tabs-data concat @circular-tabs-data @circular-tabs-data)
                                               (reset! circular-tabs-data-updating false)))))
           scroll-handler (animation/use-switcher-scroll-handler translate-x update-circular-data)
           flat-list-ref (react/create-ref)
           header-tabs (map-indexed (fn [index tab] {:name (:name tab) :index index}) @circular-tabs-data)]

       [rn/view {:style {:flex 1 :flex-direction :column}}
        [reanimated/flat-list {:style {:flex 1}
                               :data @circular-tabs-data
                               :on-scroll scroll-handler
                               :scroll-event-throttle 16
                               :ref flat-list-ref
                               :horizontal true
                               :bounces false
                               :shows-horizontal-scroll-indicator false
                               :paging-enabled true
                               :key-extractor (fn [item index] (str (:name item) index))
                               :render-item (fn [props]
                                              (let [item (:item (js->clj props :keywordize-keys true))]
                                                (reagent/as-element
                                                 [switcher-tab (:name item)])))}]

        [switcher-header header-tabs translate-x flat-list-ref]]))])
