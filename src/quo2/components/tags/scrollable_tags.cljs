(ns quo2.components.tags.scrollable-tags
  (:require [reagent.core :as reagent]
            [quo.react-native :as rn]
            [oops.core :refer [oget]]
            [status-im.ui.components.react :as react]
            [status-im.utils.core :as utils]
            [quo2.components.tags.tag :as tag]
            [utils.number :as number-utils]))

(def default-tab-size 32)

(defn calculate-fade-end-percentage
  [{:keys [offset-x content-width layout-width max-fade-percentage]}]
  (let [fade-percentage (max max-fade-percentage
                             (/ (+ layout-width offset-x)
                                content-width))]
    ;; Truncate to avoid unnecessary rendering.
    (if (> fade-percentage 0.99)
      0.99
      (number-utils/naive-round fade-percentage 2))))

(defn view
  "Usage:
   {:type             :icon/:emoji/:label
    :component        tag/tab
    :size             32/24
    :on-press         fn
    :blurred?         true/false 
    :labelled?        true/false
    :disabled?        true/false
    :scroll-on-press? true
    :fade-end?        true
    :on-change        fn
    :default-active   tag-id
    :data             [{:id :label \"\" :resource \"url\"}
                       {:id :label \"\" :resource \"url\"}]}
  Opts:
   - `component` this is to determine which component is to be rendered since the 
                 logic in this view is shared between tab and tag component
   - `blurred`   boolean: use to determine border color if the background is blurred
   - `type`      can be icon or emoji with or without a tag label
   - `labelled`  boolean: is true if tag has label else false
   - `size` number
   - `scroll-on-press?` When non-nil, clicking on a tag centers it the middle
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
                 size
                 type
                 labelled?
                 disabled?
                 blurred?
                 icon-color]
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
                       :render-fn                          (fn [{:keys [id label resource]} index]
                                                             [rn/view {:style {:margin-right  (if (= size default-tab-size) 12 8)
                                                                               :padding-right (when (= index (dec (count data)))
                                                                                                (get-in props [:style :padding-left]))}}
                                                              [tag/tag {:id            id
                                                                        :size          size
                                                                        :active        (= id @active-tab-id)
                                                                        :resource      resource
                                                                        :blurred?      blurred?
                                                                        :icon-color    icon-color
                                                                        :disabled?     disabled?
                                                                        :label         label
                                                                        :type          type
                                                                        :labelled?     labelled?
                                                                        :on-press      (fn [id]
                                                                                         (reset! active-tab-id id)
                                                                                         (when scroll-on-press?
                                                                                           (.scrollToIndex @flat-list-ref
                                                                                                           #js {:animated     true
                                                                                                                :index        index
                                                                                                                :viewPosition 0.5}))
                                                                                         (when on-change
                                                                                           (on-change id)))}
                                                               label]])})])))))