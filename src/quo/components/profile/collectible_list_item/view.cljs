(ns quo.components.profile.collectible-list-item.view
  (:require
    [clojure.string :as string]
    [quo.components.avatars.collection-avatar.view :as collection-avatar]
    [quo.components.counter.collectible-counter.view :as collectible-counter]
    [quo.components.icon :as icon]
    [quo.components.list-items.preview-list.view :as preview-list]
    [quo.components.markdown.text :as text]
    [quo.components.profile.collectible-list-item.style :as style]
    [quo.context]
    [quo.foundations.colors :as colors]
    [quo.foundations.gradients :as gradients]
    [react-native.core :as rn]
    [react-native.reanimated :as reanimated]
    [schema.core :as schema]
    [utils.datetime :as datetime]
    [utils.i18n :as i18n]))

(def timing-options-out 650)
(def timing-options-in 1000)
(def first-load-time 500)
(def cached-load-time 200)
(def error-wait-time 800)

(defn animate-loading-image
  [{:keys [load-time set-state loader-opacity image-opacity]}]
  (reanimated/animate loader-opacity 0 timing-options-out)
  (reanimated/animate image-opacity 1 timing-options-in)
  (if (> load-time cached-load-time)
    (js/setTimeout
     (fn []
       (set-state (fn [prev-state]
                    (assoc prev-state :image-loaded? true))))
     first-load-time)
    (set-state (fn [prev-state]
                 (assoc prev-state :image-loaded? true)))))

(defn on-load
  [evt set-state]
  (let [source       (.. evt -nativeEvent -source)
        aspect-ratio (/ (.-width source) (.-height source))]
    (set-state (fn [prev-state] (assoc prev-state :image-aspect-ratio aspect-ratio)))))

(defn on-load-error
  [set-state]
  (js/setTimeout (fn []
                   (set-state (fn [prev-state] (assoc prev-state :image-error? true))))
                 error-wait-time))

(defn on-load-avatar
  [{:keys [load-time set-state loader-opacity avatar-opacity]}]
  (reanimated/animate loader-opacity 0 timing-options-out)
  (reanimated/animate avatar-opacity 1 timing-options-in)
  (if (> load-time cached-load-time)
    (js/setTimeout
     (fn []
       (set-state (fn [prev-state]
                    (assoc prev-state :avatar-loaded? true))))
     first-load-time)
    (set-state (fn [prev-state]
                 (assoc prev-state :avatar-loaded? true)))))

(defn- fallback-view
  [{:keys [label theme image-opacity on-load-end]}]
  (rn/use-mount on-load-end)
  [reanimated/view {:style (style/fallback {:opacity image-opacity :theme theme})}
   [rn/view
    [icon/icon :i/sad
     {:color (colors/theme-colors colors/neutral-40 colors/neutral-50 theme)}]]
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
  [{:keys [theme gradient-color-index loader-opacity]}]
  [reanimated/view
   {:style (style/loading-image-with-opacity theme loader-opacity)}
   [gradients/view
    {:theme           theme
     :container-style (style/loading-image theme)
     :color-index     gradient-color-index}]])

(defn- card-details
  [{:keys [community? avatar-image-src collectible-name theme state set-state loading?]}]
  (let [loader-opacity            (reanimated/use-shared-value 1)
        avatar-opacity            (reanimated/use-shared-value 0)
        [load-time set-load-time] (rn/use-state (datetime/now))
        set-avatar-loaded         (fn []
                                    (on-load-avatar {:set-state      set-state
                                                     :load-time      load-time
                                                     :loader-opacity loader-opacity
                                                     :avatar-opacity avatar-opacity}))
        empty-name?               (string/blank? collectible-name)]
    (rn/use-mount (fn []
                    (when (and (string/blank? avatar-image-src) (not loading?))
                      (set-avatar-loaded))))
    [rn/view {:style style/card-details-container}
     [reanimated/view {:style (style/avatar-container avatar-opacity)}
      (cond
        (string/blank? avatar-image-src)
        [loading-square theme]

        community?
        [preview-list/view {:type :communities :size :size-20}
         [avatar-image-src]]

        :else
        [collection-avatar/view
         {:size        :size-20
          :on-start    #(set-load-time (fn [start-time] (- (datetime/now) start-time)))
          :on-load-end set-avatar-loaded
          :image       avatar-image-src}])
      [text/text
       {:size            :paragraph-1
        :weight          :semi-bold
        :ellipsize-mode  :tail
        :number-of-lines 1
        :style           (style/card-detail-text empty-name? theme)}
       (if empty-name?
         (i18n/label :t/unknown)
         collectible-name)]]

     (when-not (:avatar-loaded? state)
       [reanimated/view {:style (style/card-loader loader-opacity)}
        [loading-square theme]
        [loading-message theme]])]))

(defn- card-view
  [{:keys [avatar-image-src collectible-name community? counter state set-state
           gradient-color-index image-src supported-file? loading?]}]
  (let [theme                     (quo.context/use-theme)
        loader-opacity            (reanimated/use-shared-value (if supported-file? 1 0))
        image-opacity             (reanimated/use-shared-value (if supported-file? 0 1))
        [load-time set-load-time] (rn/use-state (datetime/now))
        set-image-loaded          (fn []
                                    (animate-loading-image {:load-time      load-time
                                                            :set-state      set-state
                                                            :loader-opacity loader-opacity
                                                            :image-opacity  image-opacity}))]
    [rn/view {:style (style/card-view-container theme)}
     [rn/view {:style {:aspect-ratio 1}}
      (cond
        (:image-error? state) [fallback-view
                               {:image-opacity image-opacity
                                :on-load-end   set-image-loaded
                                :theme         theme
                                :label         (i18n/label :t/cant-fetch-info)}]
        (not supported-file?) [fallback-view
                               {:image-opacity image-opacity
                                :on-load-end   set-image-loaded
                                :theme         theme
                                :label         (i18n/label :t/unsupported-file)}]
        (or (not (:image-loaded? state))
            loading?)         [loading-image
                               {:loader-opacity       loader-opacity
                                :theme                theme
                                :gradient-color-index gradient-color-index}])

      (when (and supported-file? (not loading?))
        [reanimated/view {:style (style/supported-file image-opacity)}
         [rn/image
          {:style         style/image
           :on-load       #(on-load % set-state)
           :on-load-start #(set-load-time (fn [start-time] (- (datetime/now) start-time)))
           :on-load-end   set-image-loaded
           :on-error      #(on-load-error set-state)
           :source        image-src}]])]

     (when (and (or (:image-loaded? state)
                    (not supported-file?))
                (not (:image-error? state))
                counter)
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
       :loading?         loading?
       :theme            theme}]]))

(defn- image-view
  [{:keys [avatar-image-src community? counter state set-state
           gradient-color-index image-src supported-file?]}]
  (let [theme                     (quo.context/use-theme)
        loader-opacity            (reanimated/use-shared-value (if supported-file? 1 0))
        image-opacity             (reanimated/use-shared-value (if supported-file? 0 1))
        [load-time set-load-time] (rn/use-state (datetime/now))]
    [rn/view {:style style/image-view-container}
     (cond
       (:image-error? state)
       [fallback-view
        {:image-opacity image-opacity
         :theme         theme
         :label         (i18n/label :t/cant-fetch-info)}]

       (not supported-file?)
       [fallback-view
        {:image-opacity image-opacity
         :theme         theme
         :label         (i18n/label :t/unsupported-file)}]

       (not (:image-loaded? state))
       [loading-image
        {:loader-opacity       loader-opacity
         :theme                theme
         :gradient-color-index gradient-color-index}])
     (when supported-file?
       [reanimated/view {:style (style/supported-file image-opacity)}
        [rn/image
         {:style         style/image
          :on-load       #(on-load % set-state)
          :on-load-start #(set-load-time (fn [start-time] (- (datetime/now) start-time)))
          :on-load-end   #(animate-loading-image {:load-time      load-time
                                                  :set-state      set-state
                                                  :loader-opacity loader-opacity
                                                  :image-opacity  image-opacity})
          :on-error      #(on-load-error set-state)
          :source        image-src}]])
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
  (let [[state set-state]  (rn/use-state {:image-loaded?      false
                                          :image-aspect-ratio nil
                                          :image-error?       false
                                          :avatar-loaded?     false})
        collectible-ready? (or (:image-loaded? state) (not supported-file?))]
    [rn/pressable
     {:on-press            (when collectible-ready? #(on-press (:image-aspect-ratio state)))
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
      [:loading? {:optional true} [:maybe boolean?]]
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
