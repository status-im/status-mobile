(ns status-im2.contexts.chat.camera.view
  (:require
    [quo2.core :as quo]
    [quo2.foundations.colors :as colors]
    [react-native.camera-kit :as camera-kit]
    [react-native.core :as rn]
    [react-native.fast-image :as fast-image]
    [react-native.orientation :as orientation]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [utils.i18n :as i18n]
    [status-im2.contexts.chat.camera.style :as style]
    [utils.re-frame :as rf]))

(defn retake
  [flash uri]
  (let [current-flash @flash]
    (when platform/android?
      ;; On Android, setting flash needs to be delayed until camera has initialized
      (reset! flash false)
      (js/setTimeout #(reset! flash current-flash) 300))
    (reset! uri nil)))

(defn handle-orientation
  [current-orientation rotate]
  (orientation/use-device-orientation-change (fn [result]
                                               (reset! current-orientation result)
                                               (cond
                                                 (= result orientation/landscape-left)
                                                 (reanimated/animate rotate "90deg")
                                                 (= result orientation/landscape-right)
                                                 (reanimated/animate rotate "-90deg")
                                                 :else
                                                 (reanimated/animate rotate "0deg")))))

(defn- f-zoom-button
  [{:keys [value current-zoom rotate]}]
  (let [selected? (= @current-zoom value)
        size      (reanimated/use-shared-value (if selected? 37 25))]
    (rn/use-effect #(reanimated/animate size (if selected? 37 25)) [@current-zoom])
    [rn/touchable-opacity
     {:on-press            #(reset! current-zoom value)
      :style               style/zoom-button-container
      :accessibility-label (str "zoom-" value)}
     [reanimated/view {:style (style/zoom-button size rotate)}
      [quo/text
       {:size            (if selected? :paragraph-2 :label)
        :weight          :semi-bold
        :number-of-lines 1
        :style           {:color (if selected?
                                   colors/system-yellow
                                   colors/white)}}
       (str value (when selected? "x"))]]]))

(defn zoom-button
  [args]
  [:f> f-zoom-button args])

(defn snap-button
  [camera-ref uri]
  [rn/view
   {:style               style/outer-circle
    :accessibility-label :snap}
   [rn/touchable-opacity
    {:on-press (fn []
                 (camera-kit/capture @camera-ref #(reset! uri %)))
     :style    style/inner-circle}]])

(defn zoom-buttons
  []
  (let [current-zoom (reagent/atom 1)]
    (fn [top insets rotate]
      [rn/view {:style (style/zoom-container top insets)}
       [zoom-button {:value 0.5 :current-zoom current-zoom :rotate rotate}]
       [zoom-button {:value 1 :current-zoom current-zoom :rotate rotate}]
       [zoom-button {:value 2 :current-zoom current-zoom :rotate rotate}]
       [zoom-button {:value 3 :current-zoom current-zoom :rotate rotate}]])))


(defn- f-bottom-area
  [{:keys [top insets uri camera-ref rotate]} back flip-camera]
  [rn/view {:style (style/bottom-area top insets @uri)}
   [quo/text {:style style/photo-text} (i18n/label :t/photo-caps)]
   [rn/view {:style style/actions-container}
    [quo/text
     {:on-press            back
      :style               {:font-size 17
                            :color     colors/white}
      :accessibility-label :cancel}
     (i18n/label :t/cancel)]
    [snap-button camera-ref uri]
    [reanimated/touchable-opacity
     {:style    (reanimated/apply-animations-to-style {:transform [{:rotate rotate}]} {})
      :on-press flip-camera}
     [quo/icon :i/rotate-camera
      {:size 48 :color colors/white :accessibility-label :flip-camera}]]]])

(defn bottom-area
  [{:keys [flash camera-type] :as args}]
  (let [back        #(rf/dispatch [:navigate-back])
        flip-camera (fn []
                      (reset! flash false)
                      (reset! camera-type (if (= @camera-type camera-kit/camera-type-back)
                                            camera-kit/camera-type-front
                                            camera-kit/camera-type-back)))]
    [:f> f-bottom-area args back flip-camera]))

(defn- f-camera-screen
  [{:keys [camera-ref uri camera-type current-orientation flash toggle-flash]}]
  (let [window                 (rn/get-window)
        {:keys [width height]} window
        camera-window-height   (* width 1.33)
        insets                 (safe-area/get-insets)
        top                    (/ (- height camera-window-height (:bottom insets)) 2)
        top-landscape          (/ (- height (* width 0.75) (:bottom insets)) 2)
        portrait?              (= @current-orientation orientation/portrait)
        rotate                 (reanimated/use-shared-value "0deg")
        on-press               #(retake flash uri)
        use-photo              (fn []
                                 (rf/dispatch [:photo-selector/camera-roll-pick {:uri @uri}])
                                 (rf/dispatch [:navigate-back]))]
    (handle-orientation current-orientation rotate)
    [rn/view {:style style/screen-container}
     [reanimated/touchable-opacity
      {:active-opacity 1
       :on-press       toggle-flash
       :style          (style/flash-container rotate @uri)}
      (when-not @flash
        [rn/view {:style style/cancel-dash}])
      [quo/icon :i/flash-camera
       {:color colors/white
        :size  24}]]
     (if @uri
       [fast-image/fast-image
        {:style  (style/image width camera-window-height (if portrait? top top-landscape) portrait?)
         :source {:uri @uri}}]
       [camera-kit/camera
        {:ref         #(reset! camera-ref %)
         :style       (style/camera-window width camera-window-height top)
         :flash-mode  (if @flash :on :off)
         :camera-type @camera-type}])
     (when-not @uri
       [zoom-buttons top insets rotate])
     [rn/view {:style (style/confirmation-container insets @uri)}
      [quo/text
       {:on-press on-press
        :style    {:font-size 17
                   :color     colors/white}}
       (i18n/label :t/retake)]
      [quo/text
       {:on-press use-photo
        :style    {:font-size 17
                   :color     colors/white}}
       (i18n/label :t/use-photo)]]
     [bottom-area
      {:top         top
       :insets      insets
       :uri         uri
       :camera-type camera-type
       :camera-ref  camera-ref
       :flash       flash
       :rotate      rotate}]]))

(defn camera-screen
  []
  (let [flash (reagent/atom false)
        args  {:camera-ref          (atom nil)
               :uri                 (reagent/atom nil)
               :camera-type         (reagent/atom camera-kit/camera-type-back)
               :current-orientation (atom orientation/portrait)
               :flash               flash
               :toggle-flash        #(swap! flash not)}]
    [:f> f-camera-screen args]))
