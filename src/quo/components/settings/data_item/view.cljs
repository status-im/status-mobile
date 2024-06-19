(ns quo.components.settings.data-item.view
  (:require
    [quo.components.avatars.account-avatar.view :as account-avatar]
    [quo.components.icon :as icons]
    [quo.components.list-items.preview-list.view :as preview-list]
    [quo.components.markdown.text :as text]
    [quo.components.settings.data-item.style :as style]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]))

(defn- left-loading
  [{:keys [size blur?]}]
  (let [theme (quo.theme/use-theme)]
    [rn/view {:style (style/loading-container size blur? theme)}]))

(defn- left-subtitle
  [{:keys [size subtitle-type icon icon-color blur? subtitle customization-color emoji network-image]
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
      {:weight :medium
       :size   :paragraph-2
       :style  (style/description blur? theme)}
      subtitle]]))

(defn- left-title
  [{:keys [title blur?]}]
  (let [theme (quo.theme/use-theme)]
    [rn/view {:style style/title-container}
     [text/text
      {:weight :regular
       :size   :paragraph-2
       :style  (style/title blur? theme)}
      title]]))

(defn- left-side
  "The description can either be given as a string `subtitle-type` or a component `custom-subtitle`"
  [{:keys [title status size blur? custom-subtitle icon subtitle subtitle-type icon-color
           customization-color network-image emoji]
    :as   props}]
  (let [theme (quo.theme/use-theme)]
    [rn/view {:style style/left-side}
     [left-title
      {:title title
       :blur? blur?
       :theme theme}]
     (if (= status :loading)
       [left-loading
        {:size  size
         :blur? blur?
         :theme theme}]
       (if custom-subtitle
         [custom-subtitle props]
         [left-subtitle
          {:theme               theme
           :size                size
           :subtitle-type       subtitle-type
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

(defn view
  [{:keys [blur? card? right-icon right-content status size on-press container-style]
    :as   props}]
  (let [theme      (quo.theme/use-theme)
        icon-color (if (or blur? (= :dark theme))
                     colors/neutral-40
                     colors/neutral-50)]
    [rn/pressable
     {:accessibility-label :data-item
      :disabled            (not right-icon)
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
