(ns status-im.ui.screens.onboarding.views
  (:require [status-im.ui.components.react :as react]
            [re-frame.core :as re-frame]
            [status-im.ui.screens.onboarding.styles :as styles]
            [status-im.i18n.i18n :as i18n]
            [quo.design-system.colors :as colors]
            [quo.core :as quo]
            [status-im.ui.components.toolbar :as toolbar]))

(defn learn-more [title content]
  [react/text {:on-press            #(re-frame/dispatch [:bottom-sheet/show-sheet :learn-more
                                                         {:title   (i18n/label title)
                                                          :content (i18n/label content)}])
               :style               (merge (styles/wizard-text) {:color colors/blue})
               :accessibility-label :learn-more}
   (i18n/label :learn-more)])

(defn title-with-description [title description]
  [react/view {:style {:margin-vertical   16
                       :margin-horizontal 32}}
   [quo/text {:style  styles/wizard-title
              :align  :center
              :weight :bold
              :size   :x-large}
    (i18n/label title)]
   [react/text {:style (styles/wizard-text)}
    (i18n/label description)]])

(defn next-button [handler disabled]
  [toolbar/toolbar
   {:show-border? true
    :right        [quo/button
                   {:on-press            handler
                    :accessibility-label :onboarding-next-button
                    :type                :secondary
                    :disabled            disabled
                    :after               :main-icons/next}
                   (i18n/label :t/next)]}])