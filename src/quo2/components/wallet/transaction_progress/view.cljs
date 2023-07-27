(ns quo2.components.wallet.transaction-progress.view
  (:require [quo2.components.wallet.transaction-progress.style :as style]
            [quo2.components.icon :as icons]
            [quo2.components.selectors.selectors.view :as selectors]
            [quo2.components.buttons.button :as button]
            [quo2.components.markdown.text :as text]
            [quo2.components.tags.status-tags :as status-tag]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]))

(defn transaction-progress-title
  [title status-tag-props override-theme]
  [rn/view
   {:style style/title-container}
   (when title
     [text/text
      {:accessibility-label :setting-item-name-text
       :ellipsize-mode      :tail
       :style               (style/title override-theme)
       :override-theme      override-theme
       :number-of-lines     1
       :weight              :medium
       :size                :paragraph-1}
      title])])

(defn left-icon-comp
  [icon]
  [rn/view {:style style/icon}
   [icons/icon icon
    {:color (colors/theme-colors
             colors/neutral-50
             colors/neutral-40)}]])

(def chevron-icon
  [rn/view
   [icons/icon :chevron-right
    {:color (colors/theme-colors
             colors/neutral-50
             colors/neutral-40)}]])

(def numbers [1 2 3 4 5 6 7 8 9 10])

(defn progress-boxes [numbers]
  [rn/view
  {:style style/item-container}
   (for [n numbers]
     [rn/view
      {:style style/progress-box}
      ])])

(defn transaction-progress

  [{:keys [title
           on-press
           accessibility-label
           left-icon
           chevron?
           statusIcon
           container-style
           override-theme
           status-tag-props]}]
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
      [left-icon-comp "placeholder"]
      [rn/view
        {:style style/title-container}
          [text/text
            {
            :typography          :main-semibold
            :accessibility-label :title-name-text
            :ellipsize-mode      :tail
            :style               (style/title override-theme)
            :override-theme      override-theme
            :number-of-lines     1
            :weight              :semi-bold
            :size                :paragraph-1}
            title]]
      ]
      ]
      [rn/view
      {:style style/padding-row}
      [rn/view
        {:style style/inner-containers}
          [left-icon-comp "dark"]
          [text/text
            {
            :typography          :main-semibold
            :accessibility-label :title-name-text
            :ellipsize-mode      :tail
            :style               (style/title override-theme)
            :override-theme      override-theme
            :number-of-lines     1
            :weight              :semi-bold
            :size                :paragraph-1}
            "Doodle #120"]]]
      [rn/view
      {:style style/item-container}
      [rn/view
      
      {:style (merge style/top-border style/inner-container)}
      (when statusIcon
        [left-icon-comp statusIcon])
        [rn/view
        {:style style/title-container}
          [text/text
            {
            :typography          :typography/font-regular
            :accessibility-label :subtitle-name-text
            :ellipsize-mode      :tail
            :style               (style/title override-theme)
            :override-theme      override-theme
            :number-of-lines     1
            :size                :paragraph-2}
            "Pending on Mainnet"]]
      
            [rn/view
            [text/text
              {
              :typography          :typography/font-regular
              :accessibility-label :status-text
              :ellipsize-mode      :tail
              :style               {:color                colors/neutral-50}
              :override-theme      override-theme
              :number-of-lines     1
              :weight              :regular
              :size                :paragraph-2
              }
              "0/4"]
              ]
      ]]

      [progress-boxes numbers]
   
   ]
   ]
     
]
     )
