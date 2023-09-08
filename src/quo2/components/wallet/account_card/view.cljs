(ns quo2.components.wallet.account-card.view
  (:require [react-native.core :as rn]
            [quo2.foundations.colors :as colors]
            [quo2.components.icon :as icon]
            [quo2.components.wallet.account-card.style :as style]
            [quo2.components.buttons.button.view :as button]
            [quo2.components.markdown.text :as text]
            [utils.i18n :as i18n]
            [quo2.theme :as theme]
            [reagent.core :as reagent]
            [quo2.foundations.customization-colors :as customization-colors]))

(defn- loading-view
  [{:keys [customization-color type theme metrics?]}]
  (let [watch-only? (= :watch-only type)
        empty-type? (= :empty type)]
    [rn/view
     {:accessibility-label :loading
      :style               (style/card customization-color watch-only? metrics? theme false)}
     [rn/view {:style style/loader-container}
      [rn/view
       {:style (assoc (style/loader-view 16
                                         16
                                         watch-only?
                                         theme)
                      :margin-right 8
                      :margin-top   2)}]
      [rn/view {:style style/watch-only-container}
       [rn/view {:style (style/loader-view 57 8 watch-only? theme)}]
       (when watch-only? [icon/icon :reveal {:color colors/neutral-50 :size 12}])]]
     [rn/view
      {:style (assoc (style/loader-view
                      (if empty-type? 56 80)
                      16
                      watch-only?
                      theme)
                     :margin-top
                     13)}]
     (when metrics?
       [rn/view
        {:accessibility-label :metrics
         :style               (assoc (style/loader-view
                                      (if empty-type? 37 96)
                                      8
                                      watch-only?
                                      theme)
                                     :margin-top
                                     10)}])]))

(def user-account
  (let [pressed? (reagent/atom false)]
    (fn [{:keys [state name balance percentage-value loading? amount customization-color type emoji
                 metrics?
                 theme on-press]}]
      (let [watch-only?        (= :watch-only type)
            empty-type?        (= :empty type)
            account-amount     (if (= :empty state) "€0.00" amount)
            account-name       (if (= :empty state) (i18n/label :t/Account 1) name)
            account-percentage (if (= :empty state) "€0.00" percentage-value)]
        (if loading?
          [loading-view
           {:customization-color customization-color
            :type                type
            :theme               theme
            :metrics?            metrics?}]
          [rn/pressable
           {:on-press-in  #(reset! pressed? true)
            :on-press-out #(reset! pressed? false)
            :style        (style/card customization-color watch-only? metrics? theme @pressed?)
            :on-press     on-press}
           (when (and customization-color (not= :watch-only type))
             [customization-colors/overlay
              {:customization-color customization-color
               :border-radius       16
               :theme               theme
               :pressed?            @pressed?}])
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
               {:weight              :semi-bold
                :size                :paragraph-2
                :accessibility-label :metrics
                :style               (style/metrics watch-only? theme)}
               account-percentage]
              (when (not empty-type?)
                [:<>
                 [rn/view (style/separator watch-only? theme)]
                 [text/text
                  {:weight :semi-bold
                   :size   :paragraph-2
                   :style  (style/metrics watch-only? theme)} account-amount]
                 [rn/view {:style {:margin-left 4}}
                  [icon/icon :positive
                   {:color (colors/theme-colors (if watch-only? colors/neutral-50 colors/white-opa-70)
                                                colors/white-opa-70
                                                theme)
                    :size  16}]]])])])))))

(def add-account-view
  (let [pressed? (reagent/atom false)]
    (fn [{:keys [on-press customization-color theme metrics?]}]
      [rn/pressable
       {:on-press     on-press
        :on-press-in  #(reset! pressed? true)
        :on-press-out #(reset! pressed? false)
        :style        (style/add-account-container theme metrics? @pressed?)}
       [button/button
        {:type                :primary
         :size                24
         :icon                true
         :accessibility-label :add-account
         :on-press            on-press
         :pressed?            @pressed?
         :on-press-in         #(reset! pressed? true)
         :on-press-out        #(reset! pressed? false)
         :customization-color customization-color
         :icon-only?          true}
        :i/add]])))

(defn- view-internal
  [{:keys [type] :as props}]
  (case type
    :watch-only  [user-account props]
    :add-account [add-account-view props]
    :default     [user-account props]
    :empty       [user-account props]
    nil))

(def view (theme/with-theme view-internal))
