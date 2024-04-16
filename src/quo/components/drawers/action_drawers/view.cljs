(ns quo.components.drawers.action-drawers.view
  (:require
    [quo.components.drawers.action-drawers.style :as style]
    [quo.components.icon :as icon]
    [quo.components.markdown.text :as text]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]))

(defn- get-icon-color
  [danger? theme]
  (if danger?
    (colors/theme-colors colors/danger-50 colors/danger-60 theme)
    (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)))

(defn- divider
  [theme blur?]
  [rn/view
   {:style               (style/divider theme blur?)
    :accessible          true
    :accessibility-label :divider}])

(defn- maybe-pressable
  [disabled? props child]
  (if disabled?
    [rn/view (dissoc props :on-press) child]
    [rn/touchable-highlight props child]))

(defn action
  [{:keys [icon label sub-label right-icon right-text danger? disabled? on-press add-divider?
           accessibility-label icon-color no-icon-color? state customization-color blur?]}]
  (let [theme (quo.theme/use-theme)]
    [:<>
     (when add-divider?
       [divider theme blur?])
     [maybe-pressable disabled?
      {:accessibility-label accessibility-label
       :style               (style/container {:sub-label           sub-label
                                              :disabled?           disabled?
                                              :state               state
                                              :customization-color customization-color
                                              :blur?               blur?
                                              :theme               theme})
       :underlay-color      (colors/theme-colors colors/neutral-5 colors/neutral-90 theme)
       :on-press            on-press}
      [rn/view
       {:style (style/row-container sub-label)}
       (when icon
         [rn/view
          {:accessibility-label :left-icon-for-action
           :accessible          true
           :style               (style/left-icon sub-label)}
          [icon/icon icon
           {:color    (or icon-color (get-icon-color danger? theme))
            :no-color no-icon-color?
            :size     20}]])
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
       (when (or right-text right-icon (= state :selected))
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
               :size  20}]])
          (when (= state :selected)
            [rn/view {:style style/right-icon}
             [icon/icon :i/check
              {:color (if blur?
                        colors/white
                        (colors/resolve-color customization-color theme))
               :size  20}]])])]]]))

(defn action-drawer
  [sections]
  [:<>
   (doall
    (for [actions sections]
      (let [filtered-actions (filter some? actions)]
        (doall
         (map #(with-meta [action %] {:key (:label %)}) filtered-actions)))))])
