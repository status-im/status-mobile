(ns quo2.components.tabs.tabs
  (:require [quo.react-native :as rn]
            [quo2.components.tabs.tab :as tab]
            [reagent.core :as reagent]
            [status-im.ui.components.react :as react]))

(def default-tab-size 32)

(defn tabs [{:keys [default-active on-change]}]
  (let [active-tab-id (reagent/atom default-active)]
    (fn [{:keys [data size] :or {size default-tab-size}}]
      (let [active-id @active-tab-id]
        [rn/view {:flex-direction :row}
         (for [{:keys [label id]} data]
           ^{:key id}
           [rn/view {:margin-right (if (= size default-tab-size) 12 8)}
            [tab/tab
             {:id       id
              :size     size
              :active   (= id active-id)
              :on-press (fn [^js press-event id]
                          (reset! active-tab-id id)
                          (when on-change
                            (on-change press-event id)))}
             label]])]))))

(defn- linear-gradient-mask
  [fade-end-percentage]
  [react/linear-gradient {:colors         ["black" "transparent"]
                          :locations      [(- 1 fade-end-percentage)
                                           1]
                          :start          {:x 0 :y 0}
                          :end            {:x 1 :y 0}
                          :pointer-events :none
                          :style          {:width  "100%"
                                           :height "100%"}}])

(defn scrollable-tabs
  "Just like the component `tabs`, displays horizontally scrollable tabs with
  extra options to control if/how the end of the scroll view fades.

  Tabs are rendered using ReactNative's FlatList, which offers the convenient
  `scrollToIndex` method. FlatList accepts VirtualizedList and ScrollView props,
  and so does this component.

  Usage:
  [tabs/scrollable-tabs
    {:scroll-on-press? true
     :fade-end?        true
     :on-change        #(...)
     :default-active   :tab-a
     :data             [{:id :tab-a :label \"Tab A\"}
                        {:id :tab-b :label \"Tab B\"}]}]]

  Opts:
  - `size` number
  - `scroll-on-press?` When non-nil, clicking on a tab centers it the middle
  (with animation enabled).
  - `fade-end?` When non-nil, causes the end of the scrollable view to fade out.
  - `fade-end-max-percentage` a number between 0 and 1 limiting the maximum size
  of the linear gradient mask. Default 0.25"
  [{:keys [default-active fade-end-max-percentage]
    :or   {fade-end-max-percentage 0.25}}]
  (let [active-tab-id (reagent/atom default-active)
        fading        (reagent/atom {:fade-end-percentage fade-end-max-percentage})
        flat-list-ref (atom nil)]
    (fn [{:keys [data
                 fade-end-max-percentage
                 fade-end?
                 on-change
                 on-scroll
                 scroll-event-throttle
                 scroll-on-press?
                 size]
          :or   {fade-end-max-percentage fade-end-max-percentage
                 fade-end?               false
                 scroll-event-throttle   64
                 scroll-on-press?        false
                 size                    default-tab-size}
          :as   props}]
      (let [maybe-mask-wrapper (if fade-end?
                                 [rn/masked-view {:mask-element (reagent/as-element
                                                                 [linear-gradient-mask (@fading :fade-end-percentage)])}]
                                 [:<>])]
        (conj maybe-mask-wrapper
              [rn/flat-list
               (merge (dissoc props
                              :default-active
                              :fade-end-max-percentage
                              :fade-end?
                              :on-change
                              :scroll-on-press?
                              :size)
                      {:ref                               (partial reset! flat-list-ref)
                       :extra-data                        @active-tab-id
                       :horizontal                        true
                       :scroll-event-throttle             scroll-event-throttle
                       :shows-horizontal-scroll-indicator false
                       :data                              data
                       :key-fn                            :id
                       :on-scroll                         (fn [^js e]
                                                            (when fade-end?
                                                              (let [offset-x      (.. e -nativeEvent -contentOffset -x)
                                                                    content-width (.. e -nativeEvent -contentSize -width)
                                                                    layout-width  (.. e -nativeEvent -layoutMeasurement -width)]
                                                                (swap! fading assoc :fade-end-percentage
                                                                       (min fade-end-max-percentage
                                                                            (- 1 (/ (+ layout-width offset-x)
                                                                                    content-width))))))
                                                            (when on-scroll
                                                              (on-scroll e)))
                       :render-fn                         (fn [{:keys [id label]} index]
                                                            [rn/view {:margin-right  (if (= size default-tab-size) 12 8)
                                                                      :padding-right (when (= index (dec (count data)))
                                                                                       (get-in props [:style :padding-left]))}
                                                             [tab/tab {:id       id
                                                                       :size     size
                                                                       :active   (= id @active-tab-id)
                                                                       :on-press (fn [id]
                                                                                   (reset! active-tab-id id)
                                                                                   (when scroll-on-press?
                                                                                     (.scrollToIndex @flat-list-ref
                                                                                                     #js {:animated     true
                                                                                                          :index        index
                                                                                                          :viewPosition 0.5}))
                                                                                   (when on-change
                                                                                     (on-change id)))}
                                                              label]])})])))))
