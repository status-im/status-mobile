(ns quo2.components.wallet.account-overview.view
  (:require [quo2.components.icon :as icons]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [utils.i18n :as i18n]
            [quo2.components.wallet.account-overview.style :as style]
            [quo2.theme :as quo.theme]))

(defn- loading-state
  [color]
  [:<>
   [rn/view
    (style/loading-bar-margin-bottom {:color         color
                                      :width         104
                                      :height        14
                                      :margin-bottom 14})]
   [rn/view
    (style/loading-bar-margin-bottom {:color         color
                                      :width         152
                                      :height        20
                                      :margin-bottom 12})]
   [rn/view {:style {:flex-direction :row}}
    [rn/view {:style (style/loading-bar-margin-right color)}]
    [rn/view {:style (style/loading-bar-margin-right color)}]
    [rn/view
     {:style (style/loading-bar-margin-right-extra {:color        color
                                                    :width        62
                                                    :height       10
                                                    :margin-right 4})}]
    [rn/view {:style (style/loading-bar-tiny color)}]]])

(defn- time-string
  [time-frame time-frame-string]
  (case time-frame
    :one-week     (i18n/label :t/one-week-int)
    :one-month    (i18n/label :t/one-month-int)
    :three-months (i18n/label :t/three-months-int)
    :one-year     (i18n/label :t/one-year)
    :all-time     (i18n/label :t/all-time)
    time-frame-string))

(defn- custom-time-frame
  [time-frame-to-string]
  [:<>
   [icons/icon :i/positive-right
    {:size            16
     :container-style style/right-arrow
     :color           colors/neutral-80-opa-90}]
   [text/text
    {:weight :medium
     :size   :paragraph-2
     :style  style/bottom-time-to-text}
    time-frame-to-string]])

(defn- numeric-changes
  [percentage-change currency-change customization-color theme up?]
  [:<>
   [text/text
    {:weight :medium
     :size   :paragraph-2
     :style  (style/percentage-change customization-color theme)}
    percentage-change]
   [rn/view
    {:style (style/dot-separator customization-color theme)}]
   [text/text
    {:weight :medium
     :size   :paragraph-2
     :style  (style/currency-change customization-color theme)}
    currency-change]
   [icons/icon
    (if up?
      :i/positive
      :i/negative)
    (style/icon-props customization-color theme)]])

(defn- account-details
  [account-name account theme]
  [rn/view {:style {:flex-direction :row}}
   [text/text
    {:weight :medium
     :size   :paragraph-1
     :style  (style/account-name (colors/theme-colors colors/neutral-100 colors/white theme))}
    account-name]
   (when (= account :watched-address)
     [icons/icon :i/reveal
      {:container-style style/reveal-icon
       :color           (colors/theme-colors colors/neutral-80-opa-40
                                             colors/white-opa-40
                                             theme)}])])

(defn- view-internal
  [{:keys [state account time-frame time-frame-string time-frame-to-string account-name current-value
           percentage-change currency-change theme metrics customization-color]
    :or   {customization-color :blue}}]
  (let [time-frame-string (time-string time-frame time-frame-string)
        up?               (= metrics :positive)]
    [rn/view
     {:style style/account-overview-wrapper}
     (if (= :loading state)
       [loading-state (colors/theme-colors colors/neutral-5 colors/neutral-90 theme)]
       [:<>
        [account-details account-name account theme]
        [text/text
         {:weight :semi-bold
          :size   :heading-1
          :style  style/current-value}
         current-value]
        [rn/view
         {:style style/row-centered}
         [:<>
          (when (seq time-frame-string)
            [text/text
             {:weight :medium
              :size   :paragraph-2
              :style  (style/bottom-time-text (and (not= :custom time-frame)
                                                   (seq time-frame-to-string)))}
             time-frame-string])
          (when (and (= :custom time-frame)
                     (seq time-frame-to-string))
            [custom-time-frame time-frame-to-string])]
         (when (and (seq percentage-change)
                    (seq currency-change))
           [numeric-changes percentage-change currency-change customization-color theme up?])]])]))

(def view
  "Create a account-overview UI component.
     | key          | description |
  | -------------|-------------|
  | `:account-name`      | A value representing the account name (default `nil`)
  | `:account`           | A value that represents if the account is a `:watched-account` or a `:default` account. (default `nil`)
  | `:time-frame`        | A value that represents the type of the timeframe, Can be from a preset of time frames. `[:1-week :1-month :3-months :1-year :custom]` (default `nil`) if custom is set, We expect a start time and if there's a space between two times that are in `time-frame-string` we'll split them with an arrow to the right icon
  | `:loading?`          | A value that indicates that component is loading data. (default `nil`)
  | `:metrics`           | A value that indicates if the account value have increased can be `:positive` or `:negative` (default `nil`)
  | `:time-frame-string` | A value representing time frame string. (default `nil`)
  | `:current-value`     | A value representing the current value of the selected account. (default `nil`)
  | `:currency-change`   | A value representing the change of the value of the selected account in the selected currency. (default `nil`)
  | `:percentage-change` | A value representing the change of the value of the selected account in percentage relative to it's previous value. (default `nil`)"
  (quo.theme/with-theme view-internal))
