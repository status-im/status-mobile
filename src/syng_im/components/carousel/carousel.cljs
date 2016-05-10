(ns syng-im.components.carousel
  (:require [syng-im.components.react :refer [android?
                                              view
                                              scroll-view
                                              touchable-without-feedback
                                              text]]
            [syng-im.components.carousel.styles :as st]
            [syng-im.utils.logging :as log]))


(defn window-page-width []
  (.-width (.get (.. js/React -Dimensions) "window")))

(def defaults {:gap 10
               :sneak 10
               :pageStyle {}
               :scrollThreshold 20})

(defn get-active-page [data]
  (get data :activePage 0))

(defn get-sneak [data]
  (get data :sneak (:sneak defaults)))

(defn get-gap [data]
  (get data :gap (:gap defaults)))

(defn compute-page-width
  ([gap sneak]
   (compute-page-width (window-page-width) gap sneak))
  ([window-page-width gap sneak]
   (- window-page-width (+ (* 2 gap) (* 2 sneak)))))

(defn get-page-width [data]
  (get data :pageWidth (compute-page-width (get-gap data) (get-sneak data))))

(defn get-page-style [data]
  (let [data-style (get data :pageStyle {})]
    (merge (:pageStyle defaults) data-style)))

(defn get-scroll-threshold [data]
  (get data :scrollThreshold (:scrollThreshold defaults)))

(defn apply-props [component props]
  (let [sneak (get-sneak props)
        page-width (get-page-width props)
        style (get-page-style props)
        gap (quot (- (- (window-page-width) (* 2 sneak)) page-width) 2)]
    (reagent.core/set-state component {:sneak sneak
                                       :pageWidth page-width
                                       :pageStyle style
                                       :gap gap})))

(defn scroll-to [component x y]
  (.scrollTo (.-scrollView component) (clj->js {:y y
                                                :x x})))

(defn get-current-position [event]
  (.-x (.-contentOffset (.-nativeEvent event))))

(defn go-to-page [component page]
  (let [props (reagent.core/props component)
        state (reagent.core/state component)
        page-width (get-page-width state)
        gap (get-gap state)
        page-position (* page (+ page-width gap))]
    (log/debug "go-to-page: props-page-width=" page-width "; gap=" gap
               "; page-position=" page-position)
    (scroll-to component page-position 0)
    (reagent.core/set-state component {:activePage page})
    (when (:onPageChange props)
      ((:onPageChange props) page))))

(defn on-scroll-end [event component starting-position]
  (let [props (reagent.core/props component)
        state (reagent.core/state component)
        scroll-threshold (get-scroll-threshold props)
        current-page (get-active-page state)
        current-position (get-current-position event)
        direction (cond
                    (> current-position (+ starting-position scroll-threshold)) 1
                    (< current-position (- starting-position scroll-threshold)) -1
                    :else 0)
        new-page (+ current-page direction)
        ]
    (log/debug state "on-scroll-end: starting position=" starting-position
               "; current-position=" current-position "; direction=" direction
               "; current-page=" current-page "; new-page=" new-page)
    (if (not= current-page new-page)
      (go-to-page component new-page)
      (scroll-to component starting-position 0))))

(defn component-will-mount [component new-args]
  (let [props (reagent.core/props component)]
    (log/debug "component-will-mount: new-args="new-args)
    (apply-props component props)))

(defn component-did-mount [component]
  (let [props (reagent.core/props component)
        initial-page (.-initialPage props)]
    (log/debug "component-did-mount: initial-page="initial-page)
    (when (pos? initial-page)
      (go-to-page component initial-page))))

(defn component-will-update [component new-argv]
  (log/debug "component-will-update: "))

(defn component-did-update [component old-argv]
  (log/debug "component-did-update"))

(defn component-will-receive-props [component new-argv]
  (log/debug "component-will-receive-props: new-argv=" new-argv)
  (apply-props component new-argv))

(defn get-event-width [event]
  (.-width (.-layout (.-nativeEvent event))))

(defn on-layout-change [event component]
  (let [state (reagent.core/state component)
        page-width (compute-page-width (get-event-width event) (get-gap state) (get-sneak state))
        state-page-width (get-page-width state)
        active-page (get-active-page state)
        gap (get-gap state)
        page-position (* active-page (+ page-width gap))]
    (log/debug "Layout changed: " " page-width=" page-width "; state-page-width=" state-page-width)
    (if (not= page-width state-page-width)
      (do
        (reagent.core/set-state component {:pageWidth page-width})
        (.setState component {:layout (.-layout (.-nativeEvent event))})
        )
      (scroll-to component page-position 0))))

(defn get-pages [component data children]
  (let [page-width (get-page-width data)
        page-style (get-page-style data)
        gap (get-gap data)
        margin (quot gap 2)]
    (doall (map-indexed (fn [index child]
                   (let [page-index index
                         touchable-data {:key index
                                         :onPress #(go-to-page component page-index)}]
                     [touchable-without-feedback touchable-data
                      [view {:style [(st/page page-width margin)
                                     page-style]
                             :onLayout #(log/debug "view onLayout" %)}

                       child]])) children))))

(defn reagent-render [data children]
  (let [starting-position (atom 0)
        component (reagent.core/current-component)
        state (reagent.core/state component)
        sneak (get-sneak state)
        gap (get-gap state)]
    (log/debug "reagent-render: " data state)
    [view {:style st/scroll-view-container}
     [scroll-view {:contentContainerStyle (st/content-container sneak gap)
                   :automaticallyAdjustContentInsets false
                   :bounces false
                   :decelerationRate 0.9
                   :horizontal true
                   :onLayout #(on-layout-change % component)
                   :onScrollBeginDrag #(reset! starting-position (get-current-position %))
                   :onScrollEndDrag #(on-scroll-end % component @starting-position)
                   :showsHorizontalScrollIndicator false
                   :ref #(set! (.-scrollView component) %)}
      (get-pages component state children)]]))

(defn carousel [data children]
  (let [component-data {:component-did-mount component-did-mount
                        :component-will-mount component-will-mount
                        :component-will-receive-props component-will-receive-props
                        :component-will-update component-will-update
                        :component-did-update component-did-update
                        :display-name "carousel"
                        :reagent-render reagent-render}]
    (log/debug "Creating carousel component: " data)
    (reagent.core/create-class component-data)))
