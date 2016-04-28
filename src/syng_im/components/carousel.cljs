(ns syng-im.components.carousel
  (:require [syng-im.components.react :refer [android?
                                              view
                                              scroll-view
                                              touchable-without-feedback
                                              text]]
            [syng-im.utils.debug :refer [log]]))


(defn page-width []
  (.-width (.get (.. js/React -Dimensions) "window")))

(def defaults {:gap 10
               :sneak 10
               :pageWidth (- (page-width) 40)})


(defn get-gap [data]
  (get data :gap (:gap defaults)))

(defn get-sneak [data]
  (get data :sneak (:sneak defaults)))

(defn get-page-width [data]
  (get data :pageWidth (:pageWidth defaults)))


(defn calculate-gap [component props]
  (let [prop-page-width (:pageWidth props)
        sneak (get-sneak props)
        _ (log "calculate-gap")
        _ (log component)
        _ (log props)
        _ (log prop-page-width)
        _ (log sneak)]
    (if (> prop-page-width (page-width))
      (println "Invalid pageWidth"))
    (reagent.core/set-state component {:gap (/ (- (- (page-width) (* 2 sneak)) prop-page-width) 2)})
    ))

(defn scroll-to [component x y]
  (.scrollTo (.-scrollView component) (clj->js {:y y
                                                :x x})))

(defn on-scroll-end [event component]
  (let [_ (log "on-scroll-end")
        _ (log event)
        _ (log component)
        props (reagent.core/props component)
        _ (log props)
        state (reagent.core/state component)
        _ (log state)
        prop-page-width (get-page-width props)
        _ (log (str "prop-page-width: " prop-page-width))
        sneak (get-sneak props)
        _ (log (str "sneak: " sneak))
        gap (get-gap state)
        _ (log (str "gap: " gap))
        page-offset (+ prop-page-width gap)
        _ (log (str "page-offset: " page-offset))
        current-position (+ (.-x (.-contentOffset (.-nativeEvent event))) (/ (page-width) 2))
        _ (log (str "current-position: " current-position))
        current-page (quot current-position page-offset)
        _ (log (str "current-page: " current-page))
        ]
    (scroll-to component (* current-page page-offset) 0)
    (reagent.core/set-state component {:activePage current-page})
    (if (:onPageChange props)
      ((:onPageChange props) current-page))))

(defn go-to-page [component position]
  (let [_ (log "go-to-page")
        props (reagent.core/props component)
        state (reagent.core/state component)
        props-page-width (get-page-width props)
        _ (log (str "props page width: " props-page-width))
        gap (get-gap state)
        _ (log (str "gap: " gap))
        page-position (* position (+ props-page-width gap))
        _ (log (str "page position: " page-position))]
    (scroll-to component page-position 0)))

(defn carousel [data children]
  (let [_ (log "Creating carousel component: ")
        _ (log data)
        _ (log children)]
    (reagent.core/create-class {:component-did-mount (fn [this]
                                                       (let [_ (log this)]
                                                       (if (> (.-initialPage (.-props this)) 0)
                                                         (go-to-page this (.-initialPage (.-props this))))))

                                :component-will-mount (fn [this]
                                                        (let [_ (log "component-will-mount")
                                                              _ (log this)]
                                                        (calculate-gap this (reagent.core/props this))))
                                :component-will-receive-props (fn [this new-argv]
                                                                (let [_ (log "component-will-receive-props-mount")
                                                                      _ (log this)
                                                                      _ (log new-argv)]
                                                                (calculate-gap this new-argv)))
                                :display-name "carousel1"
                                :reagent-render (fn [data children]
                                                  (let [_ (log "reagent-render")
                                                        _ (log data)
                                                        _ (log children)
                                                        component (reagent.core/current-component)
                                                        props-page-width (get-page-width data)
                                                        sneak (get-sneak data)
                                                        gap (get-gap data)
                                                        page-style (get data :pageStyle {})
                                                        pages (map-indexed (fn [index child]
                                                                             (let [page-index index
                                                                                   _ (log (str "page index" index "-" page-index))
                                                                                   _ (log child)]
                                                                             [touchable-without-feedback {:key index
                                                                                                          :onPress (fn [a b c]
                                                                                                                     (let [_ (log "touchable pressed")
                                                                                                                           _ (log (.-target a))
                                                                                                                           _ (log component)
                                                                                                                           _ (log page-index)]
                                                                                                                       (go-to-page component page-index)))
                                                                                                          }
                                                                              [view {:style [{:width props-page-width
                                                                                             :justifyContent "center"
                                                                                             :marginLeft (/ gap 2)
                                                                                             :marginRight (/ gap 2)}
                                                                                             page-style]}
                                                                               child]])) children)]

                                                      [view {:style {:flex 1}}
                                                       [scroll-view {:contentContainerStyle {:paddingLeft (+ sneak (/ gap 2))
                                                                                             :paddingRight (+ sneak (/ gap 2))}
                                                                     :automaticallyAdjustContentInsets false
                                                                     :bounces false
                                                                     :decelerationRate 0.9
                                                                     :horizontal true
                                                                     :onScrollEndDrag (fn [event] (on-scroll-end event component))
                                                                     :showsHorizontalScrollIndicator false
                                                                     :ref (fn [c] (set! (.-scrollView component) c))
                                                                 }
                                                    pages]]))})))

(comment


  )