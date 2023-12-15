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

(def ^:private max-mainnet-verifications 4)
(def ^:private max-sidenet-verifications 1)

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
  (case state
    (:sending :pending)      (i18n/label :t/pending-on)
    (:confirmed :finalising) (i18n/label :t/confirmed-on)
    :finalized               (i18n/label :t/finalized-on)
    :error                   (i18n/label :t/failed-on)
    nil))

(defn- calculate-counter
  [counter]
  (if (< counter 4) counter max-mainnet-verifications))

(defn mainnet-label
  [counter]
  (str counter "/" max-mainnet-verifications))

(defn subnet-label
  [counter]
  (str counter "/" max-sidenet-verifications))

(defn- text-steps
  [network state epoch-number counter]
  (let [steps (case network
                :mainnet
                {:pending    (mainnet-label 0)
                 :sending    (mainnet-label (calculate-counter counter))
                 :confirmed  (mainnet-label (calculate-counter counter))
                 :finalising (mainnet-label (calculate-counter counter))
                 :finalized  (i18n/label :t/epoch-number {:number epoch-number})
                 :error      (mainnet-label 0)}
                (or :optimism :arbitrum)
                {:pending    (subnet-label 0)
                 :sending    (subnet-label 0)
                 :confirmed  (subnet-label 0)
                 :finalising (subnet-label 1)
                 :finalized  (i18n/label :t/epoch-number {:number epoch-number})
                 :error      (subnet-label 0)}
                nil)]
    (get steps state)))

(defn- get-status-icon
  [theme state]
  (case state
    (:pending :sending)      {:icon  :i/pending-state
                              :color (colors/theme-colors colors/neutral-50
                                                          colors/neutral-40
                                                          theme)}
    (:confirmed :finalising) {:icon  :i/positive-state
                              :color (colors/resolve-color :success theme)}
    :finalized               {:icon :i/diamond}
    :error                   {:icon  :i/negative-state
                              :color (colors/resolve-color :danger theme)}
    nil))

(defn- get-network
  [networks network]
  (some #(when (= (:network %) network) %)
        networks))

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
    :optimism (i18n/label :t/optimism)
    nil))

(defn- status-row
  [{:keys [theme state network epoch-number counter]}]
  (let [{:keys [icon color]} (get-status-icon theme state)]
    [rn/view {:style style/status-row-container}
     [icon-internal icon color 16]
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
  [{:keys [theme state epoch-number counter total-box progress network]}]
  [:<>
   [status-row
    {:theme        theme
     :state        state
     :network      network
     :epoch-number epoch-number
     :counter      counter}]
   [confirmation-progress/view
    {:state          state
     :network        network
     :counter        counter
     :total-box      total-box
     :progress-value progress}]])

(defn- view-internal
  [{:keys [title on-press accessibility-label theme tag-photo tag-name tag-number networks]
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
      (let [assoc-props #(get-network networks %)]
        ^{:key (:network network)}
        [view-network (assoc-props (:network network))]))]])

(def view (quo.theme/with-theme view-internal))
