(ns status-im.components.button.view
  (:require [status-im.components.button.styles :as button.styles]
            [status-im.components.react :as react]
            [status-im.utils.platform :as platform]))

(defn- button [{:keys [on-press style text text-style disabled?]}]
  [react/touchable-highlight (merge {:style button.styles/button-container :underlay-color button.styles/border-color-high} (when (and on-press (not disabled?)) {:on-press on-press}))
   [react/view {:style (merge (button.styles/button disabled?)
                              style)}
    [react/text {:style   (merge (button.styles/button-text disabled?)
                                 text-style)
              :font       :medium
              :uppercase? platform/android?}
     text]]])

(defn primary-button [m]
  (button (merge {:style button.styles/primary-button :text-style button.styles/primary-button-text} m)))

(defn secondary-button [m]
  (button (merge {:style button.styles/secondary-button :text-style button.styles/secondary-button-text} m)))

(defn- position [i v]
  (cond
    (zero? i) :first
    (= i (dec (count v))) :last
    :else :other))

(defn buttons
  ([v] (buttons nil v))
  ([{:keys [style button-text-style]} v]
   [react/view {:style (merge button.styles/buttons-container style)}
    (doall
      (map-indexed
        (fn [i m] ^{:key i} [button (merge m {:style (button.styles/button-bar (position i v)) :text-style button-text-style})])
        v))]))