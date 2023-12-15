(ns quo.components.wallet.required-tokens.view
  (:require [quo.components.markdown.text :as text]
            [quo.components.utilities.token.view :as token]
            [quo.components.wallet.required-tokens.style :as style]
            quo.theme
            [react-native.core :as rn]
            [schema.core :as schema]))

(def ?schema
  [:=>
   [:catn
    [:props
     [:map {:closed true}
      [:type [:enum :token :collectible]]
      [:amount {:optional true} [:maybe [:or :string :int]]]
      [:token {:optional true} [:maybe :string]]
      [:token-img-src {:optional true} [:maybe :schema.common/image-source]]
      [:collectible-img-src {:optional true} :schema.common/image-source]
      [:collectible-name {:optional true} [:maybe :string]]
      [:divider? {:optional true} [:maybe :boolean]]
      [:theme :schema.common/theme]
      [:container-style {:optional true} [:maybe :map]]]]]
   :any])

(defn- view-internal
  [{:keys [type amount token token-img-src collectible-img-src collectible-name divider? theme
           container-style]}]
  [rn/view
   {:style               (merge style/container container-style)
    :accessibility-label :wallet-required-tokens}
   (case type
     :token       [token/view
                   (assoc (if token-img-src
                            {:image-source token-img-src}
                            {:token token})
                          :size
                          14)]
     :collectible [rn/image
                   {:style  style/collectible-img
                    :source collectible-img-src}]
     nil)
   [text/text
    {:size   :paragraph-2
     :weight :medium
     :style  {:margin-left 4}}
    (case type
      :token       (str amount " " token)
      :collectible collectible-name
      nil)]
   (when divider?
     [rn/view
      {:style (style/divider theme)}])])

(def view
  (quo.theme/with-theme
   (schema/instrument #'view-internal ?schema)))
