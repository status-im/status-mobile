(ns status-im2.contexts.chat.lightbox.text-sheet.utils
  (:require
    [oops.core :as oops]
    [react-native.gesture :as gesture]
    [react-native.reanimated :as reanimated]
    [status-im2.contexts.chat.lightbox.constants :as constants]
    [utils.worklets.lightbox :as worklet]))

(defn sheet-gesture
  [{:keys [derived-value saved-top overlay-opacity gradient-opacity]}
   expanded-height max-height overlay-z-index expanded? dragging?]
  (-> (gesture/gesture-pan)
      (gesture/on-start (fn []
                          (reset! overlay-z-index 1)
                          (reset! dragging? true)
                          (reanimated/animate gradient-opacity 0)))
      (gesture/on-update
       (fn [e]
         (let [new-value     (+ (reanimated/get-shared-value saved-top) (oops/oget e "translationY"))
               bounded-value (max (min (- new-value) expanded-height) constants/text-min-height)
               progress      (/ (- new-value) max-height)]
           (reanimated/set-shared-value overlay-opacity progress)
           (reanimated/set-shared-value derived-value bounded-value))))
      (gesture/on-end
       (fn []
         (if (or (> (- (reanimated/get-shared-value derived-value))
                    (reanimated/get-shared-value saved-top))
                 (= (reanimated/get-shared-value derived-value)
                    constants/text-min-height))
           (do ; minimize
             (reanimated/animate derived-value constants/text-min-height)
             (reanimated/animate overlay-opacity 0)
             (reanimated/set-shared-value saved-top (- constants/text-min-height))
             (reset! expanded? false)
             (js/setTimeout #(reset! overlay-z-index 0) 300))
           (reanimated/set-shared-value saved-top
                                        (- (reanimated/get-shared-value derived-value))))
         (when (= (reanimated/get-shared-value derived-value) expanded-height)
           (reset! expanded? true))
         (reset! dragging? false)))))

(defn expand-sheet
  [{:keys [derived-value overlay-opacity saved-top]}
   expanded-height max-height overlay-z-index expanded? text-sheet-lock?]
  (when-not @text-sheet-lock?
    (reanimated/animate derived-value expanded-height)
    (reanimated/animate overlay-opacity (/ expanded-height max-height))
    (reanimated/set-shared-value saved-top (- expanded-height))
    (reset! overlay-z-index 1)
    (reset! expanded? true)))

(defn on-scroll
  [e expanded? dragging? {:keys [gradient-opacity]}]
  (if (and (> (oops/oget e "nativeEvent.contentOffset.y") 0) expanded? (not dragging?))
    (reanimated/animate gradient-opacity 1)
    (reanimated/animate gradient-opacity 0)))

(defn on-layout
  [e text-height]
  (reset! text-height (oops/oget e "nativeEvent.layout.height")))

(defn init-animations
  [overlay-opacity]
  {:derived-value    (reanimated/use-shared-value constants/text-min-height)
   :saved-top        (reanimated/use-shared-value (- constants/text-min-height))
   :gradient-opacity (reanimated/use-shared-value 0)
   :overlay-opacity  overlay-opacity})

(defn init-derived-animations
  [{:keys [derived-value]}]
  {:height (worklet/text-sheet derived-value true)
   :top    (worklet/text-sheet derived-value false)})
