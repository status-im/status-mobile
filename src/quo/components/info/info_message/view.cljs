(ns quo.components.info.info-message.view
  (:require [quo.components.icon :as icons]
            [quo.components.info.info-message.style :as style]
            [quo.components.markdown.text :as text]
            [quo.foundations.colors :as colors]
            [quo.theme :as quo.theme]
            [react-native.core :as rn]
            [schema.core :as schema]))

(def ?schema
  [:=>
   [:catn
    [:props
     [:map {:closed true}
      [:status {:optional true} [:maybe [:enum :default :success :error :warning]]]
      [:size {:optional true} [:maybe [:enum :default :tiny]]]
      [:blur? {:optional true} [:maybe :boolean]]
      [:icon :keyword]
      [:color {:optional true} [:maybe :string]]
      [:text-color {:optional true} [:maybe :string]]
      [:icon-color {:optional true} [:maybe :string]]
      [:no-icon-color? {:optional true} [:maybe :boolean]]
      [:accessibility-label {:optional true} [:maybe :keyword]]
      [:container-style {:optional true} [:maybe :map]]]]
    [:message :string]]
   :any])

(defn- get-color
  [status theme blur?]
  (case status
    :success (if blur? colors/success-60 (colors/resolve-color :success theme))
    :error   (if blur? colors/danger-60 (colors/resolve-color :danger theme))
    :warning (if blur? colors/warning-60 (colors/resolve-color :warning theme))
    (if blur? colors/white-opa-40 (colors/theme-colors colors/neutral-50 colors/neutral-40 theme))))

(defn view-internal
  [{:keys [status size blur? icon color icon-color text-color no-icon-color? container-style
           accessibility-label]} message]
  (let [theme         (quo.theme/use-theme)
        default-color (get-color status theme blur?)
        text-color    (or text-color color default-color)
        icon-color    (or icon-color color default-color)]
    [rn/view {:style (merge style/container container-style)}
     [icons/icon icon
      {:size      (if (= size :tiny) 12 16)
       :color     icon-color
       :no-color? no-icon-color?}]
     [text/text
      {:size                (if (= size :tiny) :label :paragraph-2)
       :accessibility-label accessibility-label
       :style               {:color text-color}} message]]))

(def view (schema/instrument #'view-internal ?schema))
