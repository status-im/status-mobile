(ns quo.components.wallet.approval-label.view
  (:require [oops.core :as oops]
            [quo.components.buttons.button.view :as button]
            [quo.components.icon :as icon]
            [quo.components.markdown.text :as text]
            [quo.components.wallet.approval-label.schema :as approval-label.schema]
            [quo.components.wallet.approval-label.style :as style]
            [quo.foundations.colors :as colors]
            quo.theme
            [react-native.core :as rn]
            [react-native.hole-view :as hole-view]
            [schema.core :as schema]
            [utils.i18n :as i18n]))

(def ^:private status-icons
  {:approve   :i/alert
   :approving :i/pending-state
   :approved  :i/check})

(def ^:private status-message
  {:approve   :t/approve-amount-symbol
   :approving :t/approving-amount-symbol
   :approved  :t/approved-amount-symbol})

(defn- view-internal
  [{:keys [status token-value token-symbol
           container-style button-props show-view-button?]
    :as   props}]
  (let [theme                                 (quo.theme/use-theme)
        customization-color                   (or (:customization-color props) :blue)
        [container-width set-container-width] (rn/use-state 0)
        on-layout                             (rn/use-callback
                                               #(set-container-width
                                                 (oops/oget % :nativeEvent :layout :width)))]
    [hole-view/hole-view
     {:holes     [{:x                       0
                   :y                       0
                   :height                  style/top-hole-view-height
                   :width                   (int container-width)
                   :borderBottomStartRadius 16
                   :borderBottomEndRadius   16}]
      :style     {:margin-top (- style/top-hole-view-height)}
      :on-layout on-layout}
     [rn/view
      {:style               (merge (style/container customization-color theme)
                                   container-style)
       :accessibility-label :approval-label}
      [rn/view {:style style/content}
       [icon/icon
        (status-icons status)
        {:color (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)
         :size  16}]
       [text/text
        {:size  :paragraph-2
         :style (style/message theme)}
        (i18n/label (status-message status)
                    {:amount token-value
                     :symbol token-symbol})]]
      (when (and button-props
                 (or (= status :approve)
                     (and (not= status :approve) show-view-button?)))
        [button/button
         (merge {:type                (if (= status :approve) :primary :grey)
                 :background          (when-not (= status :approve) :blur)
                 :customization-color customization-color
                 :size                24}
                button-props)
         (i18n/label (if (= status :approve)
                       :t/approve
                       :t/view))])]]))

(def view (schema/instrument #'view-internal approval-label.schema/?schema))
