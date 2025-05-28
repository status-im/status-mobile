(ns status-im.contexts.browser.hooks
  (:require [oops.core :as oops]
            [react-native.core :as rn]
            [react-native.gesture :as gesture]
            [react-native.reanimated :as reanimated]
            [react-native.view-shot :as view-shot]
            [status-im.contexts.browser.core :as browser]
            [utils.re-frame :as rf]
            [utils.worklets.browser :as worklets.browser]))

(defn animate-with-spring
  [animation-val to-val]
  (reanimated/animate-shared-value-with-spring animation-val
                                               to-val
                                               {:mass      1
                                                :damping   100
                                                :stiffness 400}))

(def drag-threshold 100)
(defn- make-drag-gesture
  [{:keys [enabled? can-move-left? can-move-right? x-translation-value focused-tab-idx on-tab-change]}]
  (let [initial-x-val (atom 0)]
    (->
      (gesture/gesture-pan)
      (gesture/enabled enabled?)
      (gesture/on-begin (fn [_]
                          (reset! initial-x-val (browser/tab-position focused-tab-idx))))
      (gesture/on-update (fn [event]
                           (let [x-translation (oops/oget event "translationX")
                                 new-val       (- @initial-x-val x-translation)]
                             (reanimated/set-shared-value x-translation-value new-val))))
      (gesture/on-end
       (fn [event]
         (let [x-translation (oops/oget event "translationX")
               drag-right?   (>= x-translation drag-threshold)
               drag-left?    (<= x-translation (- drag-threshold))]
           (cond
             (and drag-right? can-move-left?)
             (let [tab-idx (dec focused-tab-idx)]
               (animate-with-spring x-translation-value (browser/tab-position tab-idx))
               (on-tab-change tab-idx))

             (and drag-left? can-move-right?)
             (let [tab-idx (inc focused-tab-idx)]
               (animate-with-spring x-translation-value (browser/tab-position tab-idx))
               (on-tab-change tab-idx))

             :else
             (animate-with-spring x-translation-value @initial-x-val))))))))

(defn use-swipe-gesture
  [scroll-ref]
  (let [focused-tab-idx     (rf/sub [:browser/focused-tab-idx])
        browser-mode        (rf/sub [:browser/mode])
        gestures-enabled?   (= browser-mode :browser-mode/browser)
        x-translation-value (reanimated/use-shared-value
                             (or (browser/tab-position focused-tab-idx) 0))
        can-focus-next?     (rf/sub [:browser/can-focus-next?])
        can-focus-prev?     (rf/sub [:browser/can-focus-prev?])]

    (rn/use-effect
     (fn []
       (reanimated/set-shared-value x-translation-value (browser/tab-position focused-tab-idx)))
     [focused-tab-idx])

    (worklets.browser/use-scroll-tab {:animated-ref  scroll-ref
                                      :x-translation x-translation-value
                                      :animate       true})

    {:x-translation-value x-translation-value
     :gesture-prop        (make-drag-gesture
                           {:enabled?            gestures-enabled?
                            :x-translation-value x-translation-value
                            :can-move-left?      can-focus-prev?
                            :can-move-right?     can-focus-next?
                            :focused-tab-idx     focused-tab-idx
                            :on-tab-change       (fn [tab-idx]
                                                   (js/setTimeout #(rf/dispatch-sync
                                                                    [:browser/focus-tab-by-idx tab-idx])
                                                                  200))})}))

(defn use-screenshot-tab
  [tab-id]
  (let [options {:fileName (str "tab-screenshot-" tab-id)
                 :format   :jpg
                 :quality  0.9}]
    {:options options
     :capture (fn []
                (rf/dispatch [:browser.screenshots/capture tab-id]))
     :ref     (fn [ref]
                (rf/dispatch [:browser.screenshots/add-ref tab-id ref]))}))
