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
            [utils.i18n :as i18n]))

(defn icon-internal
  ([icon]
   (icon-internal icon nil 20))
  ([icon color]
   (icon-internal icon color 20))
  ([icon color size]
   [rn/view {:style style/icon}
    [icons/icon
     icon
     {:color    color
      :size     size
      :no-color (when (not color) true)}]]))

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
    :else                                                                   :pending))

(defn progress-boxes
  [state counter total-box]
  [rn/view
   {:accessibility-label :mainnet-progress-box
    :style               (style/progress-box-container true)}
   (let [numbers (range 1 total-box)]
     (doall (for [n numbers]
              [progress-box/view
               {:state               (calculate-box-state state counter n)
                :customization-color :blue
                :key                 n}])))])

(defn calculate-box-state-arbitrum-left
  [state network]
  (cond
    (= state :error)                                                     :error
    (and (= network :arbitrum) (= state :sending))                       :confirmed
    (or (= state :confirmed) (= state :finalising) (= state :finalized)) :confirmed
    :else                                                                :pending))

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
  [rn/view
   {:accessibility-label :arbitrum-progress-box
    :style               (style/progress-box-container bottom-large?)}
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
    :or   {weight :semi-bold
           size   :paragraph-1}}]
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
    (and (= state :sending) (= network :arbitrum))  (i18n/label :t/confirmed-on)
    (or (= state :sending) (= state :pending))      (i18n/label :t/pending-on)
    (or (= state :confirmed) (= state :finalising)) (i18n/label :t/confirmed-on)
    (= state :finalized)                            (i18n/label :t/finalized-on)
    (= state :error)                                (i18n/label :t/failed-on)))

(defn text-steps
  [network state epoch-number counter]
  (cond
    (and (= network :mainnet)
         (not= state :finalized)
         (not= state :error))   (str (if (< counter 4)
                                       counter
                                       "4")
                                     "/4")
    (= state :finalized)        (i18n/label :t/epoch-number {:number epoch-number})
    (and (= network :mainnet)
         (= state :error))      "0/4"
    (and (= network :arbitrum)
         (= state :confirmed))  "0/1"
    (and (not= network :mainnet)
         (= state :finalising)) "1/1"
    (and (= network :arbitrum)
         (= state :sending))    "0/1"
    (not= network :mainnet)     "0/1"))

(defn get-status-icon
  [theme network state]
  (cond
    (and (= network :arbitrum)
         (= state :sending))   [:i/positive-state (colors/resolve-color :success theme)]
    (or (= state :pending)
        (= state :sending))    [:i/pending-state
                                (colors/theme-colors colors/neutral-50
                                                     colors/neutral-40
                                                     theme)]
    (or (= state :confirmed)
        (= state :finalising)) [:i/positive-state (colors/resolve-color :success theme)]
    (= state :finalized)       [:i/diamond]
    (= state :error)           [:i/negative-state (colors/resolve-color :danger theme)]))

(defn title-internal
  [state title theme]
  [rn/view {:style style/title-container}
   [icon-internal :i/placeholder (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)]
   [rn/view {:style style/title-text-container}
    [text-internal title]]
   (when (= state :error)
     [button/button
      {:size      24
       :icon-left :i/refresh}
      (i18n/label :t/retry)])])

(defn tag-internal
  [tag-photo tag-name tag-number theme]
  [rn/view {:style (style/context-tag-container theme)}
   [context-tag/view
    {:size               24
     :collectible        tag-photo
     :collectible-name   tag-name
     :collectible-number tag-number
     :type               :collectible}]])

(defn get-network-text
  [network]
  (case network
    :arbitrum (i18n/label :t/arbitrum)
    :mainnet  (i18n/label :t/mainnet)
    :optimism (i18n/label :t/optimism)))

(defn status-row
  [theme state network epoch-number counter]
  (let [[status-icon color] (get-status-icon theme network state)]
    [rn/view {:style style/status-row-container}
     [icon-internal status-icon color 16]
     [rn/view {:style style/title-text-container}
      [text-internal
       (str (network-type-text network state) " " (get-network-text network))
       {:weight :regular
        :size   :paragraph-2}]]
     [rn/view
      [text-internal
       (text-steps network state epoch-number counter)
       {:weight :regular
        :size   :paragraph-2
        :color  (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)}]]]))

(defn view-internal
  [{:keys [title on-press accessibility-label network state theme tag-photo tag-name tag-number
           epoch-number counter total-box]}]
  [rn/touchable-without-feedback
   {:on-press            on-press
    :accessibility-label accessibility-label}
   [rn/view {:style (style/box-style theme)}
    [title-internal state title theme]
    [tag-internal tag-photo tag-name tag-number theme]
    (case network
      :mainnet           [:<>
                          [status-row theme state :mainnet epoch-number @counter]
                          [progress-boxes state @counter total-box]]
      :optimism-arbitrum [:<>
                          [status-row theme state :arbitrum epoch-number @counter]
                          [progress-boxes-arbitrum state :arbitrum false]
                          [status-row theme state :optimism epoch-number @counter]
                          [progress-boxes-arbitrum state :optimism true]]
      nil)]])

(def view (quo.theme/with-theme view-internal))
