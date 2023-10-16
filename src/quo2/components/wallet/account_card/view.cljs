(ns quo2.components.wallet.account-card.view
  (:require
    [quo2.components.buttons.button.view :as button]
    [quo2.components.icon :as icon]
    [quo2.components.markdown.text :as text]
    [quo2.components.wallet.account-card.style :as style]
    [quo2.foundations.colors :as colors]
    [quo2.foundations.customization-colors :as customization-colors]
    [quo2.theme :as quo.theme]
    [react-native.core :as rn]
    [reagent.core :as reagent]))

(defn- loading-view
  [{:keys [customization-color type theme metrics?]}]
  (let [watch-only? (= :watch-only type)
        empty-type? (= :empty type)]
    [rn/view
     {:accessibility-label :loading
      :style               (style/card {:customization-color customization-color
                                        :watch-only?         watch-only?
                                        :metrics?            metrics?
                                        :theme               theme
                                        :pressed?            false})}
     [rn/view {:style style/loader-container}
      [rn/view
       {:style (assoc (style/loader-view {:width       16
                                          :height      16
                                          :watch-only? watch-only?
                                          :theme       theme})
                      :margin-right 8
                      :margin-top   2)}]
      [rn/view {:style style/watch-only-container}
       [rn/view
        {:style (style/loader-view {:width       57
                                    :height      8
                                    :watch-only? watch-only?
                                    :theme       theme})}]
       (when watch-only? [icon/icon :i/reveal {:color colors/neutral-50 :size 12}])]]
     [rn/view
      {:style (assoc (style/loader-view {:width       (if empty-type? 56 80)
                                         :height      16
                                         :watch-only? watch-only?
                                         :theme       theme})
                     :margin-top
                     13)}]
     (when metrics?
       [rn/view
        {:accessibility-label :metrics
         :style               (assoc (style/loader-view {:width       (if empty-type? 37 96)
                                                         :height      8
                                                         :watch-only? watch-only?
                                                         :theme       theme})
                                     :margin-top
                                     10)}])]))

(defn- metrics-percentage
  [watch-only? theme account-percentage]
  [text/text
   {:weight              :semi-bold
    :size                :paragraph-2
    :accessibility-label :metrics
    :style               (style/metrics watch-only? theme)}
   account-percentage])

(defn- metrics-info
  [watch-only? theme account-amount]
  [:<>
   [rn/view (style/separator watch-only? theme)]
   [text/text
    {:weight :semi-bold
     :size   :paragraph-2
     :style  (style/metrics watch-only? theme)}
    account-amount]
   [rn/view {:style style/metrics-icon-container}
    [icon/icon
     :i/positive
     {:color (colors/theme-colors (if watch-only? colors/neutral-50 colors/white-opa-70)
                                  colors/white-opa-70
                                  theme)
      :size  16}]]])

(defn- user-account
  []
  (let [pressed?     (reagent/atom false)
        on-press-in  #(reset! pressed? true)
        on-press-out #(reset! pressed? false)]
    (fn [{:keys [name balance percentage-value loading? amount customization-color type emoji metrics?
                 theme on-press]}]
      (let [watch-only? (= :watch-only type)]
        (if loading?
          [loading-view
           {:customization-color customization-color
            :type                type
            :theme               theme
            :metrics?            metrics?}]
          [rn/pressable
           {:on-press-in  on-press-in
            :on-press-out on-press-out
            :style        (style/card {:customization-color customization-color
                                       :watch-only?         watch-only?
                                       :metrics?            metrics?
                                       :theme               theme
                                       :pressed?            @pressed?})
            :on-press     on-press}
           (when (and customization-color (not watch-only?))
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
              name]
             (when watch-only? [icon/icon :i/reveal {:color colors/neutral-50 :size 12}])]]
           [text/text
            {:size   :heading-2
             :weight :semi-bold
             :style  (style/account-value watch-only? theme)}
            balance]
           (when metrics?
             [rn/view {:style style/metrics-container}
              [metrics-percentage watch-only? theme percentage-value]
              (when (not= :empty type)
                [metrics-info watch-only? theme amount])])])))))

(defn- add-account-view
  []
  (let [pressed? (reagent/atom false)]
    (fn [{:keys [on-press customization-color theme metrics?]}]
      [rn/pressable
       {:on-press     on-press
        :on-press-in  #(reset! pressed? true)
        :on-press-out #(reset! pressed? false)
        :style        (style/add-account-container {:theme    theme
                                                    :metrics? metrics?
                                                    :pressed? @pressed?})}
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

(def view (quo.theme/with-theme view-internal))
