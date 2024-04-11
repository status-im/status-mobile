(ns quo.components.tags.tags
  (:require
    [oops.core :refer [oget]]
    [quo.components.tags.tag :as tag]
    [react-native.core :as rn]
    [react-native.linear-gradient :as linear-gradient]
    [react-native.masked-view :as masked-view]
    [reagent.core :as reagent]
    [utils.collection :as utils.collection]
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

(defn tags
  "Usage:
   {:type             :icon/:emoji/:label
    :component        tag/tab
    :size             32/24
    :on-press         fn
    :blurred?         true/false
    :labelled?        true/false
    :disabled?        true/false
    :scroll-on-press? true
    :scrollable?      false
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

  [{:keys [default-active data fade-end-percentage fade-end? on-change on-scroll scroll-event-throttle
           scrollable?
           scroll-on-press? size type labelled? disabled? blurred? icon-color]
    :or   {fade-end-percentage   0.8
           fade-end?             false
           scroll-event-throttle 64
           scrollable?           false
           scroll-on-press?      false
           size                  default-tab-size}
    :as   props}]
  (let [[active-tab-id
         set-active-tab-id] (rn/use-state default-active)
        [fading set-fading] (rn/use-state {:fade-end-percentage fade-end-percentage})
        flat-list-ref       (rn/use-ref-atom nil)
        maybe-mask-wrapper  (rn/use-memo
                             (fn []
                               (if fade-end?
                                 [masked-view/masked-view
                                  {:mask-element (reagent/as-element
                                                  [linear-gradient/linear-gradient
                                                   {:colors         [:black :transparent]
                                                    :locations      [(get fading :fade-end-percentage)
                                                                     1]
                                                    :start          {:x 0 :y 0}
                                                    :end            {:x 1 :y 0}
                                                    :pointer-events :none
                                                    :style          {:width  "100%"
                                                                     :height "100%"}}])}]
                                 [:<>]))
                             [fade-end? fading])
        on-scroll           (rn/use-context
                             (fn [^js e]
                               (when fade-end?
                                 (let [offset-x       (oget e "nativeEvent.contentOffset.x")
                                       content-width  (oget e "nativeEvent.contentSize.width")
                                       layout-width   (oget e "nativeEvent.layoutMeasurement.width")
                                       new-percentage (calculate-fade-end-percentage
                                                       {:offset-x            offset-x
                                                        :content-width       content-width
                                                        :layout-width        layout-width
                                                        :max-fade-percentage fade-end-percentage})]
                                   ;; Avoid unnecessary re-rendering.
                                   (when (not= new-percentage (get fading :fade-end-percentage))
                                     (set-fading (assoc fading :fade-end-percentage new-percentage)))))
                               (when on-scroll
                                 (on-scroll e)))
                             [fade-end? fading fade-end-percentage])
        data-count          (count data)
        style-padding-left  (get-in props [:style :padding-left])
        render-fn           (rn/use-callback
                             (fn [{:keys [id label resource]} index]
                               [rn/view
                                {:style {:margin-right  (if (= size default-tab-size) 12 8)
                                         :padding-right (when (= index (dec data-count))
                                                          style-padding-left)}}

                                [tag/tag
                                 {:id         id
                                  :size       size
                                  :active     (= id active-tab-id)
                                  :resource   resource
                                  :blurred?   blurred?
                                  :icon-color icon-color
                                  :disabled?  disabled?
                                  :label      (if labelled?
                                                label
                                                (when (= type :label) label))
                                  :type       type
                                  :labelled?  labelled?
                                  :on-press   (fn [id]
                                                (set-active-tab-id id)
                                                (when scroll-on-press?
                                                  (.scrollToIndex ^js @flat-list-ref
                                                                  #js
                                                                   {:animated     true
                                                                    :index        index
                                                                    :viewPosition 0.5}))
                                                (when on-change
                                                  (on-change id)))}]])
                             [size default-tab-size data-count style-padding-left active-tab-id blurred?
                              icon-color disabled? labelled? type scroll-on-press? on-change])
        on-press            (rn/use-callback #(do (set-active-tab-id %) (when on-change (on-change %)))
                                             [on-change])
        key-fn              (rn/use-callback (comp str :id))
        ref                 (rn/use-callback #(reset! flat-list-ref %))
        clean-props         (dissoc props
                             :default-active
                             :fade-end-percentage
                             :fade-end?
                             :on-change
                             :scroll-on-press?
                             :size)]
    (if scrollable?
      (conj
       maybe-mask-wrapper
       [rn/flat-list
        (merge
         clean-props
         (when scroll-on-press?
           {:initial-scroll-index (utils.collection/first-index #(= active-tab-id (:id %)) data)})
         {:ref                               ref
          :extra-data                        (str active-tab-id)
          :horizontal                        true
          :scroll-event-throttle             scroll-event-throttle
          :shows-horizontal-scroll-indicator false
          :data                              data
          :key-fn                            key-fn
          :on-scroll                         on-scroll
          :render-fn                         render-fn})])

      [rn/view {:style {:flex-direction :row}}
       (for [{:keys [id label resource]} data]
         ^{:key id}
         [rn/view {:style {:margin-right 8}}
          [tag/tag
           {:id         id
            :size       size
            :type       type
            :label      (if labelled?
                          label
                          (when (= type :label) label))
            :active     (= id active-tab-id)
            :disabled?  disabled?
            :blurred?   blurred?
            :icon-color icon-color
            :labelled?  (if (= type :label) true labelled?)
            :resource   (if (= type :icon) :i/placeholder resource)
            :on-press   on-press}]])])))
