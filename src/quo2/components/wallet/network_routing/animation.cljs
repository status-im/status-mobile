(ns quo2.components.wallet.network-routing.animation
  (:require [react-native.reanimated :as reanimated]))

(def ^:private slider-timing 300)

(defn show-slider
  [opacity-shared-value]
  (reanimated/animate opacity-shared-value 1 slider-timing))

(defn hide-slider
  [opacity-shared-value]
  (reanimated/animate opacity-shared-value 0 slider-timing))

(defn increase-slider
  [width-shared-value height-shared-value]
  (reanimated/animate width-shared-value 8 slider-timing)
  (reanimated/animate height-shared-value 40 slider-timing))

(defn decrease-slider
  [width-shared-value height-shared-value]
  (reanimated/animate width-shared-value 4 slider-timing)
  (reanimated/animate height-shared-value 32 slider-timing))

(def ^:private pressed-bar-timing 600)

(defn move-previous-bars
  [{:keys [bars bars-widths-negative]}]
  (doseq [[bar-idx bar] (map-indexed vector bars)
          :let          [new-translation-x (->> (take (inc bar-idx) bars-widths-negative)
                                                (reduce +)
                                                (dec))]]
    (reanimated/animate (:translate-x-shared-value bar)
                        new-translation-x
                        pressed-bar-timing)))

(defn move-pressed-bar
  [{:keys                              [bars-widths-negative number-previous-bars]
    {:keys [translate-x-shared-value]} :bar}]
  (let [new-translation-x (reduce + (take number-previous-bars bars-widths-negative))]
    (reanimated/animate translate-x-shared-value
                        new-translation-x
                        (- pressed-bar-timing 20))))

(defn move-next-bars
  [{:keys [bars bars-widths-negative number-previous-bars extra-offset add-new-timeout]}]
  (doseq [[bar-idx bar] (map-indexed vector bars)
          :let          [number-bars-before (+ number-previous-bars
                                               (inc bar-idx))
                         new-translation-x  (->> (take number-bars-before bars-widths-negative)
                                                 (reduce +)
                                                 (* 1.05))]]
    (reanimated/animate (:translate-x-shared-value bar) new-translation-x pressed-bar-timing)
    (add-new-timeout
     :fix-next-bars-position
     #(let [translate-x-value (reanimated/get-shared-value (:translate-x-shared-value bar))
            hidden-position   (- translate-x-value extra-offset)]
        (reanimated/set-shared-value (:translate-x-shared-value bar) hidden-position))
     pressed-bar-timing)))

(def ^:private max-limit-bar-timing 300)

(defn show-max-limit-bar
  [max-limit-bar-opacity]
  (reanimated/animate max-limit-bar-opacity 1 max-limit-bar-timing))

(defn hide-max-limit-bar
  [max-limit-bar-opacity]
  (reanimated/animate max-limit-bar-opacity 0 max-limit-bar-timing))

(defn reset-bars-positions
  [bars unlock-press-fn add-new-timeout]
  (let [bars-reset-timing 500]
    (doseq [{:keys [translate-x-shared-value]} bars]
      (reanimated/animate translate-x-shared-value 0 bars-reset-timing))
    (add-new-timeout :unlock-press unlock-press-fn bars-reset-timing)))

(defn align-bars-off-screen
  [{:keys [new-network-values network-bars amount->width add-new-timeout]}]
  (let [width-to-off-screen (->> new-network-values
                                 (reduce #(- %1 (:amount %2)) 0)
                                 (amount->width))]
    (doseq [[{new-amount :amount} bar] (map vector new-network-values network-bars)]
      (reanimated/set-shared-value (:amount-shared-value bar) new-amount)
      (reanimated/set-shared-value (:translate-x-shared-value bar) (* 2 width-to-off-screen))
      (add-new-timeout
       :align-bars
       #(reanimated/set-shared-value (:translate-x-shared-value bar) width-to-off-screen)
       1))))

(def ^:private hide-bar-timing 400)

(defn hide-pressed-bar
  [{{:keys [translate-x-shared-value
            amount-shared-value]} :bar
    amount->width                 :amount->width}]
  (let [bar-width         (amount->width (reanimated/get-shared-value amount-shared-value))
        new-translation-x (- (reanimated/get-shared-value translate-x-shared-value)
                             bar-width)]
    (reanimated/animate translate-x-shared-value new-translation-x hide-bar-timing)))

(defn update-bar-values-and-reset-animations
  [{:keys [new-network-values network-bars amount->width reset-values-fn add-new-timeout
           lock-press-fn unlock-press-fn]}]
  (lock-press-fn)
  (add-new-timeout
   :update-bars-values
   (fn []
     (align-bars-off-screen {:new-network-values new-network-values
                             :network-bars       network-bars
                             :amount->width      amount->width
                             :add-new-timeout    add-new-timeout})
     (reset-values-fn)
     (add-new-timeout :reset-bars
                      #(reset-bars-positions network-bars unlock-press-fn add-new-timeout)
                      100))
   hide-bar-timing))
