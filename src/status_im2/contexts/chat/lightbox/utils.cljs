(ns status-im2.contexts.chat.lightbox.utils
  (:require
    [clojure.string :as string]
    [oops.core :as oops]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [react-native.gesture :as gesture]
    [react-native.orientation :as orientation]
    [react-native.platform :as platform]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im2.contexts.chat.lightbox.animations :as anim]
    [status-im2.contexts.chat.lightbox.constants :as constants]
    [status-im2.contexts.chat.lightbox.top-view :as top-view]
    [utils.re-frame :as rf]
    [utils.worklets.chat.lightbox :as worklet]))

(defn clear-timers
  [timers]
  (js/clearTimeout (:mount-animation @timers))
  (js/clearTimeout (:mount-index-lock @timers))
  (js/clearTimeout (:hide-0 @timers))
  (js/clearTimeout (:hide-1 @timers))
  (js/clearTimeout (:show-0 @timers))
  (js/clearTimeout (:show-1 @timers))
  (js/clearTimeout (:show-2 @timers)))

(defn effect
  [{:keys [flat-list-ref scroll-index-lock? timers]} {:keys [opacity layout border]} index]
  (rn/use-effect
   (fn []
     (reagent/next-tick (fn []
                          (when @flat-list-ref
                            (.scrollToOffset ^js @flat-list-ref
                                             #js
                                              {:animated false
                                               :offset   (* (+ (:width (rn/get-window))
                                                               constants/separator-width)
                                                            index)}))))
     (swap! timers assoc
       :mount-animation
       (js/setTimeout (fn []
                        (anim/animate opacity 1)
                        (anim/animate layout 0)
                        (anim/animate border 16))
                      (if platform/ios? 250 100)))
     (swap! timers assoc :mount-index-lock (js/setTimeout #(reset! scroll-index-lock? false) 300))
     (fn []
       (rf/dispatch [:chat.ui/zoom-out-signal nil])
       (when platform/android?
         (rf/dispatch [:chat.ui/lightbox-scale 1]))
       (clear-timers timers)))))

(defn handle-orientation
  [result {:keys [flat-list-ref]} {:keys [scroll-index]} animations]
  (let [insets        (safe-area/get-insets)
        window        (rn/get-window)
        window-width  (:width window)
        window-height (:height window)
        window-height (if platform/android?
                        (+ window-height (:top insets))
                        window-height)
        screen-width  (if (or platform/ios? (= result orientation/portrait))
                        window-width
                        window-height)
        screen-height (if (or platform/ios? (= result orientation/portrait))
                        window-height
                        window-width)
        landscape?    (string/includes? result orientation/landscape)
        item-width    (if (and landscape? platform/ios?) screen-height screen-width)]
    (when (or landscape? (= result orientation/portrait))
      (rf/dispatch [:chat.ui/orientation-change result]))
    (cond
      landscape?
      (orientation/lock-to-landscape "lightbox")
      (= result orientation/portrait)
      (orientation/lock-to-portrait "lightbox"))
    (js/setTimeout
     (fn []
       (when @flat-list-ref
         (.scrollToOffset
          ^js @flat-list-ref
          #js {:animated false :offset (* (+ item-width constants/separator-width) @scroll-index)})))
     100)
    (when platform/ios?
      (top-view/animate-rotation result screen-width screen-height insets animations))))

(defn orientation-change
  [props state animations]
  (orientation/use-device-orientation-change
   (fn [result]
     (if platform/ios?
       (handle-orientation result props state animations)
       ;; `use-device-orientation-change` will always be called on Android, so need to check
       (orientation/get-auto-rotate-state
        (fn [enabled?]
          ;; RNN does not support landscape-right
          (when (and enabled? (not= result orientation/landscape-right))
            (handle-orientation result props state animations))))))))

(defn on-scroll
  [e item-width {:keys [images-opacity]} landscape?]
  (let [total-item-width (+ item-width constants/separator-width)
        progress         (/ (if landscape?
                              (oops/oget e "nativeEvent.contentOffset.y")
                              (oops/oget e "nativeEvent.contentOffset.x"))
                            total-item-width)
        index-initial    (max (Math/floor progress) 0)
        index-final      (inc index-initial)
        decimal-part     (- progress index-initial)]
    (anim/set-val (nth images-opacity index-initial) (- 1 decimal-part))
    (when (< index-final (count images-opacity))
      (anim/set-val (nth images-opacity index-final) decimal-part))))

(defn drag-gesture
  [{:keys [pan-x pan-y background-color opacity layout]} x? set-full-height?]
  (->
    (gesture/gesture-pan)
    (gesture/enabled true)
    (gesture/max-pointers 1)
    (gesture/on-start #(reset! set-full-height? false))
    (gesture/on-update
     (fn [e]
       (let [translation (if x? (oops/oget e "translationX") (oops/oget e "translationY"))
             progress    (Math/abs (/ translation constants/drag-threshold))]
         (anim/set-val (if x? pan-x pan-y) translation)
         (anim/set-val opacity (- 1 progress))
         (anim/set-val layout (* progress -20)))))
    (gesture/on-end (fn [e]
                      (if (> (Math/abs (if x? (oops/oget e "translationX") (oops/oget e "translationY")))
                             constants/drag-threshold)
                        (do
                          (anim/animate background-color "rgba(0,0,0,0)")
                          (anim/animate opacity 0)
                          (rf/dispatch [:navigate-back]))
                        (do
                          (reset! set-full-height? true)
                          (anim/animate (if x? pan-x pan-y) 0)
                          (anim/animate opacity 1)
                          (anim/animate layout 0)))))))

(defn init-props
  []
  {:flat-list-ref      (atom nil)
   :small-list-ref     (atom nil)
   :scroll-index-lock? (atom true)
   :text-sheet-lock?   (atom false)
   :timers             (atom {})})

(defn init-state
  [messages index]
  ;; The initial value of data is the image that was pressed (and not the whole album) in order
  ;; for the transition animation to execute properly, otherwise it would animate towards
  ;; outside the screen (even if we have `initialScrollIndex` set).
  {:data             (reagent/atom (if (number? index) [(nth messages index)] []))
   :scroll-index     (reagent/atom index)
   :transparent?     (reagent/atom false)
   :set-full-height? (reagent/atom false)
   :overlay-z-index  (reagent/atom 0)})

(defn initialize-opacity
  [size selected-index]
  (mapv #(if (= % selected-index)
           (anim/use-val 1)
           (anim/use-val 0))
        (range size)))

(defn init-animations
  [size index]
  {:background-color  (anim/use-val colors/neutral-100-opa-0)
   :border            (anim/use-val (if platform/ios? 0 16))
   :full-screen-scale (anim/use-val 1)
   :opacity           (anim/use-val 0)
   :overlay-opacity   (anim/use-val 0)
   :images-opacity    (initialize-opacity size index)
   :rotate            (anim/use-val "0deg")
   :layout            (anim/use-val -10)
   :top-view-y        (anim/use-val 0)
   :top-view-x        (anim/use-val 0)
   :top-view-width    (anim/use-val (:width (rn/get-window)))
   :top-view-bg       (anim/use-val colors/neutral-100-opa-0)
   :pan-y             (anim/use-val 0)
   :pan-x             (anim/use-val 0)})

(defn init-derived-animations
  [{:keys [layout]}]
  {:top-layout    (worklet/info-layout layout true)
   :bottom-layout (worklet/info-layout layout false)})
