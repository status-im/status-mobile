(ns quo2.components.wallet.transaction-progress.view
  (:require [quo2.components.wallet.transaction-progress.style :as style]
            [quo2.core :as quo2]
            [quo2.components.selectors.selectors.view :as selectors]
            [quo2.components.buttons.button :as button]
            [quo2.components.markdown.text :as text]
            [quo2.components.tags.status-tags :as status-tag]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]))

(defn load-icon
  [icon]
  [rn/view {:style style/icon}
   [quo2/icon icon
    {:color (colors/theme-colors
             colors/neutral-50
             colors/neutral-40)}]])

(def total-box 69)

(defn progress-boxes [green blue red]
  [rn/view
  {:style style/progress-box-container}
  (let [numbers (range 1 total-box)] ; Numbers from 1 to 30 (inclusive)
   (for [n numbers]
     [rn/view
     (assoc (let [box-style (cond
                          (<= n green) (assoc {:style style/progress-box} :background-color colors/success-50)
                          (<= n blue) (assoc {:style style/progress-box} :background-color (colors/custom-color-by-theme :blue 50 60))
                          (<= n red) (assoc {:style style/progress-box} :background-color colors/danger-50)
                          :else (assoc {:style style/progress-box} :background-color colors/neutral-5))]
         box-style) :key n)
      ]))])

(defn render-text
  [title override-theme & {:keys [typography weight size style]
           :or   {typography :main-semibold
                  weight :semi-bold
                  size   :paragraph-1
                  style   (style/title override-theme)}}]
    [text/text
            {
            :typography          typography
            :accessibility-label :title-name-text
            :ellipsize-mode      :tail
            :style               style
            :override-theme      override-theme
            :number-of-lines     1
            :weight              weight
            :size                size}
            title])

(defn network-type-text [networkType networkState]
  (cond
    (and (= networkType "mainnet")
         (or (= networkState "sending") (= networkState "pending"))) "Pending on Mainnet"
    (and (= networkType "mainnet")
         (or (= networkState "confirmed") (= networkState "finalising"))) "Confirmed on Mainnet"
    (and (= networkType "mainnet") (= networkState "finalised")) "Finalised on Mainnet"
    (and (= networkType "mainnet") (= networkState "error")) "Failed on Mainnet"))

(defn steps-text [networkType networkState]
  (cond
    (and (= networkType "mainnet") (= networkState "pending")) "0/4"
    (and (= networkType "mainnet") (= networkState "sending")) "2/4"
    (and (= networkType "mainnet")
         (or (= networkState "confirmed") (= networkState "finalising"))) "4/4"
    (and (= networkType "mainnet") (= networkState "finalised")) "Epoch 181,329"
    (and (= networkType "mainnet") (= networkState "error")) "0/4"))

(defn get-status-count [networkType networkState]
  (cond
    (and (= networkType "mainnet") (= networkState "pending")) [0 0 0]
    (and (= networkType "mainnet") (= networkState "sending")) [2 0 0]
    (and (= networkType "mainnet") (= networkState "confirmed")) [4 0 0]
    (and (= networkType "mainnet") (= networkState "finalising")) [4 10 0]
    (and (= networkType "mainnet") (= networkState "finalised")) [4 total-box 0]
    (and (= networkType "mainnet") (= networkState "error")) [0 0 1]))
    
(defn transaction-progress
  [{:keys [title
           on-press
           accessibility-label
           networkType
           networkState
           container-style
           override-theme]}]
[rn/view
{:style style/main-container}
  [rn/touchable-without-feedback
   {:on-press            on-press
    :accessibility-label accessibility-label}
   
   [rn/view
   {:style style/box-style}
    [rn/view
      {:style style/item-container}
      [rn/view
      {:style style/inner-container}
      [load-icon "placeholder"]
      
      [rn/view
        {:style style/title-container}
          [render-text title override-theme]]
      ]
      ]
      [rn/view
      {:style style/padding-row}
      [rn/view
        {:style style/doodle-container}
          [quo2/icon :i/doodle]
          [render-text "Doodle #120" override-theme]
          ]]
      [rn/view
      {:style style/item-container}
      [rn/view
      
      {:style (merge style/top-border style/inner-container)}
      [load-icon "placeholder"]
        [rn/view
        {:style style/title-container}
          [render-text (network-type-text networkType networkState) override-theme :typography :typography/font-regular :weight :regular :size :paragraph-2]
            ]
      
            [rn/view
            [render-text (steps-text networkType networkState) override-theme :typography :typography/font-regular :weight :regular :size :paragraph-2 :style {:color colors/neutral-50}]            
              ]
      ]]

      (let [[green blue red] (get-status-count networkType networkState)]
    [progress-boxes green blue red])
   
   ]
   ]
     
]
     )
