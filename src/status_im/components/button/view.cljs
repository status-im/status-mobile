(ns status-im.components.button.view
  (:require [cljs.spec.alpha :as s]
            [status-im.components.button.styles :as cst]
            [status-im.components.react :as rn]
            [status-im.utils.platform :as p]))

(defn button [{:keys [on-press style text text-style disabled?]
               :or {style cst/action-button}}]
  [rn/touchable-highlight (when (and on-press (not disabled?)) {:on-press on-press})
   [rn/view {:style style}
    [rn/text {:style      (or text-style
                              (if disabled? cst/action-button-text-disabled cst/action-button-text))
              :font       :medium
              :uppercase? p/android?}
     text]]])

(defn primary-button [m]
  (button (merge {:style cst/primary-button :text-style cst/primary-button-text} m)))

(defn secondary-button [m]
  (button (merge {:style cst/secondary-button :text-style cst/secondary-button-text} m)))

(defn- first-or-last [i v] (or (zero? i) (= i (dec (count v)))))

(defn- button-style [i v] (if (first-or-last i v) cst/action-button cst/action-button-center))

(defn buttons
  ([v] (buttons {} v))
  ([m v]
   [rn/view {:style (merge cst/action-buttons-container m)}
    (doall
      (map-indexed
        (fn [i m] ^{:key i} [button (merge m {:style (button-style i v)})])
        v))]))