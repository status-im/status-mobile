(ns status-im2.contexts.chat.lightbox.view
  (:require
    [clojure.string :as string]
    [quo2.foundations.colors :as colors]
    [react-native.core :as rn]
    [react-native.orientation :as orientation]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [status-im2.contexts.chat.lightbox.common :as common]
    [utils.re-frame :as rf]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im2.contexts.chat.lightbox.style :as style]
    [status-im2.contexts.chat.lightbox.zoomable-image.view :as zoomable-image]
    [status-im2.contexts.chat.lightbox.top-view :as top-view]
    [status-im2.contexts.chat.lightbox.bottom-view :as bottom-view]
    [oops.core :refer [oget]]))

(def seperator-width 16)

(defn toggle-opacity
  [opacity-value border-value transparent? index {:keys [small-list-ref]}]
  (let [opacity (reanimated/get-shared-value opacity-value)]
    (if (= opacity 1)
      (do
        (common/set-val-timing opacity-value 0)
        (js/setTimeout #(reset! transparent? (not @transparent?)) 400))
      (do
        (reset! transparent? (not @transparent?))
        (reanimated/animate-shared-value-with-delay-default-easing opacity-value 1 300 50)
        (js/setTimeout #(when @small-list-ref
                          (.scrollToIndex ^js @small-list-ref #js {:animated false :index index}))
                       100)))
    (common/set-val-timing border-value (if (= opacity 1) 0 12))))

(defn handle-orientation
  [result index window animations {:keys [flat-list-ref insets-atom]}]
  (let [screen-width  (if (or platform/ios? (= result orientation/portrait))
                        (:width window)
                        (:height window))
        screen-height (if (or platform/ios? (= result orientation/portrait))
                        (:height window)
                        (:width window))
        landscape?    (string/includes? result orientation/landscape)
        item-width    (if (and landscape? platform/ios?) screen-height screen-width)
        timeout       (if platform/ios? 50 100)]
    (when (or landscape? (= result orientation/portrait))
      (rf/dispatch [:chat.ui/orientation-change result]))
    (cond
      landscape?
      (orientation/lock-to-landscape "lightbox")
      (= result orientation/portrait)
      (orientation/lock-to-portrait "lightbox"))
    (js/setTimeout #(when @flat-list-ref
                      (.scrollToOffset
                       ^js @flat-list-ref
                       #js {:animated false :offset (* (+ item-width seperator-width) @index)}))
                   timeout)
    (when platform/ios?
      (top-view/animate-rotation result screen-width screen-height @insets-atom animations))))

(defn get-item-layout
  [_ index item-width]
  #js {:length item-width :offset (* (+ item-width seperator-width) index) :index index})

(defn on-viewable-items-changed
  [e scroll-index {:keys [scroll-index-lock? small-list-ref]}]
  (when-not @scroll-index-lock?
    (let [changed (-> e (oget :changed) first)
          index   (oget changed :index)]
      (reset! scroll-index index)
      (when @small-list-ref
        (.scrollToIndex ^js @small-list-ref #js {:animated true :index index}))
      (rf/dispatch [:chat.ui/update-shared-element-id (:message-id (oget changed :item))]))))

(defn image
  [message index _ {:keys [opacity-value border-value transparent? width height atoms]}]
  [:f>
   (fn []
     [rn/view
      {:style {:flex-direction  :row
               :width           (+ width seperator-width)
               :height          height
               :align-items     :center
               :justify-content :center}}
      [zoomable-image/zoomable-image message index border-value
       #(toggle-opacity opacity-value border-value transparent? index atoms)]
      [rn/view {:style {:width seperator-width}}]])])

(defn lightbox
  []
  [:f>
   (fn []
     (let [{:keys [messages index]} (rf/sub [:get-screen-params])
           atoms                    {:flat-list-ref      (atom nil)
                                     :small-list-ref     (atom nil)
                                     :scroll-index-lock? (atom true)
                                     :insets-atom        (atom nil)}
           ;; The initial value of data is the image that was pressed (and not the whole album) in order
           ;; for the transition animation to execute properly, otherwise it would animate towards
           ;; outside the screen (even if we have `initialScrollIndex` set).
           data                     (reagent/atom [(nth messages index)])
           scroll-index             (reagent/atom index)
           transparent?             (reagent/atom false)
           window                   (rf/sub [:dimensions/window])
           animations               {:border         (common/use-val (if platform/ios? 0 12))
                                     :opacity        (common/use-val 0)
                                     :rotate         (common/use-val "0deg")
                                     :top-layout     (common/use-val -10)
                                     :bottom-layout  (common/use-val 10)
                                     :top-view-y     (common/use-val 0)
                                     :top-view-x     (common/use-val 0)
                                     :top-view-width (common/use-val (:width window))
                                     :top-view-bg    (common/use-val colors/neutral-100-opa-0)}

           callback                 (fn [e]
                                      (on-viewable-items-changed e scroll-index atoms))]
       (reset! data messages)
       (orientation/use-device-orientation-change
        (fn [result]
          (if platform/ios?
            (handle-orientation result scroll-index window animations atoms)
            ;; `use-device-orientation-change` will always be called on Android, so need to check
            (orientation/get-auto-rotate-state
             (fn [enabled?]
               ;; RNN does not support landscape-right
               (when (and enabled? (not= result orientation/landscape-right))
                 (handle-orientation result scroll-index window animations atoms)))))))
       (rn/use-effect (fn []
                        (when @(:flat-list-ref atoms)
                          (.scrollToIndex ^js @(:flat-list-ref atoms)
                                          #js {:animated false :index index}))
                        (js/setTimeout (fn []
                                         (common/set-val-timing (:opacity animations) 1)
                                         (common/set-val-timing (:top-layout animations) 0)
                                         (common/set-val-timing (:bottom-layout animations) 0)
                                         (common/set-val-timing (:border animations) 12))
                                       (if platform/ios? 250 100))
                        (js/setTimeout #(reset! (:scroll-index-lock? atoms) false) 300)
                        (fn []
                          (rf/dispatch [:chat.ui/zoom-out-signal nil])
                          (when platform/android?
                            (rf/dispatch [:chat.ui/lightbox-scale 1])))))
       [safe-area/consumer
        (fn [insets]
          (let [curr-orientation (or (rf/sub [:lightbox/orientation]) orientation/portrait)
                landscape?       (string/includes? curr-orientation orientation/landscape)
                horizontal?      (or platform/android? (not landscape?))
                inverted?        (and platform/ios? (= curr-orientation orientation/landscape-right))
                screen-width     (if (or platform/ios? (= curr-orientation orientation/portrait))
                                   (:width window)
                                   (:height window))
                screen-height    (if (or platform/ios? (= curr-orientation orientation/portrait))
                                   (:height window)
                                   (:width window))
                item-width       (if (and landscape? platform/ios?) screen-height screen-width)]
            (reset! (:insets-atom atoms) insets)
            [rn/view {:style style/container-view}
             (when-not @transparent?
               [top-view/top-view (first messages) insets scroll-index animations landscape?
                screen-width])
             [rn/flat-list
              {:ref                               #(reset! (:flat-list-ref atoms) %)
               :key-fn                            :message-id
               :style                             {:width (+ screen-width seperator-width)}
               :data                              @data
               :render-fn                         image
               :render-data                       {:opacity-value (:opacity animations)
                                                   :border-value  (:border animations)
                                                   :transparent?  transparent?
                                                   :height        screen-height
                                                   :width         screen-width
                                                   :atoms         atoms}
               :horizontal                        horizontal?
               :inverted                          inverted?
               :paging-enabled                    true
               :get-item-layout                   (fn [_ index] (get-item-layout _ index item-width))
               :viewability-config                {:view-area-coverage-percent-threshold 50
                                                   :wait-for-interaction                 true}
               :shows-vertical-scroll-indicator   false
               :shows-horizontal-scroll-indicator false
               :on-viewable-items-changed         callback
               :content-container-style           {:justify-content :center
                                                   :align-items     :center}}]
             (when (and (not @transparent?) (not landscape?))
               [bottom-view/bottom-view messages index scroll-index insets animations
                item-width atoms])]))]))])
