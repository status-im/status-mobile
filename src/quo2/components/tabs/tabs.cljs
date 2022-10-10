(ns quo2.components.tabs.tabs
  (:require [oops.core :refer [oget]]
            [quo.react-native :as rn]
            [quo2.components.tabs.tab :as tab]
            [reagent.core :as reagent]
            [status-im.ui.components.react :as react]
            [status-im.utils.core :as utils]
            [status-im.utils.number :as number-utils]))

(def default-tab-size 32)

(defn tabs [{:keys [default-active on-change]}]
  (let [active-tab-id (reagent/atom default-active)]
    (fn [{:keys [data size] :or {size default-tab-size}}]
      (let [active-id @active-tab-id]
        [rn/view {:flex-direction :row}
         (for [{:keys [label id]} data]
           ^{:key id}
           [rn/view {:style {:margin-right (if (= size default-tab-size) 12 8)}}
            [tab/tab
             {:id       id
              :size     size
              :active   (= id active-id)
              :on-press (fn [^js press-event id]
                          (reset! active-tab-id id)
                          (when on-change
                            (on-change press-event id)))}
             label]])]))))

(defn- calculate-fade-end-percentage
  [{:keys [offset-x content-width layout-width max-fade-percentage]}]
  (let [fade-percentage (max max-fade-percentage
                             (/ (+ layout-width offset-x)
                                content-width))]
    ;; Truncate to avoid unnecessary rendering.
    (if (> fade-percentage 0.99)
      0.99
      (number-utils/naive-round fade-percentage 2))))

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
  - `fade-end-percentage` Percentage where fading starts relative to the total
  layout width of the `flat-list` data."
  [{:keys [default-active fade-end-percentage]
    :or   {fade-end-percentage 0.8}}]
  (let [active-tab-id (reagent/atom default-active)
        fading        (reagent/atom {:fade-end-percentage fade-end-percentage})
        flat-list-ref (atom nil)]
    (fn [{:keys [data
                 fade-end-percentage
                 fade-end?
                 on-change
                 on-scroll
                 scroll-event-throttle
                 scroll-on-press?
                 size]
          :or   {fade-end-percentage   fade-end-percentage
                 fade-end?             false
                 scroll-event-throttle 64
                 scroll-on-press?      false
                 size                  default-tab-size}
          :as   props}]
      (let [maybe-mask-wrapper (if fade-end?
                                 [react/masked-view
                                  {:mask-element (reagent/as-element
                                                  [react/linear-gradient {:colors         [:black :transparent]
                                                                          :locations      [(get @fading :fade-end-percentage) 1]
                                                                          :start          {:x 0 :y 0}
                                                                          :end            {:x 1 :y 0}
                                                                          :pointer-events :none
                                                                          :style          {:width  "100%"
                                                                                           :height "100%"}}])}]
                                 [:<>])]
        (conj maybe-mask-wrapper
              [rn/flat-list
               (merge (dissoc props
                              :default-active
                              :fade-end-percentage
                              :fade-end?
                              :on-change
                              :scroll-on-press?
                              :size)
                      (when scroll-on-press?
                        {:initial-scroll-index (utils/first-index #(= @active-tab-id (:id %)) data)})
                      {:ref                               #(reset! flat-list-ref %)
                       :extra-data                        (str @active-tab-id)
                       :horizontal                        true
                       :scroll-event-throttle             scroll-event-throttle
                       :shows-horizontal-scroll-indicator false
                       :data                              data
                       :key-fn                            (comp str :id)
                       :on-scroll                         (fn [^js e]
                                                            (when fade-end?
                                                              (let [offset-x       (oget e "nativeEvent.contentOffset.x")
                                                                    content-width  (oget e "nativeEvent.contentSize.width")
                                                                    layout-width   (oget e "nativeEvent.layoutMeasurement.width")
                                                                    new-percentage (calculate-fade-end-percentage {:offset-x            offset-x
                                                                                                                   :content-width       content-width
                                                                                                                   :layout-width        layout-width
                                                                                                                   :max-fade-percentage fade-end-percentage})]
                                                                ;; Avoid unnecessary re-rendering.
                                                                (when (not= new-percentage (get @fading :fade-end-percentage))
                                                                  (swap! fading assoc :fade-end-percentage new-percentage))))
                                                            (when on-scroll
                                                              (on-scroll e)))
                       :render-fn                         (fn [{:keys [id label]} index]
                                                            [rn/view {:style {:margin-right  (if (= size default-tab-size) 12 8)
                                                                              :padding-right (when (= index (dec (count data)))
                                                                                               (get-in props [:style :padding-left]))}}
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
