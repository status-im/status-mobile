(ns quo.components.list-items.account.view
  (:require
    [quo.components.avatars.account-avatar.view :as account-avatar]
    [quo.components.icon :as icon]
    [quo.components.list-items.account.style :as style]
    [quo.components.markdown.text :as text]
    [quo.components.wallet.address-text.view :as address-text]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [reagent.core :as reagent]))

(defn- account-view
  [{:keys [account-props title-icon blur? theme]}]
  [rn/view {:style style/left-container}
   [account-avatar/view (assoc account-props :size 32)]
   [rn/view {:style style/account-container}
    [rn/view
     {:style style/account-title-container}
     [text/text
      {:weight :semi-bold
       :size   :paragraph-1}
      (:name account-props)]
     (when title-icon
       [icon/icon title-icon
        {:size                20
         :color               (if blur?
                                colors/white-opa-40
                                (colors/theme-colors colors/neutral-50 colors/neutral-40 theme))
         :container-style     style/title-icon-container
         :accessibility-label :title-icon}])]
    [address-text/view
     {:networks (:networks account-props)
      :address  (:address account-props)
      :format   :short}]]])

(defn- balance-view
  [{:keys [balance-props type theme]}]
  [rn/view
   {:style               style/balance-container
    :accessibility-label :balance-container}
   [text/text
    {:weight :medium
     :size   :paragraph-2}
    (:fiat-value balance-props)]
   [rn/view
    {:style style/metrics-container}
    [text/text
     {:size  :paragraph-2
      :style (style/metric-text type theme)}
     (str (:percentage-change balance-props) "%")]
    [rn/view {:style (style/dot-divider type theme)}]
    [text/text
     {:size  :paragraph-2
      :style (style/metric-text type theme)}
     (:fiat-change balance-props)]
    (when (not= type :balance-neutral)
      [rn/view
       {:style               style/arrow-icon-container
        :accessibility-label :arrow-icon}
       [icon/icon (if (= type :balance-positive) :i/positive :i/negative)
        (assoc (style/arrow-icon type theme)
               :accessibility-label
               (if (= type :balance-positive) :icon-positive :icon-negative))]])]])

(defn- token-tag
  [{:keys [token-props blur? theme]}]
  ;; TODO: Use Tiny tag component when available (issue #17341)
  [rn/view
   {:style               (style/token-tag-text-container blur? theme)
    :accessibility-label :tag-container}
   [text/text
    {:size   :label
     :weight :medium
     :style  (style/token-tag-text blur? theme)}
    (str (:value token-props) " " (:symbol token-props))]])

(defn- options-button
  [{:keys [on-options-press blur? theme]}]
  [rn/pressable
   {:accessibility-label :options-button
    :on-press            #(when on-options-press
                            (on-options-press))}
   [icon/icon :i/options
    {:color (if blur?
              colors/white-opa-70
              (colors/theme-colors colors/neutral-50 colors/neutral-40 theme))}]])

(defn- check-icon
  [{:keys [blur? customization-color theme]}]
  [rn/view {:accessibility-label :check-icon}
   [icon/icon :i/check
    {:color (if blur?
              colors/white
              (colors/resolve-color customization-color theme))}]])

(defn- internal-view
  [_]
  (let [pressed?     (reagent/atom false)
        on-press-in  #(reset! pressed? true)
        on-press-out #(reset! pressed? false)]
    (fn [{:keys [type state blur? customization-color on-press]
          :or   {customization-color :blue
                 type                :default
                 state               :default
                 blur?               false}
          :as   props}]
      [rn/pressable
       {:style               (style/container
                              {:state               state
                               :blur?               blur?
                               :customization-color customization-color
                               :pressed?            @pressed?})
        :on-press-in         on-press-in
        :on-press            on-press
        :on-press-out        on-press-out
        :accessibility-label :container}
       [account-view props]
       [rn/view {:style (when (= type :tag) style/token-tag-container)}
        (cond
          (#{:balance-neutral :balance-negative :balance-positive} type)
          [balance-view props]

          (= type :tag)
          [token-tag props]

          (= type :action)
          [options-button props]

          (and (= type :default) (= state :selected))
          [check-icon props])]])))

(def view (quo.theme/with-theme internal-view))
