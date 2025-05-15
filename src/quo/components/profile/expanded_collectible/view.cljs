(ns quo.components.profile.expanded-collectible.view
  (:require
    [clojure.string :as string]
    [quo.components.counter.collectible-counter.view :as collectible-counter]
    [quo.components.icon :as icon]
    [quo.components.markdown.text :as text]
    [quo.components.profile.expanded-collectible.style :as style]
    [quo.context]
    [quo.foundations.colors :as colors]
    [quo.foundations.gradients :as gradients]
    [react-native.core :as rn]
    [react-native.reanimated :as reanimated]
    [schema.core :as schema]
    [utils.i18n :as i18n]))

(def loader-out 650)
(def image-in 1000)
(def error-wait-time 800)

(defn on-load-end
  [{:keys [loader-opacity image-opacity]}]
  (reanimated/animate loader-opacity 0 loader-out)
  (reanimated/animate image-opacity 1 image-in))

(defn on-load-error
  [set-error]
  (js/setTimeout set-error error-wait-time))

(defn- loading-image
  [{:keys [theme gradient-color-index loader-opacity aspect-ratio]}]
  [reanimated/view {:style (style/loading-image-with-opacity loader-opacity)}
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

(defn- collectible-image
  [{:keys [aspect-ratio theme gradient-color-index supported-file? set-error native-ID
           square? image-src on-collectible-load counter]}]
  (let [loader-opacity (reanimated/use-shared-value (if supported-file? 1 0))
        image-opacity  (reanimated/use-shared-value (if supported-file? 0 1))]
    [:<>
     [loading-image
      {:aspect-ratio         aspect-ratio
       :loader-opacity       loader-opacity
       :theme                theme
       :gradient-color-index gradient-color-index}]

     [reanimated/view {:style (style/supported-file image-opacity)}
      [rn/image
       {:style       (style/image square? aspect-ratio theme)
        :source      image-src
        :native-ID   native-ID
        :on-load-end (fn []
                       (on-load-end {:loader-opacity loader-opacity
                                     :image-opacity  image-opacity}))
        :on-error    #(on-load-error set-error)
        :on-load     on-collectible-load}]
      (when counter
        [counter-view counter])
      [rn/view {:style (style/collectible-border theme)}]]]))

(defn invalid-image?
  [image-src]
  (or (nil? image-src)
      (string/blank? image-src)))

(defn view-internal
  [{:keys [container-style square? on-press counter image-src native-ID supported-file?
           on-collectible-load aspect-ratio gradient-color-index]
    :or   {gradient-color-index :gradient-1
           on-collectible-load  (fn [])}}]
  (let [theme              (quo.context/use-theme)
        [error? set-error] (rn/use-state (invalid-image? image-src))]
    (rn/use-effect #(set-error (invalid-image? image-src))
                   [image-src])
    [rn/pressable
     {:style               (merge container-style (style/container aspect-ratio))
      :accessibility-label :expanded-collectible
      :on-press            (when (and (not error?) supported-file?)
                             on-press)}
     (if (or (not supported-file?) error?)
       [fallback-view
        {:label    (i18n/label (if-not supported-file?
                                 :t/unsupported-file
                                 :t/cant-fetch-info))
         :counter  counter
         :theme    theme
         :on-mount on-collectible-load}]
       [collectible-image
        {:aspect-ratio         aspect-ratio
         :theme                theme
         :supported-file?      supported-file?
         :set-error            set-error
         :native-ID            native-ID
         :square?              square?
         :image-src            image-src
         :on-collectible-load  on-collectible-load
         :counter              counter
         :gradient-color-index gradient-color-index}])]))

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
      [:on-collectible-load {:optional true} [:maybe fn?]]
      [:gradient-color-index {:optional true} [:maybe keyword?]]]]]
   :any])

(def view (schema/instrument #'view-internal ?schema))
