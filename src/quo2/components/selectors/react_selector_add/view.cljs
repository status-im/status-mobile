(ns quo2.components.selectors.react-selector-add.view
  (:require [quo2.components.icon :as icons]
            [quo2.foundations.colors :as colors]
            [quo2.theme :as theme]
            [react-native.core :as rn]))

(defn- get-color
  [pinned-colors default-colors pinned? theme]
  (apply colors/theme-colors
         (conj (if pinned? pinned-colors default-colors)
               theme)))

(defn- container-style
  [pinned? theme]
  (let [pinned-cols  [colors/neutral-80-opa-5 colors/white-opa-5]
        default-cols [colors/neutral-20 colors/neutral-70]
        border-color (get-color pinned-cols
                                default-cols
                                pinned?
                                theme)]
    {:justify-content    :center
     :align-items        :center
     :padding-horizontal 7
     :border-radius      8
     :border-width       1
     :height             24
     :border-color       border-color}))

(defn- view-internal
  [{:keys [on-press pinned? theme]}]
  (let [pinned-icon-cols  [colors/neutral-80-opa-70 colors/white-opa-70]
        default-icon-cols [colors/neutral-50 colors/neutral-40]]
    [rn/touchable-opacity
     {:on-press            on-press
      :accessibility-label :emoji-reaction-add
      :style               (container-style pinned? theme)}
     [icons/icon :i/add-reaction
      {:size  20
       :color (get-color pinned-icon-cols
                         default-icon-cols
                         pinned?
                         theme)}]]))

(def view (theme/with-theme view-internal))
