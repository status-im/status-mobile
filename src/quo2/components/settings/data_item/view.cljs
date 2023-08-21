(ns quo2.components.settings.data-item.view
  (:require [quo2.theme :as quo.theme]
            [react-native.core :as rn]
            [quo2.components.icon :as icons]
            [quo2.components.settings.data-item.style :as style]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [quo2.foundations.resources :as resources]
            [react-native.fast-image :as fast-image]
            [quo2.components.avatars.account-avatar.view :as account-avatar]
            [quo2.components.list-items.preview-list :as preview-list]
            [quo2.components.common.not-implemented :as not-implemented]
            [utils.i18n :as i18n]))

(defn- right-side
  [label icon-right? icon-color]
  [rn/view {:style style/right-container}
   (case label
     :preview [preview-list/preview-list
               {:type      :user
                :size      24
                :list-size 3}]
     :graph   [text/text "graph"]
     :none    nil
     nil)
   (when icon-right?
     [rn/view {:style (style/right-icon label)}
      [icons/icon
       (if (= :none label)
         :i/copy
         :i/chevron-right)
       {:color icon-color
        :size  20}]])])

(defn- left-side
  [theme title status size blur? description icon subtitle label icon-color]
  [rn/view {:style style/left-side}
   [rn/view {:style style/title-container}
    [text/text
     {:weight :regular
      :size   :paragraph-2
      :style  style/title}
     title]
    (when (and (= :graph label) (not= :small size))
      [text/text
       {:weight :regular
        :size   :label
        :style  style/title}
       (i18n/label :t/days)])]
   (if (= status :loading)
     [rn/view {:style (style/loading-container size blur?)}]
     [rn/view {:style style/subtitle-container}
      (when (not= :small size)
        [rn/view {:style (style/subtitle-icon-container description)}
         (case description
           :icon    [icons/icon icon {:size 16 :color icon-color}]
           :account [account-avatar/view
                     {:customization-color (get-in colors/customization
                                                   [:yellow (if (= theme :dark) 60 50)])
                      :size                16
                      :emoji               "🎮"
                      :type                :defaul}]
           :network [fast-image/fast-image {:source (resources/tokens :eth) :style style/image}]
           :default nil
           nil)])
      [text/text
       {:weight :medium
        :size   :paragraph-2
        :style  (style/description blur?)}
       subtitle]])])

(def view-internal
  (fn [{:keys [blur? card? icon-right? label description status size theme on-press title subtitle icon]}]
    (let [icon-color (cond
                       (or blur? (= :dark theme)) colors/white
                       (= :light theme)           colors/neutral-100)]
      (if (= :graph label)
        [not-implemented/not-implemented [not-implemented/not-implemented [text/text "not implemented"]]]
        [rn/pressable
         {:disabled (not icon-right?)
          :on-press on-press
          :style    (style/container size card? blur?)}
         [left-side theme title status size blur? description icon subtitle label icon-color]
         (when (and (= :default status) (not= :small size))
           [right-side label icon-right? icon-color])]))))

(def view (quo.theme/with-theme view-internal))
