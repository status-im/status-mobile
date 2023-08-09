(ns quo2.components.settings.data-item.view
  (:require [quo2.theme :as quo.theme]
            [react-native.core :as rn]
            [quo2.components.icon :as icons]
            [quo2.components.settings.data-item.style :as style]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [quo2.foundations.resources :as quo2.resources]
            [react-native.fast-image :as fast-image]
            [quo2.components.avatars.account-avatar.view :as account-avatar]
            [quo2.components.list-items.preview-list :as preview-list]
            [quo2.components.common.not-implemented :as not-implemented]))

(defn- render-right-side
  [label icon-right?]
  [rn/view
   {:style style/right-container}
   (case label
     :preview [preview-list/preview-list
               {:type      :user
                :size      24
                :list-size 3}]
     :graph   [text/text "graph"]
     :none    nil)
   (when icon-right?
     [rn/view
      {:style {:margin-left (if (or (= label :graph) (= label :none)) 12 8)}}
      [icons/icon
       (if (= :none label)
         :i/copy
         :i/chevron-right)
       {:size 20}]])])

(defn- render-left-side
  [theme title status size blur? description icon subtitle label]
  [rn/view
   [rn/view
    {:style style/title-container}
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
       "Days"])]
   (if (= status :loading)
     [rn/view {:style (style/loading-container size theme blur?)}]
     [rn/view
      {:style style/subtitle-container}
      (when (not= :small size)
        [rn/view {:style (style/subtitle-icon-container description)}
         (case description
           :icon    [icons/icon icon {:size 16}]
           :account [account-avatar/view
                     {:customization-color (get-in colors/customization
                                                   [:yellow (if (= theme :dark) 60 50)])
                      :size                16
                      :emoji               "🎮"
                      :type                :defaul}]
           :network [fast-image/fast-image {:source (quo2.resources/tokens :eth) :style style/image}]
           :default nil
           nil)])
      [text/text
       {:weight :medium
        :size   :paragraph-2
        :style  (style/description theme blur?)}
       subtitle]])])

(defn- view-internal
  []
  (fn [{:keys [blur? card? icon-right? label description status size theme on-press]}
       {:keys [title subtitle icon]}]
    (if (= :graph label)
      [not-implemented/not-implemented [not-implemented/not-implemented [text/text "not implemented"]]]
      [rn/pressable
       {:disabled (not icon-right?)
        :on-press on-press
        :style    (style/container size card? theme blur?)}
       [render-left-side theme title status size blur? description icon subtitle label]
       (when (and (= :default status) (not= :small size))
         [render-right-side label icon-right?])])))

(def view (quo.theme/with-theme view-internal))
