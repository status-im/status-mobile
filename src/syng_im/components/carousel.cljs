(ns syng-im.components.carousel
  (:require [syng-im.components.react :refer [android?
                                              view
                                              scroll-view
                                              touchable-without-feedback
                                              text]]
            [syng-im.utils.logging :as log]))


(defn page-width []
  (.-width (.get (.. js/React -Dimensions) "window")))

(def defaults {:gap 10
               :sneak 10
               :pageWidth (- (page-width) 40)
               :scrollThreshold 20})


(defn get-gap [data]
  (get data :gap (:gap defaults)))

(defn get-sneak [data]
  (get data :sneak (:sneak defaults)))

(defn get-page-width [data]
  (get data :pageWidth (:pageWidth defaults)))

(defn get-scroll-threshold [data]
  (get data :scrollThreshold (:scrollThreshold defaults)))

(defn calculate-gap [component props]
  (let [prop-page-width (get-page-width props)
        page-width (page-width)
        sneak (get-sneak props)
        gap (quot (- (- page-width (* 2 sneak)) prop-page-width) 2)]
    (log/debug "calculate-gap: prop-page-width=" prop-page-width
               "; page-width=" page-width"; sneak=" sneak "; gap=" gap)
    (when (> prop-page-width page-width)
      (log/warn "Invalid pageWidth"))
    (reagent.core/set-state component {:gap gap})
    ))

(defn scroll-to [component x y]
  (.scrollTo (.-scrollView component) (clj->js {:y y
                                                :x x})))

(defn get-current-position [event]
  (.-x (.-contentOffset (.-nativeEvent event))))

(defn on-scroll-end [event component starting-position]
  (let [props (reagent.core/props component)
        state (reagent.core/state component)
        prop-page-width (get-page-width props)
        sneak (get-sneak props)
        scroll-threshold (get-scroll-threshold props)
        gap (get-gap state)
        page-offset (+ prop-page-width gap)
        current-position (get-current-position event)
        direction (if (> current-position (+ starting-position scroll-threshold))
                    1
                    (if (< current-position (- starting-position scroll-threshold))
                      -1
                      0))
        current-page (+ (quot starting-position page-offset) direction)
        ]
    (log/debug "on-scroll-end: prop-page-width=" prop-page-width
               "; sneak=" sneak "; gap=" gap "; page-offset=" page-offset
               "; starting position=" starting-position
               "; current-position=" current-position
               "; direction=" direction "; current-page=" current-page)
    (scroll-to component (* current-page page-offset) 0)
    (reagent.core/set-state component {:activePage current-page})
    (when (:onPageChange props)
      ((:onPageChange props) current-page))))

(defn go-to-page [component position]
  (let [props (reagent.core/props component)
        state (reagent.core/state component)
        props-page-width (get-page-width props)
        gap (get-gap state)
        page-position (* position (+ props-page-width gap))]
    (log/debug "go-to-page: props-page-width=" props-page-width "; gap=" gap
               "; page-position=" page-position)
    (scroll-to component page-position 0)))

(defn component-will-mount [component new-args]
  (let [props (reagent.core/props component)]
    (log/debug "component-will-mount: component=" component "; new-args="new-args)
    (calculate-gap component props)))

(defn component-did-mount [component]
  (let [props (reagent.core/props component)
        initial-page (.-initialPage props)]
    (log/debug "component-did-mount: initial-page="initial-page)
    (when (pos? initial-page)
      (go-to-page component initial-page))))

(defn component-will-receive-props [component new-argv]
  (log/debug "component-will-receive-props: component=" component
             "; new-argv=" new-argv)
  (calculate-gap component new-argv))

(defn get-pages [component data children]
  (let [props-page-width (get-page-width data)
        page-style (get data :pageStyle {})
        gap (get-gap data)
        margin (quot gap 2)]
    (map-indexed (fn [index child]
                   (let [page-index index
                         touchable-data {:key index
                                         :onPress (fn [event]
                                                    (go-to-page component page-index))}]
                     (log/debug "page " index " - " child)
                     [touchable-without-feedback touchable-data
                      [view {:style [{:width props-page-width
                                      :justifyContent "center"
                                      :marginLeft margin
                                      :marginRight margin}
                                     page-style]}
                       child]])) children)))

(defn reagent-render [data children]
  (let [starting-position (atom 0)
        component (reagent.core/current-component)
        sneak (get-sneak data)
        gap (get-gap data)
        pages (get-pages component data children)]
  (log/debug "reagent-render: ")
  [view {:style {:flex 1}}
   [scroll-view {:contentContainerStyle {:paddingLeft (+ sneak (quot gap 2))
                                         :paddingRight (+ sneak (quot gap 2))}
                 :automaticallyAdjustContentInsets false
                 :bounces false
                 :decelerationRate 0.9
                 :horizontal true
                 :onScrollBeginDrag (fn [event]
                                      (let []
                                        (reset! starting-position (get-current-position event))))
                 :onScrollEndDrag (fn [event] (on-scroll-end event component @starting-position))
                 :showsHorizontalScrollIndicator false
                 :ref (fn [c] (set! (.-scrollView component) c))
                 }
    pages]]
  ))

(defn carousel [data children]
  (let [component-data {:component-did-mount component-did-mount
                        :component-will-mount component-will-mount
                        :component-will-receive-props component-will-receive-props
                        :display-name "carousel"
                        :reagent-render reagent-render}]
    (log/debug "Creating carousel component: " data children)
    (reagent.core/create-class component-data)))
