(ns status-im.ui.components.button.view
  (:require [status-im.ui.components.button.styles :as button.styles]
            [status-im.ui.components.react :as react]
            [status-im.utils.platform :as platform]))

(defn button-text [{:keys [disabled? text-style]} label]
  [react/text {:style      (merge (button.styles/button-text disabled?)
                                  text-style)
               :font       (if platform/android? :medium :default)
               :uppercase? (get-in platform/platform-specific [:uppercase?])}
   label])

(defn button [{:keys [on-press style disabled? fit-to-text?] :as props} label]
  [react/touchable-highlight (merge {:underlay-color button.styles/border-color-high}
                                    (when-not fit-to-text?
                                      {:style button.styles/button-container})
                                    (when (and on-press (not disabled?))
                                      {:on-press on-press}))
   [react/view {:style (merge (button.styles/button disabled?)
                              style)}
    [button-text props
     label]]])

(defn primary-button [{:keys [style text-style] :as m} label]
  [button (assoc m
                 :fit-to-text? true
                 :style        (merge button.styles/primary-button style)
                 :text-style   (merge button.styles/primary-button-text text-style))
   label])

(defn secondary-button [{:keys [style text-style] :as m} label]
  [button (assoc m
                 :fit-to-text? true
                 :style        (merge button.styles/secondary-button style)
                 :text-style   (merge button.styles/secondary-button-text text-style))
   label])
