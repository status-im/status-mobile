(ns status-im.contexts.market.token.view
  (:require
    [quo.context]
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [status-im.common.events-helper :as events-helper]
    [status-im.common.floating-button-page.view :as floating-button-page]
    [status-im.contexts.market.token.style :as style]
    [utils.i18n :as i18n]
    [utils.number]))

(defn token-overview
  [{:keys [theme value change]}]
  [rn/view {:style style/token-overview}
   [quo/text
    {:size   :heading-2
     :weight :semi-bold}
    value]
   [rn/view {:style style/token-overview-info-row}
    [quo/icon
     (if (pos? change)
       :i/positive
       :i/negative)
     (style/token-overview-icon-props theme (pos? change))]
    [quo/text
     {:style (style/token-overview-change-text theme (pos? change))
      :size  :paragraph-2}
     (utils.number/format-as-percentage change) "%"]
    [quo/text
     {:style  (style/token-overview-change-time-text theme)
      :size   :paragraph-2
      :weight :medium}
     (i18n/label :t/time-24h)]]])

(defn token-parameter
  [{:keys [theme title value first?]}]
  [rn/view {:style (style/token-parameter first?)}
   [quo/text
    {:size  :paragraph-2
     :style (style/token-parameter-title theme)}
    title]
   [quo/text
    {:size   :paragraph-2
     :weight :medium
     :style  (style/token-parameter-value theme)}
    value]])

(defn dashed-line
  [{:keys [type]}]
  (let [theme (quo.context/use-theme)]
    [rn/view {:style (style/dashed-line-outer-container theme type)}
     [rn/view {:style (style/dashed-line-inner-container theme type)}]]))

(defn view
  []
  (let [theme (quo.context/use-theme)]
    [floating-button-page/view
     {:footer-container-padding 0
      :header                   [rn/view {:style (style/header-page-nav theme)}
                                 [quo/page-nav
                                  {:type       :no-title
                                   :icon-name  :i/close
                                   :on-press   events-helper/navigate-back
                                   :margin-top safe-area/top
                                   :background :blur}]]}
     [rn/view {:style (style/header-top theme)}
      [rn/view {:style style/header-title-row}
       [quo/token {:token :eth :size :size-32}]
       [quo/text
        {:size                :heading-1
         :weight              :semi-bold
         :style               style/header-token-name-text
         :accessibility-label :token-name}
        "Ethereum"]
       [quo/text
        {:size   :heading-2
         :weight :medium
         :style  (style/header-token-ticker-text theme)}
        "ETH"]]
      [rn/view {:style style/header-buttons}
       [rn/view {:style style/header-button}
        [quo/button
         {:size      40
          :icon-left :i/swap
          :type      :primary}
         (i18n/label :t/swap)]]
       [rn/view {:style style/header-button}
        [quo/button
         {:size      40
          :icon-left :i/buy
          :type      :outline}
         (i18n/label :t/buy)]]]]
     [rn/view {:style (style/content-container theme)}
      [rn/view {:style style/content-row}
       [token-overview
        {:theme  theme
         :value  "€575.56"
         :change -0.101}]]
      [dashed-line {:type :horizontal}]
      [rn/view {:style style/content-row}
       [token-parameter
        {:theme  theme
         :title  "Market cap"
         :value  "€84,817,829,837"
         :first? true}]
       [dashed-line {:type :vertical}]
       [token-parameter
        {:theme  theme
         :title  "24h Volume"
         :value  "280,080,208"
         :first? false}]]]]))
