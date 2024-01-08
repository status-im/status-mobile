(ns quo.components.wallet.account-card.view
  (:require
    [quo.components.buttons.button.view :as button]
    [quo.components.icon :as icon]
    [quo.components.markdown.text :as text]
    [quo.components.wallet.account-card.properties :as properties]
    [quo.components.wallet.account-card.style :as style]
    [quo.foundations.colors :as colors]
    [quo.foundations.customization-colors :as customization-colors]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.linear-gradient :as linear-gradient]
    [reagent.core :as reagent]))

(defn- loading-view
  [{:keys [customization-color type theme metrics?]}]
  (let [watch-only? (= :watch-only type)
        empty-type? (= :empty type)]
    [rn/view
     {:accessibility-label :loading
      :style               (style/card {:customization-color customization-color
                                        :type                type
                                        :theme               theme
                                        :pressed?            false
                                        :metrics?            metrics?})}
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
  [type theme account-percentage]
  [text/text
   {:weight              :medium
    :size                :paragraph-2
    :accessibility-label :metrics
    :style               (style/metrics type theme)}
   account-percentage])

(defn- metrics-info
  [type theme account-amount]
  [:<>
   [rn/view (style/separator type theme)]
   [text/text
    {:weight :medium
     :size   :paragraph-2
     :style  (style/metrics type theme)}
    account-amount]
   [rn/view {:style style/metrics-icon-container}
    [icon/icon
     :i/positive
     {:color (colors/theme-colors (if (or (= :missing-keypair type)
                                          (= :watch-only type))
                                    colors/neutral-50
                                    colors/white-opa-70)
                                  colors/white-opa-70
                                  theme)
      :size  16}]]])

(defn- gradient-overview
  [theme customization-color]
  [linear-gradient/linear-gradient
   {:colors [(properties/gradient-start-color theme customization-color)
             (properties/gradient-end-color theme customization-color)]
    :style  style/gradient-view
    :start  {:x 0 :y 0}
    :end    {:x 1 :y 0}}])

(defn- user-account
  [_]
  (let [pressed?     (reagent/atom false)
        on-press-in  #(reset! pressed? true)
        on-press-out #(reset! pressed? false)]
    (fn [{:keys [name balance percentage-value loading? amount customization-color type emoji metrics?
                 theme on-press]}]
      (let [watch-only?      (= :watch-only type)
            missing-keypair? (= :missing-keypair type)]
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
                                       :type                type
                                       :theme               theme
                                       :pressed?            @pressed?
                                       :metrics?            metrics?})
            :on-press     on-press}
           (when (and customization-color (and (not watch-only?) (not missing-keypair?)))
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
              {:size            :paragraph-2
               :weight          :medium
               :number-of-lines 1
               :max-width       110
               :margin-right    4
               :ellipis-mode    :tail
               :style           (style/account-name type theme)}
              name]
             (when watch-only? [icon/icon :i/reveal {:color colors/neutral-50 :size 12}])
             (when missing-keypair?
               [icon/icon :i/alert {:color (properties/alert-icon-color theme) :size 12}])]]
           [text/text
            {:size   :heading-2
             :weight :semi-bold
             :style  (style/account-value type theme)}
            balance]
           (when metrics?
             [rn/view {:style style/metrics-container}
              [metrics-percentage type theme percentage-value]
              (when (not= :empty type)
                [metrics-info type theme amount])])
           (when watch-only?
             [gradient-overview theme customization-color])])))))

(defn- add-account-view
  [_]
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
    (:watch-only :default :empty :missing-keypair) [user-account props]
    :add-account                                   [add-account-view props]
    nil))

(def view (quo.theme/with-theme view-internal))
