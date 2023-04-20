(ns quo2.components.notifications.count-down-circle
  (:require [goog.string :as gstring]
            [quo2.foundations.colors :as colors]
            [quo2.theme :as theme]
            [react-native.core :as rn]
            [react-native.svg :as svg]
            [reagent.core :as reagent]))

(defn- get-path-props
  [size stroke-width rotation]
  (let [half-size          (/ size 2)
        half-stroke-width  (/ stroke-width 2)
        arc-radius         (- half-size half-stroke-width)
        arc-diameter       (* arc-radius 2)
        rotation-indicator (if (= rotation :clockwise) "1,0" "0,1")
        path-length        (* js/Math.PI arc-diameter)
        path               (gstring/format
                            "m %s,%s a %s,%s 0 %s 0,%s a %s,%s 0 %s 0 ,-%s"
                            half-size
                            half-stroke-width
                            arc-radius
                            arc-radius
                            rotation-indicator
                            arc-diameter
                            arc-radius
                            arc-radius
                            rotation-indicator
                            arc-diameter)]
    {:path        path
     :path-length path-length}))

(defn- linear-ease
  [time start goal duration]
  (if (zero? duration)
    start
    (-> time
        (/ duration)
        (* goal)
        (+ start))))

(defn- get-start-at
  [duration initial-remaining-time]
  (cond (or (zero? duration) (= duration initial-remaining-time)) 0
        (number? initial-remaining-time)                          (- duration initial-remaining-time)
        :else                                                     0))

(def ^:private themes
  {:color {:dark  colors/neutral-80-opa-40
           :light colors/white-opa-40}})

(defn circle-timer
  [{:keys [color duration size stroke-width trail-color rotation initial-remaining-time]}]
  (let [rotation                     (or rotation :clockwise)
        duration                     (or duration 4)
        stroke-width                 (or stroke-width 1)
        size                         (or size 9)
        max-stroke-width             (max stroke-width 0)
        {:keys [path path-length]}   (get-path-props size max-stroke-width rotation)
        start-at                     (get-start-at duration initial-remaining-time)
        elapsed-time                 (reagent/atom 0)
        prev-frame-time              (reagent/atom nil)
        frame-request                (reagent/atom nil)
        display-time                 (reagent/atom start-at)
        ;; get elapsed frame time
        swap-elapsed-time-each-frame (fn swap-elapsed-time-each-frame [frame-time]
                                       (if (nil? @prev-frame-time)
                                         (do (reset! prev-frame-time frame-time)
                                             (reset! frame-request (js/requestAnimationFrame
                                                                    swap-elapsed-time-each-frame)))
                                         (let [delta                (- (/ frame-time 1000)
                                                                       (/ @prev-frame-time 1000))
                                               current-elapsed      (swap! elapsed-time + delta)
                                               current-display-time (+ start-at current-elapsed)
                                               completed?           (>= current-display-time duration)]
                                           (reset! display-time (if completed?
                                                                  duration
                                                                  current-display-time))
                                           (when-not completed?
                                             (reset! prev-frame-time frame-time)
                                             (reset! frame-request (js/requestAnimationFrame
                                                                    swap-elapsed-time-each-frame))))))]
    (reagent/create-class
     {:component-will-unmount #(js/cancelAnimationFrame @frame-request)
      :reagent-render
      (fn []
        (reset! frame-request (js/requestAnimationFrame swap-elapsed-time-each-frame))
        [rn/view
         {:style {:position :relative
                  :width    size
                  :height   size}}
         [svg/svg
          {:view-box (str "0 0 " size " " size)
           :width    size
           :height   size}
          [svg/path
           {:d path :fill :none :stroke (or trail-color :transparent) :stroke-width stroke-width}]
          (when-not (= @display-time duration)
            [svg/path
             {:d                 path
              :fill              :none
              :stroke            (or color (get-in themes [:color (theme/get-theme)]))
              :stroke-linecap    :square
              :stroke-width      stroke-width
              :stroke-dasharray  path-length
              :stroke-dashoffset (linear-ease @display-time 0 path-length duration)}])]])})))
