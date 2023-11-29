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
  [{:keys [weight size accessibility-label color]
    :or   {weight :semi-bold
           size   :paragraph-1}}
   title]
  [text/text
   {:accessibility-label accessibility-label
    :ellipsize-mode      :tail
    :number-of-lines     1
    :weight              weight
    :size                size
    :style               {:color color}}
   title])

(defn- network-type-text
  [state]
  (cond
    (or (= state :sending) (= state :pending))      (i18n/label :t/pending-on)
    (or (= state :confirmed) (= state :finalising)) (i18n/label :t/confirmed-on)
    (= state :finalized)                            (i18n/label :t/finalized-on)
    (= state :error)                                (i18n/label :t/failed-on)))

(defn- calculate-counter
  [counter]
  (if (< @counter 4) @counter "4"))

(defn- text-steps
  [network state epoch-number counter]
  (let [steps (case network
                :mainnet
                {:pending    "0/4"
                 :sending    (str (calculate-counter counter) "/4")
                 :confirmed  (str (calculate-counter counter) "/4")
                 :finalising (str (calculate-counter counter) "/4")
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
  [theme state]
  (cond
    (or (= state :pending)
        (= state :sending))    [:i/pending-state
                                (colors/theme-colors colors/neutral-50
                                                     colors/neutral-40
                                                     theme)]
    (or (= state :confirmed)
        (= state :finalising)) [:i/positive-state (colors/resolve-color :success theme)]
    (= state :finalized)       [:i/diamond]
    (= state :error)           [:i/negative-state (colors/resolve-color :danger theme)]))

(defn- get-network
  [networks network]
  (first (filter #(= (:network %) network) networks)))

(defn- calculate-error-state
  [networks]
  (some #(= (:state %) :error) networks))

(defn- title-internal
  [{:keys [title theme networks]}]
  [rn/view {:style style/title-container}
   [icon-internal :i/placeholder (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)]
   [rn/view {:style style/title-text-container}
    [text-internal nil title]]
   (when (calculate-error-state networks)
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
  (let [[status-icon color] (get-status-icon theme state)]
    [rn/view {:style style/status-row-container}
     [icon-internal status-icon color 16]
     [rn/view {:style style/title-text-container}
      [text-internal 
       {:weight :regular
        :size   :paragraph-2}
       (str (network-type-text state) " " (get-network-text network))]]
     [rn/view
      [text-internal 
       {:weight :regular
        :size   :paragraph-2
        :color  (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)}
       (text-steps network state epoch-number counter)]]]))

(defn- view-network
  [{:keys [theme state epoch-number counter total-box customization-color progress bottom-large? network]}]
  [:<>
   [status-row
    {:theme        theme
     :state        state
     :network      network
     :epoch-number epoch-number
     :counter      counter}]
   [confirmation-progress/view
    {:state               state
     :network             network
     :counter             counter
     :total-box           total-box
     :progress-value      progress
     :bottom-large?       bottom-large?
     :customization-color customization-color}]])

(defn- view-internal
  [{:keys [title on-press accessibility-label theme tag-photo tag-name tag-number networks
           customization-color]
    :or   {accessibility-label :transaction-progress}}]
  [rn/touchable-without-feedback
   {:on-press            on-press
    :accessibility-label accessibility-label}
   [rn/view {:style (style/box-style theme)}
    [title-internal
     {:title    title
      :theme    theme
      :networks networks}]
    [tag-internal tag-photo tag-name tag-number theme]
    (for [network networks]
      ^{:key (:network network)}
      (let [assoc-props #(assoc (get-network networks %)
                                :customization-color
                                customization-color)] 
        [view-network (assoc-props (:network network))]))]])

(def view (quo.theme/with-theme view-internal))
