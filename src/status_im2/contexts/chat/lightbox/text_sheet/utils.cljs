(ns status-im2.contexts.chat.lightbox.text-sheet.utils
  (:require
    [oops.core :as oops]
    [react-native.gesture :as gesture]
    [react-native.reanimated :as reanimated]
    [reagent.core :as r]
    [status-im2.contexts.chat.lightbox.constants :as constants]))

(defn- collapse-sheet
  [{:keys [derived-value overlay-opacity saved-top expanded? overlay-z-index]}]
  (reanimated/animate derived-value constants/text-min-height)
  (reanimated/animate overlay-opacity 0)
  (reanimated/set-shared-value saved-top (- constants/text-min-height))
  (reset! expanded? false)
  (js/setTimeout #(reset! overlay-z-index 0) 300))

(defn sheet-gesture
  [{:keys [derived-value saved-top overlay-opacity gradient-opacity]}
   expanded-height max-height full-height overlay-z-index expanded? dragging? expanding-message?]
  (let [disable-gesture-update (r/atom false)]
    (-> (gesture/gesture-pan)
        (gesture/enabled expanding-message?)
        (gesture/on-start (fn []
                            (reset! overlay-z-index 1)
                            (reset! dragging? true)
                            (reset! disable-gesture-update false)
                            (when (not expanded?)
                              (reanimated/animate gradient-opacity 0))))
        (gesture/on-update
         (fn [e]
           (when-not @disable-gesture-update
             (let [event-value       (oops/oget e :translationY)
                   old-value         (reanimated/get-shared-value saved-top)
                   new-value         (+ old-value event-value)
                   progress          (/ (- new-value) max-height)
                   reached-expanded? (< new-value (- max-height))
                   upper-boundary?   (< new-value (- full-height))
                   lower-boundary?   (and (> new-value (- constants/text-min-height))
                                          (pos? event-value))]
               (when (and (not upper-boundary?) (not lower-boundary?))
                 (reset! expanded? false)
                 (reanimated/set-shared-value overlay-opacity progress)
                 (reanimated/set-shared-value derived-value (- new-value)))
               (when reached-expanded? (reset! expanded? true))
               (when lower-boundary?
                 (reset! disable-gesture-update true)
                 (collapse-sheet {:derived-value   derived-value
                                  :overlay-opacity overlay-opacity
                                  :saved-top       saved-top
                                  :expanded?       expanded?
                                  :overlay-z-index overlay-z-index}))))))
        (gesture/on-end
         (fn []
           (let [shared-derived-value (reanimated/get-shared-value derived-value)
                 below-max-height?    (< shared-derived-value max-height)
                 below-saved-top?     (> (- shared-derived-value)
                                         (reanimated/get-shared-value saved-top))]
             (if (and below-max-height? below-saved-top?)
               (collapse-sheet {:derived-value   derived-value
                                :overlay-opacity overlay-opacity
                                :saved-top       saved-top
                                :expanded?       expanded?
                                :overlay-z-index overlay-z-index})
               (reanimated/set-shared-value saved-top
                                            (- shared-derived-value)))
             (when (= shared-derived-value expanded-height)
               (reset! expanded? true))
             (reset! dragging? false)))))))

(defn expand-sheet
  [{:keys [derived-value overlay-opacity saved-top]}
   expanded-height max-height overlay-z-index expanded? text-sheet-lock?]
  (when-not @text-sheet-lock?
    (reanimated/animate derived-value expanded-height)
    (reanimated/animate overlay-opacity (/ expanded-height max-height))
    (reanimated/set-shared-value saved-top (- expanded-height))
    (reset! overlay-z-index 1)
    (reset! expanded? true)))

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
  {:height derived-value
   :top    (reanimated/interpolate derived-value [0 1] [0 -1])})
