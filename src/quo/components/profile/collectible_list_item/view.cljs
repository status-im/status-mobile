(ns quo.components.profile.collectible-list-item.view
  (:require
    [quo.components.avatars.collection-avatar.view :as collection-avatar]
    [quo.components.counter.collectible-counter.view :as collectible-counter]
    [quo.components.icon :as icon]
    [quo.components.list-items.preview-list.view :as preview-list]
    [quo.components.markdown.text :as text]
    [quo.components.profile.collectible-list-item.style :as style]
    [quo.foundations.colors :as colors]
    [quo.foundations.gradients :as gradients]
    [quo.theme]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [schema.core :as schema]
    [status-im.contexts.wallet.collectible.utils :as utils]
    [utils.i18n :as i18n]))

(defn- fallback-view
  [{:keys [label theme]}]
  [rn/view
   {:style (style/fallback {:theme theme})}
   [rn/view
    [icon/icon :i/sad {:color (colors/theme-colors colors/neutral-40 colors/neutral-50 theme)}]]
   [rn/view {:style {:height 4}}]
   [text/text
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
  [{:keys [community? avatar-image-src collectible-name theme loading? error?]}]
  [rn/view {:style style/card-details-container}
   (cond @loading?
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
          [text/text
           {:size   :paragraph-1
            :weight :semi-bold
            :style  style/card-detail-text}
           collectible-name]])

   [rn/view
    {:style (style/avatar-container @loading?)}
    (when-not @error?
      [:<>
       [collection-avatar/view
        {:size        :size-20
         :on-load-end #(reset! loading? false)
         :on-error    #(reset! error? true)
         :image       avatar-image-src}]
       [rn/view {:style {:width 8}}]])
    [text/text
     {:size            :paragraph-1
      :weight          :semi-bold
      :ellipsize-mode  :tail
      :number-of-lines 1
      :style           style/card-detail-text}
     collectible-name]]])

(defn- card-view
  [{:keys [avatar-image-src collectible-name community? counter avatar-error? avatar-loading?
           gradient-color-index image-src image-loading? image-error? collectible-mime]}]
  (let [theme (quo.theme/use-theme-value)]
    [rn/view {:style (style/card-view-container theme)}
     [rn/view {:style {:aspect-ratio 1}}
      (cond
        @image-error?
        [fallback-view
         {:theme theme
          :label (i18n/label :t/cant-fetch-info)}]

        (not (utils/collectible-supported? collectible-mime))
        [fallback-view
         {:theme theme
          :label (i18n/label :t/unsupported-file)}]

        @image-loading?
        [loading-image
         {:theme                theme
          :gradient-color-index gradient-color-index}])
      (when (utils/collectible-supported? collectible-mime)
        [rn/view {:style {:aspect-ratio 1}}
         [rn/image
          {:style       style/image
           :on-load-end #(reset! image-loading? false)
           :on-error    #(reset! image-error? true)
           :source      image-src}]])]
     (when (and (not @image-loading?) (not @image-error?) counter)
       [collectible-counter/view
        {:container-style style/collectible-counter
         :size            :size-24
         :value           counter}])
     [card-details
      {:loading?         avatar-loading?
       :error?           avatar-error?
       :community?       community?
       :avatar-image-src avatar-image-src
       :collectible-name collectible-name
       :theme            theme}]]))

(defn- image-view
  [{:keys [avatar-image-src community? counter image-error?
           gradient-color-index image-src image-loading? collectible-mime]}]
  (let [theme (quo.theme/use-theme-value)]
    [rn/view {:style style/image-view-container}
     (cond
       @image-error?
       [fallback-view
        {:theme theme
         :label (i18n/label :t/cant-fetch-info)}]

       (not (utils/collectible-supported? collectible-mime))
       [fallback-view
        {:theme theme
         :label (i18n/label :t/unsupported-file)}]

       @image-loading?
       [loading-image
        {:theme                theme
         :gradient-color-index gradient-color-index}])
     (when (utils/collectible-supported? collectible-mime)
       [rn/view {:style {:aspect-ratio 1}}
        [rn/image
         {:style       style/image
          :on-load-end #(reset! image-loading? false)
          :on-error    #(reset! image-error? true)
          :source      image-src}]])
     (when (and (not @image-loading?) (not @image-error?) counter)
       [collectible-counter/view
        {:container-style style/collectible-counter
         :size            :size-24
         :value           counter}])
     (when (and (not @image-loading?) (not @image-error?) community?)
       [preview-list/view
        {:container-style style/avatar
         :type            :communities
         :size            :size-24}
        [avatar-image-src]])]))

(defn- view-internal
  []
  (let [image-loading?  (reagent/atom true)
        image-error?    (reagent/atom false)
        avatar-loading? (reagent/atom true)
        avatar-error?   (reagent/atom false)]
    (fn [{:keys [container-style type on-press]
          :as   props}]
      [rn/pressable
       {:on-press            (when-not @image-loading? on-press)
        :accessibility-label :collectible-list-item
        :style               (merge container-style style/container)}
       (if (= type :card)
         [card-view
          (assoc props
                 :image-loading?  image-loading?
                 :image-error?    image-error?
                 :avatar-loading? avatar-loading?
                 :avatar-error?   avatar-error?)]
         [image-view
          (assoc props
                 :image-loading? image-loading?
                 :image-error?   image-error?)])])))

(def ?schema
  [:=>
   [:catn
    [:props
     [:map {:closed true}
      [:avatar-image-src {:optional true} [:maybe :schema.common/image-source]]
      [:collectible-name {:optional true} [:maybe string?]]
      [:collectible-mime {:optional true} [:maybe [:or string? keyword?]]]
      [:community? {:optional true} [:maybe boolean?]]
      [:counter {:optional true} [:maybe [:or :string :int]]]
      [:gradient-color-index {:optional true}
       [:maybe [:enum :gradient-1 :gradient-2 :gradient-3 :gradient-4 :gradient-5]]]
      [:image-src {:optional true} [:maybe :schema.common/image-source]]
      [:on-press {:optional true} [:maybe fn?]]
      [:type [:enum :card :image]]
      [:container-style {:optional true} [:maybe :map]]]]]
   :any])

(def view (schema/instrument #'view-internal ?schema))
