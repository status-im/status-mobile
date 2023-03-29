(ns status-im2.contexts.chat.messages.composer-new.view
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
    [status-im2.contexts.chat.messages.composer-new.style :as style]
    [utils.re-frame :as rf]))

(def ^:const drag-threshold 30)

(def input-height (if platform/ios? 32 44))

(def ios-extra-offset 12)

(def overlay-height 80)

;;; CONTROLS
(defn image-button
  [chat-id]
  [quo/button
   {:on-press (fn []
                (permissions/request-permissions
                  {:permissions [:read-external-storage :write-external-storage]
                   :on-allowed  #(rf/dispatch
                                   [:open-modal :photo-selector {:chat-id chat-id}])
                   :on-denied   (fn []
                                  (background-timer/set-timeout
                                    #(utils-old/show-popup (i18n/label :t/error)
                                                           (i18n/label
                                                             :t/external-storage-denied))
                                    50))}))
    :icon     true
    :type     :outline
    :size     32}
   :i/image])

(defn drag-gesture
  [height saved-height opacity bg-bottom window-height keyboard-shown max-height input-ref lines add-keyboard-height saved-keyboard-height emojis-open]
  (let [expanding? (atom true)]
    (->
      (gesture/gesture-pan)
      (gesture/on-start (fn [e] (reanimated/set-shared-value bg-bottom 0)
                          (reset! expanding? (neg? (oops/oget e "velocityY")))))
      (gesture/on-update (fn [e]
                           (let [translation          (oops/oget e "translationY")
                                 new-height           (Math/max input-height (Math/min (+ (- (/ translation 1)) (reanimated/get-shared-value saved-height)) max-height))
                                 remaining-height     (if @expanding? (- max-height (reanimated/get-shared-value saved-height)) (reanimated/get-shared-value saved-height))
                                 progress             (/ translation remaining-height)
                                 progress             (if (= new-height input-height) 1 progress)
                                 currently-expanding? (neg? (oops/oget e "velocityY"))
                                 maximum-opacity?     (and currently-expanding? (= (reanimated/get-shared-value opacity) 1))]
                             (if keyboard-shown
                               (if (>= translation 0)
                                 (do
                                   (reanimated/set-shared-value height new-height)
                                   (when (and (pos? progress) (not= (reanimated/get-shared-value opacity) 0))
                                     (reanimated/set-shared-value opacity (- 1 progress))))
                                 (do
                                   (reanimated/set-shared-value height new-height)
                                   (when (and @expanding? (not maximum-opacity?))
                                     (reanimated/set-shared-value opacity (Math/abs progress)))))
                               (.focus ^js @input-ref)))))
      (gesture/on-end (fn [e]
                        (let [collapsing? (pos? (oops/oget e "velocityY"))
                              diff        (- (reanimated/get-shared-value height) (reanimated/get-shared-value saved-height))
                              remaining   (if (not collapsing?) (- max-height (reanimated/get-shared-value height)) (reanimated/get-shared-value height))
                              threshold   (if (> remaining drag-threshold) drag-threshold 10)]
                          (if (>= diff 0)
                            (if (and (> diff threshold) (not collapsing?))
                              (do
                                (reanimated/animate height max-height)
                                (reanimated/set-shared-value saved-height max-height)
                                (reanimated/set-shared-value bg-bottom 0)
                                (reanimated/animate opacity 1))
                              (do
                                (reanimated/animate height (reanimated/get-shared-value saved-height))
                                (when (or (and collapsing? (not= (reanimated/get-shared-value saved-height) max-height)) (= (reanimated/get-shared-value saved-height) input-height))
                                  (reanimated/animate opacity 0)
                                  (reanimated/animate-delay bg-bottom (- window-height) 300))))
                            (if (or (< (Math/abs diff) threshold) (and (not collapsing?) (> (Math/abs diff) threshold)))
                              (do
                                (reanimated/animate height max-height)
                                (reanimated/set-shared-value saved-height max-height)
                                (reanimated/animate opacity 1))
                              (do
                                (let [target-height (if (> lines 1) (+ input-height 18) input-height)]

                                  (when @add-keyboard-height
                                    (reset! emojis-open false)
                                    (reset! saved-keyboard-height @add-keyboard-height)
                                    (reset! add-keyboard-height nil))

                                  (.blur ^js @input-ref)
                                  (reanimated/animate height target-height)
                                  (js/setTimeout #(reanimated/set-shared-value saved-height target-height) 300)
                                  (js/setTimeout #(reanimated/set-shared-value bg-bottom (- window-height)) 300)
                                  (reanimated/animate opacity 0)))))))))))

(defn handle
  []
  [rn/view {:style (style/handle-container)}
   [rn/view {:style (style/handle)}]])

(defn actions
  [input-ref text-value]
  [rn/view {:style (style/actions-container)}
   [rn/view
    [image-button]]
   (when-not (empty? @text-value)
     [quo/button
      {:icon                true
       :size                32
       :accessibility-label :send-message-button
       :on-press            (fn []
                              ;(on-send)
                              (reset! text-value "")
                              (.clear ^js @input-ref)
                              (messages.list/scroll-to-bottom)
                              (rf/dispatch [:chat.ui/send-current-message]))}
      :i/arrow-up])])

;;; MAIN
(defn sheet
  [insets blur-opacity layout-height]
  [:f> (fn []
         (let [line-height            (:line-height typography/paragraph-1)
               height                 (reanimated/use-shared-value input-height)
               saved-height           (reanimated/use-shared-value input-height)
               last-height            (reanimated/use-shared-value input-height)
               opacity                (reanimated/use-shared-value 0)
               overlay-opacity        (reanimated/use-shared-value 0)
               overlay-z-index        (reagent/atom 0)
               window-height          (rf/sub [:dimensions/window-height])
               bg-bottom              (reanimated/use-shared-value (- window-height))
               input-ref              (atom nil)
               content-height         (reagent/atom input-height)
               focused?               (reagent/atom false)
               cursor-position        (reagent/atom {:start 0 :end 0})
               saved-cursor-position  (reagent/atom {:start 0 :end 0})
               text-value             (reagent/atom "")
               lock-selection         (reagent/atom true)
               lock-layout?           (reagent/atom false)
               keyboard-show-listener (reagent/atom nil)
               add-keyboard-height    (atom nil)
               saved-keyboard-height  (atom nil)
               margin-top             (if platform/ios? (:top insets) (+ (:top insets) 10))
               emojis-open            (reagent/atom false)]
           (rn/use-effect
             (fn [] (reset! keyboard-show-listener (.addListener rn/keyboard "keyboardWillShow"
                                                                 (fn [e]
                                                                   (let [start-h   (oops/oget e "startCoordinates.height")
                                                                         end-h     (oops/oget e "endCoordinates.height")
                                                                         diff      (- end-h start-h)
                                                                         max       (- window-height end-h margin-top style/handle-container-height style/actions-container-height)
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
                                                                         (reset! add-keyboard-height nil)))
                                                                     ))))
               (fn [] (.remove ^js @keyboard-show-listener))))
           [:f>
            (fn []
              (let [{:keys [keyboard-shown keyboard-height]} (hooks/use-keyboard)
                    max-height (- window-height margin-top keyboard-height style/handle-container-height style/actions-container-height)
                    lines      (Math/round (/ @content-height line-height))
                    lines      (if platform/ios? lines (dec lines))
                    max-lines  (Math/round (/ max-height line-height))
                    max-lines  (if platform/ios? max-lines (dec max-lines))
                    expanded?  (= (reanimated/get-shared-value height) max-height)]
                [:<>
                 [reanimated/view {:style (style/background opacity bg-bottom window-height height)}]
                 [gesture/gesture-detector {:gesture (drag-gesture height saved-height opacity bg-bottom window-height keyboard-shown max-height input-ref lines add-keyboard-height saved-keyboard-height emojis-open)}
                  [rn/view {:style     (style/container insets @focused? (not-empty @text-value))
                            :on-layout (fn [e]
                                         (when-not @lock-layout?
                                           (reanimated/set-shared-value layout-height (oops/oget e "nativeEvent.layout.height"))))}
                   [handle]
                   [reanimated/view {:style (style/input-container height max-height @emojis-open)}
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
                                                (reset! lock-layout? true)
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
                                                (reanimated/set-shared-value blur-opacity 0))
                      :on-blur                (fn []
                                                (let [target-height (if (> lines 1) (+ input-height 18) input-height)]
                                                  (reset! saved-cursor-position @cursor-position)
                                                  (reanimated/set-shared-value last-height (if (empty? @text-value) target-height (Math/min (+ @content-height (if platform/ios? 5 0))
                                                                                                                                            (reanimated/get-shared-value saved-height))))
                                                  (reanimated/set-shared-value blur-opacity 1)
                                                  (reanimated/animate height target-height)
                                                  (reanimated/set-shared-value saved-height target-height)
                                                  (reset! focused? false)
                                                  (reset! lock-selection true)
                                                  (js/setTimeout #(reset! lock-layout? false) 500)
                                                  (reanimated/animate overlay-opacity 0)
                                                  (reset! overlay-z-index 0)))
                      :style                  (style/input @focused? expanded? @saved-keyboard-height)
                      :on-scroll              (fn [e] (let [y (oops/oget e "nativeEvent.contentOffset.y")]
                                                        (when (and (> y line-height) (>= lines max-lines) (= @overlay-z-index 0) @focused?)
                                                          (reset! overlay-z-index 1)
                                                          (js/setTimeout #(reanimated/animate overlay-opacity 1) 0))
                                                        (when (and (<= y line-height) (= @overlay-z-index 1))
                                                          (reanimated/animate overlay-opacity 0)
                                                          (js/setTimeout #(reset! overlay-z-index 0) 300))))
                      :on-content-size-change (fn [e]
                                                ;(when @focused?
                                                (let [extra-offset (if platform/ios? (if @emojis-open ios-extra-offset 5) 0)
                                                      x            (+ (oops/oget e "nativeEvent.contentSize.height") extra-offset)
                                                      diff         (Math/abs (- x (reanimated/get-shared-value height)))]
                                                  (reset! content-height (oops/oget e "nativeEvent.contentSize.height"))
                                                  (when (and (> diff 10) (not-empty @text-value) (<= x (+ max-height line-height)) (not= (reanimated/get-shared-value height) max-height))
                                                    (reanimated/animate height (Math/min x max-height))
                                                    (reanimated/set-shared-value saved-height (Math/min x max-height)))
                                                  (if (> (reanimated/get-shared-value saved-height) (* 0.75 max-height))
                                                    (do
                                                      (reanimated/set-shared-value bg-bottom 0)
                                                      (reanimated/animate opacity 1))
                                                    (do
                                                      (reanimated/animate opacity 0)
                                                      (reanimated/animate-delay bg-bottom (- window-height) 300))))
                                                ;)
                                                )
                      :max-height             max-height
                      :multiline              true
                      :placeholder-text-color (colors/theme-colors colors/neutral-40 colors/neutral-60)
                      :placeholder            (i18n/label :t/type-something)}]
                    (when (and (not-empty @text-value) (not @focused?) (> lines 1))
                      [rn/touchable-without-feedback
                       {:on-press #(.focus ^js @input-ref)}
                       [linear-gradient/linear-gradient
                        {:colors ["rgba(255,255,255,1)" "rgba(255,255,255,0)"]
                         :start  {:x 0 :y 1}
                         :end    {:x 0 :y 0}
                         :style  (style/text-overlay)}]])]
                   [actions input-ref text-value]]]]))]))])


(defn blur-view [insets blur-opacity layout-height]
  [:f>
   (fn []
     [reanimated/view {:style (style/blur-container blur-opacity layout-height)}
      [blur/view {:style       {:width  "100%"
                                :height "100%"}
                  :blur-radius 20
                  :blur-type   :light
                  :blur-amount 20}]])])

(defn composer
  []
  [:f>
   (fn []
     (let [insets        (safe-area/use-safe-area)
           blur-opacity  (reanimated/use-shared-value 1)
           layout-height (reanimated/use-shared-value (+ (if platform/ios? 108 120) (:bottom insets)))]
       [rn/view
        [blur-view insets blur-opacity layout-height]
        [sheet insets blur-opacity layout-height]]))])

