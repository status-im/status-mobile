(ns quo2.components.settings.data-item.view
  (:require [quo2.theme :as quo.theme]
            [react-native.core :as rn]
            [quo2.components.settings.data-item.style :as style]
            [quo2.foundations.colors :as colors]
            [quo2.components.common.not-implemented.view :as not-implemented]
            [quo2.components.icon :as icons]
            [quo2.components.markdown.text :as text]
            [quo2.components.list-items.preview-list.view :as preview-list]
            [quo2.foundations.resources :as quo.resources]
            [quo2.components.avatars.account-avatar.view :as account-avatar]
            [utils.i18n :as i18n]))

(defn- left-loading
  [{:keys [size blur? theme]}]
  [rn/view {:style (style/loading-container size blur? theme)}])

(defn- left-subtitle
  [{:keys [theme size description icon icon-color blur? subtitle customization-color emoji]}]
  [rn/view {:style style/subtitle-container}
   (when (not= :small size)
     [rn/view {:style (style/subtitle-icon-container description)}
      (case description
        :icon    [icons/icon icon
                  {:accessibility-label :description-icon
                   :size                16
                   :color               icon-color}]
        :account [account-avatar/view
                  {:customization-color customization-color
                   :size                16
                   :emoji               emoji
                   :type                :default}]
        :network [rn/image
                  {:accessibility-label :description-image
                   :source              (quo.resources/tokens :eth)
                   :style               style/image}]
        nil)])
   [text/text
    {:weight :medium
     :size   :paragraph-2
     :style  (style/description blur? theme)}
    subtitle]])

(defn- left-title
  [{:keys [title label size theme]}]
  [rn/view {:style style/title-container}
   [text/text
    {:weight :regular
     :size   :paragraph-2
     :style  (style/title theme)}
    title]
   (when (and (= :graph label) (not= :small size))
     [text/text
      {:weight :regular
       :size   :label
       :style  (style/title theme)}
      (i18n/label :t/days)])])

(defn- left-side
  [{:keys [theme title status size blur? description icon subtitle label icon-color customization-color
           emoji]}]
  [rn/view {:style style/left-side}
   [left-title
    {:title title
     :label label
     :size  size
     :theme theme}]
   (if (= status :loading)
     [left-loading
      {:size  size
       :blur? blur?
       :theme theme}]
     [left-subtitle
      {:theme               theme
       :size                size
       :description         description
       :icon                icon
       :icon-color          icon-color
       :blur?               blur?
       :subtitle            subtitle
       :customization-color customization-color
       :emoji               emoji}])])

(defn- right-side
  [{:keys [label icon-right? icon-color communities-list]}]
  [rn/view {:style style/right-container}
   (case label
     :preview [preview-list/view
               {:type   :communities
                :number 3
                :size   :size-24}
               communities-list]
     :graph   [text/text "graph"]
     :none    nil
     nil)
   (when icon-right?
     [rn/view {:style (style/right-icon label)}
      [icons/icon
       (if (= :none label)
         :i/copy
         :i/chevron-right)
       {:accessibility-label :icon-right
        :color               icon-color
        :size                20}]])])

(def view-internal
  (fn [{:keys [blur? card? icon-right? label status size theme on-press communities-list] :as props}]
    (let [icon-color (if (or blur? (= :dark theme))
                       colors/neutral-40
                       colors/neutral-50)]
      (if (= :graph label)
        [not-implemented/view {:blur? blur?}]
        [rn/pressable
         {:accessibility-label :data-item
          :disabled            (not icon-right?)
          :on-press            on-press
          :style               (style/container size card? blur? theme)}
         [left-side props]
         (when (and (= :default status) (not= :small size))
           [right-side
            {:label            label
             :icon-right?      icon-right?
             :icon-color       icon-color
             :communities-list communities-list}])]))))

(def view (quo.theme/with-theme view-internal))
