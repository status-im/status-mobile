(ns status-im.switcher.animation
  (:require [re-frame.core :as re-frame]
            [react-native.reanimated :as reanimated]
            [quo2.foundations.colors :as colors]
            [status-im.async-storage.core :as async-storage]
            [status-im.switcher.constants :as constants]))

;;;; Bottom Tabs & Home Stack Animations

(def selected-stack-id (atom nil))
(def home-stack-open? (atom false))
(def pass-through? (atom false))

(def bottom-nav-tab-width 90)

(defn selected-stack-id-loaded [stack-id]
  (reset! selected-stack-id stack-id)
  (reset! home-stack-open? (some? stack-id)))

(defn calculate-home-stack-position []
  (let [{:keys [width height]} (constants/dimensions)
        minimize-scale         (/ bottom-nav-tab-width width)
        empty-space-half-scale (/ (- 1 minimize-scale) 2)
        left-margin            (/ (- width (* 4 bottom-nav-tab-width)) 2)
        left-empty-space       (* empty-space-half-scale width)
        top-empty-space        (* empty-space-half-scale
                                  (- height (constants/bottom-tabs-container-height)))]
    {:left  (reduce
             (fn [acc stack-id]
               (assoc acc stack-id (+ (- left-margin left-empty-space)
                                      (* (.indexOf constants/stacks-ids stack-id)
                                         bottom-nav-tab-width))))
             {:none 0} constants/stacks-ids)
     :top   (+ top-empty-space (constants/bottom-tabs-container-height))
     :scale minimize-scale}))

(defn get-shared-values []
  (let [selected-stack-id-sv    (reanimated/use-shared-value
                                 ;; passing keywords or nil is not working with reanimated
                                 (name (if @selected-stack-id @selected-stack-id :none)))
        ;; Second shared value of selected-stack-id required to make sure stack is still visible while minimizing
        selected-stack-id-sv2   (reanimated/use-shared-value
                                 (name (if @selected-stack-id @selected-stack-id :none)))
        pass-through-sv         (reanimated/use-shared-value @pass-through?)
        home-stack-open-sv      (reanimated/use-shared-value @home-stack-open?)
        animate-home-stack-left (reanimated/use-shared-value (not @home-stack-open?))
        home-stack-position     (calculate-home-stack-position)]
    (reduce
     (fn [acc id]
       (let [tabs-icon-color-keyword (get constants/tabs-icon-color-keywords id)
             stack-opacity-keyword   (get constants/stacks-opacity-keywords id)
             stack-pointer-keyword   (get constants/stacks-pointer-keywords id)]
         (assoc
          acc
          stack-opacity-keyword   (.stackOpacity
                                   ^js reanimated/worklet-factory
                                   (name id) selected-stack-id-sv2)
          stack-pointer-keyword   (.stackPointer
                                   ^js reanimated/worklet-factory
                                   (name id) selected-stack-id-sv2)
          tabs-icon-color-keyword (.bottomTabIconColor
                                   ^js reanimated/worklet-factory
                                   (name id) selected-stack-id-sv pass-through-sv
                                   colors/white colors/neutral-50 colors/white-opa-40))))
     {:selected-stack-id   selected-stack-id-sv
      :selected-stack-id2  selected-stack-id-sv2
      :pass-through?       pass-through-sv
      :home-stack-open?    home-stack-open-sv
      :animate-home-stack-left animate-home-stack-left
      :home-stack-left    (.homeStackLeft
                           ^js reanimated/worklet-factory
                           selected-stack-id-sv2 animate-home-stack-left home-stack-open-sv
                           (clj->js (:left home-stack-position)))
      :home-stack-top     (.homeStackTop
                           ^js reanimated/worklet-factory
                           home-stack-open-sv (:top home-stack-position))
      :home-stack-opacity (.homeStackOpacity
                           ^js reanimated/worklet-factory home-stack-open-sv)
      :home-stack-pointer (.homeStackPointer
                           ^js reanimated/worklet-factory home-stack-open-sv)
      :home-stack-scale   (.homeStackScale
                           ^js reanimated/worklet-factory home-stack-open-sv
                           (:scale home-stack-position))}
     constants/stacks-ids)))

;; Animation

(defn change-tab [shared-values stack-id]
  (when-not (colors/dark?)
    (js/setTimeout #(re-frame/dispatch [:change-root-status-bar-style :dark]) 300))
  (if @home-stack-open?
    (reanimated/set-shared-value (:animate-home-stack-left shared-values) false)
    (reset! home-stack-open? true))
  (reset! selected-stack-id stack-id)
  (reanimated/set-shared-value (:selected-stack-id2 shared-values) (name stack-id))
  (reanimated/set-shared-value (:selected-stack-id shared-values) (name stack-id))
  (reanimated/set-shared-value (:home-stack-open?  shared-values) true)
  (async-storage/set-item! :selected-stack-id stack-id))

(defn close-home-stack [shared-values]
  (re-frame/dispatch [:change-root-status-bar-style :light])
  (reanimated/set-shared-value (:animate-home-stack-left shared-values) true)
  (reset! home-stack-open? false)
  (reset! selected-stack-id nil)
  (reanimated/set-shared-value (:home-stack-open? shared-values) false)
  (reanimated/set-shared-value (:selected-stack-id shared-values) "none")
  (async-storage/set-item! :selected-stack-id nil))

