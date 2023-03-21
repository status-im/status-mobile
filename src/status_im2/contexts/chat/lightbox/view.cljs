(ns status-im2.contexts.chat.lightbox.view
  (:require
    [clojure.string :as string]
    [quo2.foundations.colors :as colors]
    [react-native.core :as rn]
    [react-native.navigation :as navigation]
    [react-native.orientation :as orientation]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [status-im2.contexts.chat.lightbox.animations :as anim]
    [status-im2.contexts.chat.lightbox.style :as style]
    [utils.re-frame :as rf]
    [reagent.core :as reagent]
    [react-native.gesture :as gesture]
    [status-im2.contexts.chat.lightbox.zoomable-image.view :as zoomable-image]
    [status-im2.contexts.chat.lightbox.top-view :as top-view]
    [status-im2.contexts.chat.lightbox.bottom-view :as bottom-view]
    [utils.worklets.lightbox :as worklet]
    [oops.core :refer [oget]]))

(def ^:const seperator-width 16)

(def ^:const drag-threshold 100)

(defn toggle-opacity
  [index {:keys [opacity-value border-value transparent? atoms]} portrait?]
  (let [{:keys [small-list-ref]} atoms
        opacity (reanimated/get-shared-value opacity-value)]
    (if (= opacity 1)
      (do
        (when platform/ios?
          ;; status-bar issue: https://github.com/status-im/status-mobile/issues/15343
          (js/setTimeout #(navigation/merge-options "lightbox" {:statusBar {:visible false}}) 75))
        (anim/animate opacity-value 0)
        (js/setTimeout #(reset! transparent? (not @transparent?)) 400))
      (do
        (reset! transparent? (not @transparent?))
        (js/setTimeout #(anim/animate opacity-value 1) 50)
        (js/setTimeout #(when @small-list-ref
                          (.scrollToIndex ^js @small-list-ref #js {:animated false :index index}))
                       100)
        (when (and platform/ios? portrait?)
          (js/setTimeout #(navigation/merge-options "lightbox" {:statusBar {:visible true}}) 150))))
    (anim/animate border-value (if (= opacity 1) 0 12))))

(defn handle-orientation
  [result index window-width window-height animations insets {:keys [flat-list-ref]}]
  (let [screen-width  (if (or platform/ios? (= result orientation/portrait))
                        window-width
                        window-height)
        screen-height (if (or platform/ios? (= result orientation/portrait))
                        window-height
                        window-width)
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
      (top-view/animate-rotation result screen-width screen-height insets animations))))

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
  [message index _ {:keys [screen-width screen-height] :as args}]
  [rn/view
   {:style (style/image (+ screen-width seperator-width) screen-height)}
   [zoomable-image/zoomable-image message index args
    #(toggle-opacity index args %)]
   [rn/view {:style {:width seperator-width}}]])

(defn drag-gesture
  [{:keys [pan-x pan-y background-color opacity layout]} x? set-full-height?]
  (->
    (gesture/gesture-pan)
    (gesture/enabled true)
    (gesture/max-pointers 1)
    (gesture/on-start #(reset! set-full-height? false))
    (gesture/on-update (fn [e]
                         (let [translation (if x? (oget e "translationX") (oget e "translationY"))
                               progress    (Math/abs (/ translation drag-threshold))]
                           (anim/set-val (if x? pan-x pan-y) translation)
                           (anim/set-val opacity (- 1 progress))
                           (anim/set-val layout (* progress -20)))))
    (gesture/on-end (fn [e]
                      (if (> (Math/abs (if x? (oget e "translationX") (oget e "translationY")))
                             drag-threshold)
                        (do
                          (anim/animate background-color "rgba(0,0,0,0)")
                          (anim/animate opacity 0)
                          (rf/dispatch [:navigate-back]))
                        (do
                          #(reset! set-full-height? true)
                          (anim/animate (if x? pan-x pan-y) 0)
                          (anim/animate opacity 1)
                          (anim/animate layout 0)))))))

(defn lightbox
  []
  [:f>
   (fn []
     ;; we get `insets` from `screen-params` because trying to consume it from
     ;; lightbox screen causes lots of problems
     (let [{:keys [messages index insets]} (rf/sub [:get-screen-params])
           render-list      (reagent/atom false)
           atoms            {:flat-list-ref      (atom nil)
                             :small-list-ref     (atom nil)
                             :scroll-index-lock? (atom true)}
           ;; The initial value of data is the image that was pressed (and not the whole album) in order
           ;; for the transition animation to execute properly, otherwise it would animate towards
           ;; outside the screen (even if we have `initialScrollIndex` set).
           data             (reagent/atom [(nth messages index)])
           scroll-index     (reagent/atom index)
           transparent?     (reagent/atom false)
           set-full-height? (reagent/atom false)
           window           (rf/sub [:dimensions/window])
           window-width     (:width window)
           window-height    (:height window)
           window-height    (if platform/android?
                              (+ window-height (:top insets))
                              window-height)
           animations       {:background-color (anim/use-val "rgba(0,0,0,0)")
                             :border           (anim/use-val (if platform/ios? 0 12))
                             :opacity          (anim/use-val 0)
                             :rotate           (anim/use-val "0deg")
                             :layout           (anim/use-val -10)
                             :top-view-y       (anim/use-val 0)
                             :top-view-x       (anim/use-val 0)
                             :top-view-width   (anim/use-val window-width)
                             :top-view-bg      (anim/use-val colors/neutral-100-opa-0)
                             :pan-y            (anim/use-val 0)
                             :pan-x            (anim/use-val 0)}
           derived          {:top-layout    (worklet/info-layout (:layout animations)
                                                                 true)
                             :bottom-layout (worklet/info-layout (:layout animations)
                                                                 false)}
           callback         (fn [e]
                              (on-viewable-items-changed e scroll-index atoms))]
       (anim/animate (:background-color animations) "rgba(0,0,0,1)")
       (reset! data messages)
       (orientation/use-device-orientation-change
         (fn [result]
           (if platform/ios?
             (handle-orientation result scroll-index window-width window-height animations insets atoms)
             ;; `use-device-orientation-change` will always be called on Android, so need to check
             (orientation/get-auto-rotate-state
               (fn [enabled?]
                 ;; RNN does not support landscape-right
                 (when (and enabled? (not= result orientation/landscape-right))
                   (handle-orientation result
                                       scroll-index
                                       window-width
                                       window-height
                                       animations
                                       insets
                                       atoms)))))))
       (rn/use-effect (fn []
                        (when @(:flat-list-ref atoms)
                          (.scrollToIndex ^js @(:flat-list-ref atoms)
                                          #js {:animated false :index index}))
                        (js/setTimeout (fn []
                                         (anim/animate (:opacity animations) 1)
                                         (anim/animate (:layout animations) 0)
                                         (anim/animate (:border animations) 12))
                                       (if platform/ios? 250 100))
                        (js/setTimeout #(reset! (:scroll-index-lock? atoms) false) 300)
                        ;(js/setTimeout #(reset! render-list true) 500)
                        (fn []
                          (rf/dispatch [:chat.ui/zoom-out-signal nil])
                          (when platform/android?
                            (rf/dispatch [:chat.ui/lightbox-scale 1])))))
       [:f>
        (fn []
          (let [curr-orientation (or (rf/sub [:lightbox/orientation]) orientation/portrait)
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
            ;[rn/view {:style (merge (style/image (+ screen-width seperator-width) screen-height) {:background-color :black})}
            ;[reanimated/fast-image
            ; {:source    {:uri (:image (:content (nth messages index)))}
            ;  :native-ID :shared-element
            ;  :style {:width window-width
            ;          :height (* (:image-height  (nth messages index)) (/ window-width (:image-width (nth messages index))))}
            ;  ;:style     (style/image dimensions animations (:border-value args))
            ;  }]]

            [reanimated/view
             {:style (reanimated/apply-animations-to-style {:background-color (:background-color
                                                                                animations)}
                                                           {:height screen-height})}
             ;(when-not @transparent?
             ;  [top-view/top-view (first messages) insets scroll-index animations derived landscape?
             ;   screen-width])
             [gesture/gesture-detector
              {:gesture (drag-gesture animations (and landscape? platform/ios?) set-full-height?)}
              [reanimated/view
               {:style (reanimated/apply-animations-to-style
                         {:transform [{:translateY (:pan-y animations)}
                                      {:translateX (:pan-x animations)}]}
                         {})}
               (if @render-list
                 [gesture/flat-list
                  {:ref                               #(reset! (:flat-list-ref atoms) %)
                   :key-fn                            :message-id
                   :style                             {:width (+ screen-width seperator-width)}
                   :data                              @data
                   :render-fn                         image
                   :render-data                       {:opacity-value    (:opacity animations)
                                                       :border-value     (:border animations)
                                                       :transparent?     transparent?
                                                       :set-full-height? set-full-height?
                                                       :screen-height    screen-height
                                                       :screen-width     screen-width
                                                       :window-height    window-height
                                                       :window-width     window-width
                                                       :atoms            atoms}
                   :initial-scroll-index index
                   :horizontal                        horizontal?
                   :inverted                          inverted?
                   :paging-enabled                    true
                   :get-item-layout                   (fn [_ index] (get-item-layout _ index item-width))
                   :viewability-config                {:view-area-coverage-percent-threshold 50
                                                       :wait-for-interaction                 true}
                   :shows-vertical-scroll-indicator   false
                   :shows-horizontal-scroll-indicator false
                   :on-viewable-items-changed         callback}]
               [rn/view
                {:style (style/image (+ screen-width seperator-width) screen-height)}
                 [rn/view {:style {:width window-width
                                   :height (* (:image-height  (nth messages index)) (/ window-width (:image-width (nth messages index))))}}
                  [reanimated/fast-image
                   {:source    {:uri (:image (:content (nth messages index)))}
                    :native-ID :shared-element
                    :style {:width window-width
                            :height (* (:image-height  (nth messages index)) (/ window-width (:image-width (nth messages index))))}
                    ;:style     (style/image dimensions animations (:border-value args))
                    }]
                  ]
                [rn/view {:style {:width seperator-width}}]]
                 )
               ]]
             ;(when (and (not @transparent?) (not landscape?))
             ;  [bottom-view/bottom-view messages index scroll-index insets animations derived
             ;   item-width atoms])
             ]
            ))]))])
