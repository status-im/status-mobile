(ns quo.components.wallet.transaction-progress.view
  (:require [quo.components.buttons.button.view :as button]
            [quo.components.icon :as icons]
            [quo.components.markdown.text :as text]
            [quo.components.tags.context-tag.view :as context-tag]
            [quo.components.wallet.progress-bar.view :as progress-box]
            [quo.components.wallet.transaction-progress.style :as style] 
            [quo.foundations.colors :as colors]
            [quo.theme :as quo.theme]
            [react-native.core :as rn]
            [reagent.core :as reagent]))

(defn load-icon
  [icon color]
  [rn/view {:style style/icon}
   [icons/icon icon
    {:color color}]])

(def total-box 85)
(def interval-ms 50)

(def lcounter (reagent/atom 0))
(def interval-id (reagent/atom nil))

(defn stop-interval
  []
  (when @interval-id
    (js/clearInterval @interval-id)
    (reset! interval-id nil)))

(defn clear-counter
  []
  (reset! lcounter 0))

(defn update-counter
  [network-state]
  (let [new-counter-value (-> @lcounter inc)]
    (if (or (and (= network-state :pending) (> new-counter-value 0))
            (and (= network-state :sending) (> new-counter-value 2))
            (and (= network-state :confirmed) (> new-counter-value 4))
            (and (= network-state :finalising) (> new-counter-value 18))
            (and (= network-state :finalized) (> new-counter-value total-box))
            (and (= network-state :error) (> new-counter-value 2)))
      (stop-interval)
      (swap! lcounter (fn [_] new-counter-value)))))

(defn start-interval
  [network-state]
  (reset! interval-id
          (js/setInterval
           (fn []
             (update-counter network-state))
           interval-ms)))

(defn calculate-box-state
  [network-state counter index]
  (cond
    (and (= network-state :sending) (>= counter index) (< index 3))                 :confirmed
    (and (= network-state :confirmed) (>= counter index) (< index 5))               :confirmed
    (and (= network-state :finalising) (>= counter index) (< index 5))              :confirmed
    (and (= network-state :finalising) (>= counter index) (> index 4) (< index 20)) :finalized
    (and (= network-state :finalized) (>= counter index) (< index 5))               :confirmed
    (and (= network-state :finalized) (>= counter index) (> index 4))               :finalized
    (and (= network-state :error) (>= counter index) (< index 2))                   :error
    :else                                                                           :pending))

(defn progress-boxes
  [network-state]
  [rn/view
   {:style style/progress-box-container}
   (let [numbers (range 1 total-box)]
     (doall (for [n numbers]
              [progress-box/view
               {:state               (calculate-box-state network-state @lcounter n)
                :customization-color :blue
                :key                 n}])))])

(defn calculate-box-state-arbitrum
  [network-state network-type]
  (cond
    (= network-state :error)                                    :error
    (and (= network-type :arbitrum) (= network-state :sending)) :confirmed
    (or (= network-state :confirmed) (= network-state :finalising) (= network-state :finalized))  :confirmed
    :else                                                       :pending))

(defn calculate-box-width
  [showHalf?]
  (cond
    (and showHalf? (< @lcounter 30)) (- total-box @lcounter)
    showHalf?                        30
    (< @lcounter total-box)          (- total-box @lcounter)
    :else                            0))

(defn progress-boxes-arbitrum
  [theme network-state network-type]
  [rn/view
   {:style style/progress-box-container}
   [progress-box/view
    {:state               (calculate-box-state-arbitrum network-state network-type)
     :customization-color :blue}]
   [rn/view
    {:style (style/progress-box-arbitrum theme)}
    [rn/view
     (assoc
      (let [box-style (cond
                        (= network-state :finalising)
                        (assoc {:style style/progress-box-arbitrum-abs}
                               :right (str (calculate-box-width true) "%")
                               :background-color (colors/resolve-color :blue theme))

                        (= network-state :finalized)
                        (assoc {:style style/progress-box-arbitrum-abs}
                               :right (str (calculate-box-width false) "%")
                               :background-color (colors/resolve-color :blue theme))

                        :else
                        (assoc {:style style/progress-box-arbitrum-abs}
                               :background-color (colors/theme-colors colors/neutral-5
                                                                      colors/neutral-70
                                                                      theme)))]
        box-style)
      :align-self "flex-end"
      :border-color
      (colors/theme-colors colors/neutral-10 colors/neutral-80 theme))]]])

(defn render-text
  [title override-theme &
   {:keys [typography weight size style]
    :or   {typography :main-semibold
           weight     :semi-bold
           size       :paragraph-1
           style      style/title}}]
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
  [network-type network-state]
  (cond
    (and (= network-state :sending) (= network-type :arbitrum))     "Confirmed on "
    (or (= network-state :sending) (= network-state :pending))      "Pending on "
    (or (= network-state :confirmed) (= network-state :finalising)) "Confirmed on "
    (= network-state :finalized)                                    "Finalized on "
    (= network-state :error)                                        "Failed on "))

(defn steps-text
  [network-type network-state]
  (cond
    (and (= network-type :mainnet)
         (not= network-state :finalized)
         (not= network-state :error))       (str (if (< @lcounter 4)
                                                   @lcounter
                                                   "4")
                                                 "/4")
    (= network-state :finalized)            "Epoch 181,329"
    (and (= network-type :mainnet)
         (= network-state :error))          "0/4"
    (and (not= network-type :mainnet)
         (or (= network-state :finalising)
             (= network-state :confirmed))) "1/1"
    (and (= network-type :arbitrum)
         (= network-state :sending))        "1/1"
    (not= network-type :mainnet)            "0/1"))

(defn get-status-icon
  [override-theme network-type network-state]
  (cond
    (and (= network-type :arbitrum)
         (= network-state :sending))   ["positive-state"
                                        (colors/theme-colors colors/success-50
                                                             colors/success-60
                                                             override-theme)]
    (or (= network-state :pending)
        (= network-state :sending))    ["pending-state"
                                        (colors/theme-colors colors/neutral-50
                                                             colors/neutral-60
                                                             override-theme)]
    (or (= network-state :confirmed)
        (= network-state :finalising)) ["positive-state"
                                        (colors/theme-colors colors/success-50
                                                             colors/success-60
                                                             override-theme)]
    (= network-state :finalized)       ["diamond"
                                        (colors/theme-colors colors/success-50
                                                             colors/success-60
                                                             override-theme)]
    (= network-state :error)           ["negative-state"
                                        (colors/theme-colors colors/danger-50
                                                             colors/danger-60
                                                             override-theme)]))

(defn render-title
  [network-state title override-theme btn-title]
  [rn/view
   {:style style/title-item-container}
   [rn/view
    {:style style/inner-container}
    [load-icon "placeholder" (colors/theme-colors colors/neutral-50 colors/neutral-60 override-theme)]
    [rn/view
     {:style style/title-container}
     [render-text title override-theme]]
    (when (= network-state :error)
      [button/button
       {:size      24
        :icon-left :i/refresh
        :type      :primary} btn-title])]])

(defn render-tag
  [tag-photo tag-name]
  [rn/view
   {:style style/padding-row}
   [context-tag/view {:blur? [false]} tag-photo tag-name]])

(defn get-network-text
  [network-type]
  (cond
    (= network-type :arbitrum) "Arbitrum"
    (= network-type :mainnet)  "Mainnet"
    (= network-type :optimism) "Optimism"))

(defn render-status-row
  [override-theme network-state network-type]
  [rn/view
   {:style style/item-container}
   [rn/view
    {:style (assoc style/progress-container
                   :border-color
                   (colors/theme-colors colors/neutral-10 colors/neutral-80 override-theme))}
    (let [[status-icon color] (get-status-icon override-theme network-type network-state)]
      [load-icon status-icon color])
    [rn/view
     {:style style/title-container}
     [render-text (str (network-type-text network-type network-state) (get-network-text network-type))
      override-theme
      :typography
      :typography/font-regular :weight :regular :size :paragraph-2]]
    [rn/view
     [render-text (steps-text network-type network-state) override-theme :typography
      :typography/font-regular :weight :regular :size :paragraph-2 :style
      {:color (colors/theme-colors colors/neutral-50 colors/neutral-60 override-theme)}]]]])

(defn view-internal
  [{:keys [title on-press accessibility-label network-type network-state start-interval-now theme tag-photo tag-name btn-title]}]
  ;; (rn/use-effect
  ;;  (fn []
  ;;    (when start-interval-now
  ;;      (start-interval network-state))
  ;;    (clear-counter)
  ;;    (fn []
  ;;      (stop-interval)))
  ;;  [network-state])
  [rn/view
   [rn/touchable-without-feedback
    {:on-press            on-press
     :accessibility-label accessibility-label}
    [rn/view {:style style/box-style}
     [render-title network-state title theme btn-title]
     [render-tag tag-photo tag-name]
     (case network-type
       :mainnet [:<>
                 [render-status-row theme network-state :mainnet]
                 [progress-boxes network-state]]
       :optimism-arbitrum [:<>
                           [render-status-row theme network-state :arbitrum]
                           [progress-boxes-arbitrum theme network-state :arbitrum]
                           [render-status-row theme network-state :optimism]
                           [progress-boxes-arbitrum theme network-state :optimism]]
       nil)]]])

(def view (quo.theme/with-theme view-internal))
