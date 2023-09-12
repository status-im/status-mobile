(ns quo2.components.drawers.action-drawers.view
  (:require [react-native.core :as rn]
            [quo2.components.drawers.action-drawers.style :as style]
            [quo2.components.icon :as icon]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [quo2.theme :as quo.theme]))

(defn- get-icon-color
  [danger? theme]
  (if danger?
    (colors/theme-colors colors/danger-50 colors/danger-60 theme)
    (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)))

(defn- divider
  [theme]
  [rn/view
   {:style               (style/divider theme)
    :accessible          true
    :accessibility-label :divider}])

(defn- maybe-pressable
  [disabled? props child]
  (if disabled?
    [rn/view (dissoc props :on-press) child]
    [rn/touchable-highlight props child]))

(defn- action-internal
  [{:keys [icon
           label
           sub-label
           right-icon
           right-text
           danger?
           disabled?
           on-press
           add-divider?
           theme
           accessibility-label
           icon-color]}]
  [:<>
   (when add-divider?
     [divider theme])
   [maybe-pressable disabled?
    {:accessibility-label accessibility-label
     :style               (style/container sub-label disabled?)
     :underlay-color      (colors/theme-colors colors/neutral-5 colors/neutral-90 theme)
     :on-press            on-press}
    [rn/view
     {:style (style/row-container sub-label)}
     [rn/view
      {:accessibility-label :left-icon-for-action
       :accessible          true
       :style               (style/left-icon sub-label)}
      [icon/icon icon
       {:color (or icon-color (get-icon-color danger? theme))
        :size  20}]]
     [rn/view
      {:style style/text-container}
      [text/text
       {:size   :paragraph-1
        :weight :medium
        :style  {:color
                 (cond
                   danger? (colors/theme-colors colors/danger-50 colors/danger-60 theme)
                   :else   (colors/theme-colors colors/neutral-100 colors/white theme))}}
       label]
      (when sub-label
        [text/text
         {:size  :paragraph-2
          :style {:color
                  (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)}}
         sub-label])]
     (when (or right-text right-icon)
       [rn/view {:style style/right-side-container}
        (when right-text
          [text/text
           {:accessibility-label :right-text-for-action
            :size                :paragraph-1
            :style               (style/right-text theme)}
           right-text])
        (when right-icon
          [rn/view
           {:style               style/right-icon
            :accessible          true
            :accessibility-label :right-icon-for-action}
           [icon/icon right-icon
            {:color (get-icon-color danger? theme)
             :size  20}]])])]]])

(def ^:private action (quo.theme/with-theme action-internal))

(defn action-drawer
  [sections]
  [:<>
   (doall
    (for [actions sections]
      (let [filtered-actions (filter some? actions)]
        (doall
         (map #(with-meta [action %] {:key (:label %)}) filtered-actions)))))])
