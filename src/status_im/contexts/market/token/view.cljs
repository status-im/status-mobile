(ns status-im.contexts.market.token.view
  (:require
    [quo.components.icon :as icon]
    [quo.components.utilities.token.view :as token]
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [status-im.common.events-helper :as events-helper]
    [status-im.common.floating-button-page.view :as floating-button-page]
    [status-im.contexts.market.token.style :as style]
    [utils.i18n :as i18n]
    [utils.number :as number-utils]))

(defn token-overview
  [{:keys [theme value change]}]
  (let [formatted-change (-> change
                             (* 100)
                             (number-utils/naive-round 2)
                             (str "%"))]
    [rn/view {:style (style/token-overview theme)}
     [quo/text
      {:size   :heading-2
       :weight :semi-bold}
      value]
     [rn/view {:style style/token-overview-info-row}
      [icon/icon
       (if (> change 0)
         :i/positive
         :i/negative)
       (style/token-overview-icon-props theme (> change 0))]
      [quo/text
       {:style (style/token-overview-change-text theme (> change 0))
        :size  :paragraph-2}
       formatted-change]
      [quo/text
       {:style  (style/token-overview-change-time-text theme)
        :size   :paragraph-2
        :weight :medium}
       (i18n/label :t/time-24h)]]]))

(defn token-parameter
  [{:keys [theme title value first?]}]
  [rn/view {:style (style/token-parameter theme first?)}
   [quo/text
    {:size  :paragraph-2
     :style (style/token-parameter-title theme)}
    title]
   [quo/text
    {:size   :paragraph-2
     :weight :medium
     :style  (style/token-parameter-value theme)}
    value]])

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
                                   :margin-top (safe-area/get-top)
                                   :background :blur}]]}
     [rn/view {:style (style/header-top theme)}
      [rn/view {:style style/header-title-row}
       [token/view {:token :eth :size :size-32}]
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
      [rn/view {:style (style/content-row theme true)}
       [token-overview
        {:theme  theme
         :value  "€575.56"
         :change -0.101}]]
      [rn/view {:style (style/content-row theme false)}
       ^{:key "market-cap"}
       [token-parameter
        {:theme  theme
         :title  "Market cap"
         :value  "€84,817,829,837"
         :first? true}]
       ^{:key "24h-volume"}
       [token-parameter
        {:theme  theme
         :title  "24h Volume"
         :value  "280,080,208"
         :first? false}]]]]))
