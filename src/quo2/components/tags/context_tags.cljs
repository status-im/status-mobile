(ns quo2.components.tags.context-tags
  (:require [quo2.components.avatars.group-avatar :as group-avatar]
            [quo2.components.icon :as icons]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [quo2.theme :as quo2.theme]
            [react-native.core :as rn]))

(defn padding-left-for-type
  [type]
  (case type
    :group-avatar 3
    8))

(defn trim-public-key
  [pk]
  (str (subs pk 0 6) "..." (subs pk (- (count pk) 3))))

(defn base-tag
  [_ _]
  (fn [{:keys [override-theme style]} & children]
    (let [theme (or override-theme (quo2.theme/get-theme))]
      (into
       [rn/view
        (merge
         {:border-radius    100
          :padding-vertical 3
          :flex-direction   :row
          :padding-right    8
          :padding-left     8
          :background-color (if (= theme :light)
                              colors/neutral-10
                              colors/neutral-80)}
         style)]
       children))))

(defn group-avatar-tag
  [_ _]
  (fn [label opts]
    [base-tag
     (-> opts
         (select-keys [:override-theme :style])
         (assoc-in [:style :padding-left] 3))
     [group-avatar/group-avatar opts]
     [text/text
      {:weight :medium
       :size   :paragraph-2
       :style  (:text-style opts)}
      (str " " label)]]))

(defn public-key-tag
  [_ _]
  (fn [params public-key]
    [base-tag params
     [text/text
      {:weight :monospace
       :size   :paragraph-2}
      (trim-public-key public-key)]]))

(defn context-tag
  [_ _]
  (fn [params photo name channel-name]
    (let [text-style  (:text-style params)
          text-params {:weight :medium
                       :size   :paragraph-2
                       :style  (assoc text-style :justify-content :center)}
          icon-color  (colors/theme-colors colors/neutral-50 colors/neutral-40)]
      [base-tag (assoc-in params [:style :padding-left] 3)
       [rn/image
        {:style {:width 20
                 :border-radius 10
                 :background-color :white
                 :height 20}
         :source photo}]
       [rn/view
        {:style {:align-items :center
                 :flex-direction :row}}
        [text/text text-params (str " " name)]
        (when channel-name
          [:<>
           [icons/icon
            :i/chevron-right
            {:color icon-color
             :size  16}]
           [text/text text-params (str "# " channel-name)]])]])))

(defn user-avatar-tag
  []
  (fn [params username photo]
    [context-tag params {:uri photo} username]))
