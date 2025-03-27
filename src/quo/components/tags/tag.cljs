(ns quo.components.tags.tag
  (:require
    [quo.components.icon :as icons]
    [quo.components.markdown.text :as text]
    [quo.components.tags.base-tag :as base-tag]
    [quo.context]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]))

(def themes
  {:light {:default  {:border-color         colors/neutral-20
                      :blurred-border-color colors/neutral-80-opa-5
                      :text-color           {:style {:color colors/neutral-100}}}
           :active   {:border-color         colors/neutral-30
                      :blurred-border-color colors/neutral-80-opa-10
                      :text-color           {:style {:color colors/neutral-100}}}
           :disabled {:border-color         colors/neutral-20
                      :blurred-border-color colors/neutral-80-opa-5
                      :text-color           {:style {:color colors/neutral-100}}}}
   :dark  {:default  {:border-color         colors/neutral-70
                      :blurred-border-color colors/white-opa-10
                      :text-color           {:style {:color colors/white}}}
           :active   {:border-color         colors/neutral-60
                      :blurred-border-color colors/white-opa-20
                      :text-color           {:style {:color colors/white}}}
           :disabled {:border-color         colors/neutral-70
                      :blurred-border-color colors/white-opa-10
                      :text-color           {:style {:color colors/white}}}}})

(defn- emoji-comp
  [size resource]
  (let [dimension (case size
                    32 20
                    24 12
                    nil)]
    (if (string? resource)
      [rn/text {:style {:margin-right 4 :font-size dimension}}
       resource]
      [rn/image
       {:source resource
        :style  {:margin-right 4 :width dimension :height dimension}}])))

(defn tag-resources
  [size type resource icon-color label text-color labelled?]
  [rn/view
   {:style (merge {:flex-direction  :row
                   :align-items     :center
                   :justify-content :center}
                  (when label
                    {:padding-horizontal (case size
                                           32 12
                                           24 8)}))}
   (when (= type :icon)
     [icons/icon resource
      {:container-style (when label
                          {:margin-right 4})
       :resize-mode     :center
       :size            (case size
                          32 20
                          24 12)
       :color           icon-color}])
   (when (= type :emoji)
     [emoji-comp size resource])
   (when labelled?
     [text/text
      (merge {:size            (case size
                                 32 :paragraph-1
                                 24 :paragraph-2
                                 20 :label
                                 nil)
              :weight          :medium
              :number-of-lines 1}
             text-color)
      label])])

(defn tag
  "opts
   {:type         :icon/:emoji/:label
    :label        string
    :size         32/24
    :on-press     fn
    :blurred?     true/false
    :resource     icon/image/text(emojis)
    :labelled?    true/false
    :disabled?    true/false}

   opts
    - `blurred`  boolean: use to determine border color if the background is blurred
    - `type`     can be icon or emoji with or without a tag label
    - `labelled` boolean: is true if tag has label else false"
  [{:keys [id on-press disabled? size active accessibility-label label resource type
           labelled? blurred? icon-color]
    :or   {size 32}}]
  (let [theme                (quo.context/use-theme)
        state                (cond
                               disabled? :disabled
                               active    :active
                               :else     :default)
        {:keys [border-color
                blurred-border-color
                text-color]} (get-in themes [theme state])]
    [rn/view {:style {:align-items :center}}
     [base-tag/base-tag
      {:id                  id
       :size                size
       :border-width        1
       :border-color        (if blurred?
                              blurred-border-color
                              border-color)
       :on-press            on-press
       :accessibility-label accessibility-label
       :disabled?           disabled?
       :type                type
       :labelled?           (if (= type :label) true labelled?)}
      [tag-resources size type resource icon-color label text-color labelled?]]]))
