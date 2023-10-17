(ns status-im2.contexts.chat.lightbox.view
  (:require
    [clojure.string :as string]
    [oops.core :as oops]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [react-native.gesture :as gesture]
    [react-native.orientation :as orientation]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [react-native.safe-area :as safe-area]
    [status-im2.contexts.chat.lightbox.animations :as anim]
    [status-im2.contexts.chat.lightbox.bottom-view :as bottom-view]
    [status-im2.contexts.chat.lightbox.constants :as constants]
    [status-im2.contexts.chat.lightbox.style :as style]
    [status-im2.contexts.chat.lightbox.top-view :as top-view]
    [status-im2.contexts.chat.lightbox.utils :as utils]
    [status-im2.contexts.chat.lightbox.zoomable-image.view :as zoomable-image]
    [utils.re-frame :as rf]))

(defn get-item-layout
  [_ index item-width]
  #js
   {:length item-width
    :offset (* (+ item-width constants/separator-width) index)
    :index  index})

(defn on-viewable-items-changed
  [e {:keys [scroll-index-lock? small-list-ref]} {:keys [scroll-index]}]
  (when-not @scroll-index-lock?
    (let [changed (-> e (oops/oget :changed) first)
          index   (oops/oget changed :index)]
      (reset! scroll-index index)
      (when @small-list-ref
        (.scrollToIndex ^js @small-list-ref #js {:animated true :index index}))
      (rf/dispatch [:chat.ui/update-shared-element-id (:message-id (oops/oget changed :item))]))))

(defn image
  [message index _ {:keys [screen-width screen-height] :as args}]
  [rn/view
   {:style (style/image (+ screen-width constants/separator-width) screen-height)}
   [:f> zoomable-image/zoomable-image message index args]
   [rn/view {:style {:width constants/separator-width}}]])

(defn lightbox-content
  [props {:keys [data transparent? scroll-index set-full-height?] :as state}
   animations derived messages index handle-items-changed]
  (let [insets           (safe-area/get-insets)
        window           (rn/get-window)
        window-width     (:width window)
        window-height    (if platform/android?
                           (+ (:height window) (:top insets))
                           (:height window))
        curr-orientation (or (rf/sub [:lightbox/orientation]) orientation/portrait)
        landscape?       (string/includes? curr-orientation orientation/landscape)
        horizontal?      (or platform/android? (not landscape?))
        inverted?        (and platform/ios? (= curr-orientation orientation/landscape-right))
        screen-width     (if (or platform/ios? (= curr-orientation orientation/portrait))
                           window-width
                           window-height)
        screen-height    (if (or platform/ios? (= curr-orientation orientation/portrait))
                           window-height
                           window-width)
        item-width       (if (and landscape? platform/ios?) screen-height screen-width)]
    [reanimated/view
     {:style (reanimated/apply-animations-to-style {:background-color (:background-color animations)}
                                                   {:height screen-height})}
     (when-not @transparent?
       [:f> top-view/top-view messages insets scroll-index animations derived landscape?
        screen-width])
     [gesture/gesture-detector
      {:gesture (utils/drag-gesture animations (and landscape? platform/ios?) set-full-height?)}
      [reanimated/view
       {:style (reanimated/apply-animations-to-style
                {:transform [{:translateY (:pan-y animations)}
                             {:translateX (:pan-x animations)}]}
                {})}
       [reanimated/linear-gradient
        {:colors         [colors/neutral-100-opa-0 colors/neutral-100-opa-0 colors/neutral-100-opa-100
                          colors/neutral-100]
         :locations      [0.3 0.4 0.6 1]
         :pointer-events :none
         :style          (style/background-bottom-gradient animations @(:overlay-z-index state))}]
       [reanimated/linear-gradient
        {:colors         [colors/neutral-100-opa-50 colors/neutral-100]
         :pointer-events :none
         :style          (style/background-top-gradient animations @(:overlay-z-index state))}]
       [gesture/flat-list
        {:ref                               #(reset! (:flat-list-ref props) %)
         :key-fn                            :message-id
         :on-scroll                         #(utils/on-scroll % item-width animations landscape?)
         :scroll-event-throttle             8
         :style                             {:width (+ screen-width constants/separator-width)}
         :data                              @data
         :render-fn                         image
         :render-data                       {:opacity-value     (:opacity animations)
                                             :overlay-opacity   (:overlay-opacity animations)
                                             :border-value      (:border animations)
                                             :full-screen-scale (:full-screen-scale animations)
                                             :images-opacity    (:images-opacity animations)
                                             :transparent?      transparent?
                                             :set-full-height?  set-full-height?
                                             :screen-height     screen-height
                                             :screen-width      screen-width
                                             :window-height     window-height
                                             :window-width      window-width
                                             :props             props
                                             :curr-orientation  curr-orientation}
         :horizontal                        horizontal?
         :inverted                          inverted?
         :paging-enabled                    true
         :get-item-layout                   (fn [_ index] (get-item-layout _ index item-width))
         :viewability-config                {:view-area-coverage-percent-threshold 50
                                             :wait-for-interaction                 true}
         :shows-vertical-scroll-indicator   false
         :shows-horizontal-scroll-indicator false
         :on-viewable-items-changed         handle-items-changed}]]]
     ;; NOTE: not un-mounting bottom-view based on `transparent?` (like we do with the top-view
     ;;       above), since we need to save the state of the text-sheet position. Instead, we use
     ;;       the `:display` style property to hide the bottom-sheet.
     (when (not landscape?)
       [:f> bottom-view/bottom-view messages index scroll-index insets animations derived
        item-width props state transparent?])]))

(defn- f-lightbox
  []
  (let [{:keys [messages index]} (rf/sub [:get-screen-params])
        props                    (utils/init-props)
        state                    (utils/init-state messages index)
        handle-items-changed     (fn [e]
                                   (on-viewable-items-changed e props state))]
    (fn []
      (let [animations (utils/init-animations (count messages) index)
            derived    (utils/init-derived-animations animations)]
        (anim/animate (:background-color animations) colors/neutral-100)
        (reset! (:data state) messages)
        (when platform/ios? ; issue: https://github.com/wix/react-native-navigation/issues/7726
          (utils/orientation-change props state animations))
        (utils/effect props animations index)
        [:f> lightbox-content props state animations derived messages index handle-items-changed]))))

(defn lightbox
  []
  [:f> f-lightbox])
