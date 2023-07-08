(ns quo2.components.wallet.account-card.view
  (:require [react-native.core :as rn]
            [quo2.foundations.colors :as colors]
            [quo2.components.icon :as icon]
            [quo2.components.wallet.account-card.style :as style]
            [status-im2.common.plus-button.view :as plus-button]
            [utils.i18n :as i18n]
            [quo2.components.markdown.text :as text]))

(defn user-account-view
  [{:keys [name balance percentage-value amount customization-color type emoji]}]
  (let [watch-only? (= :watch-only type)]
    [:<>
     [rn/view (style/card customization-color watch-only?)
      [rn/view style/profile-container
       [rn/view {:style {:padding-bottom 2 :margin-right 2}}
        [text/text {:style style/emoji} emoji]]
       [rn/view {:style style/watch-only-container}
        [text/text
         {:size   :paragraph-2
          :weight :medium
          :style  (style/account-name watch-only?)}
         name]
        (when watch-only? [icon/icon :reveal {:color colors/neutral-50 :size 12}])]]

      [text/text
       {:size   :heading-2
        :weight :semi-bold
        :style  (style/account-value watch-only?)} balance]
      [rn/view style/metrics-container
       [rn/view {:style {:margin-right 4}}
        [icon/icon :positive
         {:color (if (and watch-only? (not (colors/dark?)))
                   colors/neutral-50
                   colors/white)
          :size  16}]]
       [text/text
        {:weight :semi-bold
         :size   :paragraph-2
         :style  (style/metrics watch-only?)} percentage-value]
       [rn/view (style/separator watch-only?)]
       [text/text
        {:weight :semi-bold
         :size   :paragraph-2
         :style  (style/metrics watch-only?)} amount]]]]))

(defn add-account-view
  [{:keys [handler customization-color]}]
  [rn/view (style/add-account-container)
   [plus-button/plus-button
    {:on-press            handler
     :customization-color customization-color
    }]
   [text/text
    {:size   :paragraph-2
     :weight :medium
     :style  {:margin-top 4}} (i18n/label :t/add-account)]])

(defn view
  [{:keys [type] :as props}]
  (case type
    :watch-only  [user-account-view props]
    :add-account [add-account-view props]
    :default     [user-account-view
                  props]
    nil))
