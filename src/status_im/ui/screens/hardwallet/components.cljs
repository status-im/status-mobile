(ns status-im.ui.screens.hardwallet.components
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [reagent.core :as reagent]
            [status-im.ui.components.animation :as animation]
            [status-im.ui.screens.hardwallet.setup.styles :as styles]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.react :as react]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]))

(defn maintain-card []
  (let [animation-value (animation/create-value 0)
        ;TODO(dmitryn): make animation smoother
        interpolate-fn (fn [output-range]
                         (animation/interpolate animation-value
                                                {:inputRange  [0 0.25 0.5 0.75 1]
                                                 :outputRange output-range}))]
    (reagent/create-class
     {:component-did-mount (fn []
                             (-> animation-value
                                 (animation/timing {:toValue         1
                                                    :duration        1000
                                                    :useNativeDriver true})
                                 (animation/anim-loop)
                                 (animation/start)))
      :display-name        "maintain-card"
      :reagent-render      (fn [] [react/view styles/maintain-card-container
                                   [react/view styles/hardwallet-icon-container
                                    [vector-icons/icon :icons/hardwallet {:color colors/blue}]
                                    [vector-icons/icon :icons/indicator-small {:color           colors/blue
                                                                               :container-style (styles/hardwallet-icon-indicator-small-container
                                                                                                 (interpolate-fn [0 0.5 1 0.5 0]))}]
                                    [vector-icons/icon :icons/indicator-middle {:color           colors/blue
                                                                                :container-style (styles/hardwallet-icon-indicator-middle-container
                                                                                                  (interpolate-fn [1 0.4 0 0.4 0.8]))}]
                                    [vector-icons/icon :icons/indicator-big {:color           colors/blue
                                                                             :container-style (styles/hardwallet-icon-indicator-big-container
                                                                                               (interpolate-fn [0.5 0.8 0.5 0.8 0.4]))}]]
                                   [react/text {:style           styles/maintain-card-text
                                                :number-of-lines 2}
                                    (i18n/label :t/maintain-card-to-phone-contact)]])})))

(defn- wizard-step [step-number]
  (when step-number
    [react/text {:style styles/wizard-step-text}
     (i18n/label :wizard-step {:current step-number
                               :total   5})]))
