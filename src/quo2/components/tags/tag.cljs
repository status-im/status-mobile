(ns quo2.components.tags.tag
  (:require [quo2.foundations.colors :as colors]
            [quo.theme :as theme]
            [quo.react-native :as rn]
            [quo2.components.icon :as icons]
            [quo2.components.markdown.text :as text]
            [quo2.components.tags.base-tag :as base-tag]))

(def themes {:light {:default  {:border-color             colors/neutral-20
                                :blurred-border-color     colors/neutral-80-opa-5
                                :text-color               {:style {:color colors/neutral-100}}}
                     :active   {:border-color             colors/neutral-30
                                :blurred-border-color     colors/neutral-80-opa-10
                                :text-color               {:style {:color colors/neutral-100}}}
                     :disabled {:border-color             colors/neutral-20
                                :blurred-border-color     colors/neutral-80-opa-5
                                :text-color               {:style {:color colors/neutral-100}}}}
             :dark  {:default  {:border-color             colors/neutral-70
                                :blurred-border-color     colors/white-opa-10
                                :text-color               {:style {:color colors/white}}}
                     :active   {:border-color             colors/neutral-60
                                :blurred-border-color     colors/white-opa-20
                                :text-color               {:style {:color colors/white}}}
                     :disabled {:border-color             colors/neutral-70
                                :blurred-border-color     colors/white-opa-10
                                :text-color               {:style {:color colors/white}}}}})

(defn tag-resources [size type resource icon-color label text-color labelled]
  [rn/view {:style (merge {:flex-direction  :row
                           :align-items        :center
                           :justify-content    :center}
                          (when label
                            {:padding-horizontal (case size 32 12 24 8)}))}
   (when (= type :icon)
     [icons/icon resource {:container-style (when label
                                              {:margin-right 4})
                           :resize-mode      :center
                           :size             (case size
                                               32 20
                                               24 12)
                           :color            icon-color}])
   (when (= type :emoji)
     [rn/image {:source resource
                :style  (merge (case size
                                 32 {:height 20
                                     :width  20}
                                 24 {:height 12
                                     :width  12})
                               (when label
                                 {:margin-right 4}))}])
   (when labelled
     [text/text (merge {:size            (case size
                                           32 :paragraph-1
                                           24 :paragraph-2
                                           20 :label nil)
                        :weight          :medium
                        :number-of-lines 1}
                       text-color)
      label])])

(defn tag
  [_ _]
  (fn [{:keys [id on-press disabled size resource active accessibility-label
               label type labelled blurred icon-color] :or   {size 32}}]
    (let [state (cond disabled :disabled active :active :else :default)
          {:keys [border-color blurred-border-color text-color]}
          (get-in themes [(theme/get-theme) state])]
      [base-tag/base-tag {:id                  id
                          :size                size
                          :border-width        1
                          :border-color        (if blurred
                                                 blurred-border-color
                                                 border-color)
                          :on-press            on-press
                          :accessibility-label accessibility-label
                          :disabled            disabled
                          :type                type
                          :label               label}
       [tag-resources size type resource icon-color label text-color labelled]])))

