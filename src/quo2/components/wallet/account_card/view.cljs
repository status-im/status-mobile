(ns quo2.components.wallet.account-card.view
  (:require [react-native.core :as rn]
            [quo2.foundations.colors :as colors]
            [quo2.components.icon :as icon]
            [quo2.components.wallet.account-card.style :as style]
            [quo2.components.buttons.button.view :as button]
            [utils.i18n :as i18n]
            [quo2.components.markdown.text :as text]
            [quo2.theme :as theme]))

(defn loading-view
  [{:keys [customization-color type theme metrics?]}]
  (let [watch-only? (= :watch-only type)
        empty?      (= :empty type)]
    [rn/view (style/card customization-color watch-only? metrics? theme)
     [rn/view {:style {:flex-direction :row :align-items :center}}
      [rn/view {:margin-right 8 :margin-top 2 :style (style/loader-view 16 16 watch-only? theme)}]
      [rn/view {:style style/watch-only-container}
       [rn/view {:style (style/loader-view 57 8 watch-only? theme)}]
       (when watch-only? [icon/icon :reveal {:color colors/neutral-50 :size 12}])]]
     [rn/view {:margin-top 13 :style (style/loader-view (if empty? 56 80) 16 watch-only? theme)}]
     (when metrics?
       [rn/view
        {:margin-top 10
         :style      (style/loader-view (if empty? 37 96) 8 watch-only? theme)}])]))

(defn user-account
  [{:keys [state name balance percentage-value loading? amount customization-color type emoji metrics?
           theme]}]
  (let [watch-only?        (= :watch-only type)
        empty?             (= :empty type)
        account-amount     (if (= :empty state) "€0.00" amount)
        account-name       (if (= :empty state) "Account 1" name)
        account-percentage (if (= :empty state) "€0.00" percentage-value)
       ]
    (if loading?
      [loading-view
       {:customization-color customization-color
        :type                type
        :theme               theme
        :metrics?            metrics?}]
      [:<>
       [rn/view {:style (style/card customization-color watch-only? metrics? theme)}
        [rn/view {:style style/profile-container}
         [rn/view {:style {:padding-bottom 2 :margin-right 2}}
          [text/text {:style style/emoji} emoji]]
         [rn/view {:style style/watch-only-container}
          [text/text
           {:size   :paragraph-2
            :weight :medium
            :style  (style/account-name watch-only? theme)}
           account-name]
          (when watch-only? [icon/icon :reveal {:color colors/neutral-50 :size 12}])]]
        [text/text
         {:size   :heading-2
          :weight :semi-bold
          :style  (style/account-value watch-only? theme)}
         balance]
        (when metrics?
          [rn/view {:style style/metrics-container}
           [text/text
            {:weight :semi-bold
             :size   :paragraph-2
             :style  (style/metrics watch-only? theme)}
            account-percentage]
           (when (not empty?)
             [:<> [rn/view (style/separator watch-only? theme)]
              [text/text
               {:weight :semi-bold
                :size   :paragraph-2
                :style  (style/metrics watch-only? theme)} account-amount]
              [rn/view {:style {:margin-left 4}}
               [icon/icon :positive
                {:color (if (and watch-only? (not (colors/dark?)))
                          colors/neutral-50
                          colors/white-opa-70)
                 :size  16}]]])])]])))

(defn- add-account-view
  [{:keys [handler customization-color theme]}]
  [rn/view (style/add-account-container theme)
   [button/button
    {:type                :primary
     :size                32
     :icon                true
     :accessibility-label :add-account
     :on-press            handler
     :customization-color customization-color}
    :i/add]
   [text/text
    {:size   :paragraph-2
     :weight :medium
     :style  {:margin-top 4}}
    (i18n/label :t/add-account)]])

(defn- view-internal
  [{:keys [type] :as props}]
  (case type
    :watch-only  [user-account props]
    :add-account [add-account-view props]
    :default     [user-account props]
    :empty       [user-account props]
    nil))

(def view (theme/with-theme view-internal))
