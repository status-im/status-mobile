(ns quo2.components.tabs.tabs
  (:require [oops.core :refer [oget]]
            [quo2.components.notifications.notification-dot :refer [notification-dot]]
            [quo2.components.tabs.tab :as tab]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.linear-gradient :as linear-gradient]
            [react-native.masked-view :as masked-view]
            [reagent.core :as reagent]
            [utils.collection :as utils.collection]
            [utils.number :as utils.number]))

(def default-tab-size 32)
(def unread-count-offset 3)

(defn- indicator
  []
  [rn/view
   {:accessibility-label :notification-dot
    :style               {:position         :absolute
                          :z-index          1
                          :right            (- unread-count-offset)
                          :top              (- unread-count-offset)
                          :width            10
                          :height           10
                          :border-radius    5
                          :justify-content  :center
                          :align-items      :center
                          :background-color (colors/theme-colors colors/neutral-5 colors/neutral-95)}}
   [notification-dot]])

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
  [{:keys [fading fade-end?]} & children]
  (if fade-end?
    (into [masked-view/masked-view
           {:mask-element
            (reagent/as-element
             [linear-gradient/linear-gradient
              {:colors         [:black :transparent]
               :locations      [(get @fading :fade-end-percentage) 1]
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
  [{:keys [size data style override-theme blur? active-tab-id scroll-on-press? flat-list-ref on-change]}
   {:keys [id label notification-dot? accessibility-label]}
   index]
  [rn/view
   {:style {:margin-right  (if (= size default-tab-size) 12 8)
            :padding-right (when (= index (dec (count data)))
                             (:padding-left style))}}
   (when notification-dot?
     [indicator])
   [tab/tab
    {:id                  id
     :accessibility-label accessibility-label
     :size                size
     :override-theme      override-theme
     :blur?               blur?
     :active              (= id @active-tab-id)
     :on-press            (fn [id]
                            (reset! active-tab-id id)
                            (when scroll-on-press?
                              (.scrollToIndex ^js @flat-list-ref
                                              #js
                                               {:animated     true
                                                :index        index
                                                :viewPosition 0.5}))
                            (when on-change
                              (on-change id)))}
    label]])

(defn tabs
  "Usage:
   {:type             :icon/:emoji/:label
    :component        tag/tab
    :size             32/24
    :on-press         fn
    :blurred?         true/false
    :labelled?        true/false
    :disabled?        true/false
    :scrollable?      false
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
    (fn
      [{:keys [data
               fade-end-percentage
               fade-end?
               on-change
               on-scroll
               scroll-event-throttle
               scroll-on-press?
               scrollable?
               style
               size
               blur?
               override-theme]
        :or   {fade-end-percentage   fade-end-percentage
               fade-end?             false
               scroll-event-throttle 64
               scrollable?           false
               scroll-on-press?      false
               size                  default-tab-size}
        :as   props}]
      [rn/view {:style {:margin-top (- (dec unread-count-offset))}}
       (if scrollable?
         [masked-view-wrapper {:fading fading :fade-end? fade-end?}
          [rn/flat-list
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
             ;; https://github.com/facebook/react-native/issues/3121
             :content-container-style           {:padding-top (dec unread-count-offset)}
             :extra-data                        (str @active-tab-id)
             :horizontal                        true
             :scroll-event-throttle             scroll-event-throttle
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
                                                          :data             data
                                                          :flat-list-ref    flat-list-ref
                                                          :on-change        on-change
                                                          :override-theme   override-theme
                                                          :scroll-on-press? scroll-on-press?
                                                          :size             size
                                                          :style            style})})]]
         [rn/view (merge style {:flex-direction :row})
          (doall
           (for [{:keys [label id notification-dot? accessibility-label]} data]
             ^{:key id}
             [rn/view {:style {:margin-right (if (= size default-tab-size) 12 8)}}
              (when notification-dot?
                [indicator])
              [tab/tab
               {:id                  id
                :size                size
                :accessibility-label accessibility-label
                :active              (= id @active-tab-id)
                :on-press            (fn []
                                       (reset! active-tab-id id)
                                       (when on-change
                                         (on-change id)))}
               label]]))])])))
