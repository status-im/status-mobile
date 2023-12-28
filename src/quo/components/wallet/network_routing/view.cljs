(ns quo.components.wallet.network-routing.view
  (:require
    [oops.core :as oops]
    [quo.components.wallet.network-routing.animation :as animation]
    [quo.components.wallet.network-routing.style :as style]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.gesture :as gesture]
    [react-native.reanimated :as reanimated]
    [reagent.core :as reagent]
    [utils.number]))

(def ^:private timeouts (atom {}))

(defn- add-new-timeout
  [k f ms]
  (letfn [(exec-fn-and-remove-timeout []
            (f)
            (swap! timeouts dissoc k))]
    (js/clearTimeout (k @timeouts))
    (swap! timeouts assoc k (js/setTimeout exec-fn-and-remove-timeout ms))))

(defn- f-slider
  [slider-shared-values]
  [rn/view {:style style/slider-container}
   [reanimated/view {:style (style/slider slider-shared-values)}]])

(defn f-network-bar
  [_]
  (let [detecting-gesture?      (reagent/atom false)
        amount-on-gesture-start (atom 0)]
    (fn [{:keys                [total-width total-amount on-press on-new-amount allow-press?]
          {:keys [amount-shared-value
                  max-amount]} :bar
          :as                  props}]
      (let [slider-width-shared-value   (reanimated/use-shared-value 4)
            slider-height-shared-value  (reanimated/use-shared-value 32)
            slider-opacity-shared-value (reanimated/use-shared-value 0)
            network-bar-shared-value    (reanimated/interpolate amount-shared-value
                                                                [0 total-amount]
                                                                [0 total-width])
            width->amount               #(/ (* % total-amount) total-width)]
        [rn/touchable-without-feedback
         {:on-press (fn []
                      (when (and (not @detecting-gesture?) allow-press?)
                        (on-press)
                        (reset! detecting-gesture? true)
                        (animation/show-slider slider-opacity-shared-value)))}
         [reanimated/view
          {:style               (style/network-bar props network-bar-shared-value)
           :accessibility-label :network-routing-bar}
          [gesture/gesture-detector
           {:gesture
            (-> (gesture/gesture-pan)
                (gesture/enabled @detecting-gesture?)
                (gesture/on-begin
                 (fn [_]
                   (animation/increase-slider slider-width-shared-value slider-height-shared-value)
                   (reset! amount-on-gesture-start (reanimated/get-shared-value amount-shared-value))))
                (gesture/on-update
                 (fn [event]
                   (let [new-amount (-> (oops/oget event "translationX")
                                        (width->amount)
                                        (+ @amount-on-gesture-start)
                                        (utils.number/value-in-range 1 max-amount))]
                     (reanimated/set-shared-value amount-shared-value new-amount))))
                (gesture/on-finalize
                 (fn [_]
                   (animation/decrease-slider slider-width-shared-value slider-height-shared-value)
                   (animation/hide-slider slider-opacity-shared-value)
                   (on-new-amount (reanimated/get-shared-value amount-shared-value))
                   (add-new-timeout :turn-off-gesture #(reset! detecting-gesture? false) 20))))}
           [:f> f-slider
            {:width-shared-value   slider-width-shared-value
             :height-shared-value  slider-height-shared-value
             :opacity-shared-value slider-opacity-shared-value}]]]]))))

(defn- add-bar-shared-values
  [{:keys [amount] :as network}]
  (assoc network
         :amount-shared-value      (reanimated/use-shared-value amount)
         :translate-x-shared-value (reanimated/use-shared-value 0)))

(def ^:private get-negative-amount
  (comp - reanimated/get-shared-value :amount-shared-value))

(defn- dashed-line
  [network-name]
  (into [rn/view {:style style/dashed-line}]
        (take 19
              (interleave (repeat [rn/view {:style (style/dashed-line-line network-name)}])
                          (repeat [rn/view {:style style/dashed-line-space}])))))

(defn f-network-routing-bars
  [_]
  (let [selected-network-idx (reagent/atom nil)
        press-locked?        (reagent/atom false)
        lock-press           #(reset! press-locked? true)
        unlock-press         #(reset! press-locked? false)
        reset-state-values   #(reset! selected-network-idx nil)]
    (fn [{:keys [networks total-width total-amount requesting-data? on-amount-selected]}]
      (let [bar-opacity-shared-value (reanimated/use-shared-value 0)
            network-bars             (map add-bar-shared-values networks)
            amount->width            #(* % (/ total-width total-amount))
            bars-widths-negative     (map #(-> % get-negative-amount amount->width)
                                          network-bars)
            last-bar-idx             (dec (count network-bars))]
        (rn/use-effect
         #(when (and (not requesting-data?) @selected-network-idx)
            (let [bar (nth network-bars @selected-network-idx)]
              (animation/hide-pressed-bar bar amount->width))
            (animation/update-bar-values-and-reset-animations
             {:new-network-values networks
              :network-bars       network-bars
              :amount->width      amount->width
              :reset-values-fn    reset-state-values
              :lock-press-fn      lock-press
              :unlock-press-fn    unlock-press
              :add-new-timeout    add-new-timeout}))
         [requesting-data?])
        [:<>
         (doall
          (for [[bar-idx bar] (map-indexed vector network-bars)
                :let          [bar-max-width      (amount->width (:max-amount bar))
                               bar-width          (-> (:amount-shared-value bar)
                                                      (reanimated/get-shared-value)
                                                      (amount->width))
                               hide-division?     (or (= last-bar-idx bar-idx) @selected-network-idx)
                               this-bar-selected? (= @selected-network-idx bar-idx)]]
            ^{:key (str "network-bar-" bar-idx)}
            [:f> f-network-bar
             {:bar           bar
              :max-width     bar-max-width
              :total-width   total-width
              :total-amount  total-amount
              :bar-division? hide-division?
              :on-top?       this-bar-selected?
              :allow-press?  (and (or (not @selected-network-idx) this-bar-selected?)
                                  (not requesting-data?)
                                  (not @press-locked?))
              :on-press      (fn []
                               (when-not @selected-network-idx
                                 (let [[previous-bars [_ & next-bars]] (split-at bar-idx network-bars)
                                       number-previous-bars            bar-idx]
                                   (animation/move-previous-bars
                                    {:bars                 previous-bars
                                     :bars-widths-negative bars-widths-negative})
                                   (animation/move-pressed-bar
                                    {:bar                  bar
                                     :bars-widths-negative bars-widths-negative
                                     :number-previous-bars number-previous-bars})
                                   (animation/move-next-bars
                                    {:bars                 next-bars
                                     :bars-widths-negative bars-widths-negative
                                     :number-previous-bars (inc number-previous-bars)
                                     :extra-offset         (max 0 (- bar-max-width bar-width))
                                     :add-new-timeout      add-new-timeout}))
                                 (animation/show-max-limit-bar bar-opacity-shared-value)
                                 (reset! selected-network-idx bar-idx)))
              :on-new-amount (fn [new-amount]
                               (animation/hide-max-limit-bar bar-opacity-shared-value)
                               (when on-amount-selected
                                 (on-amount-selected new-amount @selected-network-idx)))}]))

         (let [{:keys [max-amount network-name]} (some->> @selected-network-idx
                                                          (nth network-bars))
               limit-bar-width                   (amount->width max-amount)]
           [reanimated/view
            {:style (style/max-limit-bar
                     {:opacity-shared-value bar-opacity-shared-value
                      :width                limit-bar-width})}
            [rn/view {:style (style/max-limit-bar-background network-name)}]
            [dashed-line network-name]])]))))

(defn view-internal
  [{:keys [networks container-style theme] :as params}]
  (reagent/with-let [total-width (reagent/atom nil)]
    [rn/view
     {:accessibility-label :network-routing
      :style               (style/container container-style theme)
      :on-layout           #(reset! total-width (oops/oget % "nativeEvent.layout.width"))}
     (when @total-width
       ^{:key (str "network-routing-" (count networks))}
       [:f> f-network-routing-bars (assoc params :total-width @total-width)])]
    (finally
     (doseq [[_ living-timeout] @timeouts]
       (js/clearTimeout living-timeout)))))

(def view (quo.theme/with-theme view-internal))
