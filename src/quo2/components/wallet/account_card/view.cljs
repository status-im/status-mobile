(ns quo2.components.wallet.account-card.view
  (:require [react-native.core :as rn]
            [quo2.foundations.colors :as colors]
            [quo2.components.icon :as icon]
            [quo2.components.wallet.account-card.style :as style]
            [status-im2.common.plus-button.view :as plus-button]
            [utils.i18n :as i18n]
            [quo2.components.markdown.text :as text]))

(defn user-account-view
  [{:keys [name balance percentage-value amount customization-color watch-only type]}]
  [rn/view
   [rn/view (style/card customization-color watch-only)
    [rn/view style/profile-container
     [icon/icon :contact {:size 20}]
     [rn/view style/watch-only-container
      [text/text (style/account-name watch-only)
       name]
      (if watch-only [icon/icon :reveal {:color colors/neutral-50 :size 12}])]
    ]
    [text/text (style/account-value watch-only) balance]
    [rn/view style/metrics-container
     [rn/view {:margin-right 5.5}
      [icon/icon :positive
       {:color (if (and watch-only (not (colors/dark?)))
                 colors/neutral-100
                 colors/white)
        :size  16}]]
     [text/text (style/metrics watch-only) percentage-value]
     [rn/view (style/separator watch-only)]
     [text/text (style/metrics watch-only) amount]]]]
)

(defn add-account-view
  [{:keys [handler type add-account]}]
  [rn/view style/add-account-container
   [plus-button/plus-button
    {:on-press            handler
     :customization-color :blue
    }]
   [text/text style/add-account-title (i18n/label :t/add-account)]])

(defn view
  [{:keys [name balance percentage-value amount customization-color handler watch-only add-account
           type]}]
  (case type
    :watch-only  [user-account-view
                  {:name                name
                   :balance             balance
                   :percentage-value    percentage-value
                   :amount              amount
                   :customization-color customization-color
                   :watch-only          watch-only}]
    :add-account [add-account-view {:handler handler}]
    :default     [user-account-view
                  {:name                name
                   :balance             balance
                   :percentage-value    percentage-value
                   :amount              amount
                   :customization-color customization-color
                   :watch-only          watch-only}]
    nil))