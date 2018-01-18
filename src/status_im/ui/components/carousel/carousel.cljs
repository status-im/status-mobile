(ns status-im.ui.components.carousel.carousel
  (:require [reagent.impl.component :as rc]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.carousel.styles :as st]
            [taoensso.timbre :as log]
            [status-im.react-native.js-dependencies :as rn-dependencies]))


(defn window-page-width []
  (.-width (.get (.. rn-dependencies/react-native -Dimensions) "window")))

(def defaults {:gap 8
               :sneak 8
               :pageStyle {}
               :scrollThreshold 20})

(defn get-active-page [data]
  (get data :activePage 0))

(defn get-sneak [{:keys [sneak gap count]}]
  (if (> (or count 2) 1)
    (or sneak (:sneak defaults))
    gap))

(defn get-gap [data]
  (get data :gap (:gap defaults)))

(defn get-count [data]
  (get data :count))

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
        gap (quot (- (window-page-width)
                     (* 2 sneak)
                     page-width)
                  2)
        count (get-count props)]
    (reagent.core/set-state component {:sneak sneak
                                       :pageWidth page-width
                                       :pageStyle style
                                       :gap gap
                                       :count count})))

(defn scroll-to [component x y]
  (.scrollTo (.-scrollView component) (clj->js {:y y
                                                :x x})))

(defn get-current-position [event]
  (.-x (.-contentOffset (.-nativeEvent event))))

(defn get-page-position [state page]
  (let [page-width (get-page-width state)
        gap (get-gap state)
        sneak (get-sneak state)
        count (get-count state)
        addition (condp = page
                   0 gap
                   (dec count) (- (* 3 gap) (* sneak 2))
                   (- sneak (* gap 2)))]
    (+ (* page page-width)
       (* (dec page) gap)
       addition)))

(defn go-to-page [component page]
  (let [props (reagent.core/props component)
        state (reagent.core/state component)
        page-width (get-page-width state)
        gap (get-gap state)
        page-position (get-page-position state page)]
    (log/debug "go-to-page: props-page-width=" page-width "; gap=" gap
               "; page-position=" page-position "; page: " page)
    (reagent.core/set-state component {:scrolling? true})
    (js/setTimeout #(reagent.core/set-state component {:scrolling? false}) 200)
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
        page-count (get-count state)
        direction (cond
                    (> current-position (+ starting-position scroll-threshold)) 1
                    (< current-position (- starting-position scroll-threshold)) -1
                    :else 0)
        new-page (+ current-page direction)]
    (log/debug state "on-scroll-end: starting position=" starting-position
               "; current-position=" current-position "; direction=" direction
               "; current-page=" current-page "; new-page=" new-page)
    (if (and (not= current-page new-page)
             (< -1 new-page page-count))
      (go-to-page component new-page)
      (scroll-to component starting-position 0))))

(defn component-will-mount [component new-args]
  (let [props (reagent.core/props component)]
    (log/debug "component-will-mount: new-args=" new-args)
    (apply-props component props)))

(defn component-did-mount [component]
  (let [props (reagent.core/props component)
        initial-page (.-initialPage props)]
    (log/debug "component-did-mount: initial-page="initial-page)
    (when (pos? initial-page)
      (go-to-page component initial-page))))

(defn component-will-receive-props [component new-argv]
  (let [props (rc/extract-props new-argv)]
    (log/debug "component-will-receive-props: props=" props)
    (apply-props component props)))

(defn get-event-width [event]
  (.-width (.-layout (.-nativeEvent event))))

(defn on-layout-change [event component]
  (let [state (reagent.core/state component)
        page-width (compute-page-width (get-event-width event)
                                       (get-gap state)
                                       (get-sneak state))
        state-page-width (get-page-width state)
        active-page (get-active-page state)
        gap (get-gap state)
        page-position (* active-page (+ page-width gap))]
    (log/debug "Layout changed: " " page-width=" page-width "; state-page-width=" state-page-width)
    (if (not= page-width state-page-width)
      (do
        (reagent.core/set-state component {:pageWidth page-width})
        (.setState component (clj->js {:layout (.-layout (.-nativeEvent event))})))
      (scroll-to component page-position 0))))

(defn get-pages [component data children]
  (let [page-width (get-page-width data)
        page-style (get-page-style data)
        gap (get-gap data)
        count (get-count data)]
    (doall (map-indexed (fn [index child]
                         (let [page-index index
                               touchable-data {:key index
                                               :onPress #(go-to-page component page-index)}]
                           [react/touchable-without-feedback touchable-data
                            [react/view {:style [(st/page index count page-width gap)
                                                 page-style]
                                         :onLayout #(log/debug "view onLayout" %)}

                             child]])) children))))

(defn reagent-render [data children]
  (let [starting-position (atom 0)
        component (reagent.core/current-component)
        state (reagent.core/state component)
        gap (get-gap state)]
    (log/debug "reagent-render: " data state)
    [react/view {:style st/scroll-view-container}
     [react/scroll-view {:contentContainerStyle (st/content-container gap)
                         :automaticallyAdjustContentInsets false
                         :bounces false
                         :decelerationRate 0.9
                         :horizontal true
                         :onLayout #(on-layout-change % component)
                         :scrollEnabled (not (get state :scrolling?))
                         :onScrollBeginDrag #(reset! starting-position (get-current-position %))
                         :onScrollEndDrag #(on-scroll-end % component @starting-position)
                         :showsHorizontalScrollIndicator false
                         :ref #(set! (.-scrollView component) %)}
      (get-pages component state children)]]))

(defn carousel [_ _]
  (let [component-data {:component-did-mount component-did-mount
                        :component-will-mount component-will-mount
                        :component-will-receive-props component-will-receive-props
                        :display-name "carousel"
                        :reagent-render reagent-render}]
    (reagent.core/create-class component-data)))
