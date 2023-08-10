(ns quo2.components.wallet.transaction-progress.view
  (:require [quo2.components.wallet.transaction-progress.style :as style]
            [quo2.core :as quo2]
            [quo2.components.selectors.selectors.view :as selectors]
            [quo2.components.buttons.button.view :as button]
            [quo2.components.wallet.progress-bar.view :as progress-box]
            [quo2.components.markdown.text :as text]
            [quo2.components.tags.status-tags :as status-tag]
            [quo2.foundations.colors :as colors]
            [status-im2.common.resources :as resources]
            [quo2.theme :as theme]
            [reagent.core :as reagent]
            [react-native.core :as rn]))

(defn get-colors
  [color]
  (let [current-theme (theme/get-theme)]
    (cond
      (= current-theme :dark)
      (cond
        (= color "neutral-5")  colors/neutral-70
        (= color "neutral-10") colors/neutral-80
        (= color "neutral-40") colors/neutral-50
        (= color "neutral-50") colors/neutral-60
        (= color "danger-50")  colors/danger-60
        (= color "success-50") colors/success-60)
      :else
      (cond
        (= color "neutral-5")  colors/neutral-5
        (= color "neutral-10") colors/neutral-10
        (= color "neutral-40") colors/neutral-40
        (= color "neutral-50") colors/neutral-50
        (= color "danger-50")  colors/danger-50
        (= color "success-50") colors/success-50))))

(defn load-icon
  [icon color]
  [rn/view {:style style/icon}
   [quo2/icon icon
    {:color color}]])

(def total-box 85)

(def app-state (reagent/atom {:counter 0}))
(def interval-id (reagent/atom nil)) ; To store the interval ID

(defn stop-interval
  []
  (when @interval-id
    (js/clearInterval @interval-id)
    (reset! interval-id nil))) ; Clear the interval ID

(defn clear-counter
  []
  (swap! app-state assoc :counter 0))

(defn update-counter
  [networkState]
  (let [new-counter-value (-> @app-state :counter inc)]
    (if (or (and (= networkState "pending") (> new-counter-value 1))
            (and (= networkState "sending") (> new-counter-value 2))
            (and (= networkState "confirmed") (> new-counter-value 4))
            (and (= networkState "finalising") (> new-counter-value 18))
            (and (= networkState "finalised") (> new-counter-value total-box))
            (and (= networkState "error") (> new-counter-value 2)))
      (stop-interval)
      (swap! app-state assoc :counter new-counter-value))))

(defn start-interval
  [networkState]
  (reset! interval-id
    (js/setInterval
     (fn []
       (update-counter networkState))
     50))) ; Interval of 50ms

(defn calculate-box-state
  [networkState counter index]
  (cond
    (and (= networkState "sending") (>= counter index) (< index 3))                 "confirmed"
    (and (= networkState "confirmed") (>= counter index) (< index 5))               "confirmed"
    (and (= networkState "finalising") (>= counter index) (< index 5))              "confirmed"
    (and (= networkState "finalising") (>= counter index) (> index 4) (< index 20)) "finalised"
    (and (= networkState "finalised") (>= counter index) (< index 5))               "confirmed"
    (and (= networkState "finalised") (>= counter index) (> index 4))               "finalised"
    (and (= networkState "error") (>= counter index) (< index 2))                   "error"
    :else                                                                           "pending"))

(defn progress-boxes
  [networkState]
  [rn/view
   {:style style/progress-box-container}
   (let [numbers (range 1 total-box)]
     (doall (for [n numbers]
              [progress-box/progress-bar
               {:network-state    (calculate-box-state networkState (@app-state :counter) n)
                :width            "8"
                :height           "12"
                :marginHorizontal 2
                :key              n
               }])))])

(defn calculate-box-state-arbitrum
  [networkState]
  (cond
    (= networkState "pending") "pending"
    (= networkState "error")   "error"
    :else                      "confirmed"))

(defn calculate-box-width
  [showHalf]
  (cond
    (and showHalf (< (@app-state :counter) 30)) (- total-box (@app-state :counter))
    showHalf                                    30
    (< (@app-state :counter) total-box)         (- total-box (@app-state :counter))
    :else                                       0))

(defn progress-boxes-arbitrum
  [networkState]
  [rn/view
   {:style style/progress-box-container}
   [progress-box/progress-bar
    {:network-state    (calculate-box-state-arbitrum networkState)
     :width            "8"
     :height           "12"
     :marginHorizontal 2}]
   [rn/view
    {:style            style/progress-box-arbitrum
     :background-color (get-colors "neutral-5")
     :border-color     (get-colors "neutral-10")}
    [rn/view
     (assoc
      (let [box-style (cond
                        (= networkState "finalising") (assoc {:style style/progress-box-arbitrum-abs}
                                                             :right (str (calculate-box-width true) "%")
                                                             :background-color
                                                             (colors/custom-color-by-theme :blue 50 60))
                        (= networkState "finalised")  (assoc {:style style/progress-box-arbitrum-abs}
                                                             :right (str (calculate-box-width false) "%")
                                                             :background-color
                                                             (colors/custom-color-by-theme :blue 50 60))
                        :else                         (assoc {:style style/progress-box-arbitrum-abs}
                                                             :background-color
                                                             (get-colors "neutral-5")))]
        box-style)
      :align-self "flex-end"
      :border-color
      (get-colors "neutral-10"))]]])

(defn render-text
  [title override-theme &
   {:keys [typography weight size style]
    :or   {typography :main-semibold
           weight     :semi-bold
           size       :paragraph-1
           style      (style/title override-theme)}}]
  [text/text
   {:typography          typography
    :accessibility-label :title-name-text
    :ellipsize-mode      :tail
    :style               style
    :override-theme      override-theme
    :number-of-lines     1
    :weight              weight
    :size                size}
   title])

(defn network-type-text
  [networkType networkState]
  (cond
    (or (= networkState "sending") (= networkState "pending"))      "Pending on"
    (or (= networkState "confirmed") (= networkState "finalising")) "Confirmed on"
    (= networkState "finalised")                                    "Finalised on"
    (= networkState "error")                                        "Failed on"))

(defn steps-text
  [networkType networkState]
  (cond
    (and (= networkType "mainnet")
         (not= networkState "finalised")
         (not= networkState "error"))   (str (if (< (@app-state :counter) 4)
                                               (@app-state :counter)
                                               "4")
                                             "/4")
    (= networkState "finalised")        "Epoch 181,329"
    (and (= networkType "mainnet")
         (= networkState "error"))      "0/4"
    (and (= networkType "optimism/arbitrum")
         (= networkState "finalising")) "1/1"
    (= networkType "optimism/arbitrum") "0/1"))

(defn get-status-icon
  [networkType networkState]
  (cond
    (or (= networkState "pending") (= networkState "sending"))      ["pending-state"
                                                                     (get-colors "neutral-50")]
    (or (= networkState "confirmed") (= networkState "finalising")) ["positive-state"
                                                                     (get-colors "success-50")]
    (= networkState "finalised")                                    ["diamond" (get-colors "success-50")]
    (= networkState "error")                                        ["negative-state"
                                                                     (get-colors "danger-50")]))

(defn transaction-progress
  [{:keys [title
           on-press
           accessibility-label
           networkType
           networkState
           container-style
           override-theme]}]
  (let [count (reagent/atom 0)]
    (rn/use-effect
     (fn []
       (start-interval networkState)
       (clear-counter)
       (fn []
         (stop-interval)))
     [networkState])
    [rn/view
     [rn/touchable-without-feedback
      {:on-press            on-press
       :accessibility-label accessibility-label}
      [rn/view
       {:style style/box-style}
       [rn/view
        {:style style/title-item-container}
        [rn/view
         {:style style/inner-container}
         [load-icon "placeholder" (get-colors "neutral-50")]
         [rn/view
          {:style style/title-container}
          [render-text title override-theme]]
         (if (= networkState "error")
           [button/button
            {:size   32
             :before :i/refresh
             :type   :primary} "Retry"])]]
       [rn/view
        {:style style/padding-row}
        [quo2/context-tag {:blur? [false]}
         (resources/get-mock-image :collectible)
         "Doodle #120"]]
       (if (= networkType "mainnet")
         [rn/view
          {:style style/item-container}
          [rn/view
           {:style (assoc style/progress-container :border-color (get-colors "neutral-10"))}
           (let [[status-icon color] (get-status-icon networkType networkState)]
             [load-icon status-icon color])
           [rn/view
            {:style style/title-container}
            [render-text (str (network-type-text networkType networkState) " Mainnet") override-theme
             :typography
             :typography/font-regular :weight :regular :size :paragraph-2]]
           [rn/view
            [render-text (steps-text networkType networkState) override-theme :typography
             :typography/font-regular :weight :regular :size :paragraph-2 :style
             {:color (get-colors "neutral-50")}]]]])
       (if (= networkType "optimism/arbitrum")
         [rn/view
          {:style style/item-container}
          [rn/view
           {:style (assoc style/progress-container :border-color (get-colors "neutral-10"))}
           (let [[status-icon color] (get-status-icon networkType networkState)]
             [load-icon status-icon color])
           [rn/view
            {:style style/title-container}
            [render-text (str (network-type-text networkType networkState) " Arbitrum") override-theme
             :typography
             :typography/font-regular :weight :regular :size :paragraph-2]]
           [rn/view
            [render-text (steps-text networkType networkState) override-theme :typography
             :typography/font-regular :weight :regular :size :paragraph-2 :style
             {:color (get-colors "neutral-50")}]]]])
       (if (= networkType "optimism/arbitrum")
         [progress-boxes-arbitrum networkState])
       (if (= networkType "optimism/arbitrum")
         [rn/view
          {:style style/item-container}
          [rn/view
           {:style (assoc style/progress-container :border-color (get-colors "neutral-10"))}
           (let [[status-icon color] (get-status-icon networkType networkState)]
             [load-icon status-icon color])
           [rn/view
            {:style style/title-container}
            [render-text (str (network-type-text networkType networkState) " Optimism") override-theme
             :typography
             :typography/font-regular :weight :regular :size :paragraph-2]]
           [rn/view
            [render-text (steps-text networkType networkState) override-theme :typography
             :typography/font-regular :weight :regular :size :paragraph-2 :style
             {:color (get-colors "neutral-50")}]]]])
       (if (= networkType "optimism/arbitrum")
         [progress-boxes-arbitrum networkState])
       (if (= networkType "mainnet")
         [progress-boxes networkState])]]]))
