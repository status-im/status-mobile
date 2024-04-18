(ns quo.components.wallet.wallet-overview.view
  (:require
    [quo.components.dropdowns.network-dropdown.view :as network-dropdown]
    [quo.components.icon :as icon]
    [quo.components.markdown.text :as text]
    [quo.components.wallet.wallet-overview.schema :as component-schema]
    [quo.components.wallet.wallet-overview.style :as style]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [schema.core :as schema]
    [utils.i18n :as i18n]))

(def ^:private time-frames
  {:one-week     (i18n/label :t/one-week-int)
   :one-month    (i18n/label :t/one-month-int)
   :three-months (i18n/label :t/three-months-int)
   :one-year     (i18n/label :t/one-year)
   :all-time     (i18n/label :t/all-time)})

(defn- loading-bars
  [bars theme]
  (map-indexed (fn [index {:keys [width height margin]}]
                 ^{:key index}
                 [rn/view {:style (style/loading-bar width height margin theme)}])
               bars))

(defn- view-info-top
  [{:keys [state balance networks dropdown-on-press dropdown-state]}]
  (let [theme (quo.theme/use-theme)]
    [rn/view {:style style/container-info-top}
     (if (= state :loading)
       (loading-bars [{:width 201 :height 20 :margin 0}] theme)
       [text/text
        {:weight :semi-bold
         :size   :heading-1
         :style  (style/style-text-heading theme)}
        balance])
     [network-dropdown/view
      {:state    (or dropdown-state :default)
       :blur?    true
       :on-press dropdown-on-press}
      networks]]))

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
   [icon/icon
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
      [icon/icon :i/positive-right
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

(def view (schema/instrument #'view-internal component-schema/?schema))
