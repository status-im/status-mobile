(ns quo.components.wallet.transaction-progress.view
  (:require [quo.components.buttons.button.view :as button]
            [quo.components.icon :as icons]
            [quo.components.markdown.text :as text]
            [quo.components.tags.context-tag.view :as context-tag]
            [quo.components.wallet.confirmation-progress.view :as confirmation-progress]
            [quo.components.wallet.transaction-progress.style :as style]
            [quo.foundations.colors :as colors]
            [quo.theme :as quo.theme]
            [react-native.core :as rn]
            [utils.i18n :as i18n]))

(defn- icon-internal
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

(defn- text-internal
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

(defn- network-type-text
  [network state]
  (cond
    (and (= state :sending) (= network :arbitrum))  (i18n/label :t/confirmed-on)
    (or (= state :sending) (= state :pending))      (i18n/label :t/pending-on)
    (or (= state :confirmed) (= state :finalising)) (i18n/label :t/confirmed-on)
    (= state :finalized)                            (i18n/label :t/finalized-on)
    (= state :error)                                (i18n/label :t/failed-on)))

(defn- text-steps
  [network state epoch-number counter]
  (let [steps (case network
                :mainnet
                {:pending    "0/4"
                 :sending    (str (if (< counter 4) counter "4") "/4")
                 :confirmed  (str (if (< counter 4) counter "4") "/4")
                 :finalising (str (if (< counter 4) counter "4") "/4")
                 :finalized  (i18n/label :t/epoch-number {:number epoch-number})
                 :error      "0/4"}
                (or :optimism :arbitrum :optimism-arbitrum)
                {:pending    "0/1"
                 :sending    "0/1"
                 :confirmed  "0/1"
                 :finalising "1/1"
                 :finalized  (i18n/label :t/epoch-number {:number epoch-number})
                 :error      "0/1"}
                nil)]
    (get-in steps [state])))

(defn- get-status-icon
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

(defn- calculate-error-state
  [{:keys [state-arbitrum state-optimism state-mainnet network]}]
  (case network
    :mainnet           (= :error state-mainnet)
    :arbitrum          (= :error state-arbitrum)
    :optimism          (= :error state-optimism)
    :optimism-arbitrum (or (= :error state-arbitrum) (= :error state-optimism))
    nil))

(defn- title-internal
  [{:keys [state-arbitrum state-optimism state-mainnet title theme network]}]
  [rn/view {:style style/title-container}
   [icon-internal :i/placeholder (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)]
   [rn/view {:style style/title-text-container}
    [text-internal title]]
   (when (calculate-error-state {:state-arbitrum state-arbitrum
                                 :state-mainnet  state-mainnet
                                 :state-optimism state-optimism
                                 :network        network})
     [button/button
      {:size      24
       :icon-left :i/refresh}
      (i18n/label :t/retry)])])

(defn- tag-internal
  [tag-photo tag-name tag-number theme]
  [rn/view {:style (style/context-tag-container theme)}
   [context-tag/view
    {:size               24
     :collectible        tag-photo
     :collectible-name   tag-name
     :collectible-number tag-number
     :type               :collectible}]])

(defn- get-network-text
  [network]
  (case network
    :arbitrum (i18n/label :t/arbitrum)
    :mainnet  (i18n/label :t/mainnet)
    :optimism (i18n/label :t/optimism)))

(defn- status-row
  [{:keys [theme state network epoch-number counter]}]
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

(defn- view-mainnet
  [{:keys [theme state-mainnet epoch-number-mainnet counter total-box customization-color]}]
  [:<>
   [status-row
    {:theme        theme
     :state        state-mainnet
     :network      :mainnet
     :epoch-number epoch-number-mainnet
     :counter      @counter}]
   [confirmation-progress/view
    {:state               state-mainnet
     :network             :mainnet
     :counter             counter
     :total-box           total-box
     :customization-color customization-color}]])

(defn- view-arbitrum
  [{:keys [theme state-arbitrum epoch-number-arbitrum customization-color arbitrum-progress-percentage]}]
  [:<>
   [status-row
    {:theme        theme
     :state        state-arbitrum
     :network      :arbitrum
     :epoch-number epoch-number-arbitrum}]
   [confirmation-progress/view
    {:state               state-arbitrum
     :network             :arbitrum
     :bottom-large?       false
     :customization-color customization-color
     :progress-value      arbitrum-progress-percentage}]])

(defn- view-optimism
  [{:keys [theme state-optimism epoch-number-optimism customization-color optimism-progress-percentage]}]
  [:<>
   [status-row
    {:theme        theme
     :state        state-optimism
     :network      :optimism
     :epoch-number epoch-number-optimism}]
   [confirmation-progress/view
    {:state               state-optimism
     :network             :optimism
     :bottom-large?       true
     :customization-color customization-color
     :progress-value      optimism-progress-percentage}]])

(defn- view-internal
  [{:keys [title on-press accessibility-label network theme tag-photo tag-name tag-number state-arbitrum
           state-optimism state-mainnet]
    :as   props
    :or   {accessibility-label :transaction-progress}}]
  [rn/touchable-without-feedback
   {:on-press            on-press
    :accessibility-label accessibility-label}
   [rn/view {:style (style/box-style theme)}
    [title-internal
     {:state-arbitrum state-arbitrum
      :state-optimism state-optimism
      :state-mainnet  state-mainnet
      :title          title
      :theme          theme
      :network        network}]
    [tag-internal tag-photo tag-name tag-number theme]
    (case network
      :mainnet           [view-mainnet props]
      :optimism-arbitrum [:<>
                          [view-arbitrum props]
                          [view-optimism props]]
      :arbitrum          [view-arbitrum props]
      :optimism          [view-optimism props]
      nil)]])

(def view (quo.theme/with-theme view-internal))
