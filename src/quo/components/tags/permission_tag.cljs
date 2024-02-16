(ns quo.components.tags.permission-tag
  (:require
    [quo.components.icon :as icons]
    [quo.components.markdown.text :as text]
    [quo.components.tags.base-tag :as base-tag]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]))

(defn outer-resource-container
  [size background-color]
  {:background-color background-color
   :border-radius    size
   :width            size
   :height           size
   :margin-left      (case size
                       32 -12
                       24 -8)
   :align-items      :center
   :justify-content  :center})

(defn extra-count-styles
  [size]
  {:background-color (colors/theme-colors
                      colors/neutral-20
                      colors/neutral-70)
   :height           (case size
                       32 28
                       24 20)
   :width            (case size
                       32 28
                       24 20)
   :border-radius    size
   :justify-content  :center
   :align-items      :center})

(defn extra-count
  [total-group-count selected-count size background-color]
  (let [extra-group-count (- total-group-count selected-count)]
    (when (> extra-group-count 0)
      [rn/view (outer-resource-container size background-color)
       [rn/view (extra-count-styles size)
        (if (< extra-group-count 4)
          [text/text
           {:size  :label
            :style {:align-items :center
                    :color       (colors/theme-colors
                                  colors/neutral-50
                                  colors/neutral-40)}}
           (str "+" extra-group-count)]
          [icons/icon :i/pending-default
           {:container-style {:align-items     :center
                              :justify-content :center}
            :color           (colors/theme-colors
                              colors/neutral-50
                              colors/neutral-40)
            :size            12}])]])))

(defn selected-token-count
  [group]
  (cond
    (= (count group) 3) 3

    (> (count group) 3) 2

    :else
    (count group)))

(defn token-group
  []
  (fn [{:keys [group size last-group background-color]
        :or   {size 24}}]
    (let [tokens-count    (count group)
          selected-tokens (take (selected-token-count group) group)]
      [rn/view
       {:flex-direction :row
        :align-items    :center}
       (for [{:keys [token-icon id]} selected-tokens]
         ^{:key id}
         [rn/view {:flex-direction :row}
          [rn/view (outer-resource-container size background-color)
           [rn/image
            {:source token-icon
             :style  {:height        (case size
                                       32 28
                                       24 20)
                      :width         (case size
                                       32 28
                                       24 20)
                      :border-radius size}}]]])

       [extra-count tokens-count (count selected-tokens) size]
       (when-not last-group
         [rn/view {:align-items :center}
          [text/text
           {:weight :medium
            :size   (case size
                      32 :paragraph-2
                      24 :label)
            :style  {:color          (colors/theme-colors
                                      colors/neutral-50
                                      colors/neutral-40)
                     :padding-left   4
                     :text-transform :lowercase
                     :padding-right  (case size
                                       32 16
                                       24 12)}}
           "or"]])])))

(defn selected-group-count
  [tokens]
  (cond
    (> (count tokens) 3) 3

    :else
    (count tokens)))

(defn tag-tokens
  []
  (fn [{:keys [tokens size background-color]
        :or   {size 24}}]
    (let [selected-groups (take (selected-group-count tokens) tokens)
          last-group-id   ((last selected-groups) :id)]
      [rn/view
       {:flex-direction :row
        :align-items    :center}
       (for [{:keys [id group]} selected-groups]
         ^{:key id}
         [token-group
          {:group            group
           :size             size
           :last-group       (if (= id last-group-id) true false)
           :background-color background-color}])])))

(defn tag
  [_ _]
  (fn [{:keys [locked? tokens size background-color on-press accessibility-label]
        :or   {size 24}}]
    [base-tag/base-tag
     {:accessibility-label accessibility-label
      :background-color    background-color
      :size                size
      :type                :permission
      :on-press            on-press}
     [rn/view
      {:flex-direction  :row
       :align-items     :center
       :justify-content :flex-end}
      [rn/view
       {:padding-left  (case 32
                         8 24
                         6)
        :padding-right (case size
                         32 16
                         24 12)}
       [icons/icon
        (if locked?
          :i/locked
          :i/unlocked)
        {:resize-mode :center
         :size        (case size
                        32 20
                        24 16)
         :color       (colors/theme-colors
                       colors/neutral-50
                       colors/neutral-40)}]]
      [tag-tokens
       {:tokens           tokens
        :size             size
        :background-color background-color}]]]))
