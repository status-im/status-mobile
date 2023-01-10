(ns quo2.components.dropdowns.dropdown
  (:require [quo2.components.icon :as icons]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.reanimated :as ra]
            [reagent.core :as reagent]))

(defn apply-anim
  [dd-height val]
  (ra/animate-delay dd-height
                    val
                    0
                    300
                    :easing1))

(def sizes
  {:big    {:icon-size 20
            :font      {:font-size :paragraph-1}
            :height    40
            :padding   {:padding-with-icon    {:padding-vertical   9
                                               :padding-horizontal 12}
                        :padding-with-no-icon {:padding-vertical 9
                                               :padding-left     12
                                               :padding-right    8}}}
   :medium {:icon-size 20
            :font      {:font-size :paragraph-1}
            :height    32
            :padding   {:padding-with-icon    {:padding-vertical   5
                                               :padding-horizontal 8}
                        :padding-with-no-icon {:padding-vertical 5
                                               :padding-left     12
                                               :padding-right    8}}}
   :small  {:icon-size 15
            :font      {:font-size :paragraph-2}
            :height    24
            :padding   {:padding-with-icon    {:padding-vertical   3
                                               :padding-horizontal 6}
                        :padding-with-no-icon {:padding-vertical   3
                                               :padding-horizontal 6}}}})
(defn color-by-10
  [color]
  (colors/alpha color 0.6))

(defn dropdown-comp
  [{:keys [icon open? dd-height size disabled? dd-color use-border? border-color]}]
  (let [dark?                                                         (colors/dark?)
        {:keys [width height width-with-icon padding font icon-size]} (size sizes)
        {:keys [padding-with-icon padding-with-no-icon]}              padding
        font-size                                                     (:font-size font)
        spacing                                                       (case size
                                                                        :big    4
                                                                        :medium 2
                                                                        :small  2)]
    [rn/touchable-opacity
     (cond->
       {:on-press (fn []
                    (if (swap! open? not)
                      (apply-anim dd-height 120)
                      (apply-anim dd-height 0)))
        :style    (cond->
                    (merge
                     (if icon
                       padding-with-icon
                       padding-with-no-icon)
                     {:width            (if icon
                                          width-with-icon
                                          width)
                      :height           height
                      :border-radius    (case size
                                          :big    12
                                          :medium 10
                                          :small  8)
                      :flex-direction   :row
                      :align-items      :center
                      :background-color (if @open?
                                          dd-color
                                          (color-by-10 dd-color))})
                    use-border? (assoc :border-width 1
                                       :border-color (if @open?
                                                       border-color
                                                       (color-by-10 border-color))))}
       disabled? (assoc-in [:style :opacity] 0.3)
       disabled? (assoc :disabled true))
     (when icon
       [icons/icon icon
        {:no-color        true
         :size            20
         :container-style {:margin-right spacing
                           :margin-top   1
                           :width        icon-size
                           :height       icon-size}}])
     [text/text
      {:size   font-size
       :weight :medium
       :font   :font-medium
       :color  :main} "Dropdown"]
     [icons/icon
      (if @open?
        (if dark?
          :main-icons/pullup-dark
          :main-icons/pullup)
        (if dark?
          :main-icons/dropdown-dark
          :main-icons/dropdown))
      {:size            20
       :no-color        true
       :container-style {:width         (+ icon-size 3)
                         :border-radius 20
                         :margin-left   (if (= :small size)
                                          2
                                          4)
                         :margin-top    1
                         :height        (+ icon-size 4)}}]]))

(defn items-comp
  [{:keys [items on-select]}]
  (let [items-count (count items)]
    [rn/scroll-view
     {:style               {:height "100%"}
      :horizontal          false
      :nestedScrollEnabled true}
     (doall
      (map-indexed (fn [index item]
                     [rn/touchable-opacity
                      {:key      (str item index)
                       :style    {:padding             4
                                  :border-bottom-width (if (= index (- items-count 1))
                                                         0
                                                         1)
                                  :border-color        (colors/theme-colors
                                                        colors/neutral-100
                                                        colors/white)
                                  :text-align          :center}
                       :on-press #(on-select item)}
                      [text/text {:style {:text-align :center}} item]])
                   items))]))

(defn dropdown
  [{:keys [items icon text default-item on-select size disabled? border-color use-border? dd-color]}]
  [:f>
   (fn []
     (let [open?     (reagent/atom false)
           dd-height (ra/use-val 0)]
       [rn/view {:style {:flex-grow 1}}
        [dropdown-comp
         {:items        items
          :icon         icon
          :disabled?    disabled?
          :size         size
          :dd-color     dd-color
          :text         text
          :border-color (colors/custom-color-by-theme border-color 50 60)
          :use-border?  use-border?
          :default-item default-item
          :open?        open?
          :dd-height    dd-height}]
        [ra/view
         {:style (ra/apply-animations-to-style
                  {:height dd-height}
                  {:height dd-height})}
         [items-comp
          {:items     items
           :on-select on-select}]]]))])
