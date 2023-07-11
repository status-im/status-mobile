(ns status-im2.contexts.chat.camera.view
  (:require
    [quo2.core :as quo]
    [quo2.foundations.colors :as colors]
    [react-native.camera-kit :as camera-kit]
    [react-native.core :as rn]
    [react-native.fast-image :as fast-image]
    [react-native.reanimated :as reanimated]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [utils.i18n :as i18n]
    [status-im2.contexts.chat.camera.style :as style]
    [utils.re-frame :as rf]))

(defn- f-zoom-button
  [{:keys [value curr-zoom]}]
  (let [selected (= @curr-zoom value)
        size     (reanimated/use-shared-value (if selected 37 25))]
    (rn/use-effect #(reanimated/animate size (if selected 37 25)) [@curr-zoom])
    [rn/touchable-opacity
     {:on-press #(reset! curr-zoom value)
      :style    style/zoom-button-container}
     [reanimated/view {:style (style/zoom-button size)}
      [quo/text
       {:size   (if selected :paragraph-2 :label)
        :weight :semi-bold
        :style  {:color (if selected
                          colors/system-yellow
                          colors/white)}}
       (str value (when selected "x"))]]]))

(defn zoom-button
  [args]
  [:f> f-zoom-button args])

(defn snap-button
  [camera-ref uri]
  [rn/view {:style style/outer-circle}
   [rn/touchable-opacity
    {:on-press (fn [] (camera-kit/capture @camera-ref #(reset! uri (.-uri %))))
     :style    style/inner-circle}]])

(defn camera-screen
  []
  (let [camera-ref (atom nil)
        uri        (reagent/atom nil)
        curr-zoom  (reagent/atom "1")]
    (fn []
      (let [window                 (rn/get-window)
            {:keys [width height]} window
            camera-window-height   (* width 1.33)
            insets                 (safe-area/get-insets)
            top                    (/ (- height camera-window-height (:bottom insets)) 2)]
        [rn/view
         {:style {:flex             1
                  :background-color colors/black}}
         [rn/view {:style style/flash-container}
          [quo/icon :i/flash-camera {:color colors/white
                                     :size 24}]]
         (if-not @uri
           [camera-kit/camera
            {:ref   #(reset! camera-ref %)
             :style (style/camera-window width camera-window-height top)}]
           [fast-image/fast-image
            {:style  (style/camera-window width camera-window-height top)
             :source {:uri @uri}}])
         [rn/view {:style (style/zoom-container top insets)}
          [zoom-button {:value "0.5" :curr-zoom curr-zoom}]
          [zoom-button {:value "1" :curr-zoom curr-zoom}]
          [zoom-button {:value "2" :curr-zoom curr-zoom}]
          [zoom-button {:value "3" :curr-zoom curr-zoom}]]
         (if-not @uri
           [rn/view {:style (style/bottom-area top insets)}
            [quo/text {:style style/photo-text} (i18n/label :PHOTO)]
            [rn/view {:style style/actions-container}
             [quo/text
              {:on-press #(rf/dispatch [:navigate-back])
               :style    {:font-size 17
                          :color     colors/white}} (i18n/label :cancel)]
             [snap-button camera-ref uri]
             [quo/icon :i/rotate-camera {:size 48 :color colors/white}]]]
           [rn/view {:style (style/confirmation-container insets)}
            [quo/text
             {:on-press #(reset! uri nil)
              :style    {:font-size 17
                         :color     colors/white}} (i18n/label :retake)]
            [quo/text
             {:on-press (fn []
                          (rf/dispatch [:photo-selector/camera-roll-pick {:uri @uri}])
                          (rf/dispatch [:navigate-back]))
              :style    {:font-size 17
                         :color     colors/white}} (i18n/label :use-photo)]])]))))
