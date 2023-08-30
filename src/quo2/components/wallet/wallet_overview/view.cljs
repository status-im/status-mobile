(ns quo2.components.wallet.wallet-overview.view
  (:require [quo2.components.icon :as icons]
            [quo2.components.markdown.text :as text]
            [react-native.core :as rn]
            [utils.i18n :as i18n]
            [quo2.components.wallet.wallet-overview.style :as style]
            [quo2.theme :as quo.theme]))

(def ^:private time-frames
  {:one-week     (i18n/label :t/one-week-int)
   :one-month    (i18n/label :t/one-month-int)
   :three-months (i18n/label :t/three-months-int)
   :one-year     (i18n/label :t/one-year)
   :all-time     (i18n/label :t/all-time)})

(defn- loading-bars
  [bars theme]
  (map (fn [{:keys [width height margin]}]
         [rn/view {:style (style/loading-bar width height margin theme)}])
       bars))

;; temporary placeholder for network dropdown component
(defn- network-dropdown-temporary
  []
  [:<>
   [rn/view
    {:style (merge style/network-dropdown
                   {:justify-content :center
                    :align-items     :center})}
    [text/text "[WIP]"]]])

(defn- view-info-top
  [{:keys [state balance theme]}]
  [rn/view {:style style/container-info-top}
   (if (= state :loading)
     (loading-bars [{:width 201 :height 20 :margin 0}] theme)
     [text/text
      {:weight :semi-bold
       :size   :heading-1
       :style  (style/style-text-heading theme)}
      balance])
   [network-dropdown-temporary]])

(defn- view-metrics
  [{:keys [metrics currency-change percentage-change theme]}]
  [rn/view
   {:style {:flex-direction :row
            :align-items    :center}}
   [text/text
    {:weight :medium
     :size   :paragraph-2
     :style  {:color (style/color-metrics metrics theme)}}
    percentage-change]
   [rn/view
    {:style (style/dot-separator metrics theme)}]
   [text/text
    {:weight :medium
     :size   :paragraph-2
     :style  {:color        (style/color-metrics metrics theme)
              :margin-right 4}}
    currency-change]
   [icons/icon
    (if (= metrics :positive) :i/positive :i/negative)
    {:color (style/color-metrics metrics theme)
     :size  16}]])

(defn- view-info-bottom
  [{:keys [state time-frame metrics date begin-date end-date
           currency-change percentage-change theme]}]
  [rn/view {:style style/container-info-bottom}
   (when (= state :loading)
     [rn/view
      {:style {:flex-direction :row
               :align-items    :center}}
      (loading-bars [{:width 32 :height 10 :margin 8}
                     {:width 32 :height 10 :margin 2}
                     {:width 62 :height 10 :margin 4}
                     {:width 10 :height 10 :margin 0}]
                    theme)])
   (when (and (= state :default) (= time-frame :selected))
     [text/text
      {:weight :medium
       :size   :paragraph-2
       :style  (style/style-text-paragraph theme)}
      date])
   (when (and (= state :default) (= time-frame :custom))
     [rn/view {:style {:flex-direction :row}}
      [text/text
       {:weight :medium
        :size   :paragraph-2
        :style  (style/style-text-paragraph theme)}
       begin-date]
      [icons/icon :i/positive-right
       {:color (style/color-text-paragraph theme)
        :size  16}]
      [text/text
       {:weight :medium
        :size   :paragraph-2
        :style  (style/style-text-paragraph theme)}
       end-date]])
   (when (and (= state :default) (not (#{:none :selected} time-frame)))
     [rn/view {:style {:flex-direction :row}}
      [text/text
       {:weight :medium
        :size   :paragraph-2
        :style  {:color        (style/color-text-paragraph theme)
                 :margin-right 8}}
       (time-frame time-frames)]
      (when (and (= state :default) (not= metrics :none))
        [view-metrics
         {:metrics           metrics
          :currency-change   currency-change
          :percentage-change percentage-change
          :theme             theme}])])])

(defn- view-internal
  [props]
  [rn/view {:style style/container-info}
   [view-info-top props]
   [view-info-bottom props]])

(def view
  (quo.theme/with-theme view-internal))
