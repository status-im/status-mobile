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

(defn icon-internal
  ([icon]
   (icon-internal icon nil 20))
  ([icon color]
   (icon-internal icon color 20))
  ([icon color size]
   [rn/view {:style style/icon}
    [icons/icon
     icon
     {:color color
      :size  size
      :no-color (when (not color) true)}]]))

(def total-box 85)
;; (def interval-ms 50)

(def lcounter (reagent/atom 0))
;; (def interval-id (reagent/atom nil))

;; (defn stop-interval
;;   []
;;   (when @interval-id
;;     (js/clearInterval @interval-id)
;;     (reset! interval-id nil)))

;; (defn clear-counter
;;   []
;;   (reset! lcounter 0))

;; (defn update-counter
;;   [network-state]
;;   (let [new-counter-value (-> @lcounter inc)]
;;     (if (or (and (= network-state :pending) (> new-counter-value 0))
;;             (and (= network-state :sending) (> new-counter-value 2))
;;             (and (= network-state :confirmed) (> new-counter-value 4))
;;             (and (= network-state :finalising) (> new-counter-value 18))
;;             (and (= network-state :finalized) (> new-counter-value total-box))
;;             (and (= network-state :error) (> new-counter-value 2)))
;;       (stop-interval)
;;       (swap! lcounter (fn [_] new-counter-value)))))

;; (defn start-interval
;;   [network-state]
;;   (reset! interval-id
;;           (js/setInterval
;;            (fn []
;;              (update-counter network-state))
;;            interval-ms)))

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
  [rn/view {:style (style/progress-box-container true)}
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
  [theme network-state network-type bottom-large?]
  [rn/view {:style (style/progress-box-container bottom-large?)}
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

(defn text-internal
  [title
   {:keys [weight size accessibility-label color]
    :or   {weight     :semi-bold
           size       :paragraph-1}}]
  [text/text
   {:accessibility-label accessibility-label
    :ellipsize-mode      :tail
    :number-of-lines     1
    :weight              weight
    :size                size
    :style               {:color color}}
   title])

(defn network-type-text
  [network-type network-state]
  (cond
    (and (= network-state :sending) (= network-type :arbitrum))     "Confirmed on "
    (or (= network-state :sending) (= network-state :pending))      "Pending on "
    (or (= network-state :confirmed) (= network-state :finalising)) "Confirmed on "
    (= network-state :finalized)                                    "Finalized on "
    (= network-state :error)                                        "Failed on "))

(defn text-steps
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
  [theme network-type network-state]
  (cond
    (and (= network-type :arbitrum)
         (= network-state :sending))   [:i/positive-state
                                        (colors/theme-colors colors/success-50
                                                             colors/success-60
                                                             theme)]
    (or (= network-state :pending)
        (= network-state :sending))    [:i/pending-state
                                        (colors/theme-colors colors/neutral-50
                                                             colors/neutral-60
                                                             theme)]
    (or (= network-state :confirmed)
        (= network-state :finalising)) [:i/positive-state
                                        (colors/theme-colors colors/success-50
                                                             colors/success-60
                                                             theme)]
    (= network-state :finalized)       [:i/diamond]
    (= network-state :error)           [:i/negative-state
                                        (colors/theme-colors colors/danger-50
                                                             colors/danger-60
                                                             theme)]))

(defn title-internal
  [network-state title theme btn-title]
  [rn/view {:style style/title-container} 
    [icon-internal :i/placeholder (colors/theme-colors colors/neutral-50 colors/neutral-60 theme)]
    [rn/view {:style style/title-text-container}
     [text-internal title]]
    (when (= network-state :error)
      [button/button
       {:size      24
        :icon-left :i/refresh} 
       btn-title])])

(defn tag-internal
  [tag-photo tag-name tag-number theme]
  [rn/view {:style (style/context-tag-container theme)}
   [context-tag/view {:size 24
                      :collectible tag-photo
                      :collectible-name tag-name
                      :collectible-number tag-number 
                      :type :collectible}]])

(defn get-network-text
  [network-type]
  (cond
    (= network-type :arbitrum) "Arbitrum"
    (= network-type :mainnet)  "Mainnet"
    (= network-type :optimism) "Optimism"))

(defn status-row
  [theme network-state network-type]
  (let [[status-icon color] (get-status-icon theme network-type network-state)]
     [rn/view {:style style/status-row-container}
      [icon-internal status-icon color 16]
      [rn/view {:style style/title-text-container}
       [text-internal
        (str (network-type-text network-type network-state) (get-network-text network-type))
        {:weight :regular
         :size :paragraph-2}]]
      [rn/view
       [text-internal
        (text-steps network-type network-state)
        {:weight :regular
         :size   :paragraph-2
         :color  (colors/theme-colors colors/neutral-50 colors/neutral-60 theme)}]]]))

(defn view-internal
  [{:keys [title on-press accessibility-label network state start-interval-now theme tag-photo tag-name btn-title tag-number]}]
  ;; (rn/use-effect
  ;;  (fn []
  ;;    (when start-interval-now
  ;;      (start-interval state))
  ;;    (clear-counter)
  ;;    (fn []
  ;;      (stop-interval)))
  ;;  [state])
  [rn/view
   [rn/touchable-without-feedback
    {:on-press            on-press
     :accessibility-label accessibility-label}
    [rn/view {:style style/box-style}
     [title-internal state title theme btn-title]
     [tag-internal tag-photo tag-name tag-number theme]
     (case network
       :mainnet [:<>
                 [status-row theme state :mainnet]
                 [progress-boxes state]]
       :optimism-arbitrum [:<>
                           [status-row theme state :arbitrum]
                           [progress-boxes-arbitrum theme state :arbitrum false]
                           [status-row theme state :optimism]
                           [progress-boxes-arbitrum theme state :optimism true]]
       nil)]]])

(def view (quo.theme/with-theme view-internal))
