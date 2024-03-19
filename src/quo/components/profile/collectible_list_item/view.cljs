(ns quo.components.profile.collectible-list-item.view
  (:require
    [quo.components.avatars.collection-avatar.view :as collection-avatar]
    [quo.components.counter.collectible-counter.view :as collectible-counter]
    [quo.components.icon :as icon]
    [quo.components.list-items.preview-list.view :as preview-list]
    [quo.components.markdown.text :as quo]
    [quo.components.profile.collectible-list-item.style :as style]
    [quo.foundations.colors :as colors]
    [quo.foundations.gradients :as gradients]
    [quo.theme]
    [react-native.core :as rn]
    [schema.core :as schema]
    [utils.i18n :as i18n]))

(defn- fallback-view
  [{:keys [label theme type]}]
  [rn/view
   {:style (style/fallback {:type  type
                            :theme theme})}
   [rn/view
    [icon/icon :i/sad {:color (colors/theme-colors colors/neutral-40 colors/neutral-50 theme)}]]
   [rn/view {:style {:height 4}}]
   [quo/text
    {:size  :paragraph-2
     :style {:color (colors/theme-colors colors/neutral-40 colors/neutral-50 theme)}}
    label]])

(defn- loading-square
  [theme]
  [rn/view {:style (style/loading-square theme)}])

(defn- loading-message
  [theme]
  [rn/view {:style (style/loading-message theme)}])

(defn- loading-image
  [{:keys [theme gradient-color-index]}]
  [gradients/view
   {:theme           theme
    :container-style (style/loading-image theme)
    :color-index     gradient-color-index}])

(defn- card-details
  [{:keys [status community? avatar-image-src collectible-name theme]}]
  [rn/view {:style style/card-details-container}

   (cond (= :cant-fetch status)
         [quo/text
          {:size   :paragraph-1
           :weight :medium
           :style  {:color (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)}}
          (i18n/label :t/unknown)]

         (= :loading status)
         [rn/view {:style {:flex-direction :row}}
          [loading-square theme]
          [loading-message theme]]

         community?
         [:<>
          [preview-list/view
           {:type :communities
            :size :size-20}
           [avatar-image-src]]
          [rn/view {:style {:width 8}}]
          [quo/text
           {:size   :paragraph-1
            :weight :semi-bold
            :style  style/card-detail-text}
           collectible-name]]

         :else
         [:<>
          [collection-avatar/view
           {:size  :size-20
            :image avatar-image-src}]
          [rn/view {:style {:width 8}}]
          [quo/text
           {:size            :paragraph-1
            :weight          :semi-bold
            :ellipsize-mode  :tail
            :number-of-lines 1
            :style           style/card-detail-text}
           collectible-name]])])

(defn- card-view
  [{:keys [avatar-image-src collectible-name community? counter
           gradient-color-index image-src status type]}]
  (let [theme (quo.theme/use-theme-value)]
    [rn/view {:style (style/card-view-container theme)}
     [rn/view {:style {:aspect-ratio 1}}
      (cond
        (= :loading status)
        [loading-image
         {:theme                theme
          :gradient-color-index gradient-color-index}]

        (= status :unsupported)
        [fallback-view
         {:theme theme
          :type  type
          :label (i18n/label :t/unsupported-file)}]

        (= status :cant-fetch)
        [fallback-view
         {:theme theme
          :type  type
          :label (i18n/label :t/cant-fetch-info)}]

        :else
        [rn/view {:style {:aspect-ratio 1}}
         [rn/image
          {:style  style/image
           :source image-src}]])]
     (when (and (not= status :loading) (not= status :cant-fetch) counter)
       [collectible-counter/view
        {:container-style style/collectible-counter
         :size            :size-24
         :value           counter}])
     [card-details
      {:status           status
       :community?       community?
       :avatar-image-src avatar-image-src
       :collectible-name collectible-name
       :theme            theme}]]))

(defn- image-view
  [{:keys [avatar-image-src community? counter
           gradient-color-index image-src status]}]
  (let [theme (quo.theme/use-theme-value)]
    [rn/view {:style style/image-view-container}
     (cond
       (= :loading status)
       [loading-image
        {:theme                theme
         :gradient-color-index gradient-color-index}]

       (= status :unsupported)
       [fallback-view
        {:theme theme
         :type  type
         :label (i18n/label :t/unsupported-file)}]

       (= status :cant-fetch)
       [fallback-view
        {:theme theme
         :type  type
         :label (i18n/label :t/cant-fetch-info)}]

       :else [rn/view {:style {:aspect-ratio 1}}
              [rn/image
               {:style  style/image
                :source image-src}]])
     (when (and (not= status :loading) (not= status :cant-fetch) counter)
       [collectible-counter/view
        {:container-style style/collectible-counter
         :size            :size-24
         :value           counter}])
     (when (and (not= status :loading) (not= status :cant-fetch) community?)
       [preview-list/view
        {:container-style style/avatar
         :type            :communities
         :size            :size-24}
        [avatar-image-src]])]))

(defn- view-internal
  [{:keys [container-style type on-press status]
    :as   props}]
  [rn/pressable
   {:on-press            (when-not (= status :loading) on-press)
    :accessibility-label :collectible-list-item
    :style               (merge container-style style/container)}
   (if (= type :card)
     [card-view props]
     [image-view props])])

(def ?schema
  [:=>
   [:catn
    [:props
     [:map {:closed true}
      [:avatar-image-src {:optional true} [:maybe :schema.common/image-source]]
      [:collectible-name {:optional true} [:maybe string?]]
      [:community? {:optional true} [:maybe boolean?]]
      [:counter {:optional true} [:maybe string?]]
      [:gradient-color-index {:optional true}
       [:maybe [:enum :gradient-1 :gradient-2 :gradient-3 :gradient-4 :gradient-5]]]
      [:image-src {:optional true} [:maybe :schema.common/image-source]]
      [:on-press {:optional true} [:maybe fn?]]
      [:status {:optional true} [:maybe [:enum :default :loading :cant-fetch :unsupported]]]
      [:type [:enum :card :image]]]]]
   :any])

(def view (schema/instrument #'view-internal ?schema))
