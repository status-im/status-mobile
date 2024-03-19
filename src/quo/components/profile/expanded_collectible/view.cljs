(ns quo.components.profile.expanded-collectible.view
  (:require
    [promesa.core :as p]
    [quo.components.counter.collectible-counter.view :as collectible-counter]
    [quo.components.icon :as icon]
    [quo.components.markdown.text :as text]
    [quo.components.profile.expanded-collectible.style :as style]
    [quo.foundations.colors :as colors]
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
  [{:keys [container-style square? status on-press counter image-src] :or {status :default}}]
  (let [theme                       (quo.theme/use-theme-value)
        [image-size set-image-size] (rn/use-state {})]
    (rn/use-effect
     (fn []
       (p/let [[image-width image-height] (rn/image-get-size image-src)]
         (set-image-size {:width        image-width
                          :height       image-height
                          :aspect-ratio (/ image-width image-height)})))
     [image-src])
    [rn/pressable
     {:on-press            on-press
      :accessibility-label :expanded-collectible
      :style               (merge container-style (style/container square?))}
     (case status
       :unsupported [fallback-view
                     {:label   (i18n/label :t/unsupported-file)
                      :counter counter
                      :theme   theme}]
       :cant-fetch  [fallback-view
                     {:label   (i18n/label :t/cant-fetch-info)
                      :counter counter
                      :theme   theme}]
       [rn/view {:style {:flex 1}}
        [rn/image
         {:style  (style/image square? (:aspect-ratio image-size))
          :source image-src}]
        [counter-view counter]])]))

(def ?schema
  [:=>
   [:catn
    [:props
     [:map {:closed true}
      [:image-src string?]
      [:container-style {:optional true} [:maybe :map]]
      [:square? {:optional true} [:maybe boolean?]]
      [:counter {:optional true} [:maybe string?]]
      [:status {:optional true} [:maybe [:enum :default :loading :cant-fetch :unsupported]]]
      [:on-press {:optional true} [:maybe fn?]]]]]
   :any])

(def view (schema/instrument #'view-internal ?schema))
