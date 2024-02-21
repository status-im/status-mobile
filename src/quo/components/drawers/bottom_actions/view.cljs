(ns quo.components.drawers.bottom-actions.view
  (:require
    [quo.components.buttons.button.view :as button]
    [quo.components.drawers.bottom-actions.style :as style]
    [quo.components.icon :as icon]
    [quo.components.markdown.text :as text]
    [quo.components.tags.context-tag.view :as context-tag]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [schema.core :as schema]
    [utils.i18n :as i18n]))

(def ?schema
  [:=>
   [:catn
    [:props
     [:map {:closed true}
      [:actions [:maybe [:enum :one-action :two-actions]]]
      [:description {:optional true} [:maybe [:enum :top :bottom :top-error]]]
      [:description-text {:optional true} [:maybe :string]]
      [:error-message {:optional true} [:maybe :string]]
      [:role {:optional true} [:maybe [:enum :admin :member :token-master :owner]]]
      [:button-one-label {:optional true} [:maybe :string]]
      [:button-two-label {:optional true} [:maybe :string]]
      [:button-one-props {:optional true} [:maybe :map]]
      [:button-two-props {:optional true} [:maybe :map]]
      [:theme :schema.common/theme]
      [:scroll? {:optional true} [:maybe :boolean]]
      [:blur? {:optional true} [:maybe :boolean]]
      [:container-style {:optional true} [:maybe :map]]]]]
   :any])

(def ^:private role-icon
  {:admin        :i/gavel
   :member       :i/members
   :token-master :i/token-master
   :owner        :i/crown})

(defn- view-internal
  [{:keys [actions description description-text error-message role button-one-label button-two-label
           blur? button-one-props button-two-props theme scroll? container-style]}]
  [rn/view
   {:style (merge (style/container scroll? blur? theme) container-style)}
   (when (= description :top-error)
     [rn/view {:style style/error-message}
      [icon/icon
       :i/alert
       {:color (colors/theme-colors colors/danger-50 colors/danger-60 theme)
        :size  16}]
      [text/text
       {:size  :paragraph-2
        :style {:color (colors/theme-colors colors/danger-50 colors/danger-60 theme)}}
       error-message]])

   (when (and (= description :top) role)
     [rn/view
      {:style style/description-top}
      [text/text
       {:size  :paragraph-2
        :style (style/description-top-text scroll? blur? theme)}
       (i18n/label :t/eligible-to-join-as)]
      [context-tag/view
       {:type    :icon
        :size    24
        :icon    (role role-icon)
        :blur?   blur?
        :context (i18n/label (keyword "t" role))}]])

   [rn/view {:style style/buttons-container}
    (when (= actions :two-actions)
      [button/button
       (merge
        {:size                40
         :background          (when (or blur? scroll?) :blur)
         :container-style     style/button-container
         :theme               theme
         :accessibility-label :button-two}
        button-two-props)
       button-two-label])
    [button/button
     (merge
      {:size                40
       :container-style     style/button-container
       :background          (when (or blur? scroll?) :blur)
       :theme               theme
       :accessibility-label :button-one}
      button-one-props)
     button-one-label]]
   (when (= description :bottom)
     [text/text
      {:size  :paragraph-2
       :style (style/description-bottom scroll? blur? theme)} description-text])])

(def view
  (quo.theme/with-theme
   (schema/instrument #'view-internal ?schema)))
