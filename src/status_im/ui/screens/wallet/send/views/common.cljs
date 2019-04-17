(ns status-im.ui.screens.wallet.send.views.common
  (:require [status-im.ui.components.toolbar.actions :as actions]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.screens.wallet.styles :as wallet.styles]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.animation :as animation]
            [status-im.utils.money :as money]
            [status-im.i18n :as i18n]
            [clojure.string :as string]
            [status-im.ui.components.tooltip.views :as tooltip]
            [status-im.ui.screens.wallet.send.events :as events]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.dimensions :as dimensions]
            [taoensso.timbre :as log]
            [status-im.utils.ethereum.tokens :as tokens]))

(defn toolbar [flow title chat-id]
  (let [action (if (#{:chat :dapp} flow) actions/close-white actions/back-white)]
    [toolbar/toolbar {:style wallet.styles/toolbar}
     [toolbar/nav-button (action (if (= :chat flow)
                                   #(re-frame/dispatch [:chat.ui/navigate-to-chat chat-id {}])
                                   #(actions/default-handler)))]
     [toolbar/content-title {:color :white :font-size 17} title]]))

(defn action-button [{:keys [disabled? on-press underlay-color background-color style]} content]
  [react/touchable-highlight {:underlay-color underlay-color
                              :disabled       disabled?
                              :on-press       on-press
                              :style          (merge {:height           44
                                                      :background-color background-color
                                                      :border-radius    8
                                                      :flex             1
                                                      :align-items      :center
                                                      :justify-content  :center
                                                      :margin           3} style)}
   content])

(defn info-page [message]
  [react/view {:style {:flex             1
                       :align-items      :center
                       :justify-content  :center
                       :background-color colors/blue}}
   [vector-icons/icon :main-icons/info {:color colors/white}]
   [react/text {:style {:max-width   144
                        :margin-top  15
                        :color       colors/white
                        :font-size   15
                        :text-align  :center
                        :line-height 22}}
    message]])

;; worthy abstraction
(defn anim-ref-send
  "Call one of the methods in an animation ref.
   Takes an animation ref (a map of keys to animation tiggering methods)
   a keyword that should equal one of the keys in the map  and optional args to be sent to the animation.

   Example:
    (anim-ref-send slider-ref :open!)
    (anim-ref-send slider-ref :move-top-left! 25 25)"
  [anim-ref signal & args]
  (when anim-ref
    (assert (get anim-ref signal)
            (str "Key " signal " was not found in animation ref. Should be in "
                 (pr-str (keys anim-ref)))))
  (some-> anim-ref (get signal) (apply args)))

(defn slide-up-modal
  "Creates a modal that slides up from the bottom of the screen and
  responds to a swipe down gesture to dismiss

  The modal initially renders in the closed position.

  It takes an options map and the react child to be displayed in the
  modal.

  Options:
    :anim-ref - takes a function that will be called with a map of
        animation methods.
    :swipe-dismiss? - a boolean that determines whether the modal screen
        should be dismissed on swipe down gesture

  This slide-up-modal will callback the `anim-ref` fn and provides a
  map with 2 animation methods:

  :open!  - opens and displays the modal
  :close! - closes the modal"
  [{:keys [anim-ref swipe-dismiss?]} children]
  {:pre [(fn? anim-ref)]}
  (let [window-height (:height (react/get-dimensions "window") 1000)

        bottom-position  (animation/create-value (- window-height))

        modal-screen-bg-color
        (animation/interpolate bottom-position
                               {:inputRange [(- window-height) 0]
                                :outputRange [colors/transparent
                                              (colors/alpha colors/black 0.7)]})
        modal-screen-top
        (animation/interpolate bottom-position
                               {:inputRange [(- window-height)
                                             (+ (- window-height) 1)
                                             0]
                                :outputRange [window-height -200 -200]})

        vertical-slide-to (fn [view-bottom]
                            (animation/start
                             (animation/timing bottom-position {:toValue  view-bottom
                                                                :duration 500})))
        open-panel! #(vertical-slide-to 0)
        close-panel! #(vertical-slide-to (- window-height))
        ;; swipe-down-panhandler
        swipe-down-handlers
        (when swipe-dismiss?
          (js->clj
           (.-panHandlers
            (.create react/pan-responder
                     #js {:onMoveShouldSetPanResponder
                          (fn [e g]
                            (when-let [distance (.-dy g)]
                              (< 50 distance)))
                          :onMoveShouldSetPanResponderCapture
                          (fn [e g]
                            (when-let [distance (.-dy g)]
                              (< 50 distance)))
                          :onPanResponderRelease
                          (fn [e g]
                            (when-let [distance (.-dy g)]
                              (when (< 200 distance)
                                (close-panel!))))}))))]
    (anim-ref {:open! open-panel!
               :close! close-panel!})
    (fn [{:keys [anim-ref] :as opts} children]
      [react/animated-view (merge
                            {:style
                             {:position :absolute
                              :top      modal-screen-top
                              :bottom   0
                              :left     0
                              :right    0
                              :z-index  1
                              :background-color modal-screen-bg-color}}
                            swipe-down-handlers)
       [react/touchable-highlight {:on-press (fn [] (close-panel!))
                                   :style    {:flex 1}}
        [react/view]]
       [react/animated-view {:style
                             {:position :absolute
                              :left     0
                              :right    0
                              :z-index  2
                              :bottom   bottom-position}}
        children]])))

(defn- custom-gas-panel-action [{:keys [label active on-press icon background-color]} child]
  {:pre [label (boolean? active) on-press icon background-color]}
  [react/view {:style {:flex-direction     :row
                       :padding-horizontal 22
                       :padding-vertical   11
                       :align-items        :center}}
   [react/touchable-highlight
    {:disabled active
     :on-press on-press}
    [react/animated-view {:style {:border-radius    21
                                  :width            40
                                  :height           40
                                  :justify-content  :center
                                  :align-items      :center
                                  :background-color background-color}}
     [vector-icons/icon icon {:color (if active colors/white colors/gray)}]]]
   [react/touchable-highlight
    {:disabled active
     :on-press on-press
     :style    {:flex 1}}
    [react/text {:style {:color        colors/black
                         :font-size    17
                         :padding-left 17
                         :line-height  40}}
     label]]
   child])

(defn- custom-gas-edit
  [_opts]
  (let [gas-error (reagent/atom nil)
        gas-price-error (reagent/atom nil)]
    (fn [{:keys [on-gas-input-change
                 on-gas-price-input-change
                 gas-input
                 gas-price-input]}]
      [react/view {:style {:padding-horizontal 22
                           :padding-vertical   11}}
       [react/text (i18n/label :t/gas-price)]
       (when @gas-price-error
         [react/view {:style {:z-index 100}}
          [tooltip/tooltip @gas-price-error
           {:color        colors/blue-light
            :font-size    12
            :bottom-value -3}]])
       [react/view {:style {:border-radius      8
                            :background-color   colors/gray-lighter
                            :padding-vertical   16
                            :padding-horizontal 16
                            :flex-direction     :row
                            :align-items        :center
                            :margin-vertical    7}}
        [react/text-input {:keyboard-type       :numeric
                           :placeholder         "0"
                           :on-change-text      (fn [x]
                                                  (if-not (money/bignumber x)
                                                    (reset! gas-price-error (i18n/label :t/invalid-number-format))
                                                    (reset! gas-price-error nil))
                                                  (on-gas-price-input-change x))
                           :default-value       gas-price-input
                           :accessibility-label :gas-price-input
                           :keyboard-appearance :dark
                           :style               {:font-size        15
                                                 :flex             1
                                                 :background-color colors/gray-lighter}}]
        [react/text (i18n/label :t/gwei)]]
       [react/text {:style {:color     colors/gray
                            :font-size 12}}
        (i18n/label :t/gas-cost-explanation)]
       [react/text {:style {:margin-top 22}} (i18n/label :t/gas-limit)]
       (when @gas-error
         [react/view {:style {:z-index 100}}
          [tooltip/tooltip @gas-error
           {:color        colors/blue-light
            :font-size    12
            :bottom-value -3}]])
       [react/view {:style {:border-radius      8
                            :background-color   colors/gray-lighter
                            :padding-vertical   16
                            :padding-horizontal 16
                            :flex-direction     :row
                            :align-items        :flex-end
                            :margin-vertical    7}}
        [react/text-input {:keyboard-type       :numeric
                           :placeholder         "0"
                           :on-change-text      (fn [x]
                                                  (if-not (money/bignumber x)
                                                    (reset! gas-error (i18n/label :t/invalid-number-format))
                                                    (reset! gas-error nil))
                                                  (on-gas-input-change x))
                           :default-value       gas-input
                           :accessibility-label :gas-limit-input
                           :keyboard-appearance :dark
                           :style               {:font-size        15
                                                 :flex             1
                                                 :background-color colors/gray-lighter}}]]
       [react/text {:style {:color     colors/gray
                            :font-size 12}}
        (i18n/label :t/gas-limit-explanation)]])))

(defn- custom-gas-derived-state [{:keys [custom-open?]}
                                 {:keys [gas-input gas-price-input]}
                                 {:keys [gas gas-price
                                         optimal-gas optimal-gas-price
                                         gas-gas-price->fiat
                                         fiat-currency] :as params}]
  (let [custom-input-gas
        (or (when (not (string/blank? gas-input))
              (money/bignumber gas-input))
            gas
            optimal-gas)
        custom-input-gas-price
        (or (when (not (string/blank? gas-price-input))
              (money/->wei :gwei gas-price-input))
            gas-price
            optimal-gas-price)]
    {:optimal-fiat-price
     (str "~ " (:symbol fiat-currency)
          (gas-gas-price->fiat {:gas optimal-gas
                                :gas-price optimal-gas-price}))
     :custom-fiat-price
     (if custom-open?
       (str "~ " (:symbol fiat-currency)
            (gas-gas-price->fiat {:gas custom-input-gas
                                  :gas-price custom-input-gas-price}))
       (str "..."))
     :gas-price-input-value
     (str (or gas-price-input
              (some->> gas-price (money/wei-> :gwei))
              (some->> optimal-gas-price (money/wei-> :gwei))))
     :gas-input-value
     (str (or gas-input gas optimal-gas))
     :gas-map-for-submit
     (when custom-open?
       {:gas custom-input-gas :gas-price custom-input-gas-price})}))

;; Choosing the gas amount
(defn custom-gas-input-panel [{:keys [gas gas-price
                                      optimal-gas optimal-gas-price
                                      gas-gas-price->fiat on-submit
                                      input-state-atom] :as opts}]
  {:pre [optimal-gas optimal-gas-price gas-gas-price->fiat on-submit]}
  (let [{:keys [height]}  (dimensions/window)
        custom-height 290
        custom-open? (and gas gas-price)
        sheet-state-atom  (reagent.core/atom  {:custom-open? (boolean custom-open?)})
      ;;  state-atom   (reagent.core/atom {
      ;;                                   :gas-input       nil
      ;;                                   :gas-price-input nil})

        ;; slider animations
        slider-height (animation/create-value (if custom-open? custom-height 0))
        slider-height-to #(animation/start
                           (animation/timing slider-height {:toValue  %
                                                            :duration 500}))

        optimal-button-bg-color
        (animation/interpolate slider-height
                               {:inputRange  [0 100 custom-height]
                                :outputRange [colors/blue colors/gray-light colors/gray-light]})

        custom-button-bg-color
        (animation/interpolate slider-height
                               {:inputRange  [0 100 custom-height]
                                :outputRange [colors/gray-light colors/blue colors/blue]})

        open-slider!  #(do
                         (slider-height-to custom-height)
                         (swap! sheet-state-atom assoc :custom-open? true))
        close-slider! #(do
                         (slider-height-to 0)
                         (swap! sheet-state-atom assoc :custom-open? false))]
    (fn [opts]
      (let [{:keys [optimal-fiat-price
                    custom-fiat-price
                    gas-price-input-value
                    gas-input-value
                    gas-map-for-submit] :as params}
            (custom-gas-derived-state @sheet-state-atom @input-state-atom opts)]
        [react/scroll-view {:style {:background-color        colors/white
                                    :border-top-left-radius  8
                                    :border-top-right-radius 8
                                    :max-height              (* 0.9 height)}}
         [react/view {:style {:justify-content :center
                              :padding-top     22
                              :padding-bottom  7}}
          [react/text
           {:style {:color       colors/black
                    :font-size   22
                    :line-height 28
                    :text-align  :center}}
           (i18n/label :t/network-fee-settings)]
          [react/text
           {:style {:color              colors/gray
                    :font-size          15
                    :line-height        22
                    :text-align         :center
                    :padding-horizontal 45
                    :padding-vertical   8}}
           (i18n/label :t/network-fee-explanation)]]
         [react/view {:style {:border-top-width 1
                              :border-top-color colors/black-transparent
                              :padding-top      11
                              :padding-bottom   7}}
          (custom-gas-panel-action {:icon             :main-icons/time
                                    :label            (i18n/label :t/optimal-gas-option)
                                    :on-press         close-slider!
                                    :background-color optimal-button-bg-color
                                    :active           (not (:custom-open? @sheet-state-atom))}
                                   [react/text {:style {:color        colors/gray
                                                        :font-size    17
                                                        :padding-left 17
                                                        :line-height  20}}
                                    optimal-fiat-price])
          (custom-gas-panel-action {:icon             :main-icons/sliders
                                    :label            (i18n/label :t/custom-gas-option)
                                    :on-press         open-slider!
                                    :background-color custom-button-bg-color
                                    :active           (:custom-open? @sheet-state-atom)}
                                   [react/text {:style {:color        colors/gray
                                                        :font-size    17
                                                        :padding-left 17
                                                        :line-height  20
                                                        :text-align   :center
                                                        :min-width    60}}
                                    custom-fiat-price])
          [react/animated-view {:style {:background-color colors/white
                                        :height           slider-height
                                        :overflow         :hidden}}
           [custom-gas-edit
            {:on-gas-price-input-change #(when (money/bignumber %)
                                           (swap! input-state-atom assoc :gas-price-input %))
             :on-gas-input-change       #(when (money/bignumber %)
                                           (swap! input-state-atom assoc :gas-input %))
             :gas-price-input           gas-price-input-value
             :gas-input                 gas-input-value}]]
          [react/view {:style {:flex-direction   :row
                               :justify-content  :center
                               :padding-vertical 16}}
           [react/touchable-highlight
            {:on-press #(on-submit gas-map-for-submit)
             :style    {:padding-horizontal 39
                        :padding-vertical   12
                        :border-radius      8
                        :background-color   colors/blue-light}}
            [react/text {:style {:font-size   15
                                 :line-height 22
                                 :color       colors/blue}}
             (i18n/label :t/update)]]]]]))))

(defn token->fiat-conversion [prices token fiat-currency value]
  {:pre [(map? prices) (map? token) (map? fiat-currency) value]}
  (when-let [price (get-in prices [(:symbol token)
                                   (-> fiat-currency :code keyword)
                                   :price])]
    (some-> value
            money/bignumber
            (money/crypto->fiat price))))

(defn fiat->token-conversion [prices token fiat-currency value]
  {:pre [(map? prices) (map? token) (map? fiat-currency) value]}
  (when-let [price (get-in prices [(:symbol token)
                                   (-> fiat-currency :code keyword)
                                   :price])]
    (some-> value
            money/bignumber
            (.div (money/bignumber price)))))

(defn max-fee [{:keys [gas gas-price optimal-gas optimal-gas-price] :as params}]
  (let [gas-param       (or gas optimal-gas)
        gas-price-param (or gas-price optimal-gas-price)]
    (if (and gas-param gas-price-param)
      (money/wei->ether (.times gas-param gas-price-param))
      0)))

(defn network-fees [prices native-currency fiat-currency gas-ether-price]
  (some-> (token->fiat-conversion prices native-currency fiat-currency gas-ether-price)
          (money/with-precision 3)))

(defn fetch-optimal-gas [web3 tx-atom cb]
  (let [symbol (:symbol @tx-atom)]
    (ethereum/gas-price
     web3
     (fn [_ gas-price]
       (when gas-price
         (cb {:optimal-gas       (ethereum/estimate-gas symbol)
              :optimal-gas-price (money/bignumber gas-price)}))))))

(defn optimal-gas-present? [{:keys [optimal-gas optimal-gas-price]}]
  (and optimal-gas optimal-gas-price))

(defn current-gas [{:keys [gas gas-price optimal-gas optimal-gas-price]}]
  {:gas (or gas optimal-gas) :gas-price (or gas-price optimal-gas-price)})

(defn refresh-optimal-gas [web3 tx-atom]
  (fetch-optimal-gas web3
                     tx-atom
                     (fn [res]
                       (swap! tx-atom merge res))))
