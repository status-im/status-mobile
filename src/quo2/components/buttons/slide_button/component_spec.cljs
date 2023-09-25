(ns quo2.components.buttons.slide-button.component-spec
  (:require [quo2.components.buttons.slide-button.view :as slide-button]
            [quo2.components.buttons.slide-button.constants :as constants]
            [quo2.components.buttons.slide-button.utils :as utils]
            ["@testing-library/react-native" :as rtl]
            ["react-native-gesture-handler/jest-utils" :as gestures-jest]
            [reagent.core :as r]
            [test-helpers.component :as h]))

;; NOTE stolen from
;; (https://github.com/reagent-project/reagent/blob/a14faba55e373000f8f93edfcfce0d1222f7e71a/test/reagenttest/utils.cljs#LL104C7-L104C10),
;;
;; There's also a comment over there about it being
;; not "usable with production React", but no explanation why.
;; If we decide to keep it, can be moved to `test-helpers.component`.
(defn act
  "Run f to trigger Reagent updates,
  will return Promise which will resolve after
  Reagent and React render."
  [f]
  (js/Promise.
   (fn [resolve-fn reject]
     (try
       (.then (rtl/act
               #(let [p (js/Promise. (fn [resolve-fn2 _reject]
                                       (r/after-render (fn reagent-act-after-reagent-flush []
                                                         (resolve-fn2)))))]
                  (f)
                  p))
              resolve-fn
              reject)
       (catch :default e
         (reject e))))))

(def ^:private gesture-state
  {:untedermined 0
   :failed       1
   :began        2
   :cancelled    3
   :active       4
   :end          5})

(defn gesture-x-event
  [event position]
  (clj->js {:state        (event gesture-state)
            :translationX position}))

(defn slide-events
  [dest]
  [(gesture-x-event :began 0)
   (gesture-x-event :active 0)
   (gesture-x-event :active dest)
   (gesture-x-event :end dest)])

(defn get-by-gesture-test-id
  [test-id]
  (gestures-jest/getByGestureTestId
   (str test-id)))

(def ^:private default-props
  {:on-complete identity
   :track-text  :test-track-text
   :track-icon  :face-id})

(h/describe "slide-button"
  (h/before-each
   (fn []
     (h/use-fake-timers)))

  (h/after-each
   (fn []
     (h/clear-all-timers)
     (h/use-real-timers)))

  (h/test "render the correct text"
    (h/render [slide-button/view default-props])
    (h/is-truthy (h/get-by-text :test-track-text)))

  (h/test "render the disabled button"
    (h/render [slide-button/view (assoc default-props :disabled? true)])
    (let [track-mock (h/get-by-test-id :slide-button-track)]
      (h/has-style track-mock {:opacity constants/disable-opacity})))

  (h/test "render the small button"
    (h/render [slide-button/view (assoc default-props :size :small)])
    (let [mock         (h/get-by-test-id :slide-button-track)
          small-height (:track-height constants/small-dimensions)]
      (h/has-style mock {:height small-height})))

  (h/test "render with the correct customization-color"
    (h/render [slide-button/view (assoc default-props :customization-color :purple)])
    (let [track-mock   (h/get-by-test-id :slide-button-track)
          purple-color (utils/slider-color :track :purple)]
      (h/has-style track-mock {:backgroundColor purple-color})))

  (h/test
    "calls on-complete when dragged"
    (let [props          (merge default-props {:on-complete (h/mock-fn)})
          slide-dest     constants/default-width
          gesture-events (slide-events slide-dest)]
      (h/render [slide-button/view props])
      (let [promise
            (-> (act #(gestures-jest/fireGestureHandler (get-by-gesture-test-id :slide-button-gestures)
                                                        gesture-events)))]
        (h/advance-timers-by-time 250)
        (-> promise
            (.then #(h/was-called (:on-complete props)))))))

  (h/test
    "doesn't call on-complete if the slide was incomplete"
    (let [props          (merge default-props {:on-complete (h/mock-fn)})
          slide-dest     (- constants/default-width 100)
          gesture-events (slide-events slide-dest)]
      (h/render [slide-button/view props])
      (let [promise (-> (act #(gestures-jest/fireGestureHandler (get-by-gesture-test-id
                                                                 :slide-button-gestures)
                                                                gesture-events)))]
        (h/advance-timers-by-time 250)
        (-> promise (.then #(h/was-not-called (:on-complete props))))))))
