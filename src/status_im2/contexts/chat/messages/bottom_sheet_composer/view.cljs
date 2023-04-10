(ns status-im2.contexts.chat.messages.bottom-sheet-composer.view
  (:require
    [quo2.foundations.colors :as colors]
    [react-native.core :as rn]
    [react-native.gesture :as gesture]
    [react-native.hooks :as hooks]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [oops.core :as oops]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [utils.i18n :as i18n]
    [status-im2.contexts.chat.messages.bottom-sheet-composer.style :as style]
    [status-im2.contexts.chat.messages.bottom-sheet-composer.images.view :as images]
    [react-native.async-storage :as async-storage]
    [utils.re-frame :as rf]
    [status-im2.contexts.chat.messages.bottom-sheet-composer.utils :as utils]
    [status-im2.contexts.chat.messages.bottom-sheet-composer.constants :as c]
    [status-im2.contexts.chat.messages.bottom-sheet-composer.actions.view :as actions]
    [status-im2.contexts.chat.messages.bottom-sheet-composer.keyboard :as kb]
    [status-im2.contexts.chat.messages.bottom-sheet-composer.sub-view :as sub-view]))

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
   {:keys [height saved-height last-height opacity background-y container-opacity] :as animations}
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
                                (reanimated/set-shared-value container-opacity 1)
                                (reanimated/set-shared-value last-height max-height))
                              (.focus ^js @input-ref)
                              (reset! gesture-enabled? false))
                            (do ; else, will start gesture
                              (reanimated/set-shared-value background-y 0)
                              (reset! expanding? (neg? (oops/oget e "velocityY")))))))
      (gesture/on-update (fn [e]
                           (let [translation (oops/oget e "translationY")
                                 min-height  (utils/get-min-height lines)
                                 new-height  (+ (- translation)
                                                (reanimated/get-shared-value saved-height))
                                 new-height  (utils/bounded-val new-height min-height max-height)]
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


(defn handle-blur
  [{:keys [text-value focused? lock-selection? cursor-position saved-cursor-position gradient-z-index
           maximized?]}
   {:keys [height saved-height last-height gradient-opacity container-opacity opacity background-y]}
   {:keys [lines content-height max-height window-height]}
   images]
  (let [min-height    (utils/get-min-height lines)
        reopen-height (utils/calc-reopen-height text-value min-height content-height saved-height)]
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

(defn handle-content-size-change
  [e
   {:keys [maximized?]}
   {:keys [height saved-height opacity background-y]}
   {:keys [content-height window-height max-height]}
   keyboard-shown]
  (when keyboard-shown
    (let [content-size (+ (oops/oget e "nativeEvent.contentSize.height") c/extra-content-offset)
          new-height   (utils/bounded-val content-size c/input-height max-height)]
      (reset! content-height content-size)
      (when (utils/should-update-height content-size height max-height)
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

(defn handle-scroll
  [e
   {:keys [gradient-z-index focused?]}
   {:keys [gradient-opacity]}
   {:keys [lines max-lines]}]
  (let [y (oops/oget e "nativeEvent.contentOffset.y")]
    (when (utils/should-show-top-gradient y lines max-lines gradient-opacity focused?)
      (reset! gradient-z-index 1)
      (js/setTimeout #(reanimated/animate gradient-opacity 1) 0))
    (when (utils/should-hide-top-gradient y gradient-opacity)
      (reanimated/animate gradient-opacity 0)
      (js/setTimeout #(reset! gradient-z-index 0) 300))))

(defn handle-change-text
  [text
   {:keys [input-ref]}
   {:keys [text-value cursor-position]}]
  (reset! text-value text)
  (reagent/next-tick #(.setNativeProps ^js @input-ref
                                       (clj->js {:selection {:start @cursor-position
                                                             :end   @cursor-position}})))
  (rf/dispatch [:chat.ui/set-chat-input-text text]))

(defn handle-selection-change
  [e {:keys [lock-selection? cursor-position]}]
  (when-not @lock-selection?
    (reset! cursor-position (oops/oget e "nativeEvent.selection.end"))))

(defn handle-layout
  [e
   {:keys [lock-layout?]}
   blur-height]
  (when (utils/should-update-blur-height e lock-layout? blur-height)
    (reanimated/set-shared-value blur-height (oops/oget e "nativeEvent.layout.height"))))

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

(defn component-will-unmount
  [{:keys [keyboard-show-listener keyboard-hide-listener keyboard-frame-listener]}]
  (.remove ^js @keyboard-show-listener)
  (.remove ^js @keyboard-hide-listener)
  (.remove ^js @keyboard-frame-listener))

(defn use-effect
  [{:keys [input-ref] :as props}
   {:keys [lock-layout? kb-default-height maximized? focused? text-value] :as state}
   {:keys [height saved-height last-height container-opacity opacity background-y] :as animations}
   {:keys [max-height] :as dimensions}
   {:keys [input-content-height input-refocus?] :as chat-input}
   keyboard-height images?]
  (rn/use-effect
   (fn []
     (when (or @maximized? (>= input-content-height max-height))
       (reanimated/animate height max-height)
       (reanimated/set-shared-value saved-height max-height)
       (reanimated/set-shared-value last-height max-height))
     (when input-refocus?
       (.focus ^js @input-ref)
       (rf/dispatch [:chat.ui/set-input-refocus false]))
     (handle-reenter-screen state dimensions chat-input)
     (when-not @lock-layout?
       (js/setTimeout #(reset! lock-layout? true) 500))
     (when-not @kb-default-height
       (async-storage/get-item :kb-default-height
                               #(reset! kb-default-height (when (not= nil %) (js/parseInt %)))))
     (when (or @maximized? (>= input-content-height (* max-height c/background-threshold)))
       (reanimated/set-shared-value background-y 0)
       (reanimated/animate opacity 1))
     (when images?
       (reanimated/animate container-opacity 1))
     (when (and (empty? @text-value) (not images?) (not @maximized?) (not @focused?))
       (reanimated/animate-delay container-opacity 0.7 200))
     (kb/add-kb-listeners props state animations dimensions keyboard-height)
     #(component-will-unmount props))
   [max-height]))

;;; MAIN
(defn sheet
  [insets window-height blur-height opacity background-y]
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
                kb-height                                (kb/get-kb-height keyboard-height
                                                                           @(:kb-default-height state))
                max-height                               (utils/calc-max-height window-height
                                                                                margin-top
                                                                                kb-height
                                                                                images)
                lines                                    (utils/calc-lines @content-height)
                max-lines                                (utils/calc-lines max-height)
                initial-height                           (if (> lines 1)
                                                           c/multiline-minimized-height
                                                           c/input-height)
                animations                               {:gradient-opacity  (reanimated/use-shared-value
                                                                              0)
                                                          :container-opacity (reanimated/use-shared-value
                                                                              (if (utils/empty-input?
                                                                                   input-text
                                                                                   images)
                                                                                0.7
                                                                                1))
                                                          :height            (reanimated/use-shared-value
                                                                              initial-height)
                                                          :saved-height      (reanimated/use-shared-value
                                                                              initial-height)
                                                          :last-height       (reanimated/use-shared-value
                                                                              (utils/bounded-val
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
               :on-layout #(handle-layout % state blur-height)}
              [sub-view/bar]
              [reanimated/touchable-opacity
               {:active-opacity 1
                :on-press       #(.focus ^js @(:input-ref props))
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
               [sub-view/gradients props state animations dimensions]]
              [images/images-list]
              [actions/view (:input-ref props) state (seq images) animations window-height
               insets]]]))]))])

(defn bottom-sheet-composer
  []
  [:f>
   (fn []
     (let [window-height (rf/sub [:dimensions/window-height])
           insets        (safe-area/use-safe-area)
           opacity       (reanimated/use-shared-value 0)
           background-y  (reanimated/use-shared-value (- window-height))
           blur-height (reanimated/use-shared-value (+ c/composer-default-height (:bottom insets)))]
       [rn/view
        [reanimated/view {:style (style/background opacity background-y window-height)}]
        [sub-view/blur-view blur-height]
        [sheet insets window-height blur-height opacity background-y]]))])
