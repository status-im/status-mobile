(ns quo2.components.tabs.tabs
  (:require [oops.core :refer [oget]]
            [quo2.components.tabs.tab.view :as tab]
            [react-native.core :as rn]
            [react-native.linear-gradient :as linear-gradient]
            [react-native.masked-view :as masked-view]
            [reagent.core :as reagent]
            [utils.collection :as utils.collection]
            [utils.number :as utils.number]
            [react-native.gesture :as gesture]))

(def default-tab-size 32)
(def unread-count-offset 3)

(defn- calculate-fade-end-percentage
  [{:keys [offset-x content-width layout-width max-fade-percentage]}]
  (let [fade-percentage (max max-fade-percentage
                             (/ (+ layout-width offset-x)
                                content-width))]
    ;; Truncate to avoid unnecessary rendering.
    (if (> fade-percentage 0.99)
      0.99
      (utils.number/naive-round fade-percentage 2))))

(defn- masked-view-wrapper
  [{:keys [fade-end-percentage fade-end?]} & children]
  (if fade-end?
    (into [masked-view/masked-view
           {:mask-element
            (reagent/as-element
             [linear-gradient/linear-gradient
              {:colors         [:black :transparent]
               :locations      [fade-end-percentage 1]
               :start          {:x 0 :y 0}
               :end            {:x 1 :y 0}
               :pointer-events :none
               :style          {:width  "100%"
                                :height "100%"}}])}]
          children)
    (into [:<>] children)))

(defn- on-scroll-handler
  [{:keys [on-scroll fading fade-end-percentage fade-end?]} ^js e]
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
      (when (not= new-percentage
                  (get @fading :fade-end-percentage))
        (swap! fading assoc
          :fade-end-percentage
          new-percentage))))
  (when on-scroll
    (on-scroll e)))

(defn- render-tab
  [{:keys [active-tab-id
           blur?
           flat-list-ref
           number-of-items
           on-change
           scroll-on-press?
           size
           style]}
   {:keys [id label notification-dot? accessibility-label]}
   index]
  [rn/view
   {:style {:margin-right  (if (= size default-tab-size) 12 8)
            :padding-right (when (= index (dec number-of-items))
                             (:padding-left style))}}
   [tab/view
    {:id                  id
     :notification-dot?   notification-dot?
     :accessibility-label accessibility-label
     :size                size
     :blur?               blur?
     :active              (= id @active-tab-id)
     :on-press            (fn [id]
                            (reset! active-tab-id id)
                            (when (and scroll-on-press? @flat-list-ref)
                              (.scrollToIndex ^js @flat-list-ref
                                              #js
                                               {:animated     true
                                                :index        index
                                                :viewPosition 0.5}))
                            (when on-change
                              (on-change id)))}
    label]])

(defn tabs
  " Common options (for scrollable and non-scrollable tabs):

  - `blur?` Boolean passed down to `quo2.components.tabs.tab/tab`.
  - `data` Vector of tab items.
  - `on-change` Callback called after a tab is selected.
  - `size` 32/24
  - `style` Style map passed to View wrapping tabs or to the FlatList when tabs
    are scrollable.

  Options for scrollable tabs:
  - `fade-end-percentage` Percentage where fading starts relative to the total
    layout width of the `flat-list` data.
  - `fade-end?` When non-nil, causes the end of the scrollable view to fade out.
  - `on-scroll` Callback called on the on-scroll event of the FlatList. Only
    used when `scrollable?` is non-nil.
  - `scrollable?` When non-nil, use a scrollable flat-list to render tabs.
  - `scroll-on-press?` When non-nil, clicking on a tag centers it the middle
    (with animation enabled).
  "
  [{:keys [default-active fade-end-percentage]
    :or   {fade-end-percentage 0.8}}]
  (let [active-tab-id (reagent/atom default-active)
        fading        (reagent/atom {:fade-end-percentage fade-end-percentage})
        flat-list-ref (atom nil)]
    (fn
      [{:keys [data
               fade-end-percentage
               fade-end?
               on-change
               on-scroll
               scroll-on-press?
               scrollable?
               style
               size
               blur?
               in-scroll-view?]
        :or   {fade-end-percentage fade-end-percentage
               fade-end?           false
               scrollable?         false
               scroll-on-press?    false
               size                default-tab-size}
        :as   props}]
      (if scrollable?
        [rn/view {:style {:margin-top (- (dec unread-count-offset))}}
         [masked-view-wrapper
          {:fade-end-percentage (get @fading :fade-end-percentage) :fade-end? fade-end?}
          [(if in-scroll-view?
             gesture/flat-list
             rn/flat-list)
           (merge
            (dissoc props
             :default-active
             :fade-end-percentage
             :fade-end?
             :on-change
             :scroll-on-press?
             :size)
            (when scroll-on-press?
              {:initial-scroll-index (utils.collection/first-index #(= @active-tab-id (:id %)) data)})
            {:ref                               #(reset! flat-list-ref %)
             :style                             style
             ;; The padding-top workaround is needed because on Android
             ;; {:overflow :visible} doesn't work on components inheriting
             ;; from ScrollView (e.g. FlatList). There are open issues, here's
             ;; just one about this topic:
             ;; https://github.com/facebook/react-native/issues/31218
             :content-container-style           {:padding-top (dec unread-count-offset)}
             :extra-data                        (str @active-tab-id)
             :horizontal                        true
             :scroll-event-throttle             64
             :shows-horizontal-scroll-indicator false
             :data                              data
             :key-fn                            (comp str :id)
             :on-scroll-to-index-failed         identity
             :on-scroll                         (partial on-scroll-handler
                                                         {:fade-end-percentage fade-end-percentage
                                                          :fade-end?           fade-end?
                                                          :fading              fading
                                                          :on-scroll           on-scroll})
             :render-fn                         (partial render-tab
                                                         {:active-tab-id    active-tab-id
                                                          :blur?            blur?
                                                          :flat-list-ref    flat-list-ref
                                                          :number-of-items  (count data)
                                                          :on-change        on-change
                                                          :scroll-on-press? scroll-on-press?
                                                          :size             size
                                                          :style            style})})]]]
        [rn/view (merge style {:flex-direction :row})
         (map-indexed (fn [index item]
                        ^{:key (:id item)}
                        [render-tab
                         {:active-tab-id   active-tab-id
                          :blur?           blur?
                          :number-of-items (count data)
                          :on-change       on-change
                          :size            size
                          :style           style}
                         item
                         index])
                      data)]))))
