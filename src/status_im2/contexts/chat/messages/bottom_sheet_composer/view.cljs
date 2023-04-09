(ns status-im2.contexts.chat.messages.bottom-sheet-composer.view
  (:require
    [quo2.core :as quo]
    [quo2.foundations.colors :as colors]
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
    [status-im2.common.alert.events :as alert]
    [status-im2.contexts.chat.messages.list.view :as messages.list]
    [utils.i18n :as i18n]
    [status-im2.contexts.chat.messages.bottom-sheet-composer.style :as style]
    [status-im2.contexts.chat.messages.bottom-sheet-composer.images.view :as images]
    [react-native.async-storage :as async-storage]
    [utils.re-frame :as rf]
    [status-im2.contexts.chat.messages.bottom-sheet-composer.constants :as c]))

;;; CONTROLS
(defn send-button
  [input-ref text-value images? window-height
   {:keys [height saved-height last-height opacity background-y container-opacity]}]
  [:f>
   (fn []
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
                               (js/setTimeout #(reset! z-index 0) 300))))
                         [@text-value])
          [reanimated/view
           {:style (reanimated/apply-animations-to-style
                    {:opacity btn-opacity}
                    {:position         :absolute
                     :right            0
                     :z-index          @z-index
                     :background-color (colors/theme-colors colors/white colors/neutral-90)})}
           [quo/button
            {:icon                true
             :size                32
             :accessibility-label :send-message-button
             :on-press            (fn []
                                    (reanimated/animate height c/input-height)
                                    (reanimated/set-shared-value saved-height c/input-height)
                                    (reanimated/set-shared-value last-height c/input-height)
                                    (reanimated/animate opacity 0)
                                    (js/setTimeout #(reanimated/animate container-opacity 0.7) 300)
                                    (js/setTimeout #(reanimated/set-shared-value background-y
                                                                                 (- window-height))
                                                   300)
                                    (rf/dispatch [:chat.ui/send-current-message])
                                    (rf/dispatch [:chat.ui/set-input-maximized false])
                                    (reset! text-value "")
                                    (.clear ^js @input-ref)
                                    (messages.list/scroll-to-bottom))}
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
  [insets height]
  [quo/button
   {:on-press (fn []
                (permissions/request-permissions
                 {:permissions [:read-external-storage :write-external-storage]
                  :on-allowed  (fn []
                                 (rf/dispatch [:chat.ui/set-input-content-height
                                               (reanimated/get-shared-value height)])
                                 (rf/dispatch [:open-modal :photo-selector {:insets insets}]))
                  :on-denied   (fn []
                                 (background-timer/set-timeout
                                  #(alert/show-popup (i18n/label :t/error)
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

(defn actions
  [input-ref text-value images? {:keys [height saved-height opacity background-y] :as animations}
   window-height insets]
  [rn/view {:style (style/actions-container)}
   [rn/view {:style {:flex-direction :row}}
    [camera-button]
    [image-button insets height]
    [reaction-button]
    [format-button]]
   [send-button input-ref text-value images? window-height animations]
   [audio-button]])

(defn bar
  []
  [rn/view {:style (style/bar-container)}
   [rn/view {:style (style/bar)}]])

(defn get-min-height
  [lines]
  (if (> lines 1) c/multiline-minimized-height c/input-height))

(defn bounded-val
  [f min max]
  (Math/max min (Math/min f max)))

(defn set-opacity
  [e opacity translation expanding? min-height max-height new-height saved-height]
  (let [remaining-height     (if @expanding?
                               (- max-height (reanimated/get-shared-value saved-height))
                               (- (reanimated/get-shared-value saved-height) min-height))
        progress             (if (= new-height min-height) 1 (/ translation remaining-height))
        currently-expanding? (neg? (oops/oget e "velocityY"))
        max-opacity?         (and currently-expanding? (= (reanimated/get-shared-value opacity) 1))
        min-opacity?         (and (not currently-expanding?)
                                  (= (reanimated/get-shared-value opacity) 0))]
    (if (>= translation 0)
      (when (and (not @expanding?) (not min-opacity?))
        (reanimated/set-shared-value opacity (- 1 progress)))
      (when (and @expanding? (not max-opacity?))
        (reanimated/set-shared-value opacity (Math/abs progress))))))

(defn maximize
  [{:keys [maximized?]}
   {:keys [height saved-height background-y opacity]}
   {:keys [max-height]}]
  (reanimated/animate height max-height)
  (reanimated/set-shared-value saved-height max-height)
  (reanimated/set-shared-value background-y 0)
  (reanimated/animate opacity 1)
  (reset! maximized? true)
  (rf/dispatch [:chat.ui/set-input-maximized true]))

(defn minimize
  [{:keys [input-ref emoji-kb-extra-height saved-emoji-kb-extra-height]}]
  (when @emoji-kb-extra-height
    (reset! saved-emoji-kb-extra-height @emoji-kb-extra-height)
    (reset! emoji-kb-extra-height nil))
  (rf/dispatch [:chat.ui/set-input-maximized false])
  (.blur ^js @input-ref))

(defn bounce-back
  [{:keys [height saved-height opacity background-y]}
   {:keys [window-height]}
   starting-opacity]
  (reanimated/animate height (reanimated/get-shared-value saved-height))
  (when (= starting-opacity 0)
    (reanimated/animate opacity 0)
    (reanimated/animate-delay background-y (- window-height) 300)))

(defn drag-gesture
  [{:keys [input-ref] :as props}
   {:keys [gesture-enabled?] :as state}
   {:keys [height saved-height last-height opacity background-y] :as animations}
   {:keys [max-height lines] :as dimensions}
   keyboard-shown]
  (let [expanding?       (atom true)
        starting-opacity (reanimated/get-shared-value opacity)]
    (->
      (gesture/gesture-pan)
      (gesture/enabled @gesture-enabled?)
      (gesture/on-start (fn [e]
                          (if-not keyboard-shown
                            (do ; focus and end
                              (when (< (oops/oget e "velocityY") c/velocity-threshold)
                                (reanimated/set-shared-value last-height max-height))
                              (.focus ^js @input-ref)
                              (reset! gesture-enabled? false))
                            (do ; else, will start gesture
                              (reanimated/set-shared-value background-y 0)
                              (reset! expanding? (neg? (oops/oget e "velocityY")))))))
      (gesture/on-update (fn [e]
                           (let [translation (oops/oget e "translationY")
                                 min-height  (get-min-height lines)
                                 new-height  (+ (- translation)
                                                (reanimated/get-shared-value saved-height))
                                 new-height  (bounded-val new-height min-height max-height)]
                             (when keyboard-shown
                               (reanimated/set-shared-value height new-height)
                               (set-opacity e
                                            opacity
                                            translation
                                            expanding?
                                            min-height
                                            max-height
                                            new-height
                                            saved-height)))))
      (gesture/on-end (fn [e]
                        (let [diff (- (reanimated/get-shared-value height)
                                      (reanimated/get-shared-value saved-height))]
                          (if @gesture-enabled?
                            (if (>= diff 0)
                              (if (> diff c/drag-threshold)
                                (maximize state animations dimensions)
                                (bounce-back animations dimensions starting-opacity))
                              (if (> (Math/abs diff) c/drag-threshold)
                                (minimize props)
                                (bounce-back animations dimensions starting-opacity)))
                            (reset! gesture-enabled? true))))))))

(defn handle-refocus-emoji-kb-ios
  [{:keys [saved-emoji-kb-extra-height]}
   {:keys [height saved-height last-height]}
   {:keys [lines max-lines]}]
  (when @saved-emoji-kb-extra-height
    (js/setTimeout (fn []
                     (when (> lines max-lines)
                       (reanimated/animate height
                                           (+ (reanimated/get-shared-value last-height)
                                              @saved-emoji-kb-extra-height))
                       (reanimated/set-shared-value saved-height
                                                    (+ (reanimated/get-shared-value last-height)
                                                       @saved-emoji-kb-extra-height)))
                     (reset! saved-emoji-kb-extra-height nil))
                   600)))
(defn handle-focus
  [{:keys [input-ref] :as props}
   {:keys [text-value focused? lock-selection? saved-cursor-position gradient-z-index]}
   {:keys [height saved-height last-height opacity background-y gradient-opacity container-opacity]
    :as   animations}
   {:keys [max-height] :as dimensions}]
  (reset! focused? true)
  (reanimated/animate height (reanimated/get-shared-value last-height))
  (reanimated/set-shared-value saved-height (reanimated/get-shared-value last-height))
  (reanimated/animate container-opacity 1)
  (when (> (reanimated/get-shared-value last-height) (* c/background-threshold max-height))
    (reanimated/animate opacity 1)
    (reanimated/set-shared-value background-y 0))
  (when (= @gradient-z-index -1)
    (reanimated/animate gradient-opacity 1)
    (reset! gradient-z-index 1))
  (js/setTimeout #(reset! lock-selection? false) 300)
  (when (not-empty @text-value)
    (.setNativeProps ^js @input-ref
                     (clj->js {:selection {:start @saved-cursor-position :end @saved-cursor-position}})))
  (handle-refocus-emoji-kb-ios props animations dimensions))

(defn calc-reopen-height
  [text-value min-height content-height saved-height]
  (if (empty? @text-value)
    min-height
    (Math/min @content-height (reanimated/get-shared-value saved-height))))

(defn handle-blur
  [{:keys [text-value focused? lock-selection? cursor-position saved-cursor-position gradient-z-index
           maximized?]}
   {:keys [height saved-height last-height gradient-opacity container-opacity opacity background-y]}
   {:keys [lines content-height max-height window-height]}
   images]
  (let [min-height    (get-min-height lines)
        reopen-height (calc-reopen-height text-value min-height content-height saved-height)]
    (reset! focused? false)
    (reanimated/set-shared-value last-height reopen-height)
    (reanimated/animate height min-height)
    (reanimated/set-shared-value saved-height min-height)
    (reanimated/animate opacity 0)
    (js/setTimeout #(reanimated/set-shared-value background-y (- window-height)) 300)
    (when (and (empty? @text-value) (not (seq images)))
      (reanimated/animate container-opacity 0.7))
    (reanimated/animate gradient-opacity 0)
    (reset! lock-selection? true)
    (reset! saved-cursor-position @cursor-position)
    (reset! gradient-z-index (if (= (reanimated/get-shared-value gradient-opacity) 1) -1 0))
    (when (not= reopen-height max-height)
      (reset! maximized? false))))

(defn should-update-height
  [content-size height max-height]
  (let [diff (Math/abs (- content-size (reanimated/get-shared-value height)))]
    (and (not= (reanimated/get-shared-value height) max-height)
         (> diff c/content-change-threshold))))

(defn handle-content-size-change
  [e
   {:keys [maximized?]}
   {:keys [height saved-height opacity background-y]}
   {:keys [content-height window-height max-height]}
   keyboard-shown]
  (when keyboard-shown
    (let [content-size (+ (oops/oget e "nativeEvent.contentSize.height") c/extra-content-offset)
          new-height   (bounded-val content-size c/input-height max-height)]
      (reset! content-height content-size)
      (when (should-update-height content-size height max-height)
        (reanimated/animate height new-height)
        (reanimated/set-shared-value saved-height new-height))
      (when (= new-height max-height)
        (reset! maximized? true))
      (if (or (> new-height (* c/background-threshold max-height))
              (= (reanimated/get-shared-value saved-height) max-height))
        (do
          (reanimated/set-shared-value background-y 0)
          (reanimated/animate opacity 1))
        (when (= (reanimated/get-shared-value opacity) 1)
          (reanimated/animate opacity 0)
          (js/setTimeout #(reanimated/set-shared-value background-y (- window-height)) 300)))
      (rf/dispatch [:chat.ui/set-input-content-height new-height]))))

(defn should-show-top-gradient
  [y lines max-lines gradient-opacity focused?]
  (and
   (> y c/line-height)
   (>= lines max-lines)
   (= (reanimated/get-shared-value gradient-opacity) 0)
   @focused?))

(defn should-hide-top-gradient
  [y gradient-opacity]
  (and
   (<= y c/line-height)
   (= (reanimated/get-shared-value gradient-opacity) 1)))

(defn handle-scroll
  [e
   {:keys [gradient-z-index focused?]}
   {:keys [gradient-opacity]}
   {:keys [lines max-lines]}]
  (let [y (oops/oget e "nativeEvent.contentOffset.y")]
    (when (should-show-top-gradient y lines max-lines gradient-opacity focused?)
      (reset! gradient-z-index 1)
      (js/setTimeout #(reanimated/animate gradient-opacity 1) 0))
    (when (should-hide-top-gradient y gradient-opacity)
      (reanimated/animate gradient-opacity 0)
      (js/setTimeout #(reset! gradient-z-index 0) 300))))

(defn handle-change-text
  [text
   {:keys [input-ref]}
   {:keys [text-value cursor-position]}]
  (reset! text-value text)
  (js/setTimeout #(.setNativeProps ^js @input-ref
                                   (clj->js {:selection {:start @cursor-position
                                                         :end   @cursor-position}}))
                 20)
  (rf/dispatch [:chat.ui/set-chat-input-text text]))

(defn handle-selection-change
  [e {:keys [lock-selection? cursor-position]}]
  (when-not @lock-selection?
    (reset! cursor-position (oops/oget e "nativeEvent.selection.end"))))

(defn handle-emoji-kb-ios
  [e
   {:keys [emoji-kb-extra-height]}
   {:keys [text-value]}
   {:keys [height saved-height]}
   {:keys [max-height]}]
  (let [start-h   (oops/oget e "startCoordinates.height")
        end-h     (oops/oget e "endCoordinates.height")
        diff      (- end-h start-h)
        max       (- max-height diff)
        curr-text @text-value]
    (if (> (reanimated/get-shared-value height) max)
      (do
        (reanimated/set-shared-value height (- (reanimated/get-shared-value height) diff))
        (reanimated/set-shared-value saved-height (- (reanimated/get-shared-value saved-height) diff))
        (reset! text-value (str @text-value " "))
        (js/setTimeout #(reset! text-value curr-text) 0)
        (reset! emoji-kb-extra-height diff))
      (when @emoji-kb-extra-height
        (reanimated/set-shared-value height
                                     (+ (reanimated/get-shared-value height) @emoji-kb-extra-height))
        (reanimated/set-shared-value saved-height
                                     (+ (reanimated/get-shared-value saved-height)
                                        @emoji-kb-extra-height))
        (reset! emoji-kb-extra-height nil)))))

(defn handle-kb-hide-android
  [{:keys [input-ref]}
   {:keys [opacity height saved-height background-y]}
   {:keys [window-height lines]}]
  (when platform/android?
    (let [min-height (get-min-height lines)]
      (.blur ^js @input-ref)
      (reanimated/animate opacity 0)
      (js/setTimeout (fn []
                       (reanimated/animate height min-height)
                       (reanimated/set-shared-value saved-height min-height)
                       (reanimated/set-shared-value background-y (- window-height)))
                     100))))

(defn handle-layout
  [e
   {:keys [lock-layout?]}
   layout-height]
  (when-not @lock-layout?
    (reanimated/set-shared-value layout-height (oops/oget e "nativeEvent.layout.height"))))

(defn handle-reenter-screen
  [{:keys [text-value saved-cursor-position maximized?]}
   {:keys [content-height]}
   {:keys [input-content-height input-text input-maximized?]}]
  (when (and (empty? @text-value) (not= input-text nil))
    (reset! text-value input-text)
    (reset! content-height input-content-height)
    (reset! saved-cursor-position (count input-text)))
  (when input-maximized?
    (reset! maximized? true)))

(defn store-kb-height
  [{:keys [kb-default-height]} keyboard-height]
  (when (and (not @kb-default-height) (pos? keyboard-height))
    (async-storage/set-item :kb-default-height keyboard-height)))
(defn add-kb-listeners
  [{:keys [keyboard-show-listener keyboard-frame-listener keyboard-hide-listener] :as props}
   state animations dimensions keyboard-height]
  (reset! keyboard-show-listener (.addListener rn/keyboard
                                               "keyboardDidShow"
                                               #(store-kb-height state keyboard-height)))
  (reset! keyboard-frame-listener (.addListener
                                   rn/keyboard
                                   "keyboardWillChangeFrame"
                                   #(handle-emoji-kb-ios % props state animations dimensions)))
  (reset! keyboard-hide-listener (.addListener rn/keyboard
                                               "keyboardDidHide"
                                               #(handle-kb-hide-android props animations dimensions))))

(defn component-will-unmount
  [{:keys [keyboard-show-listener keyboard-hide-listener keyboard-frame-listener]}]
  (.remove ^js @keyboard-show-listener)
  (.remove ^js @keyboard-hide-listener)
  (.remove ^js @keyboard-frame-listener))

(defn gradient-components
  [{:keys [input-ref]}
   {:keys [gradient-z-index text-value focused?]}
   {:keys [gradient-opacity]}
   {:keys [lines]}]
  [:f>
   (fn []
     [:<>
      [reanimated/linear-gradient (style/top-gradient gradient-opacity @gradient-z-index)]
      (when (and (not-empty @text-value) (not @focused?) (> lines 2))
        [rn/touchable-without-feedback
         {:on-press #(.focus ^js @input-ref)}
         [linear-gradient/linear-gradient (style/bottom-gradient)]])])])

(defn use-effect
  [props
   {:keys [lock-layout? kb-default-height maximized? text-value] :as state}
   {:keys [height saved-height container-opacity opacity background-y] :as animations}
   {:keys [max-height] :as dimensions}
   {:keys [input-content-height input-text] :as chat-input}
   keyboard-height images?]
  (rn/use-effect
   (fn []
     (when (or @maximized? (>= input-content-height max-height))
       (reanimated/animate height max-height)
       (reanimated/set-shared-value saved-height max-height))
     (handle-reenter-screen state dimensions chat-input)
     (when (nil? input-text)
       (js/setTimeout #(reset! lock-layout? true) 500))
     (when-not @kb-default-height
       (async-storage/get-item :kb-default-height
                               #(reset! kb-default-height (when (not= nil %) (js/parseInt %)))))
     (when (or @maximized? (>= input-content-height (* max-height c/background-threshold)))
       (reanimated/set-shared-value background-y 0)
       (reanimated/animate opacity 1))
     (when images?
       (reanimated/animate container-opacity 1))
     (when (and (empty? @text-value) (not images?) (not @maximized?))
       (reanimated/animate-delay container-opacity 0.7 200))
     (add-kb-listeners props state animations dimensions keyboard-height)
     #(component-will-unmount props))
   [max-height]))

(defn get-kb-height
  [curr-height default-height]
  (if (and default-height (< curr-height default-height))
    default-height
    curr-height))

(defn calc-lines
  [height]
  (let [lines (Math/round (/ height c/line-height))]
    (if platform/ios? lines (dec lines))))

(defn calc-max-height
  [window-height margin-top kb-height images]
  (let [max-height
        (- window-height margin-top kb-height c/handle-container-height c/actions-container-height)]
    (if (seq images)
      (- max-height c/images-container-height)
      max-height)))

;;; MAIN
(defn sheet
  ;; safe-area consumer insets makes incorrect values on Android
  [insets window-height layout-height opacity background-y]
  [:f>
   (fn []
     (let [props      {:input-ref                   (atom nil)
                       :keyboard-show-listener      (atom nil)
                       :keyboard-frame-listener     (atom nil)
                       :keyboard-hide-listener      (atom nil)
                       :emoji-kb-extra-height       (atom nil)
                       :saved-emoji-kb-extra-height (atom nil)}
           state      {:text-value            (reagent/atom "")
                       :cursor-position       (reagent/atom 0)
                       :saved-cursor-position (reagent/atom 0)
                       :gradient-z-index      (reagent/atom 0)
                       :kb-default-height     (reagent/atom nil)
                       :gesture-enabled?      (reagent/atom true)
                       :lock-selection?       (reagent/atom true)
                       :focused?              (reagent/atom false)
                       :lock-layout?          (reagent/atom false)
                       :maximized?            (reagent/atom false)}
           margin-top (if platform/ios? (:top insets) (+ (:top insets) 10))]
       [:f>
        (fn []
          (let [images                                   (rf/sub [:chats/sending-image])
                {:keys [input-text input-content-height]
                 :as   chat-input}                       (rf/sub [:chats/current-chat-input])
                content-height                           (reagent/atom (or input-content-height
                                                                           c/input-height))
                {:keys [keyboard-shown keyboard-height]} (hooks/use-keyboard)
                kb-height                                (get-kb-height keyboard-height
                                                                        @(:kb-default-height state))
                max-height                               (calc-max-height window-height
                                                                          margin-top
                                                                          kb-height
                                                                          images)
                lines                                    (calc-lines @content-height)
                max-lines                                (calc-lines max-height)
                initial-height                           (if (> lines 1)
                                                           c/multiline-minimized-height
                                                           c/input-height)
                animations                               {:gradient-opacity  (reanimated/use-shared-value
                                                                              0)
                                                          :container-opacity (reanimated/use-shared-value
                                                                              (if (and (nil? input-text)
                                                                                       (not (seq
                                                                                             images)))
                                                                                0.7
                                                                                1))
                                                          :height            (reanimated/use-shared-value
                                                                              initial-height)
                                                          :saved-height      (reanimated/use-shared-value
                                                                              initial-height)
                                                          :last-height       (reanimated/use-shared-value
                                                                              (bounded-val
                                                                               @content-height
                                                                               c/input-height
                                                                               max-height))
                                                          :opacity           opacity
                                                          :background-y      background-y}
                dimensions                               {:content-height content-height
                                                          :max-height     max-height
                                                          :window-height  window-height
                                                          :lines          lines
                                                          :max-lines      max-lines}]
            (use-effect props state animations dimensions chat-input keyboard-height (seq images))
            [gesture/gesture-detector
             {:gesture (drag-gesture props state animations dimensions keyboard-shown)}
             [reanimated/view
              {:style     (style/sheet-container insets (:container-opacity animations))
               :on-layout #(handle-layout % state layout-height)}
              [bar]
              [reanimated/touchable-opacity
               {:active-opacity 1
                :on-press       #(.focus ^js @(:input-ref props)) ;; for android when first entering
                                                                  ;; screen
                :style          (style/input-container (:height animations) max-height)}
               [rn/text-input
                {:ref                      #(reset! (:input-ref props) %)
                 :default-value            @(:text-value state)
                 :on-focus                 #(handle-focus props state animations dimensions)
                 :on-blur                  #(handle-blur state animations dimensions images)
                 :on-content-size-change   #(handle-content-size-change %
                                                                        state
                                                                        animations
                                                                        dimensions
                                                                        keyboard-shown)
                 :on-scroll                #(handle-scroll % state animations dimensions)
                 :on-change-text           #(handle-change-text % props state)
                 :on-selection-change      #(handle-selection-change % state)
                 :max-height               max-height
                 :max-font-size-multiplier 1
                 :multiline                true
                 :placeholder              (i18n/label :t/type-something)
                 :placeholder-text-color   (colors/theme-colors colors/neutral-40 colors/neutral-50)
                 :style                    (style/input @(:maximized? state)
                                                        @(:saved-emoji-kb-extra-height props))}]
               [gradient-components props state animations dimensions]]
              [images/images-list]
              [actions (:input-ref props) (:text-value state) (seq images) animations window-height
               insets]]]))]))])


(defn blur-view
  [layout-height]
  [:f>
   (fn []
     [reanimated/view {:style (style/blur-container layout-height)}
      [blur/view style/blur-view]])])

(defn bottom-sheet-composer
  []
  [:f>
   (fn []
     (let [window-height (rf/sub [:dimensions/window-height])
           insets        (safe-area/use-safe-area)
           opacity       (reanimated/use-shared-value 0)
           background-y  (reanimated/use-shared-value (- window-height))
           layout-height (reanimated/use-shared-value (+ c/composer-default-height (:bottom insets)))]
       [rn/view
        [reanimated/view {:style (style/background opacity background-y window-height)}]
        [blur-view layout-height]
        [sheet insets window-height layout-height opacity background-y]]))])
