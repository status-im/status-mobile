(ns quo.components.profile.expanded-collectible.view
  (:require
    [promesa.core :as promesa]
    [quo.components.counter.collectible-counter.view :as collectible-counter]
    [quo.components.icon :as icon]
    [quo.components.markdown.text :as text]
    [quo.components.profile.expanded-collectible.style :as style]
    [quo.foundations.colors :as colors]
    [quo.theme]
    [quo.theme]
    [quo.theme]
    [react-native.core :as rn]
    [schema.core :as schema]
    [utils.i18n :as i18n]))

(defn- counter-view
  [counter]
  (when counter
    [collectible-counter/view
     {:container-style style/counter
      :value           counter}]))

(defn- fallback-view
  [{:keys [label theme counter]}]
  [rn/view
   {:style (style/fallback {:theme theme})}
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
           on-collectible-load]}]
  (let [theme                          (quo.theme/use-theme)
        [image-size set-image-size]    (rn/use-state {})
        [image-error? set-image-error] (rn/use-state false)]
    (rn/use-effect
     (fn []
       (promesa/let [[image-width image-height] (rn/image-get-size image-src)]
         (set-image-size {:width        image-width
                          :height       image-height
                          :aspect-ratio (/ image-width image-height)})))
     [image-src])
    [rn/pressable
     {:on-press            (when (and (not image-error?) supported-file?) on-press)
      :accessibility-label :expanded-collectible
      :style               (merge container-style style/container)}
     (cond
       (not supported-file?)
       [fallback-view
        {:label   (i18n/label :t/unsupported-file)
         :counter counter
         :theme   theme}]

       image-error?
       [fallback-view
        {:label   (i18n/label :t/cant-fetch-info)
         :counter counter
         :theme   theme}]

       (and (not image-error?) supported-file?)
       [rn/view
        [rn/image
         {:style     (style/image square? (:aspect-ratio image-size))
          :source    image-src
          :native-ID native-ID
          :on-error  #(set-image-error true)
          :on-load   on-collectible-load}]
        [counter-view counter]])]))

(def ?schema
  [:=>
   [:catn
    [:props
     [:map {:closed true}
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
