(ns quo2.components.permission-tag
  (:require
   [status-im.ui.components.react :as react]
   [quo2.components.text :as text]
   [quo2.foundations.colors :as colors]
   [quo2.components.icon :as icons]))

(defn permission-tag-styles [color size]
  {:flex-direction   :row
   :border-radius    200
   :height           size
   :align-items      :center
   :justify-content  :flex-end
   :padding-left     6
   :background-color color})

(defn extra-count-styles [size]
  {:background-color (colors/theme-colors
                      colors/neutral-10
                      colors/neutral-70)
   :width            size
   :height           size
   :border-radius    size
   :justify-content  :center
   :align-items      :center})

(defn tag-resources [tokens size]
  (let [tkns (take (if (> (count tokens) 3) 2 3) tokens)]
    (for [{:keys [token-icon]} tkns]
      ^{:key token-icon}
      [react/view {:flex-direction :row}
       [react/view {:background-color (colors/theme-colors
                                       colors/neutral-10
                                       colors/neutral-80)
                    :border-radius    (case size 32 28 24 20)
                    :width            size
                    :height           size
                    :align-items      :center
                    :justify-content  :center
                    :margin-left      -8}
        [react/image {:source token-icon
                      :style  {:height        (case size 32 28 24 20)
                               :width         (case size 32 28 24 20)
                               :border-radius (case size 32 28 24 20)
                               :padding       2
                               :position      :absolute}}]]])))

(defn tag-extra-count [tokens size]
  (let [taken-tkns (take (if (> (count tokens) 3) 2 3) tokens)
        extra-counts (- (count tokens) (count taken-tkns))]
    (when (> extra-counts 0)
      [react/view {:flex-direction :row}
       [react/view {:background-color (colors/theme-colors
                                       colors/neutral-20
                                       colors/neutral-70)
                    :border-radius    (case size 32 28 24 20)
                    :width            size
                    :height           size
                    :align-items      :center
                    :justify-content  :center
                    :margin-left      -8}
        [react/view (extra-count-styles (case size 32 28 24 20))
         [text/text {:weight        :medium
                     :size          :label
                     :style         {:align-items :center}}
          (str "+" extra-counts)]]]])))

(defn tag-or-clause []
  [react/view {:align-items   :center}
   [text/text {:weight        :medium
               :style         {:margin-right 12
                               :margin-left  4
                               :font-size     11}}
    "or"]])

(defn tag []
  (fn [{:keys [icon background-color icon-color token-groups size]
        :or {size 24}}]
    [react/view {:style (permission-tag-styles background-color size)}
     [react/view {:margin-right  12}
      [icons/icon icon
       {:container-style {:align-items     :center
                          :justify-content :center}
        :resize-mode      :center
        :size             (case size 32 20 24 16)
        :color            icon-color}]]

     (when (= (count token-groups) 1)
       (let [tokens  ((first token-groups) :tokens)]
         [react/view {:flex-direction :row
                      :align-items    :center}
          (tag-resources tokens size)
          [tag-extra-count tokens size]]))

     (when (= (count token-groups) 2)
       (let [left-group  ((first token-groups) :tokens)
             right-group ((nth token-groups 1) :tokens)]
         [react/view {:flex-direction :row
                      :align-items    :center}
          (tag-resources left-group size)
          [tag-extra-count left-group size]
          [tag-or-clause]
          (tag-resources right-group size)
          [tag-extra-count right-group size]]))

     (when (= (count token-groups) 3)
       (let [left-group   ((first token-groups) :tokens)
             center-group ((nth token-groups 1) :tokens)
             right-group  ((nth token-groups 2) :tokens)]
         [react/view {:flex-direction :row
                      :align-items    :center}
          (tag-resources left-group size)
          [tag-extra-count left-group size]
          [tag-or-clause]
          (tag-resources center-group size)
          [tag-extra-count center-group size]
          [tag-or-clause]
          (tag-resources right-group size)
          [tag-extra-count right-group size]]))]))