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
  [state]
  (let [new-counter-value (-> @lcounter inc)]
    (if (or (and (= state :pending) (> new-counter-value 0))
            (and (= state :sending) (> new-counter-value 2))
            (and (= state :confirmed) (> new-counter-value 4))
            (and (= state :finalising) (> new-counter-value 18))
            (and (= state :finalized) (> new-counter-value total-box))
            (and (= state :error) (> new-counter-value 2)))
      (stop-interval)
      (swap! lcounter (fn [_] new-counter-value)))))

(defn start-interval
  [state]
  (reset! interval-id
          (js/setInterval
           (fn []
             (update-counter state))
           interval-ms)))

(defn calculate-box-state
  [state counter index]
  (cond
    (and (= state :sending) (>= counter index) (< index 3))                 :confirmed
    (and (= state :confirmed) (>= counter index) (< index 5))               :confirmed
    (and (= state :finalising) (>= counter index) (< index 5))              :confirmed
    (and (= state :finalising) (>= counter index) (> index 4) (< index 20)) :finalized
    (and (= state :finalized) (>= counter index) (< index 5))               :confirmed
    (and (= state :finalized) (>= counter index) (> index 4))               :finalized
    (and (= state :error) (>= counter index) (< index 2))                   :error
    :else                                                                           :pending))

(defn progress-boxes
  [state]
  [rn/view {:style (style/progress-box-container true)}
   (let [numbers (range 1 total-box)]
     (doall (for [n numbers]
              [progress-box/view
               {:state               (calculate-box-state state @lcounter n)
                :customization-color :blue
                :key                 n}])))])

(defn calculate-box-state-arbitrum-left
  [state network]
  (cond
    (= state :error)                                    :error
    (and (= network :arbitrum) (= state :sending)) :confirmed
    (or (= state :confirmed) (= state :finalising) (= state :finalized))  :confirmed
    :else                                                       :pending))

(defn calculate-box-state-arbitrum-right
  [state network]
  (cond
    (= state :error)                                    
    :error
    (and (= network :arbitrum) 
         (= state :sending)) 
    :confirmed
    (or (= state :confirmed) 
        (= state :finalising) 
        (= state :finalized))  
    :finalized
    :else                                                       
    :pending))

(defn calculate-progressed-value
  [state network]
  (case state
    :finalising (if (= network :arbitrum) 26 16)
    :finalized  100
    0))

(defn progress-boxes-arbitrum
  [state network bottom-large?]
  [rn/view {:style (style/progress-box-container bottom-large?)}
   [progress-box/view
    {:state               (calculate-box-state-arbitrum-left state network)
     :customization-color :blue}]
   [progress-box/view
    {:state               (calculate-box-state-arbitrum-right state network)
     :full-width?         true
     :progressed-value    (calculate-progressed-value state network)
     :customization-color :blue}]])

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
  [network state]
  (cond
    (and (= state :sending) (= network :arbitrum))     "Confirmed on "
    (or (= state :sending) (= state :pending))      "Pending on "
    (or (= state :confirmed) (= state :finalising)) "Confirmed on "
    (= state :finalized)                                    "Finalized on "
    (= state :error)                                        "Failed on "))

(defn text-steps
  [network state epoch-number]
  (cond
    (and (= network :mainnet)
         (not= state :finalized)
         (not= state :error))       (str (if (< @lcounter 4)
                                                   @lcounter
                                                   "4")
                                                 "/4")
    (= state :finalized)            (str "Epoch " epoch-number)
    (and (= network :mainnet)
         (= state :error))          "0/4"
    (and (= network :arbitrum)
         (= state :confirmed))      "0/1"
    (and (not= network :mainnet)
         (= state :finalising))     "1/1"
    (and (= network :arbitrum)
         (= state :sending))        "0/1"
    (not= network :mainnet)            "0/1"))

(defn get-status-icon
  [theme network state]
  (cond
    (and (= network :arbitrum)
         (= state :sending))   [:i/positive-state
                                        (colors/theme-colors colors/success-50
                                                             colors/success-60
                                                             theme)]
    (or (= state :pending)
        (= state :sending))    [:i/pending-state
                                        (colors/theme-colors colors/neutral-50
                                                             colors/neutral-60
                                                             theme)]
    (or (= state :confirmed)
        (= state :finalising)) [:i/positive-state
                                        (colors/theme-colors colors/success-50
                                                             colors/success-60
                                                             theme)]
    (= state :finalized)       [:i/diamond]
    (= state :error)           [:i/negative-state
                                        (colors/theme-colors colors/danger-50
                                                             colors/danger-60
                                                             theme)]))

(defn title-internal
  [state title theme btn-title]
  [rn/view {:style style/title-container} 
    [icon-internal :i/placeholder (colors/theme-colors colors/neutral-50 colors/neutral-60 theme)]
    [rn/view {:style style/title-text-container}
     [text-internal title]]
    (when (= state :error)
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
  [network]
  (cond
    (= network :arbitrum) "Arbitrum"
    (= network :mainnet)  "Mainnet"
    (= network :optimism) "Optimism"))

(defn status-row
  [theme state network epoch-number]
  (let [[status-icon color] (get-status-icon theme network state)]
     [rn/view {:style style/status-row-container}
      [icon-internal status-icon color 16]
      [rn/view {:style style/title-text-container}
       [text-internal
        (str (network-type-text network state) (get-network-text network))
        {:weight :regular
         :size :paragraph-2}]]
      [rn/view
       [text-internal
        (text-steps network state epoch-number)
        {:weight :regular
         :size   :paragraph-2
         :color  (colors/theme-colors colors/neutral-50 colors/neutral-60 theme)}]]]))

(defn f-view-internal
  [{:keys [title on-press accessibility-label network state start-interval-now theme tag-photo tag-name btn-title tag-number epoch-number]}]
  (rn/use-effect
   (fn []
     (when start-interval-now
       (start-interval state))
     (clear-counter)
     (fn []
       (stop-interval)))
   [state]) 
   [rn/touchable-without-feedback
    {:on-press            on-press
     :accessibility-label accessibility-label}
    [rn/view {:style style/box-style}
     [title-internal state title theme btn-title]
     [tag-internal tag-photo tag-name tag-number theme]
     (case network
       :mainnet [:<>
                 [status-row theme state :mainnet epoch-number]
                 [progress-boxes state]]
       :optimism-arbitrum [:<>
                           [status-row theme state :arbitrum epoch-number]
                           [progress-boxes-arbitrum state :arbitrum false]
                           [status-row theme state :optimism epoch-number]
                           [progress-boxes-arbitrum state :optimism true]]
       nil)]])

(defn view-internal
  [props]
  [:f> f-view-internal props])

(def view (quo.theme/with-theme view-internal))
