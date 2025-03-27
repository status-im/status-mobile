(ns quo.components.slideshow.slider-bar.view
  (:require
    [quo.components.slideshow.slider-bar.schema :as component-schema]
    [quo.components.slideshow.slider-bar.style :as style]
    [quo.context]
    [react-native.core :as rn]
    [react-native.reanimated :as reanimated]
    [schema.core :as schema]))

(def item-size 8)
(def item-spacing item-size)
(def max-length 6)
(def bar-width (* (+ item-spacing item-size) max-length))

(def micro-scale 0.25)
(def small-scale 0.5)
(def medium-scale 0.75)
(def default-scale 1)

(def items-before-scroll
  (/ max-length 2))

(defn- calc-relative-index
  "Calculates item index in visible area"
  [index active-index total-length]
  (let [last-index (dec total-length)]
    (cond
      (< active-index items-before-scroll)   index
      (> active-index
         (- last-index items-before-scroll)) (+ (- index total-length) max-length)
      :else                                  (+ (- index active-index) items-before-scroll))))

(defn- calc-first-item-scale
  "Calculates scale for first item in visible area"
  [active-index]
  (cond
    (= active-index items-before-scroll) medium-scale
    (> active-index items-before-scroll) small-scale
    :else                                default-scale))

(defn- calc-second-item-scale
  "Calculates scale for second item in visible area"
  [active-index]
  (if (> active-index items-before-scroll)
    medium-scale
    default-scale))

(defn- calc-last-item-scale
  "Calculates scale for last item in visible area"
  [active-index total-length]
  (let [last-index (dec total-length)]
    (if (> active-index (- last-index items-before-scroll))
      medium-scale
      small-scale)))

(defn- calc-last-but-one-item-scale
  "Calculates scale for before last item in visible area"
  [active-index total-length]
  (if (> active-index (- (dec total-length) items-before-scroll))
    default-scale
    medium-scale))

(defn- get-scale
  [index active-index total-length]
  (let [relative-index (calc-relative-index index active-index total-length)]
    (if (or (= index active-index)
            (< total-length max-length))
      default-scale
      (cond
        (< relative-index 0) micro-scale
        (= relative-index 0) (calc-first-item-scale active-index)
        (= relative-index 1) (calc-second-item-scale active-index)
        (= relative-index 4) (calc-last-but-one-item-scale active-index total-length)
        (= relative-index 5) (calc-last-item-scale active-index total-length)
        (> relative-index 5) micro-scale
        :else
        default-scale))))

(defn- bar-item
  [index _ _ {:keys [active-index total-length customization-color theme blur?]}]
  (let [active?      (= index active-index)
        shared-scale (reanimated/use-shared-value (get-scale index active-index total-length))]
    (rn/use-effect (fn []
                     (let [new-scale-value (get-scale index active-index total-length)]
                       (reanimated/animate shared-scale new-scale-value)))
                   [index active-index total-length])
    [rn/view
     {:key   index
      :style (style/item-wrapper {:size    item-size
                                  :spacing item-spacing})}
     [reanimated/view
      {:accessibility-label :slide-bar-item
       :style
       (reanimated/apply-animations-to-style
        {:transform [{:scale shared-scale}]}
        (style/item {:size                item-size
                     :spacing             item-spacing
                     :active?             active?
                     :customization-color customization-color
                     :theme               theme
                     :blur?               blur?}))}]]))

(defn- get-item-layout
  [_ index]
  (let [length (+ item-size item-spacing)]
    #js {:length length
         :index  index
         :offset (* index length)}))

(defn- view-internal
  [{:keys [customization-color blur? accessibility-label container-style]
    :as   props}]
  (let [active-index      (or (:active-index props) 0)
        total-amount      (or (:total-amount props) 1)
        theme             (quo.context/use-theme)
        flat-list-ref     (rn/use-ref-atom nil)
        set-flat-list-ref (rn/use-callback #(reset! flat-list-ref %))
        center-position   0.5
        data              (range total-amount)
        scroll-to-index   (rn/use-callback
                           (fn []
                             (some-> ^js @flat-list-ref
                                     (.scrollToIndex #js {:animated     true
                                                          :index        active-index
                                                          :viewOffset   item-spacing
                                                          :viewPosition center-position})))
                           [flat-list-ref active-index])]
    (rn/use-effect scroll-to-index [active-index])
    [rn/view
     {:style               (merge style/list-wrapper container-style)
      :accessibility-label accessibility-label}
     [reanimated/flat-list
      {:style                             (style/list-bar bar-width)
       :data                              data
       :scroll-enabled                    false
       :ref                               set-flat-list-ref
       :shows-horizontal-scroll-indicator false
       :bounces                           false
       :horizontal                        true
       :extra-data                        (str active-index)
       :render-data                       {:active-index        active-index
                                           :total-length        total-amount
                                           :customization-color customization-color
                                           :theme               theme
                                           :blur?               blur?}
       :get-item-layout                   get-item-layout
       :render-fn                         bar-item
       :key-fn                            identity}]]))

(def view (schema/instrument #'view-internal component-schema/?schema))
