(ns quo.components.settings.data-item.view
  (:require
    [quo.components.avatars.account-avatar.view :as account-avatar]
    [quo.components.icon :as icons]
    [quo.components.list-items.preview-list.view :as preview-list]
    [quo.components.markdown.text :as text]
    [quo.components.settings.data-item.style :as style]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [schema.core :as schema]))

(defn- left-loading
  [{:keys [size blur?]}]
  (let [theme (quo.theme/use-theme)]
    [rn/view {:style (style/loading-container size blur? theme)}]))

(defn- left-subtitle
  [{:keys [size subtitle-text-props subtitle-type subtitle-color icon icon-color blur? subtitle
           customization-color emoji
           network-image]
    :or   {subtitle-type :default}}]
  (let [theme (quo.theme/use-theme)]
    [rn/view {:style style/subtitle-container}
     (when (and subtitle-type (not= :small size))
       [rn/view {:style (style/subtitle-icon-container subtitle-type)}
        (case subtitle-type
          :icon    [icons/icon icon
                    {:accessibility-label :subtitle-type-icon
                     :size                16
                     :color               icon-color}]
          :account [account-avatar/view
                    {:customization-color customization-color
                     :size                16
                     :emoji               emoji
                     :type                :default}]
          :network [rn/image
                    {:accessibility-label :subtitle-type-image
                     :source              network-image
                     :style               style/image}]
          nil)])
     [text/text
      (merge {:weight :medium
              :size   :paragraph-2
              :style  (style/description subtitle-color blur? theme)}
             subtitle-text-props)
      subtitle]
     (when (= subtitle-type :editable)
       [icons/icon :i/edit
        {:accessibility-label :edit-icon
         :size                12
         :container-style     {:margin-left 2}
         :color               (if blur?
                                colors/neutral-40
                                (colors/theme-colors colors/neutral-50
                                                     colors/neutral-40
                                                     theme))}])]))

(defn- left-title
  [{:keys [title blur? title-icon]}]
  (let [theme (quo.theme/use-theme)]
    [rn/view {:style style/title-container}
     [text/text
      {:weight :regular
       :size   :paragraph-2
       :style  (style/title blur? theme)}
      title]
     (when title-icon
       [icons/icon title-icon
        {:accessibility-label :title-icon
         :size                12
         :color               (if blur?
                                colors/neutral-40
                                (colors/theme-colors colors/neutral-50
                                                     colors/neutral-40
                                                     theme))}])]))

(defn- left-side
  "The description can either be given as a string `subtitle-type` or a component `custom-subtitle`"
  [{:keys [title subtitle-text-props status size blur? custom-subtitle icon subtitle subtitle-type
           subtitle-color icon-color
           customization-color network-image emoji title-icon]
    :as   props}]
  (let [theme (quo.theme/use-theme)]
    [rn/view {:style style/left-side}
     [rn/view
      [left-title
       {:title      title
        :title-icon title-icon
        :blur?      blur?}]]
     (if (= status :loading)
       [left-loading
        {:size  size
         :blur? blur?
         :theme theme}]
       (if custom-subtitle
         [custom-subtitle props]
         [left-subtitle
          {:theme               theme
           :subtitle-text-props subtitle-text-props
           :size                size
           :subtitle-type       subtitle-type
           :subtitle-color      subtitle-color
           :icon                icon
           :icon-color          icon-color
           :blur?               blur?
           :subtitle            subtitle
           :customization-color customization-color
           :emoji               emoji
           :network-image       network-image}]))]))

(defn- right-side
  [{:keys [right-icon right-content icon-color]}]
  (let [{:keys [type data size]
         :or   {size :size-24}} right-content]
    [rn/view {:style style/right-container}
     (when type
       [preview-list/view
        {:type   type
         :number (count data)
         :size   size}
        data])
     (when right-icon
       [rn/view {:style style/right-icon}
        [icons/icon right-icon
         {:accessibility-label :icon-right
          :color               icon-color
          :size                20}]])]))


(def ?schema
  [:=>
   [:catn
    [:props
     [:map {:closed true}
      [:blur? {:optional true} [:maybe :boolean]]
      [:card? {:optional true} [:maybe :boolean]]
      [:right-icon {:optional true} [:maybe :keyword]]
      [:right-content {:optional true} [:maybe :map]]
      [:icon-color {:optional true} [:maybe :schema.common/customization-color]]
      [:status {:optional true} [:maybe [:enum :default :loading]]]
      [:subtitle-type {:optional true} [:maybe [:enum :default :icon :network :account :editable]]]
      [:subtitle-color {:optional true} [:maybe :schema.common/customization-color]]
      [:size {:optional true} [:maybe [:enum :default :small :large]]]
      [:title :string]
      [:subtitle {:optional true} [:maybe [:or :string :double]]]
      [:subtitle-text-props {:optional true} [:maybe :map]]
      [:custom-subtitle {:optional true} [:maybe fn?]]
      [:icon {:optional true} [:maybe :keyword]]
      [:title-icon {:optional true} [:maybe :keyword]]
      [:emoji {:optional true} [:maybe :string]]
      [:customization-color {:optional true} [:maybe :schema.common/customization-color]]
      [:network-image {:optional true} [:maybe :schema.common/image-source]]
      [:on-press {:optional true} [:maybe fn?]]
      [:container-style {:optional true} [:maybe :map]]]]]
   :any])

(defn view-internal
  [{:keys [blur? card? right-icon right-content status size on-press container-style]
    :as   props}]
  (let [theme      (quo.theme/use-theme)
        icon-color (if (or blur? (= :dark theme))
                     colors/neutral-40
                     colors/neutral-50)]
    [rn/pressable
     {:accessibility-label :data-item
      :disabled            (nil? on-press)
      :on-press            on-press
      :style               (merge (style/container {:size        size
                                                    :card?       card?
                                                    :blur?       blur?
                                                    :actionable? on-press
                                                    :theme       theme})
                                  container-style)}
     [left-side props]
     (when (and (= :default status) (not= :small size))
       [right-side
        {:right-icon    right-icon
         :right-content right-content
         :icon-color    icon-color}])]))

(def view (schema/instrument #'view-internal ?schema))
