(ns quo.components.colors.color-picker.view
  (:require
    [quo.components.colors.color.constants :as constants]
    [quo.components.colors.color.view :as color]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [reagent.core :as reagent]))

(defn- on-change-handler
  [selected color-name on-change]
  (reset! selected color-name)
  (when on-change (on-change color-name)))

(defn get-item-layout
  [_ index]
  #js
   {:length constants/color-size
    :offset (* (+ constants/color-size 8) index)
    :index  index})

(defn- view-internal
  "Options
   - `default-selected` Default selected color name.
   - `on-change` Callback called when a color is selected `(fn [color-name])`.
   - `blur?` Boolean to enable blur background support.}"
  [{:keys [default-selected blur? on-change feng-shui? container-style]}]
  (let [selected              (reagent/atom default-selected)
        {window-width :width} (rn/get-window)
        ref                   (atom nil)]
    (rn/use-effect
     (fn []
       (js/setTimeout
        (fn []
          (let [index (.indexOf colors/account-colors default-selected)]
            (when (and @ref (>= index 0))
              (some-> ^js @ref
                      (.scrollToIndex #js
                                       {:animated     true
                                        :index        index
                                        :viewPosition 0.5})))))
        50)))
    [rn/flat-list
     {:ref                               #(reset! ref %)
      ;; TODO: using :feng-shui? temporarily while b & w is being developed.
      ;; https://github.com/status-im/status-mobile/discussions/16676
      :data                              (if feng-shui?
                                           (conj colors/account-colors :feng-shui)
                                           colors/account-colors)
      :render-fn                         (fn [color idx]
                                           [color/view
                                            {:selected?    (= color @selected)
                                             :on-press     (fn [e]
                                                             (.scrollToIndex ^js @ref
                                                                             #js
                                                                              {:animated     true
                                                                               :index        idx
                                                                               :viewPosition 0.5})
                                                             (on-change-handler selected e on-change))
                                             :blur?        blur?
                                             :key          color
                                             :color        color
                                             :idx          idx
                                             :window-width window-width}])
      :get-item-layout                   get-item-layout
      :horizontal                        true
      :shows-horizontal-scroll-indicator false
      :content-container-style           container-style}]))

(defn view
  [props]
  [:f> view-internal props])
