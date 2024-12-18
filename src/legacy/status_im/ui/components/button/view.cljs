(ns legacy.status-im.ui.components.button.view
  (:require
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.icons.icons :as icons]
    [legacy.status-im.ui.components.spacing :as spacing]
    [legacy.status-im.ui.components.text :as text]
    [react-native.core :as rn]))

(defn style-container
  [type]
  (merge {:height          44
          :align-items     :center
          :justify-content :center
          :flex-direction  :row}
         (case type
           :primary   (:base spacing/padding-horizontal)
           :secondary (:x-tiny spacing/padding-horizontal)
           :icon      {}
           nil)))

(defn content-style
  [type]
  (case type
    :primary   (:base spacing/padding-horizontal)
    :secondary (:x-tiny spacing/padding-horizontal)
    :icon      (:tiny spacing/padding-horizontal)
    nil))

(defn themes
  [theme]
  (case theme
    :main         {:icon-color       (:icon-04 @colors/theme)
                   :background-color (:interactive-02 @colors/theme)
                   :text-color       (:text-04 @colors/theme)}
    :icon         {:icon-color       (:icon-01 @colors/theme)
                   :background-color (:interactive-02 @colors/theme)
                   :text-color       (:text-01 @colors/theme)}
    :negative     {:icon-color       (:negative-01 @colors/theme)
                   :background-color (:negative-02 @colors/theme)
                   :text-color       (:negative-01 @colors/theme)}
    :positive     {:icon-color       (:positive-01 @colors/theme)
                   :background-color (:positive-02 @colors/theme)
                   :text-color       (:positive-01 @colors/theme)}
    :accent       {:icon-color       (:icon-05 @colors/theme)
                   :background-color (:interactive-01 @colors/theme)
                   :text-color       (:text-05 @colors/theme)}
    :secondary    {:icon-color       (:icon-02 @colors/theme)
                   :background-color (:interactive-02 @colors/theme)
                   :text-color       (:text-02 @colors/theme)}
    :disabled     {:icon-color       (:icon-02 @colors/theme)
                   :background-color (:ui-01 @colors/theme)
                   :text-color       (:text-02 @colors/theme)}
    :monocromatic {:icon-color       (:icon-01 @colors/theme)
                   :background-color (:ui-background @colors/theme)
                   :text-color       (:text-01 @colors/theme)
                   :border-color     (:ui-01 @colors/theme)}))

(defn button
  [{:keys [on-press disabled type theme before after
           on-long-press accessibility-label loading border-radius style test-ID]
    :or   {theme         :main
           type          :primary
           border-radius 8}}
   children]
  (let [theme' (cond
                 disabled :disabled
                 :else    theme)
        {:keys [icon-color background-color text-color border-color]}
        (themes theme')]

    [rn/touchable-without-feedback
     (merge {:disabled            disabled
             :accessibility-label accessibility-label}
            (when on-press
              {:on-press on-press})
            (when on-long-press
              {:on-long-press on-long-press}))
     [rn/view
      (merge {:background-color background-color
              :border-radius    border-radius}
             (when border-color
               {:border-color border-color
                :border-width 1}))
      [rn/view {:test-ID test-ID :style (merge (style-container type) style)}
       (when before
         [rn/view
          [icons/icon before {:color icon-color}]])
       (when loading
         [rn/view {:style {:position :absolute}}
          [rn/activity-indicator]])
       [rn/view
        {:style (merge (content-style type)
                       (when loading
                         {:opacity 0}))}
        (cond
          (= type :icon)
          [icons/icon children {:color icon-color}]

          (string? children)
          [text/text
           {:weight          :medium
            :number-of-lines 1
            :style           {:color text-color}}
           children]

          (vector? children)
          children)]
       (when after
         [rn/view
          [icons/icon after {:color icon-color}]])]]]))
