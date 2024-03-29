(ns status-im.common.lightbox.bottom-view
  (:require
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [status-im.common.lightbox.animations :as anim]
    [status-im.common.lightbox.constants :as constants]
    [status-im.common.lightbox.style :as style]
    [status-im.common.lightbox.text-sheet.view :as text-sheet]
    [utils.re-frame :as rf]))

(defn get-small-item-layout
  [_ index]
  #js
   {:length constants/small-image-size
    :offset (* (+ constants/small-image-size 8) index)
    :index  index})

(defn- f-small-image
  [item index _ {:keys [scroll-index props]}]
  (let [size (if (= @scroll-index index) constants/focused-image-size constants/small-image-size)
        size-value (anim/use-val size)
        {:keys [scroll-index-lock? small-list-ref flat-list-ref]}
        props]
    (anim/animate size-value size)
    [rn/touchable-opacity
     {:active-opacity 1
      :on-press       (fn []
                        (rf/dispatch [:lightbox/zoom-out-signal @scroll-index])
                        (reset! scroll-index-lock? true)
                        (js/setTimeout #(reset! scroll-index-lock? false) 500)
                        (js/setTimeout
                         (fn []
                           (reset! scroll-index index)
                           (.scrollToIndex ^js @small-list-ref
                                           #js {:animated true :index index})
                           (.scrollToIndex ^js @flat-list-ref
                                           #js {:animated true :index index}))
                         (if platform/ios? 50 150))
                        (rf/dispatch [:lightbox/update-animation-shared-element-id (:id item)]))}
     [reanimated/fast-image
      {:source {:uri (:image item)}
       :style  (reanimated/apply-animations-to-style {:width  size-value
                                                      :height size-value}
                                                     {:border-radius 10})}]]))

(defn small-image
  [item index _ render-data]
  [:f> f-small-image item index _ render-data])

(defn bottom-view
  [{:keys [images index scroll-index insets animations derived item-width props state transparent?
           bottom-text-component]}]
  (let [padding-horizontal (- (/ item-width 2) (/ constants/focused-image-size 2))]
    [reanimated/linear-gradient
     {:colors   [colors/neutral-100-opa-100 colors/neutral-100-opa-80 colors/neutral-100-opa-0]
      :location [0.2 0.9]
      :start    {:x 0 :y 1}
      :end      {:x 0 :y 0}
      :style    (style/gradient-container insets animations derived transparent?)}
     (when bottom-text-component
       [text-sheet/view
        {:overlay-opacity  (:overlay-opacity animations)
         :overlay-z-index  (:overlay-z-index state)
         :text-sheet-lock? (:text-sheet-lock? props)
         :text-component   bottom-text-component}])
     [rn/flat-list
      {:ref                               #(reset! (:small-list-ref props) %)
       :key-fn                            :id
       :style                             {:height constants/small-list-height}
       :data                              images
       :render-fn                         small-image
       :render-data                       {:scroll-index scroll-index
                                           :props        props}
       :horizontal                        true
       :shows-horizontal-scroll-indicator false
       :get-item-layout                   get-small-item-layout
       :separator                         [rn/view {:style {:width 8}}]
       :initial-scroll-index              index
       :content-container-style           (style/content-container padding-horizontal)}]
     ;; This is needed so that text does not show in the bottom inset part as it is transparent
     [rn/view {:style (style/bottom-inset-cover-up insets)}]]))
