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
    [schema.core :as schema]
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
  [{:keys [community? avatar-image-src collectible-name theme state set-state]}]
  [rn/view {:style style/card-details-container}
   (cond (not (:avatar-loaded? state))
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
    {:style (style/avatar-container (:avatar-loaded? state))}
    [:<>
     [collection-avatar/view
      {:size        :size-20
       :on-load-end #(set-state (fn [prev-state] (assoc prev-state :avatar-loaded? true)))
       :image       avatar-image-src}]
     [rn/view {:style {:width 8}}]]
    [text/text
     {:size            :paragraph-1
      :weight          :semi-bold
      :ellipsize-mode  :tail
      :number-of-lines 1
      :style           style/card-detail-text}
     collectible-name]]])

(defn- card-view
  [{:keys [avatar-image-src collectible-name community? counter state set-state
           gradient-color-index image-src supported-file?]}]
  (let [theme (quo.theme/use-theme-value)]
    [rn/view {:style (style/card-view-container theme)}
     [rn/view {:style {:aspect-ratio 1}}
      (cond
        (:image-error? state)
        [fallback-view
         {:theme theme
          :label (i18n/label :t/cant-fetch-info)}]

        (not supported-file?)
        [fallback-view
         {:theme theme
          :label (i18n/label :t/unsupported-file)}]

        (not (:image-loaded? state))
        [loading-image
         {:theme                theme
          :gradient-color-index gradient-color-index}])
      (when supported-file?
        [rn/view {:style {:aspect-ratio 1}}
         [rn/image
          {:style       style/image
           :on-load-end #(set-state (fn [prev-state] (assoc prev-state :image-loaded? true)))
           :on-error    #(set-state (fn [prev-state] (assoc prev-state :image-error? true)))
           :source      image-src}]])]
     (when (and (:image-loaded? state) (not (:image-error? state)) counter)
       [collectible-counter/view
        {:container-style style/collectible-counter
         :size            :size-24
         :value           counter}])
     [card-details
      {:state            state
       :set-state        set-state
       :community?       community?
       :avatar-image-src avatar-image-src
       :collectible-name collectible-name
       :theme            theme}]]))

(defn- image-view
  [{:keys [avatar-image-src community? counter state set-state
           gradient-color-index image-src supported-file?]}]
  (let [theme (quo.theme/use-theme-value)]
    [rn/view {:style style/image-view-container}
     (cond
       (:image-error? state)
       [fallback-view
        {:theme theme
         :label (i18n/label :t/cant-fetch-info)}]

       (not supported-file?)
       [fallback-view
        {:theme theme
         :label (i18n/label :t/unsupported-file)}]

       (not (:image-loaded? state))
       [loading-image
        {:theme                theme
         :gradient-color-index gradient-color-index}])
     (when supported-file?
       [rn/view {:style {:aspect-ratio 1}}
        [rn/image
         {:style       style/image
          :on-load-end #(set-state (fn [prev-state] (assoc prev-state :image-loaded? true)))
          :on-error    #(set-state (fn [prev-state] (assoc prev-state :image-error? true)))
          :source      image-src}]])
     (when (and (:image-loaded? state) (not (:image-error? state)) counter)
       [collectible-counter/view
        {:container-style style/collectible-counter
         :size            :size-24
         :value           counter}])
     (when (and (:image-loaded? state) (not (:image-error? state)) community?)
       [preview-list/view
        {:container-style style/avatar
         :type            :communities
         :size            :size-24}
        [avatar-image-src]])]))

(defn- view-internal
  [{:keys [container-style type on-press on-long-press supported-file?]
    :as   props}]
  (let [[state set-state]  (rn/use-state {:image-loaded?  false
                                          :image-error?   false
                                          :avatar-loaded? false})
        collectible-ready? (or (:image-loaded? state) (not supported-file?))]
    [rn/pressable
     {:on-press            (when collectible-ready? on-press)
      :on-long-press       (when collectible-ready? on-long-press)
      :accessibility-label :collectible-list-item
      :style               container-style}
     (if (= type :card)
       [card-view
        (assoc props
               :state           state
               :set-state       set-state
               :supported-file? supported-file?)]
       [image-view
        (assoc props
               :state           state
               :set-state       set-state
               :supported-file? supported-file?)])]))

(def ?schema
  [:=>
   [:catn
    [:props
     [:map {:closed true}
      [:avatar-image-src {:optional true} [:maybe :schema.common/image-source]]
      [:collectible-name {:optional true} [:maybe string?]]
      [:supported-file? {:optional true} [:maybe boolean?]]
      [:native-ID {:optional true} [:maybe [:or string? keyword?]]]
      [:community? {:optional true} [:maybe boolean?]]
      [:counter {:optional true} [:maybe [:or :string :int]]]
      [:gradient-color-index {:optional true}
       [:maybe [:enum :gradient-1 :gradient-2 :gradient-3 :gradient-4 :gradient-5]]]
      [:image-src {:optional true} [:maybe :schema.common/image-source]]
      [:on-press {:optional true} [:maybe fn?]]
      [:on-long-press {:optional true} [:maybe fn?]]
      [:type [:enum :card :image]]
      [:container-style {:optional true} [:maybe :map]]]]]
   :any])

(def view (schema/instrument #'view-internal ?schema))
