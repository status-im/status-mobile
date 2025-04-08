(ns status-im.contexts.wallet.sheets.network-filter.network-field
  (:require [clojure.string :as string]
            [quo.core :as quo]
            [quo.foundations.colors :as colors]
            [react-native.core :as rn]
            [status-im.contexts.wallet.common.fiat-text.view :as fiat-text]
            [status-im.contexts.wallet.sheets.network-filter.style :as style]
            [utils.i18n :as i18n]))

(defn- icon
  [{:keys [source]}]
  [rn/view {:style style/item-icon}
   [quo/icon-avatar
    {:icon source
     :size :size-20}]])

(defn- new-tag
  []
  [quo/new-feature-gradient {:style style/new-gradient}
   [quo/text
    {:weight :semi-bold
     :size   :label
     :style  {:color colors/white}}
    (string/upper-case (i18n/label :t/new))]])

(defn- collectibles-count
  [{:keys [amount]}]
  [rn/view
   {:style {:flex-direction :row
            :align-items    :center}}
   [quo/icon :i/nft
    {:size  16
     :color colors/neutral-50}]
   [quo/text
    {:size  :paragraph-2
     :style {:margin-left 4}
     :color colors/neutral-80-opa-95} amount]])

(defn- network-balances
  [{:keys [balance n-collectibles]}]
  (let [collectibles? (not (zero? n-collectibles))]
    [rn/view {:style style/item-balances-container}
     [fiat-text/view
      {:amount balance
       :color  colors/neutral-80-opa-95
       :size   :paragraph-2}]
     (when collectibles?
       [:<>
        [quo/dot-separator]
        [collectibles-count {:amount n-collectibles}]])]))

(defn view
  [{:keys [image-source title balance n-collectibles on-press new? network-toggled? disabled?
           customization-color]}]
  [rn/pressable
   {:style               style/item-container
    :on-press            on-press
    :accessibility-label :network-item}
   [rn/view {:style style/item-left-side}
    [icon {:source image-source}]
    [rn/view {:style {:margin-left 12}}
     [rn/view {:style style/item-title-container}
      [quo/text {:weight :medium} title]
      (when new? [new-tag])]
     [network-balances
      {:balance        balance
       :n-collectibles n-collectibles}]]]
   [rn/view {:style style/action-icon-container}
    [quo/selectors
     {:type                :checkbox
      :customization-color customization-color
      :on-change           on-press
      :default-checked?    true
      :checked?            network-toggled?
      :disabled?           disabled?}]]])
