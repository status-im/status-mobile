(ns quo.components.tabs.tabs.view
  (:require
    [oops.core :refer [oget]]
    [quo.components.tabs.tab.view :as tab]
    [quo.components.tabs.tabs.schema :as component-schema]
    [quo.components.tabs.tabs.style :as style]
    [react-native.core :as rn]
    [react-native.gesture :as gesture]
    [react-native.linear-gradient :as linear-gradient]
    [react-native.masked-view :as masked-view]
    [reagent.core :as reagent]
    [schema.core :as schema]
    [utils.collection :as utils.collection]
    [utils.number]))

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
      max-fade-percentage)))

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
               :style          style/linear-gradient}])}]
          children)
    (into [:<>] children)))

(defn- on-scroll-handler
  [{:keys [on-scroll fading set-fading fade-end-percentage fade-end?]} ^js e]
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
        (set-fading new-percentage))))
  (when on-scroll
    (on-scroll e)))

(defn- tab-view
  [{:keys [id label notification-dot? accessibility-label]}
   index _
   {:keys [active-tab-id
           blur?
           customization-color
           number-of-items
           on-press
           size
           style]}]
  [rn/view
   {:style (style/tab {:size             size
                       :default-tab-size default-tab-size
                       :number-of-items  number-of-items
                       :index            index
                       :style            style})}
   [tab/view
    {:id                  id
     :notification-dot?   notification-dot?
     :customization-color customization-color
     :accessibility-label accessibility-label
     :size                size
     :blur?               blur?
     :active              (= id active-tab-id)
     :on-press            #(on-press % index)}
    label]])

(defn- view-internal
  [{:keys [default-active data fade-end-percentage fade-end? on-change on-scroll scroll-on-press?
           scrollable? style container-style size blur? in-scroll-view? customization-color
           active-tab-id]
    :or   {fade-end-percentage 0.8
           fade-end?           false
           scrollable?         false
           scroll-on-press?    false
           size                default-tab-size}
    :as   props}]
  (let [[active-tab-internal-id
         set-active-tab-internal-id] (rn/use-state default-active)
        tab-id                       (or active-tab-id active-tab-internal-id)

        [fading set-fading]          (rn/use-state fade-end-percentage)
        flat-list-ref                (rn/use-ref-atom nil)
        tabs-data                    (rn/use-memo (fn [] (filterv some? data))
                                                  [data])
        clean-props                  (dissoc props
                                      :default-active
                                      :fade-end-percentage
                                      :fade-end?
                                      :on-change
                                      :scroll-on-press?
                                      :size)
        on-scroll                    (rn/use-callback
                                      (partial on-scroll-handler
                                               {:fade-end-percentage fade-end-percentage
                                                :fade-end?           fade-end?
                                                :fading              fading
                                                :set-fading          set-fading
                                                :on-scroll           on-scroll})
                                      [fade-end? fading])
        set-initial-scroll-poisition (rn/use-callback
                                      (fn []
                                        (reagent/next-tick
                                         (fn []
                                           (.scrollToIndex
                                            @flat-list-ref
                                            #js
                                             {:animated false
                                              :index
                                              (utils.collection/first-index
                                               #(= tab-id (:id %))
                                               tabs-data)}))))
                                      [tab-id tabs-data])
        on-tab-press                 (rn/use-callback (fn [id index]
                                                        (set-active-tab-internal-id id)
                                                        (when (and scroll-on-press? @flat-list-ref)
                                                          (.scrollToIndex ^js @flat-list-ref
                                                                          #js
                                                                           {:animated     true
                                                                            :index        index
                                                                            :viewPosition 0.5}))
                                                        (when on-change
                                                          (on-change id)))
                                                      [set-active-tab-internal-id scroll-on-press?
                                                       on-change])]
    (if scrollable?
      [rn/view {:style {:margin-top (- (dec unread-count-offset))}}
       [masked-view-wrapper
        {:fade-end-percentage fading :fade-end? fade-end?}
        [(if in-scroll-view?
           gesture/flat-list
           rn/flat-list)
         (merge
          clean-props
          {:ref #(reset! flat-list-ref %)
           :style style
           ;; The padding-top workaround is needed because on Android
           ;; {:overflow :visible} doesn't work on components inheriting
           ;; from ScrollView (e.g. FlatList). There are open issues, here's just one about this
           ;; topic: Https://github.com/facebook/react-native/issues/31218
           :content-container-style
           (assoc container-style :padding-top (dec unread-count-offset))
           :horizontal true
           :scroll-event-throttle 64
           :shows-horizontal-scroll-indicator false
           :data tabs-data
           :key-fn (comp str :id)
           :on-scroll-to-index-failed identity
           :on-scroll on-scroll
           :on-layout set-initial-scroll-poisition
           :render-fn tab-view
           :render-data {:active-tab-id       tab-id
                         :blur?               blur?
                         :customization-color customization-color
                         :number-of-items     (count tabs-data)
                         :size                size
                         :on-press            on-tab-press
                         :style               style}})]]]
      [rn/view (merge style {:flex-direction :row})
       (map-indexed (fn [index item]
                      ^{:key (:id item)}
                      [tab-view item index nil
                       {:active-tab-id       tab-id
                        :blur?               blur?
                        :customization-color customization-color
                        :number-of-items     (count tabs-data)
                        :size                size
                        :on-press            on-tab-press
                        :style               style}])
                    tabs-data)])))

(def view (schema/instrument #'view-internal component-schema/?schema))
