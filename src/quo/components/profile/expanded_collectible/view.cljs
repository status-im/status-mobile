(ns quo.components.profile.expanded-collectible.view
  (:require
    [clojure.string :as string]
    [quo.components.counter.collectible-counter.view :as collectible-counter]
    [quo.components.icon :as icon]
    [quo.components.markdown.text :as text]
    [quo.components.profile.expanded-collectible.style :as style]
    [quo.foundations.colors :as colors]
    [quo.foundations.gradients :as gradients]
    [quo.theme]
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

(defn on-load-end
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

(defn on-load-error
  [set-state]
  (js/setTimeout (fn []
                   (set-state (fn [prev-state] (assoc prev-state :image-error? true))))
                 error-wait-time))

(defn- loading-image
  [{:keys [theme gradient-color-index loader-opacity aspect-ratio]}]
  [reanimated/view
   {:style (style/loading-image-with-opacity loader-opacity)}
   [gradients/view
    {:theme           theme
     :container-style (style/gradient-view aspect-ratio)
     :color-index     gradient-color-index}]])

(defn- counter-view
  [counter]
  [collectible-counter/view
   {:container-style style/counter
    :value           counter}])

(defn- fallback-view
  [{:keys [label theme counter on-mount]}]
  (rn/use-mount on-mount)
  [rn/view {:style (style/fallback theme)}
   [counter-view counter]
   [rn/view
    [icon/icon :i/sad {:color (colors/theme-colors colors/neutral-40 colors/neutral-50 theme)}]]
   [rn/view {:style {:height 4}}]
   [text/text
    {:size  :paragraph-2
     :style {:color (colors/theme-colors colors/neutral-40 colors/neutral-50 theme)}}
    label]])

(defn view-internal
  [{:keys [container-style square? on-press counter image-src native-ID supported-file?
           on-collectible-load aspect-ratio]}]
  (let [theme                     (quo.theme/use-theme)
        loader-opacity            (reanimated/use-shared-value
                                   (if supported-file? 1 0))
        image-opacity             (reanimated/use-shared-value
                                   (if supported-file? 0 1))
        [load-time set-load-time] (rn/use-state (datetime/now))
        [state set-state]         (rn/use-state {:image-loaded? false
                                                 :image-error?  (or (nil? image-src)
                                                                    (string/blank?
                                                                     image-src))})]
    [rn/pressable
     {:on-press            (when (and (not (:image-error? state)) supported-file?) on-press)
      :accessibility-label :expanded-collectible
      :style               (merge container-style
                                  (style/container aspect-ratio))}
     (cond
       (not supported-file?)
       [fallback-view
        {:aspect-ratio aspect-ratio
         :label        (i18n/label :t/unsupported-file)
         :counter      counter
         :theme        theme
         :on-mount     on-collectible-load}]

       (:image-error? state)
       [fallback-view
        {:label    (i18n/label :t/cant-fetch-info)
         :counter  counter
         :theme    theme
         :on-mount on-collectible-load}]

       (not (:image-loaded? state))
       [loading-image
        {:aspect-ratio         aspect-ratio
         :loader-opacity       loader-opacity
         :theme                theme
         :gradient-color-index :gradient-5}])
     (when supported-file?
       [reanimated/view {:style (style/supported-file image-opacity)}
        [rn/image
         {:style         (style/image square? aspect-ratio theme)
          :source        image-src
          :native-ID     native-ID
          :on-load-start #(set-load-time (fn [start-time] (- (datetime/now) start-time)))
          :on-load-end   #(on-load-end {:load-time      load-time
                                        :set-state      set-state
                                        :loader-opacity loader-opacity
                                        :image-opacity  image-opacity})
          :on-error      #(on-load-error set-state)
          :on-load       on-collectible-load}]
        (when counter
          [counter-view counter])
        [rn/view {:style (style/collectible-border theme)}]])]))

(def ?schema
  [:=>
   [:catn
    [:props
     [:map {:closed true}
      [:aspect-ratio {:optional true} [:maybe number?]]
      [:image-src {:optional true} [:maybe string?]]
      [:supported-file? {:optional true} [:maybe boolean?]]
      [:container-style {:optional true} [:maybe :map]]
      [:native-ID {:optional true} [:maybe [:or string? keyword?]]]
      [:square? {:optional true} [:maybe boolean?]]
      [:counter {:optional true} [:maybe string?]]
      [:on-press {:optional true} [:maybe fn?]]
      [:on-collectible-load {:optional true} [:maybe fn?]]]]]
   :any])

(def view (schema/instrument #'view-internal ?schema))
