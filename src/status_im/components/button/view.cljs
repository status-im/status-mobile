(ns status-im.components.button.view
  (:require [status-im.components.button.styles :as button.styles]
            [status-im.components.react :as react]
            [status-im.utils.platform :as platform]))

(defn- button [{:keys [on-press style text text-style disabled? fit-to-text?]}]
  [react/touchable-highlight (merge {:underlay-color button.styles/border-color-high}
                                    (when-not fit-to-text?
                                      {:style button.styles/button-container})
                                    (when (and on-press
                                               (not disabled?))
                                      {:on-press on-press}))
   [react/view {:style (merge (button.styles/button disabled?)
                              style)}
    [react/text {:style   (merge (button.styles/button-text disabled?)
                                 text-style)
                 :font       :medium
                 :uppercase? platform/android?}
     text]]])

(defn primary-button [{:keys [style text-style] :as m}]
  (button (assoc m
                 :fit-to-text? true
                 :style        (merge button.styles/primary-button style)
                 :text-style   (merge button.styles/primary-button-text text-style))))

(defn secondary-button [{:keys [style text-style] :as m}]
  (button (assoc m
                 :fit-to-text? true
                 :style        (merge button.styles/secondary-button style)
                 :text-style   (merge button.styles/secondary-button-text text-style))))

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
      (fn [i m] ^{:key i} [button (merge m
                                         {:style (button.styles/button-bar (position i v))
                                          :text-style button-text-style})])
      v))]))
