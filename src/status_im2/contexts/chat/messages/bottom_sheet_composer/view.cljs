(ns status-im2.contexts.chat.messages.bottom-sheet-composer.view
  (:require
    [quo2.core :as quo]
    [quo2.foundations.colors :as colors]
    [quo2.foundations.typography :as typography]
    [react-native.background-timer :as background-timer]
    [react-native.blur :as blur]
    [react-native.core :as rn]
    [react-native.gesture :as gesture]
    [react-native.hooks :as hooks]
    [react-native.linear-gradient :as linear-gradient]
    [react-native.permissions :as permissions]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [oops.core :as oops]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im.utils.utils :as utils-old]
    [status-im2.contexts.chat.messages.list.view :as messages.list]
    [utils.i18n :as i18n]
    [status-im2.contexts.chat.messages.bottom-sheet-composer.style :as style]
    [status-im2.contexts.chat.messages.bottom-sheet-composer.images.view :as images]
    [react-native.async-storage :as async-storage]
    [utils.re-frame :as rf]))

(def ^:const drag-threshold 30)

(def input-height (if platform/ios? 32 44))

(def ios-extra-offset 12)

(def overlay-height 80)

(def velocity-threshold -1000)

;;; CONTROLS
(defn send-button
  [input-ref text-value images? height saved-height opacity bg-bottom window-height]
  [:f> (fn []
         (let [btn-opacity (reanimated/use-shared-value 0)
               z-index     (reagent/atom 0)]

           [:f>
            (fn []
              (rn/use-effect (fn []
                               (if (or (not-empty @text-value) images?)
                                 (when-not (= @z-index 1)
                                   (reset! z-index 1)
                                   (js/setTimeout #(reanimated/animate btn-opacity 1) 50))
                                 (when-not (= @z-index 0)
                                   (reanimated/animate btn-opacity 0)
                                   (js/setTimeout #(reset! z-index 0) 300)))) [@text-value])
              [reanimated/view {:style (reanimated/apply-animations-to-style
                                         {:opacity btn-opacity}
                                         {:position         :absolute
                                          :right            0
                                          :z-index          @z-index
                                          :background-color  (colors/theme-colors colors/white colors/neutral-90)})}
               [quo/button
                {:icon                true
                 :size                32
                 :accessibility-label :send-message-button
                 :on-press            (fn []

                                        (reanimated/animate height input-height)
                                        (reanimated/animate opacity 0)
                                        (js/setTimeout #(reanimated/set-shared-value saved-height input-height) 300)
                                        (js/setTimeout #(reanimated/set-shared-value bg-bottom (- window-height)) 300)

                                        (reset! text-value "")
                                        (.clear ^js @input-ref)
                                        (messages.list/scroll-to-bottom)
                                        (rf/dispatch [:chat.ui/send-current-message]))}
                :i/arrow-up]])]))])

(defn audio-button
  []
  [quo/button
   {:on-press #(js/alert "to be added")
    :icon     true
    :type     :outline
    :size     32}
   :i/audio])
(defn camera-button
  []
  [quo/button
   {:on-press #(js/alert "to be implemented")
    :icon     true
    :type     :outline
    :size     32
    :style    {:margin-right 12}}
   :i/camera])
(defn image-button
  [insets]
  [quo/button
   {:on-press (fn []
                (permissions/request-permissions
                  {:permissions [:read-external-storage :write-external-storage]
                   :on-allowed  #(rf/dispatch
                                   [:open-modal :photo-selector {:insets insets}])
                   :on-denied   (fn []
                                  (background-timer/set-timeout
                                    #(utils-old/show-popup (i18n/label :t/error)
                                                           (i18n/label
                                                             :t/external-storage-denied))
                                    50))}))
    :icon     true
    :type     :outline
    :size     32
    :style    {:margin-right 12}}
   :i/image])

(defn reaction-button
  []
  [quo/button
   {:on-press #(js/alert "to be implemented")
    :icon     true
    :type     :outline
    :size     32
    :style    {:margin-right 12}}
   :i/reaction])

(defn format-button
  []
  [quo/button
   {:on-press #(js/alert "to be implemented")
    :icon     true
    :type     :outline
    :size     32}
   :i/format])

(defn drag-gesture
  [height saved-height opacity bg-bottom window-height keyboard-shown max-height input-ref lines add-keyboard-height saved-keyboard-height emojis-open gesture-enabled? last-height maximized?]
  (let [expanding? (atom true)]
    (->
      (gesture/gesture-pan)
      (gesture/enabled @gesture-enabled?)
      (gesture/on-start (fn [e]
                          (if-not keyboard-shown
                            (do
                              (println "velo" (oops/oget e "velocityY"))
                              (when (< (oops/oget e "velocityY") velocity-threshold)
                                (reanimated/set-shared-value last-height max-height))
                              (.focus ^js @input-ref)
                              (reset! gesture-enabled? false))
                            (do
                              (reanimated/set-shared-value bg-bottom 0)
                              (reset! expanding? (neg? (oops/oget e "velocityY")))))))
      (gesture/on-update (fn [e]
                           (let [translation          (oops/oget e "translationY")
                                 new-height           (Math/max input-height (Math/min (+ (- (/ translation 1)) (reanimated/get-shared-value saved-height)) max-height))
                                 remaining-height     (if @expanding? (- max-height (reanimated/get-shared-value saved-height)) (reanimated/get-shared-value saved-height))
                                 progress             (/ translation remaining-height)
                                 progress             (if (= new-height input-height) 1 progress)
                                 currently-expanding? (neg? (oops/oget e "velocityY"))
                                 maximum-opacity?     (and currently-expanding? (= (reanimated/get-shared-value opacity) 1))
                                 minimum-opacity?     (and (not currently-expanding?) (< (reanimated/get-shared-value opacity) 0.1))]
                             (when keyboard-shown
                               (if (>= translation 0)
                                 (do
                                   (reanimated/set-shared-value height new-height)
                                   (when (and (pos? progress) (not minimum-opacity?))
                                     (reanimated/set-shared-value opacity (- 1 progress))))
                                 (do
                                   (reanimated/set-shared-value height new-height)
                                   (when (and @expanding? (not maximum-opacity?))
                                     (reanimated/set-shared-value opacity (Math/abs progress)))))))))
      (gesture/on-end (fn [e]
                        (let [collapsing? (pos? (oops/oget e "velocityY"))
                              diff        (- (reanimated/get-shared-value height) (reanimated/get-shared-value saved-height)) remaining (if (not collapsing?) (- max-height (reanimated/get-shared-value height)) (reanimated/get-shared-value height))
                              threshold   (if (> remaining drag-threshold) drag-threshold 10)]
                          (if @gesture-enabled?
                            (if (>= diff 0)
                              (if (and (> diff threshold) (not collapsing?))
                                (do
                                  (reanimated/animate height max-height)
                                  (reanimated/set-shared-value saved-height max-height)
                                  (reanimated/set-shared-value bg-bottom 0)
                                  (reanimated/animate opacity 1)
                                  (reset! maximized? true))
                                (do
                                  (reanimated/animate height (reanimated/get-shared-value saved-height))
                                  (when (or (and collapsing? (not= (reanimated/get-shared-value saved-height) max-height)) (= (reanimated/get-shared-value saved-height) input-height))
                                    (reanimated/animate opacity 0)
                                    (reanimated/animate-delay bg-bottom (- window-height) 300))))
                              (if (or (> (reanimated/get-shared-value height) (- max-height threshold)) (and (not collapsing?) (> (Math/abs diff) threshold)))
                                (do
                                  (reanimated/animate height max-height)
                                  (reanimated/set-shared-value saved-height max-height)
                                  (reanimated/animate opacity 1)
                                  (reset! maximized? true))
                                (let [target-height (if (> lines 1) (+ input-height 18) input-height)]

                                  (when @add-keyboard-height
                                    (reset! emojis-open false)
                                    (reset! saved-keyboard-height @add-keyboard-height)
                                    (reset! add-keyboard-height nil))

                                  (.blur ^js @input-ref)
                                  (reanimated/animate height target-height)
                                  (js/setTimeout #(reanimated/set-shared-value saved-height target-height) 300)
                                  (js/setTimeout #(reanimated/set-shared-value bg-bottom (- window-height)) 300)
                                  (reanimated/animate opacity 0))))
                            (reset! gesture-enabled? true))))))))

(defn handle
  []
  [rn/view {:style (style/handle-container)}
   [rn/view {:style (style/handle)}]])

(defn actions
  [input-ref text-value images? height saved-height opacity bg-bottom window-height insets]
  [rn/view {:style (style/actions-container)}
   [rn/view {:style {:flex-direction :row}}
    [camera-button]
    [image-button insets]
    [reaction-button]
    [format-button]]
   [send-button input-ref text-value images? height saved-height opacity bg-bottom window-height]
   [audio-button]])

;;; MAIN
(defn sheet
  ;; safe-area consumer insets makes incorrect values on Android
  [insets blur-opacity layout-height]
  [:f> (fn []
         (let [input-ref              (atom nil)
               kb-default-height      (reagent/atom nil)
               line-height            (:line-height typography/paragraph-1)
               opacity                (reanimated/use-shared-value 0)
               overlay-opacity        (reanimated/use-shared-value 0)
               overlay-z-index        (reagent/atom 0)
               window-height          (rf/sub [:dimensions/window-height])
               bg-bottom              (reanimated/use-shared-value (- window-height))
               focused?               (reagent/atom false)
               gesture-enabled?       (reagent/atom true)
               cursor-position        (reagent/atom {:start 0 :end 0})
               saved-cursor-position  (reagent/atom {:start 0 :end 0})
               text-value             (reagent/atom "")
               lock-selection         (reagent/atom true)
               lock-layout?           (reagent/atom false)
               android-blur?          (reagent/atom true)
               keyboard-show-listener (atom nil)
               keyboard-hide-listener (atom nil)
               add-keyboard-height    (atom nil)
               saved-keyboard-height  (atom nil)
               margin-top             (if platform/ios? (:top insets) (+ (:top insets) 10))
               emojis-open            (reagent/atom false)
               maximized?             (reagent/atom false)]
           [:f>
            (fn []
              (let [
                    images         (rf/sub [:chats/sending-image])
                    {:keys [input-text input-content-height]} (rf/sub [:chats/current-chat-input])
                    content-height (reagent/atom (or input-content-height input-height))
                    {:keys [keyboard-shown keyboard-height]} (hooks/use-keyboard)
                    max-height     (- window-height margin-top keyboard-height style/handle-container-height style/actions-container-height)
                    max-height     (if (and (zero? keyboard-height) @kb-default-height) (- max-height @kb-default-height) max-height)
                    max-height     (if (seq images) (- max-height 76) max-height)
                    lines          (Math/round (/ @content-height line-height))
                    lines          (if platform/ios? lines (dec lines))
                    initial-height (if (> lines 1) (+ input-height 18) input-height)
                    height         (reanimated/use-shared-value initial-height)
                    saved-height   (reanimated/use-shared-value initial-height)
                    last-height    (reanimated/use-shared-value (Math/max input-content-height initial-height)) ;; add extra offset
                    max-lines      (Math/round (/ max-height line-height))
                    max-lines      (if platform/ios? max-lines (dec max-lines))
                    expanded?      (= (reanimated/get-shared-value height) max-height)]
                (rn/use-effect
                  (fn []
                    (js/setTimeout #(reset! lock-layout? true) 500)
                    (when-not @kb-default-height
                      (async-storage/get-item "kb-default-height" (fn [result] (reset! kb-default-height (when-not (= nil result) (js/parseInt result))))))
                    (when (and (empty? @text-value) (not= input-text nil))
                      (reset! text-value input-text)
                      (reset! content-height input-content-height)
                      (when (> lines 1)
                        (reanimated/animate height (+ input-height 18))
                        (reanimated/set-shared-value saved-height (+ input-height 18)))
                      (reset! saved-cursor-position {:start (count input-text) :end (count input-text)}))
                    (when @maximized?
                      (reanimated/set-shared-value height max-height)
                      (reanimated/set-shared-value saved-height max-height))
                    (reset! keyboard-show-listener (.addListener rn/keyboard (if platform/ios? "keyboardWillChangeFrame" "keyboardDidShow")
                                                                 (fn [e]
                                                                   (when (and (not @kb-default-height) (pos? keyboard-height))
                                                                     (async-storage/set-item "kb-default-height" keyboard-height))
                                                                   (if platform/ios?
                                                                     (let [start-h   (oops/oget e "startCoordinates.height")
                                                                           end-h     (oops/oget e "endCoordinates.height")
                                                                           diff      (- end-h start-h)
                                                                           max       (- max-height diff)
                                                                           curr-text @text-value]
                                                                       (if (> (reanimated/get-shared-value height) max)
                                                                         (do
                                                                           (reanimated/set-shared-value height (- (reanimated/get-shared-value height) diff))
                                                                           (reanimated/set-shared-value saved-height (- (reanimated/get-shared-value saved-height) diff))
                                                                           (reset! emojis-open true)
                                                                           (reset! text-value (str @text-value " "))
                                                                           (js/setTimeout #(reset! text-value curr-text) 0)
                                                                           (reset! add-keyboard-height diff))
                                                                         (when @add-keyboard-height
                                                                           (reanimated/set-shared-value height (+ (reanimated/get-shared-value height) @add-keyboard-height))
                                                                           (reanimated/set-shared-value saved-height (+ (reanimated/get-shared-value saved-height) @add-keyboard-height))
                                                                           (reset! emojis-open false)
                                                                           (reset! add-keyboard-height nil))))
                                                                     (reset! android-blur? false)))))
                    (reset! keyboard-hide-listener (.addListener rn/keyboard "keyboardDidHide"
                                                                 (fn []
                                                                   (when platform/android? ;; TODO should use target-height
                                                                     (.blur ^js @input-ref)
                                                                     (reanimated/animate opacity 0)
                                                                     (js/setTimeout (fn []
                                                                                      (reanimated/animate height input-height)
                                                                                      (reanimated/set-shared-value saved-height input-height)
                                                                                      (reanimated/set-shared-value bg-bottom (- window-height))) 100)
                                                                     (js/setTimeout #(reanimated/set-shared-value blur-opacity 1) 400)))))
                    (fn []
                      (.remove ^js @keyboard-show-listener)
                      (.remove ^js @keyboard-hide-listener))) [max-height])
                [:<>
                 [reanimated/view {:style (style/background opacity bg-bottom window-height height)}]
                 [gesture/gesture-detector {:gesture (drag-gesture height saved-height opacity bg-bottom window-height keyboard-shown max-height input-ref lines add-keyboard-height saved-keyboard-height emojis-open gesture-enabled? last-height maximized?)}
                  [rn/view {:style     (style/container insets @android-blur? @focused? (not-empty @text-value) (seq images))
                            :on-layout (fn [e]
                                         (when-not @lock-layout?
                                           (reanimated/set-shared-value layout-height (oops/oget e "nativeEvent.layout.height"))))}
                   [handle]
                   [reanimated/touchable-opacity {:active-opacity 1
                                                  :on-press       #(.focus ^js @input-ref) ;; for android when first entering screen
                                                  :style          (style/input-container height max-height @emojis-open)}
                    [reanimated/linear-gradient
                     {:colors ["rgba(255,255,255,0)" "rgba(255,255,255,1)"]
                      :start  {:x 0 :y 1}
                      :end    {:x 0 :y 0}
                      :style  (style/text-top-overlay overlay-opacity @overlay-z-index)}]
                    [rn/text-input
                     {:ref                    #(reset! input-ref %)
                      :default-value          @text-value
                      :on-change-text         (fn [text]
                                                (reset! text-value text)
                                                (js/setTimeout #(.setNativeProps ^js @input-ref (clj->js {:selection (clj->js @cursor-position)})) 20)
                                                (rf/dispatch [:chat.ui/set-chat-input-text text]))
                      :on-selection-change    (fn [e]
                                                (when-not @lock-selection
                                                  (reset! cursor-position {:start (oops/oget e "nativeEvent.selection.end") :end (oops/oget e "nativeEvent.selection.end")})))
                      :on-focus               (fn []
                                                (reset! focused? true)
                                                (when platform/android?
                                                  (reset! android-blur? false))
                                                (js/setTimeout #(reset! lock-selection false) 300)
                                                (when (not-empty @text-value)
                                                  (.setNativeProps ^js @input-ref (clj->js {:selection @saved-cursor-position})))
                                                (reanimated/animate height (reanimated/get-shared-value last-height))
                                                (reanimated/set-shared-value saved-height (reanimated/get-shared-value last-height))
                                                (when @saved-keyboard-height
                                                  (js/setTimeout (fn []
                                                                   (when (> lines max-lines)
                                                                     (reanimated/animate height (+ (reanimated/get-shared-value last-height) @saved-keyboard-height))
                                                                     (reanimated/set-shared-value saved-height (+ (reanimated/get-shared-value last-height) @saved-keyboard-height)))
                                                                   (reset! saved-keyboard-height nil)) 600))
                                                (when (> (reanimated/get-shared-value last-height) (* 0.75 max-height))
                                                  (reanimated/animate opacity 1)
                                                  (reanimated/set-shared-value bg-bottom 0))
                                                (reanimated/set-shared-value blur-opacity 0)
                                                (when (= @overlay-z-index -1)
                                                  (reanimated/animate overlay-opacity 1)
                                                  (reset! overlay-z-index 1))
                                                (rf/dispatch [:chat.ui/set-input-focused true]))
                      :on-blur                (fn []
                                                (let [target-height (if (> lines 1) (+ input-height 18) input-height)]
                                                  (reset! saved-cursor-position @cursor-position)
                                                  (reanimated/set-shared-value last-height (if (empty? @text-value) target-height (Math/min (+ @content-height (if platform/ios? 5 0))
                                                                                                                                            (reanimated/get-shared-value saved-height))))
                                                  (when (not= (reanimated/get-shared-value last-height) max-height)
                                                    (reset! maximized? false))
                                                  (reanimated/set-shared-value blur-opacity 1)
                                                  (reanimated/animate height target-height)
                                                  (reanimated/set-shared-value saved-height target-height)
                                                  (reset! focused? false)
                                                  (reset! lock-selection true)
                                                  (when platform/android?
                                                    (reset! android-blur? true))
                                                  (reanimated/animate overlay-opacity 0)
                                                  (reset! overlay-z-index (if (= @overlay-z-index 1) -1 0))
                                                  (rf/dispatch [:chat.ui/set-input-focused false])))
                      :style                  (style/input keyboard-shown @focused? expanded? @saved-keyboard-height)
                      :on-scroll              (fn [e] (let [y (oops/oget e "nativeEvent.contentOffset.y")]
                                                        (when (and (> y line-height) (>= lines max-lines) (= @overlay-z-index 0) @focused?)
                                                          (reset! overlay-z-index 1)
                                                          (js/setTimeout #(reanimated/animate overlay-opacity 1) 0))
                                                        (when (and (<= y line-height) (= @overlay-z-index 1))
                                                          (reanimated/animate overlay-opacity 0)
                                                          (js/setTimeout #(reset! overlay-z-index 0) 300))))
                      :on-content-size-change (fn [e]
                                                (when keyboard-shown
                                                  (let [extra-offset (if platform/ios? (if @emojis-open ios-extra-offset 5) 0)
                                                        x            (+ (oops/oget e "nativeEvent.contentSize.height") extra-offset)
                                                        diff         (Math/abs (- x (reanimated/get-shared-value height)))]
                                                    (reset! content-height (oops/oget e "nativeEvent.contentSize.height"))
                                                    (when (and (> diff 10) (not-empty @text-value) (<= x (+ max-height line-height)) (not= (reanimated/get-shared-value height) max-height))
                                                      (reanimated/animate height (Math/min x max-height))
                                                      (reanimated/set-shared-value saved-height (Math/min x max-height)))
                                                    (if (or (> x (* 0.75 max-height)) (= (reanimated/get-shared-value saved-height) max-height))
                                                      (do
                                                        (reanimated/set-shared-value bg-bottom 0)
                                                        (reanimated/animate opacity 1))
                                                      (do
                                                        (reanimated/animate opacity 0)
                                                        (js/setTimeout #(reanimated/set-shared-value bg-bottom (- window-height)) 300)))
                                                    (println "dispatching" (oops/oget e "nativeEvent.contentSize.height"))
                                                    (rf/dispatch [:chat.ui/set-input-content-height (Math/min x max-height)]))))
                      :max-height             max-height
                      :multiline              true
                      :placeholder-text-color (colors/theme-colors colors/neutral-40 colors/neutral-60)
                      :placeholder            (i18n/label :t/type-something)}]
                    (when (and (not-empty @text-value) (not @focused?) (> lines 2))
                      [rn/touchable-without-feedback
                       {:on-press #(.focus ^js @input-ref)}
                       [linear-gradient/linear-gradient
                        {:colors ["rgba(255,255,255,1)" "rgba(255,255,255,0)"]
                         :start  {:x 0 :y 1}
                         :end    {:x 0 :y 0}
                         :style  (style/text-overlay)}]])]
                   [images/images-list @maximized?]
                   [actions input-ref text-value (seq images) height saved-height opacity bg-bottom window-height insets]]]]))]))])


(defn blur-view [blur-opacity layout-height]
  [:f>
   (fn []
     [reanimated/view {:style (style/blur-container blur-opacity layout-height)}
      [blur/view {:style       {:width  "100%"
                                :height "100%"}
                  :blur-radius 20
                  :blur-type   :light
                  :blur-amount 20}]])])

(defn bottom-sheet-composer
  []
  [:f>
   (fn []
     (let [insets        (safe-area/use-safe-area)
           blur-opacity  (reanimated/use-shared-value 1)
           layout-height (reanimated/use-shared-value (+ (if platform/ios? 108 120) (:bottom insets)))] ;; todo, renter screen extra height
       [rn/view
        [blur-view blur-opacity layout-height]
        [sheet insets blur-opacity layout-height]]))])

