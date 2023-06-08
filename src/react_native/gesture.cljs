(ns react-native.gesture
  (:require ["react-native-gesture-handler" :refer
             (Gesture
              GestureDetector
              RectButton
              Swipeable
              TouchableWithoutFeedback
              gestureHandlerRootHOC
              FlatList
              ScrollView)]
            [react-native.flat-list :as rn-flat-list]
            [reagent.core :as reagent]))

(def gesture-detector (reagent/adapt-react-class GestureDetector))

(def gesture-handler-root-hoc gestureHandlerRootHOC)

(defn gesture-tap [] (.Tap ^js Gesture))

(defn gesture-pan [] (.Pan ^js Gesture))

(defn gesture-pinch [] (.Pinch ^js Gesture))

(defn on-begin [gesture handler] (.onBegin ^js gesture handler))

(defn on-start [gesture handler] (.onStart ^js gesture handler))

(defn on-update [gesture handler] (.onUpdate ^js gesture handler))

(defn on-end [gesture handler] (.onEnd ^js gesture handler))

(defn on-finalize [gesture handler] (.onFinalize ^js gesture handler))

(defn max-pointers [gesture count] (.maxPointers ^js gesture count))

(defn min-distance [gesture dist] (.minDistance ^js gesture dist))

(defn number-of-taps [gesture count] (.numberOfTaps ^js gesture count))

(defn enabled [gesture enabled?] (.enabled ^js gesture enabled?))

(defn average-touches [gesture average-touches?] (.averageTouches ^js gesture average-touches?))

(defn simultaneous
  ([g1 g2] (.Simultaneous ^js Gesture g1 g2))
  ([g1 g2 g3] (.Simultaneous ^js Gesture g1 g2 g3)))

(defn exclusive [g1 g2] (.Exclusive ^js Gesture g1 g2))

;; RN Gesture Handler touchables are drop-in replacements for the RN ones. In
;; some cases, it's the only touchable that works with Swipeable components.
(def touchable-without-feedback (reagent/adapt-react-class TouchableWithoutFeedback))

(def rect-button (reagent/adapt-react-class RectButton))

(def ^:private swipeable-component
  (reagent/adapt-react-class Swipeable))

(defn swipeable
  [{:keys [render-left-actions render-right-actions] :as props} & children]
  (into [swipeable-component
         (cond-> props
           render-left-actions
           (assoc :render-left-actions
                  (fn [& args]
                    (reagent/as-element (apply render-left-actions args))))

           render-right-actions
           (assoc :render-right-actions
                  (fn [& args]
                    (reagent/as-element (apply render-right-actions args)))))]
        children))

(def gesture-flat-list (reagent/adapt-react-class FlatList))

(defn flat-list
  [props]
  [gesture-flat-list (rn-flat-list/base-list-props props)])

(def scroll-view (reagent/adapt-react-class ScrollView))

;;; Custom gesture section-list
(defn- flatten-sections
  [sections]
  (mapcat (fn [{:keys [title data]}]
            (into [{:title title :header? true}] data))
   sections))

(defn section-list
  [{:keys [sections render-section-header-fn render-fn] :as props}]
  (let [data (flatten-sections sections)]
    [flat-list
     (merge props
            {:data      data
             :render-fn (fn [item]
                          (if (:header? item)
                            (render-section-header-fn item)
                            (render-fn item)))})]))
