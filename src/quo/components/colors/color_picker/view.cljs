(ns quo.components.colors.color-picker.view
  (:require
    [quo.components.colors.color.constants :as constants]
    [quo.components.colors.color.view :as color]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [react-native.gesture :as gesture]))

(defn get-item-layout
  [_ index]
  #js
   {:length constants/color-size
    :offset (* (+ constants/color-size 8) index)
    :index  index})

(defn view
  "Options
   - `default-selected` Default selected color name.
   - `on-change` Callback called when a color is selected `(fn [color-name])`.
   - `blur?` Boolean to enable blur background support.}"
  [{:keys [default-selected blur? on-change feng-shui? container-style]}]
  (let [[selected set-selected] (rn/use-state default-selected)
        {window-width :width}   (rn/get-window)
        ref                     (rn/use-ref-atom nil)
        on-ref                  (rn/use-callback #(reset! ref %))
        render-fn               (rn/use-callback
                                 (fn [color idx]
                                   [color/view
                                    {:selected?    (= color selected)
                                     :on-press     (fn [color-name]
                                                     (.scrollToIndex ^js @ref
                                                                     #js
                                                                      {:animated     true
                                                                       :index        idx
                                                                       :viewPosition 0.5})
                                                     (set-selected color-name)
                                                     (when on-change (on-change color-name)))
                                     :blur?        blur?
                                     :key          color
                                     :color        color
                                     :idx          idx
                                     :window-width window-width}])
                                 [selected blur? on-change @ref])]
    (rn/use-mount
     (fn []
       (js/setTimeout
        (fn []
          (let [index (.indexOf colors/account-colors default-selected)]
            (when (and @ref (>= index 0))
              (some-> ^js @ref
                      (.scrollToIndex #js
                                       {:animated     false
                                        :index        index
                                        :viewPosition 0.5})))))
        50)))
    [gesture/flat-list
     {:ref                               on-ref
      ;; TODO: using :feng-shui? temporarily while b & w is being developed.
      ;; https://github.com/status-im/status-mobile/discussions/16676
      :data                              (if feng-shui?
                                           (conj colors/account-colors :feng-shui)
                                           colors/account-colors)
      :render-fn                         render-fn
      :get-item-layout                   get-item-layout
      :horizontal                        true
      :shows-horizontal-scroll-indicator false
      :content-container-style           container-style}]))
